import pandas as pd
import os


# 合并文件夹中的所有csv文件
def merge_files(folder_path, output_file):
    # 获取文件夹中的所有文件路径
    file_paths = [os.path.join(folder_path, file) for file in os.listdir(folder_path) if file.endswith('.csv')]

    # 初始化一个空的DataFrame
    merged_df = pd.DataFrame(columns=['content', 'label'])

    # 遍历每个文件路径，读取csv文件并进行拼接
    for file_path in file_paths:
        try:
            # 尝试使用UTF-8编码读取
            df = pd.read_csv(file_path, encoding='utf-8')
        except UnicodeDecodeError:
            try:
                # 尝试使用GBK编码读取
                df = pd.read_csv(file_path, encoding='gbk')
            except UnicodeDecodeError:
                # 尝试使用ISO-8859-1编码读取
                df = pd.read_csv(file_path, encoding='iso-8859-1')

        # 对读取后的数据进行乱序
        df = df.sample(frac=1).reset_index(drop=True)

        merged_df = pd.concat([merged_df, df], ignore_index=True)

    # 对合并后的数据再次进行乱序
    merged_df = merged_df.sample(frac=1).reset_index(drop=True)

    # 保存合并后的数据到输出文件
    merged_df.to_csv(output_file, index=False)


# 主函数
def main():
    # 文件夹路径
    folder_path = 'Telecom_Fraud_Texts_5-main'  # 替换为你的文件夹路径

    # 输出文件路径
    output_file = 'data_telecom.csv'

    # 合并文件夹中的所有csv文件
    merge_files(folder_path, output_file)


if __name__ == '__main__':
    main()