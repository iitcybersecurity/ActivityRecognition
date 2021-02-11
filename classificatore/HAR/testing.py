import os
import pickle
from keras import models
import numpy as np
from sklearn.metrics import confusion_matrix, classification_report
import pandas as pd
from DataPreprocessing import create_train_test, read_wisdm_dataset

def reverse_onehot(onehot_data):
    # onehot_data assumed to be channel last
    data_copy = np.zeros(onehot_data.shape[:-1])
    for c in range(onehot_data.shape[-1]):
        img_c = onehot_data[..., c]
        data_copy[img_c == 1] = c
    return data_copy

def predict_walking(walking_path, y_test):
    label_name = ['Downstairs',
                  'Jogging',
                  'Sitting',
                  'Standing',
                  'Upstairs',
                  'Walking']

    model = models.load_model("model_network")
    model.load_weights('lstm_weights.h5')

    predictions = model.predict(walking_path)
    activity_predicted = []
    activity_number_predicted = []
    for pred in predictions:
        activity_number = np.argmax(pred)
        activity_predicted.append(label_name[activity_number])
        activity_number_predicted.append(activity_number)
    if len(y_test) > 0:
        y_test = reverse_onehot(y_test)
        print(confusion_matrix(y_test, activity_number_predicted))
        print(classification_report(y_test, activity_number_predicted))
    print(activity_predicted)

'''
df_train, df_test = read_wisdm_dataset("/home/giacomo/CNR/WISDM_dataset/WISDM_ar_v1.1_raw.txt")
print(df_test)
X_train, y_train, X_test, y_test = create_train_test(df_train, df_test, TIME_STEPS=200, STEP=40)
predict_walking(X_test, y_test)
'''

walking_path_file = "/home/giacomo/CNR/WISDM_dataset/Giacomo_walking/sample_1.csv"
df_walking = pd.read_csv(walking_path_file)
df = pd.DataFrame(columns=['x_axis', 'y_axis', 'z_axis', 'activity'], index=range(0))
X_test, y_test, _1, _2 = create_train_test(df_walking, df, 200, 40)
print(df_walking)
predict_walking(X_test, y_test)