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
