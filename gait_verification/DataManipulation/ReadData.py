import math

import numpy as np

TRAIN = "train/"
TEST = "test/"
DATA_PATH = "/home/giacomo/CNR/Progetti/GaitRecognition/UCI HAR Dataset/UCI HAR Dataset/"

DATASET_PATH = DATA_PATH + ""

INPUT_SIGNAL_TYPES = [
    "body_acc_x_",
    "body_acc_y_",
    "body_acc_z_",
    "body_gyro_x_",
    "body_gyro_y_",
    "body_gyro_z_",
    "total_acc_x_",
    "total_acc_y_",
    "total_acc_z_"
]

# Output classes to learn how to classify
LABELS = []

for i in range (1,31):
    LABELS.append("Subject" + str(i))

# Load "X" (the neural network's training and testing inputs)

def load_X(X_signals_paths):
    X_signals = []

    for signal_type_path in X_signals_paths:
        file = open(signal_type_path, 'r')
        # Read dataset from disk, dealing with text files' syntax
        X_signals.append(
            [np.array(serie, dtype=np.float32) for serie in [
                row.replace('  ', ' ').strip().split(' ') for row in file
            ]]
        )
        file.close()
    return np.transpose(np.array(X_signals), (1, 2, 0))




# Load "y" (the neural network's training and testing outputs)

def load_y(y_path):
    file = open(y_path, 'r')
    # Read dataset from disk, dealing with text file's syntax
    y_ = np.array(
        [elem for elem in [
            row.replace('  ', ' ').strip().split(' ') for row in file
        ]],
        dtype=np.int32
    )
    file.close()

    # Substract 1 to each output class for friendly 0-based indexing
    return y_ - 1

def load_data_encoder(activities, split_train_test, mode, subject):
    # Load inertial data
    X_train_signals_paths = [
        DATASET_PATH + TRAIN + "Inertial Signals/" + signal + "train.txt" for signal in INPUT_SIGNAL_TYPES
    ]
    X_test_signals_paths = [
        DATASET_PATH + TEST + "Inertial Signals/" + signal + "test.txt" for signal in INPUT_SIGNAL_TYPES
    ]

    X_train = load_X(X_train_signals_paths)
    X_test = load_X(X_test_signals_paths)

    # Load subject labels
    y_train_path_subject = DATASET_PATH + TRAIN + "subject_train.txt"
    y_test_path_subject = DATASET_PATH + TEST + "subject_test.txt"
    y_train_subject = load_y(y_train_path_subject)
    y_test_subject = load_y(y_test_path_subject)

    # Load activity labels
    y_train_path_activity = DATASET_PATH + TRAIN + "y_train.txt"
    y_test_path_activity = DATASET_PATH + TEST + "y_test.txt"
    y_train_activity = load_y(y_train_path_activity)
    y_test_activity = load_y(y_test_path_activity)

    X_total = np.concatenate((X_train, X_test), axis=0)
    y_subject_total = np.concatenate((y_train_subject, y_test_subject), axis=0)
    y_activity_total = np.concatenate((y_train_activity, y_test_activity), axis=0)

    if mode == "autoencoder":
        X_train_test_positive = []
        X_train_test_negative = []
        y_train_test_positive = []
        y_train_test_negative = []

        idx = 0
        for x in X_total:
            if y_activity_total[idx] in activities:
                if y_subject_total[idx] == subject:
                    X_train_test_positive.append(x)
                    y_train_test_positive.append([1])
                else:
                    X_train_test_negative.append(x)
                    y_train_test_negative.append([0])
            idx += 1

        len_min = len(X_train_test_positive)
        x_train_test_negative_arr = np.array(X_train_test_negative)
        x_train_test_positive_arr = np.array(X_train_test_positive)
        y_train_test_positive_arr = np.array(y_train_test_positive)

        seed = 50
        np.random.seed(seed)
        np.random.shuffle(x_train_test_negative_arr)

        x_train_test_negative_arr = x_train_test_negative_arr[0:len_min]
        y_train_test_negative_arr = y_train_test_negative[0:len_min]

        x_total = np.concatenate((x_train_test_negative_arr, x_train_test_positive_arr), axis=0)
        y_total = np.concatenate((y_train_test_negative_arr, y_train_test_positive_arr), axis=0)
        X_train = x_train_test_positive_arr[0:math.ceil(len(x_train_test_positive_arr) * split_train_test)]
        x_test_positive = x_train_test_positive_arr[math.ceil(len(x_train_test_positive_arr) * split_train_test):]
        y_test_positive = y_train_test_positive_arr[math.ceil(len(x_train_test_positive_arr) * split_train_test):]
        x_test_negative = x_train_test_negative_arr
        y_test_negative = y_train_test_negative_arr

        X_test = np.concatenate((x_test_positive, x_test_negative), axis=0)
        y_test = np.concatenate((y_test_positive, y_test_negative), axis=0)


        return X_train, X_test, None, y_test

