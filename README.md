# 基于BERT的实时反欺诈电话检测系统

[![Python](https://img.shields.io/badge/Python-3.8%2B-blue)](https://www.python.org/)
[![PyTorch](https://img.shields.io/badge/PyTorch-%23EE4C2C.svg?style=flat&logo=PyTorch&logoColor=white)](https://pytorch.org/)
[![Transformers](https://img.shields.io/badge/🤗-Transformers-yellow)](https://huggingface.co/transformers/)

本项目是一个端到端的实时反欺诈系统，能够将通话语音实时转换为文本，并利用微调的BERT模型判断其是否为诈骗电话。旨在为金融风控与客户安全场景提供技术解决方案。

## 🚀 项目亮点

- **高精度模型**: 采用 `bert-base-chinese` 模型进行微调，在测试集上准确率高达 **99.8%**。
- **端到端实时性**: 集成语音识别（ASR）与Flask API，实现从语音输入到欺诈判别的全流程实时响应。
- **业务导向**: 精准识别5类常见诈骗话术（如冒充公检法、冒充领导熟人等），紧扣风控业务需求。

## 📁 项目结构

基于Bert的诈骗电话识别/

├── 🤖 zhapian/                    # 【核心】模型训练与数据处理模块

│   ├── Bert.py                    # BERT模型微调训练脚本

│   ├── contact_csv.py             # 数据预处理：原始数据拼接与打乱

│   ├── data_telecom.csv           # 处理后的最终训练数据集

│   ├── predict_flask_2.py         # 【关键】Flask API服务，连接模型与前端

│   └── bert_telecom_model.pth     # 训练好的模型权重文件 (请确认是否已上传)

│

├── 🎤 XFASRDemo-master/           # 【前端】语音识别演示模块

│   └── ...                        # 语音识别前端源码，用于捕获实时音频

│

├── 📄 requirements.txt            # 项目Python环境依赖列表 (建议创建)

├── 📊 results/                    # 训练日志、性能图表等 (可选)

└── README.md                      # 您正在阅读的项目说明

## ⚙️ 环境依赖
首先，安装所需的Python库：
pip install -r requirements.txt

## 🛠️ 如何使用
1. 模型训练

cd model

python train.py

2. 启动API服务

cd app

python predict_flask.py

服务启动后，默认位于 http://localhost:5000。

3. 实时演示

连接并运行 XFASRDemo-master 前端项目，即可进行实时语音欺诈检测。

## 📊 模型性能

模型	准确率	备注

朴素贝叶斯 (基线)	~70%	传统方法，效果不佳

BERT (本项目)	99.8%	颠覆性提升

## 🎯 未来计划

探索大模型（LLM）在本任务中的应用

优化模型体积与推理速度，便于部署

增加更多欺诈场景的识别类型
