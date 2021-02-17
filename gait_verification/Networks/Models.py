from keras import regularizers
from keras.models import Sequential
from keras.layers import LSTM, Dense, Dropout, Conv2D, Flatten, MaxPooling2D, Conv1D, Input, RepeatVector, \
    TimeDistributed
from tensorflow.python.keras.models import Model


def lstm_keras(input_shape, n_classes):
    model = Sequential()
    input_shape = input_shape
    N_HIDDEN_UNITS = 32

    model.add(LSTM(32, return_sequences=True,
                   input_shape=input_shape))  # returns a sequence of vectors of dimension 32
    model.add(LSTM(64,))  # returns a sequence of vectors of dimension 64
    model.add(Flatten())  # returns a sequence of vectors of dimension 64
    model.add(Dense(N_HIDDEN_UNITS, activation='relu'))
    model.add(Dense(n_classes, activation='softmax'))

    return model

def lstm_keras1(input_shape, n_classes):
    model = Sequential()
    input_shape = input_shape
    N_HIDDEN_UNITS = 32

    model.add(LSTM(32, return_sequences=True,
                   input_shape=input_shape))  # returns a sequence of vectors of dimension 32
    model.add(LSTM(64,))  # returns a sequence of vectors of dimension 64
    model.add(Dense(N_HIDDEN_UNITS, activation='relu'))
    model.add(Dense(n_classes, activation='sigmoid'))

    return model



# define the autoencoder network model
def autoencoder_model(X):
    inputs = Input(shape=(X.shape[1], X.shape[2]))
    L1 = LSTM(16, activation='relu', return_sequences=True,
              kernel_regularizer=regularizers.l2(0.00))(inputs)
    L2 = LSTM(4, activation='relu', return_sequences=False)(L1)
    L3 = RepeatVector(X.shape[1])(L2)
    L4 = LSTM(16, activation='relu', return_sequences=True)(L3)
    L5 = LSTM(4, activation='relu', return_sequences=True)(L4)
    output = TimeDistributed(Dense(X.shape[2]))(L5)
    model = Model(inputs=inputs, outputs=output)
    return model




def cnnModel():
    kernalSize1 = 2
    poolingWindowSz = 2
    dropOutRatio = 0.5
    numNueronsFCL1 = 128
    numNueronsFCL2 = 128
    numFilters = 128
    model = Sequential()
    # adding the first convolutionial layer with 32 filters and 5 by 5 kernal size, using the rectifier as the activation function
    model.add(Conv2D(numFilters, (kernalSize1, kernalSize1), input_shape=(X_train.shape[1], X_train.shape[2], 1),
                     activation='relu'))
    # adding a maxpooling layer
    model.add(MaxPooling2D(pool_size=(poolingWindowSz, poolingWindowSz), padding='valid'))
    # adding a dropout layer for the regularization and avoiding over fitting
    model.add(Dropout(dropOutRatio))
    # flattening the output in order to apply the fully connected layer
    model.add(Flatten())
    # adding first fully connected layer with 256 outputs
    model.add(Dense(numNueronsFCL1, activation='relu'))
    # adding second fully connected layer 128 outputs
    model.add(Dense(numNueronsFCL2, activation='relu'))
    # adding softmax layer for the classification
    model.add(Dense(n_classes, activation='softmax'))

    return model


def rcnn_keras(input_shape, n_classes):
    input_shape = input_shape
    model = Sequential()
    model.add(Conv1D(32, 3,
                     activation='relu',
                     input_shape=input_shape))
    # model.add(keras.layers.normalization.BatchNormalization())
    # model.add(keras.layers.Activation('relu'))
    model.add(Dropout(0.2))
    # model.add(MaxPooling2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Conv1D(64, 3,
                     activation='relu'))
    # model.add(keras.layers.normalization.BatchNormalization())
    # model.add(keras.layers.Activation('relu'))
    # model.add(MaxPooling2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Flatten())
    model.add(Dense(n_classes, activation="softmax"))

    '''
    inp = Input(shape=(1,max_seq_len ))
    conv1 = Conv1D(num_filters, 2, padding='same')(inp)
    conv1 = keras.layers.normalization.BatchNormalization()(conv1)
    conv1 = keras.layers.Activation('relu')(conv1)
    conv1 = Dropout(0.2)(conv1)
    conv2 = Conv1D(num_filters*2, 3, padding='same')(conv1)
    conv2 = keras.layers.normalization.BatchNormalization()(conv2)
    conv2 = keras.layers.Activation('relu')(conv2)
    x = Bidirectional(GRU(128, return_sequences=True, dropout=0.1,recurrent_dropout=0.1))(conv2)
    avg_pool = GlobalAveragePooling1D()(x)
    max_pool = GlobalMaxPooling1D()(x)	
    conc = concatenate([avg_pool, max_pool])
    '''
    return model