def load_data_authentication(activities, split_train_test, mode, subject, unknown_subjects):
    # Load inertial data
    X_train_signals_paths = [
        DATASET_PATH + TRAIN + "Inertial Signals/" + signal + "train.txt" for signal in INPUT_SIGNAL_TYPES
    ]
    X_test_signals_paths = [
        DATASET_PATH + TEST + "Inertial Signals/" + signal + "test.txt" for signal in INPUT_SIGNAL_TYPES
    ]

    X_train = load_X(X_train_signals_paths)
    X_test = load_X(X_test_signals_paths)

    # Load subject labels
    y_train_path_subject = DATASET_PATH + TRAIN + "subject_train.txt"
    y_test_path_subject = DATASET_PATH + TEST + "subject_test.txt"
    y_train_subject = load_y(y_train_path_subject)
    y_test_subject = load_y(y_test_path_subject)

    # Load activity labels
    y_train_path_activity = DATASET_PATH + TRAIN + "y_train.txt"
    y_test_path_activity = DATASET_PATH + TEST + "y_test.txt"
    y_train_activity = load_y(y_train_path_activity)
    y_test_activity = load_y(y_test_path_activity)

    X_total = np.concatenate((X_train, X_test), axis=0)
    y_subject_total = np.concatenate((y_train_subject, y_test_subject), axis=0)
    y_activity_total = np.concatenate((y_train_activity, y_test_activity), axis=0)

    if mode == "binary_unknown":
        X_train_test_positive = []
        X_train_test_negative = []
        y_train_test_positive = []
        y_train_test_negative = []

        idx = 0
        for x in X_total:


            if y_activity_total[idx] in activities:
                if y_subject_total[idx] == subject:
                    X_train_test_positive.append(x)
                    y_train_test_positive.append([1])
                else:
                    if y_subject_total[idx] in unknown_subjects:
                        X_train_test_negative.append(x)
                        y_train_test_negative.append([0])
            idx +=1
    return np.array(X_train_test_positive), np.array(X_train_test_negative), np.array(y_train_test_positive), np.array(y_train_test_negative)

    if mode == "binary":
        X_train_test_positive = []
        X_train_test_negative = []
        y_train_test_positive = []
        y_train_test_negative = []

        idx = 0
        for x in X_total:


            if y_activity_total[idx] in activities:
                if y_subject_total[idx] == subject:
                    X_train_test_positive.append(x)
                    y_train_test_positive.append([1])
                else:
                    if y_subject_total[idx] not in unknown_subjects:
                        X_train_test_negative.append(x)
                        y_train_test_negative.append([0])
            idx +=1

        len_min = len(X_train_test_positive)
        x_train_test_negative_arr = np.array(X_train_test_negative)
        x_train_test_positive_arr = np.array(X_train_test_positive)
        y_train_test_positive_arr = np.array(y_train_test_positive)

        seed = 50
        np.random.seed(seed)
        np.random.shuffle(x_train_test_negative_arr)

        x_train_test_negative_arr = x_train_test_negative_arr[0:len_min]
        y_train_test_negative_arr = y_train_test_negative[0:len_min]


        x_total = np.concatenate((x_train_test_negative_arr, x_train_test_positive_arr), axis=0)
        y_total = np.concatenate((y_train_test_negative_arr, y_train_test_positive_arr), axis=0)

        seed = 50
        np.random.seed(seed)
        np.random.shuffle(x_total)
        np.random.seed(seed)
        np.random.shuffle(y_total)


        X_train = x_total[0:math.ceil(len(x_total)*split_train_test)]
        X_test = x_total[math.ceil(len(x_total)*split_train_test):]
        y_train = y_total[0:math.ceil(len(x_total)*split_train_test)]
        y_test = y_total[math.ceil(len(x_total)*split_train_test):]

        seed = 50
        np.random.seed(seed)
        np.random.shuffle(X_train)
        np.random.seed(seed)
        np.random.shuffle(y_train)


        return X_train, X_test, y_train, y_test
