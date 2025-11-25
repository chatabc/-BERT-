from transformers import BertTokenizer, BertForSequenceClassification
import torch
from torch.utils.data import DataLoader, Dataset
import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, classification_report
from torch.optim import AdamW
from transformers import get_linear_schedule_with_warmup
import tqdm

# 加载数据
df = pd.read_csv('data_telecom.csv')

# 数据预处理：将label转换为整数
label_map = {'0': 0, '冒充领导、熟人类': 1, '冒充公检法及政府机关类': 2, '贷款、代办信用卡类': 3, '冒充客服服务': 4}
df['label'] = df['label'].map(label_map)

# 分割数据集
train_df, val_df = train_test_split(df, test_size=0.2, random_state=42)

# 定义数据集类
class TelecomDataset(Dataset):
    def __init__(self, tokenizer, df, max_len=512):
        self.tokenizer = tokenizer
        self.df = df
        self.max_len = max_len

    def __len__(self):
        return len(self.df)

    def __getitem__(self, idx):
        text = self.df.iloc[idx, 0]
        label = self.df.iloc[idx, 1]

        encoding = self.tokenizer.encode_plus(
            text,
            add_special_tokens=True,
            max_length=self.max_len,
            return_token_type_ids=False,
            padding='max_length',
            truncation=True,
            return_attention_mask=True,
            return_tensors='pt'
        )

        return {
            'input_ids': encoding['input_ids'].flatten(),
            'attention_mask': encoding['attention_mask'].flatten(),
            'labels': torch.tensor(label, dtype=torch.long)
        }

# 初始化tokenizer和模型
tokenizer = BertTokenizer.from_pretrained('bert-base-chinese')
model = BertForSequenceClassification.from_pretrained('bert-base-chinese', num_labels=5)

# 创建数据集和数据加载器
train_dataset = TelecomDataset(tokenizer, train_df, max_len=128)  # 减小max_len以适应内存
val_dataset = TelecomDataset(tokenizer, val_df, max_len=128)

train_loader = DataLoader(train_dataset, batch_size=8, shuffle=True)  # 减小batch_size以适应内存
val_loader = DataLoader(val_dataset, batch_size=8)

# 定义优化器和学习率调度器
optimizer = AdamW(model.parameters(), lr=2e-5)
total_steps = len(train_loader) * 3  # 假设训练3个epoch
scheduler = get_linear_schedule_with_warmup(optimizer, num_warmup_steps=0, num_training_steps=total_steps)

# 训练模型
device = torch.device('cuda' if torch.cuda.is_available() else 'cpu')
model.to(device)

for epoch in range(3):
    model.train()
    total_loss = 0
    for batch in train_loader:
        input_ids = batch['input_ids'].to(device)
        attention_mask = batch['attention_mask'].to(device)
        labels = batch['labels'].to(device)

        optimizer.zero_grad()

        outputs = model(input_ids, attention_mask=attention_mask, labels=labels)
        loss = outputs.loss
        total_loss += loss.item()

        loss.backward()
        optimizer.step()
        scheduler.step()

    print(f'Epoch {epoch+1}, Loss: {total_loss/len(train_loader)}')

    # 验证模型
    model.eval()
    predictions = []
    true_labels = []
    with torch.no_grad():
        for batch in val_loader:
            input_ids = batch['input_ids'].to(device)
            attention_mask = batch['attention_mask'].to(device)
            labels = batch['labels'].to(device)

            outputs = model(input_ids, attention_mask=attention_mask)
            logits = outputs.logits
            preds = torch.argmax(logits, dim=1)

            predictions.extend(preds.cpu().numpy())
            true_labels.extend(labels.cpu().numpy())

    print(f'Validation Accuracy: {accuracy_score(true_labels, predictions)}')
    print(classification_report(true_labels, predictions))

# 保存模型
torch.save(model.state_dict(), 'bert_telecom_model.pth')