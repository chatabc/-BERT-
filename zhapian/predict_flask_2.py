from flask import Flask, request, jsonify
import torch
from transformers import BertTokenizer, BertForSequenceClassification

app = Flask(__name__)

# 加载tokenizer和模型
tokenizer = BertTokenizer.from_pretrained('bert-base-chinese')
model = BertForSequenceClassification.from_pretrained('bert-base-chinese', num_labels=5)
model.load_state_dict(torch.load('bert_telecom_model.pth'))
model.eval()

# 存储最近的几句话
recent_texts = []

# 预测函数
def predict(text):
    encoding = tokenizer.encode_plus(
        text,
        add_special_tokens=True,
        max_length=128,
        return_token_type_ids=False,
        padding='max_length',
        truncation=True,
        return_attention_mask=True,
        return_tensors='pt'
    )
    input_ids = encoding['input_ids']
    attention_mask = encoding['attention_mask']

    with torch.no_grad():
        outputs = model(input_ids, attention_mask=attention_mask)
        logits = outputs.logits
        preds = torch.argmax(logits, dim=1)

    return preds.cpu().numpy()[0]

# 检测函数
def detect(text):
    label_map = {0: '正常电话', 1: '冒充领导、熟人类', 2: '冒充公检法及政府机关类', 3: '贷款、代办信用卡类', 4: '冒充客服服务'}
    # 单独检测当前句子
    prediction = predict(text)
    if prediction != 0:
        return label_map[prediction]

    # 与前一句结合检测
    if len(recent_texts) >= 1:
        combined_text = recent_texts[-1] + text
        prediction = predict(combined_text)
        if prediction != 0:
            return label_map[prediction]

    # 与前两句结合检测
    if len(recent_texts) >= 2:
        combined_text = recent_texts[-2] + recent_texts[-1] + text
        prediction = predict(combined_text)
        if prediction != 0:
            return label_map[prediction]

    return label_map[0]

@app.route('/predict', methods=['POST'])
def get_prediction():
    data = request.get_json()
    text = data.get('text')
    if text:
        print(text)
        # 更新最近的几句话
        recent_texts.append(text)
        if len(recent_texts) > 2:
            recent_texts.pop(0)

        result = detect(text)
        print(result)
        print("\n")
        return jsonify({'prediction': result})
    else:
        return jsonify({'error': 'No text provided'}), 400

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)