'''

def load_data_authentication(activities, split_train_test, mode, subject):
    #Load inertial data
    X_train_signals_paths = [
        DATASET_PATH + TRAIN + "Inertial Signals/" + signal + "train.txt" for signal in INPUT_SIGNAL_TYPES
    ]
    X_test_signals_paths = [
        DATASET_PATH + TEST + "Inertial Signals/" + signal + "test.txt" for signal in INPUT_SIGNAL_TYPES
    ]

    X_train = load_X(X_train_signals_paths)
    X_test = load_X(X_test_signals_paths)

    #Load subject labels
    y_train_path_subject = DATASET_PATH + TRAIN + "subject_train.txt"
    y_test_path_subject = DATASET_PATH + TEST + "subject_test.txt"
    y_train_subject = load_y(y_train_path_subject)
    y_test_subject = load_y(y_test_path_subject)

    #Load activity labels
    y_train_path_activity = DATASET_PATH + TRAIN + "y_train.txt"
    y_test_path_activity = DATASET_PATH + TEST + "y_test.txt"
    y_train_activity = load_y(y_train_path_activity)
    y_test_activity = load_y(y_test_path_activity)

    X_total = np.concatenate((X_train, X_test), axis=0)
    y_subject_total = np.concatenate((y_train_subject, y_test_subject), axis=0)
    y_activity_total = np.concatenate((y_train_activity, y_test_activity), axis=0)

    count_element = []
    for i in range (1,3):
        count_element.append(0)

    idx = 0
    for y in y_subject_total:
        if len(activities) == 0 and (int(y) == 0 or int(y) == 1):
            count_element[int(y)] +=1
        else:
            if int(y_activity_total[idx][0]) in activities:
                count_element[int(y)] +=1
        idx +=1
    train_single_element = []
    test_single_element = []

    for element in count_element:
        train_single_element.append(math.ceil(element * split_train_test))
        test_single_element.append(element - math.ceil(element * split_train_test))

    X_train_split = []
    y_train_split = []
    X_test_split = []
    y_test_split = []

    idx = 0
    for x in X_total:
        if len(activities) == 0:
            if((int(y_subject_total[idx]) == 0 or int(y_subject_total[idx]) == 1)):
                if train_single_element[int(y_subject_total[idx])] == 0:
                    X_test_split.append(x)
                    y_test_split.append(y_subject_total[idx])
                    test_single_element[int(y_subject_total[idx])] -=1
                else:
                    X_train_split.append(x)
                    y_train_split.append(y_subject_total[idx])
                    train_single_element[int(y_subject_total[idx])] -=1
        else:
            if train_single_element[int(y_subject_total[idx])] == 0 and int(y_activity_total[idx][0]) in activities:
                X_test_split.append(x)
                y_test_split.append(y_subject_total[idx])
                test_single_element[int(y_subject_total[idx])] -=1
            else:
                if int(y_activity_total[idx][0]) in activities:
                    X_train_split.append(x)
                    y_train_split.append(y_subject_total[idx])
                    train_single_element[int(y_subject_total[idx])] -=1
        idx +=1


    X_train = np.array(X_train_split)
    y_train = np.array(y_train_split)
    X_test = np.array(X_test_split)
    y_test = np.array(y_test_split)

    seed = 50
    np.random.seed(seed)
    np.random.shuffle(X_train)
    np.random.seed(seed)
    np.random.shuffle(y_train)

    return X_train, X_test, y_train, y_test
'''



