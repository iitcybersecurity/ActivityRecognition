import pickle

import seaborn as sns
import numpy as np
import keras
from keras import models
import pandas as pd
from DataPreprocessing import read_wisdm_dataset, create_train_test, read_wisdm_dataset_1
from Settings import  BATCH_SIZE, EPOCHS, num_classes, TIME_STEPS, STEP, model_name, weights_name, \
    TRAINING_PATH, TESTING_PATH
from model import  lstm_model_1

TRAIN_FROM_SCRATCH = True


def reverse_onehot(onehot_data):
    # onehot_data assumed to be channel last
    data_copy = np.zeros(onehot_data.shape[:-1])
    for c in range(onehot_data.shape[-1]):
        img_c = onehot_data[..., c]
        data_copy[img_c == 1] = c
    return data_copy

#df_train, df_test = read_wisdm_dataset(DATASET_PATH)
df_train = read_wisdm_dataset_1(TRAINING_PATH)
df_test = read_wisdm_dataset_1(TESTING_PATH)
X_train, y_train, X_test, y_test = create_train_test(df_train, df_test, TIME_STEPS, STEP)

model = lstm_model_1(X_train.shape, num_classes)


callbacks_list = [
    keras.callbacks.ModelCheckpoint(
        filepath= weights_name + '.h5',
        monitor='val_loss', save_best_only=True),
    keras.callbacks.EarlyStopping(monitor='loss', patience=5),
    keras.callbacks.TensorBoard(log_dir='./Graph', histogram_freq=0, write_graph=True, write_images=True)
]

model.compile(loss='categorical_crossentropy',
                optimizer='adam', metrics=['accuracy'])

if TRAIN_FROM_SCRATCH == False:
    model = models.load_model(model_name)
    model.load_weights('best_model.h5')
    walking_path_file = "/home/giacomo/CNR/WISDM_dataset/Giacomo_walking/sample_2.csv"
    df_walking = pd.read_csv(walking_path_file)
    df = pd.DataFrame(columns=['x_axis', 'y_axis', 'z_axis', 'activity'], index=range(0))
    X_test, y_test, _1, _2 = create_train_test(df_walking, df, TIME_STEPS, STEP)
    print(y_test)
    # Enable validation to use ModelCheckpoint and EarlyStopping callbacks.
    history = model.fit(X_train,
                        y_train,
                        batch_size=BATCH_SIZE,
                        epochs=EPOCHS,
                        callbacks=callbacks_list,
                        validation_data=(X_test, y_test),
                        shuffle=False,
                        verbose=1)

else:
    model.save(model_name)

    # Enable validation to use ModelCheckpoint and EarlyStopping callbacks.
    history = model.fit(X_train,
                          y_train,
                          batch_size=BATCH_SIZE,
                          epochs=EPOCHS,
                          callbacks=callbacks_list,
                          validation_data=(X_test, y_test),
                          shuffle=True,
                          verbose=1)



    y_test_pred = reverse_onehot(y_test)
    print(model.evaluate(X_test, y_test))

    print(y_test_pred)
    prediction = model.predict(X_test, verbose=1)
    print(prediction)

    with open("test_prediction_without_giacomo.txt", "wb") as fp:  # Pickling
        pickle.dump(prediction, fp)

    with open("test_true.txt", "wb") as fp:  # Pickling
        pickle.dump(y_test_pred, fp)



