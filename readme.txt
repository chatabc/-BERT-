zhapian中，Bert.py文件用于训练模型，数据来源为Telecom_Fraud_Texts_5-main，其中包含五种电话类型：0（正常电话），冒充领导、熟人类，冒充公检法及政府机关类，贷款、代办信用卡类，冒充客服服务
通过contact_csv.py将其拼接并打乱，训练得到bert_telecom_model.pth文件，通过predict_flask_2.py与XFASRDemo-master进行链接。
XFASRDemo-master使用语音识别识别手机话筒声音，分句后输入到flask（predict_flask_2.py）进行预测，返回判断结果。