def load_data_recognition(activities, split_train_test, unknown):
    #Load inertial data
    X_train_signals_paths = [
        DATASET_PATH + TRAIN + "Inertial Signals/" + signal + "train.txt" for signal in INPUT_SIGNAL_TYPES
    ]
    X_test_signals_paths = [
        DATASET_PATH + TEST + "Inertial Signals/" + signal + "test.txt" for signal in INPUT_SIGNAL_TYPES
    ]

    X_train = load_X(X_train_signals_paths)
    X_test = load_X(X_test_signals_paths)

    #Load subject labels
    y_train_path_subject = DATASET_PATH + TRAIN + "subject_train.txt"
    y_test_path_subject = DATASET_PATH + TEST + "subject_test.txt"
    y_train_subject = load_y(y_train_path_subject)
    y_test_subject = load_y(y_test_path_subject)

    #Load activity labels
    y_train_path_activity = DATASET_PATH + TRAIN + "y_train.txt"
    y_test_path_activity = DATASET_PATH + TEST + "y_test.txt"
    y_train_activity = load_y(y_train_path_activity)
    y_test_activity = load_y(y_test_path_activity)

    X_total = np.concatenate((X_train, X_test), axis=0)
    y_subject_total = np.concatenate((y_train_subject, y_test_subject), axis=0)
    y_activity_total = np.concatenate((y_train_activity, y_test_activity), axis=0)

    count_element = []
    for i in range (1,31):
        count_element.append(0)

    idx = 0
    for y in y_subject_total:
        if len(activities) == 0:
            count_element[int(y)] +=1
        else:
            if int(y_activity_total[idx][0]) in activities:
                count_element[int(y)] +=1
        idx +=1
    train_single_element = []
    test_single_element = []

    for element in count_element:
        train_single_element.append(math.ceil(element * split_train_test))
        test_single_element.append(element - math.ceil(element * split_train_test))


    X_train_split = []
    y_train_split = []
    X_test_split = []
    y_test_split = []


    idx = 0
    for x in X_total:
        if len(activities) == 0:
            if train_single_element[int(y_subject_total[idx])] == 0:
                if y_subject_total[idx] not in unknown:
                    X_test_split.append(x)
                    y_test_split.append(y_subject_total[idx])
                    test_single_element[int(y_subject_total[idx])] -=1
            else:
                if y_subject_total[idx] not in unknown:
                    X_train_split.append(x)
                    y_train_split.append(y_subject_total[idx])
                    train_single_element[int(y_subject_total[idx])] -=1
        else:
            if train_single_element[int(y_subject_total[idx])] == 0 and int(y_activity_total[idx][0]) in activities:
                if y_subject_total[idx] not in unknown:
                    X_test_split.append(x)
                    y_test_split.append(y_subject_total[idx])
                    test_single_element[int(y_subject_total[idx])] -=1
            else:
                if int(y_activity_total[idx][0]) in activities:
                    if y_subject_total[idx] not in unknown:
                        X_train_split.append(x)
                        y_train_split.append(y_subject_total[idx])
                        train_single_element[int(y_subject_total[idx])] -=1
        idx +=1


    X_train = np.array(X_train_split)
    y_train = np.array(y_train_split)
    X_test = np.array(X_test_split)
    y_test = np.array(y_test_split)

    seed = 50
    np.random.seed(seed)
    np.random.shuffle(X_train)
    np.random.seed(seed)
    np.random.shuffle(y_train)

    return X_train, X_test, y_train, y_test

    '''
    #3d input shape
    X_total_3d = []
    for x in X_total:
        X_total_3d.append(x.reshape(128,9,1))
    X_total = np.array(X_total_3d)
    '''

    '''
    seed = 50
    np.random.seed(seed)
    np.random.shuffle(X_total)
    np.random.seed(seed)
    np.random.shuffle(y_total)
    X_train = X_total[0:math.ceil(len(X_total)*0.8)]
    X_test = X_total[math.ceil(len(X_total)*0.8):]
    
    y_train = y_total[0:math.ceil(len(y_total)*0.8)]
    y_test = y_total[math.ceil(len(y_total)*0.8):]
    '''
