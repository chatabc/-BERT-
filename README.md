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
bert-telecom-fraud-detection/

├── model/ # 模型训练模块

│ ├── train.py # 模型训练脚本

│ ├── preprocess.py # 数据预处理脚本

│ └── ...

├── app/ # 应用部署模块

│ ├── predict_flask.py # Flask API 服务

│ └── ...

├── data/ # 数据目录（样例）

├── requirements.txt # 项目依赖

└── README.md # 项目说明

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
