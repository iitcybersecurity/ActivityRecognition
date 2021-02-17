from tensorflow.python.keras.layers import LSTM, Dropout, Dense, TimeDistributed, Conv1D, MaxPooling1D, Flatten, \
    Reshape, Bidirectional
from tensorflow.python.keras.models import Sequential

'''
def lstm_model(trainX, trainy, testX, testy):
    N_CLASSES = 6
    verbose, epochs, batch_size = 1, 15, 64
    n_timesteps, n_features, n_outputs = trainX.shape[1], trainX.shape[2], trainy.shape[1]
    n_timesteps = trainX.shape[1]
    n_features = trainX.shape[2]
    model = Sequential()
    model.add(LSTM(32, input_shape=(n_timesteps, n_features)))
    #model.add(Dropout(0.5))
    model.add(Dense(64, activation='relu'))
    model.add(Dense(N_CLASSES, activation='softmax'))
    model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['accuracy'])
    print(model.summary())
    # fit network
    model.fit(trainX, trainy, epochs=epochs, batch_size=batch_size, verbose=verbose)
    # evaluate model
    _, accuracy = model.evaluate(testX, testy, batch_size=batch_size, verbose=0)
    return accuracy
'''
def lstm_model(input_shape, num_classes):
    model = Sequential()
    model.add(TimeDistributed(Conv1D(filters=80, kernel_size=3, activation='relu'), input_shape=(None,input_shape[2],input_shape[3])))
    model.add(TimeDistributed(Conv1D(filters=80, kernel_size=3, activation='relu')))
    model.add(TimeDistributed(Dropout(0.5)))
    model.add(TimeDistributed(MaxPooling1D(pool_size=2)))
    model.add(TimeDistributed(Flatten()))
    model.add(LSTM(100))
    model.add(Dropout(0.5))
    model.add(Dense(100, activation='relu'))
    model.add(Dense(num_classes, activation='softmax'))
    return model

def lstm_model_1(input_shape, num_classes):
    model = Sequential()
    model.add(
        Bidirectional(
            LSTM(
                units=128,
                input_shape=[input_shape[1], input_shape[2]]
            )
        )
    )
    model.add(Dropout(rate=0.5))
    model.add(Dense(units=128, activation='relu'))
    model.add(Dense(num_classes, activation='softmax'))
    model.build(input_shape)
    print(model.summary())
    return model