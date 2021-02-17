from DataManipulation.ReadData import load_data_recognition, load_data_authentication, load_data_encoder
from DataManipulation.augmentation import RandSampleTimesteps, DA_Scaling, augment_data, DA_RandSampling, DA_MagWarp, DA_Permutation, DA_Jitter, DA_TimeWarp
from Networks.Training import train_network
import numpy as np

TESTING_ACTIVITIES = [[0], [1], [2], [0,1,2]]
UNKNOWN_SUBJECTS = [1, 2, 3]
LABEL_ACTIVITIES = ["WALKING", "WALKING_UPSTAIRS", "WALKING_DOWNSTAIRS", "SITTING", "STANDING"]
LABEL_SUBJECTS = []

'''
#subject recognition
for i in range(1,31):
    LABEL_SUBJECTS.append("Subject " + str(i))
'''

#binary classification
LABEL_SUBJECTS.append("Target")
LABEL_SUBJECTS.append("Not Target")

ALL_SUBJECTS = []
for i in range (4, 31):
    ALL_SUBJECTS.append(i)

for subject in ALL_SUBJECTS:
    for activities in TESTING_ACTIVITIES:
        #activities = [0,1,2]
        split_train_test = 0.8
        DATA_VIEW = False
        if DATA_VIEW == True:
            idx = 0
            for activity in activities:
                X_train, X_test, y_train, y_test = load_data_recognition([2], split_train_test)
                #X_train, X_test, y_train, y_test = load_data_authentication(activities, 0.8, "binary", subject, UNKNOWN_SUBJECTS)

                print("\n\nACTIVITY: " + LABEL_ACTIVITIES[idx])
                idx +=1
                print("TRAIN DATA: " + str(X_train.shape))
                print("TEST DATA: " + str(X_test.shape))
                print("TOTAL SAMPLES " + str(X_train.shape[0] + X_test.shape[0]))
                sys.exit(0)
        else:
            X_train, X_test, y_train, y_test = load_data_recognition(activities, split_train_test, UNKNOWN_SUBJECTS)
            #X_train, X_test, y_train, y_test = load_data_authentication(activities, 0.8, "binary_unknown", subject, UNKNOWN_SUBJECTS)
            #X_train, X_test, y_train, y_test = load_data_encoder(activities, 0.8, "autoencoder", subject)
            #augmented_function = DA_Scaling
            #print(X_train[0].shape)
            #x_reshaped = np.reshape(X_train[0], (128,9))
            #augment_data(x_reshaped, augmented_function, sigma=0.05, nPerm=4, minSegLength=10, nSample=1000, name_augmentation="Scaling", plot=True)
            #sys.exit(0)

        model_type = "LSTM"
        TEST = False
        LABEL = ""
        for l in activities:
           LABEL = LABEL + "_" + LABEL_ACTIVITIES[l]
        print("X TRAIN:  " +  str(X_train.shape))
        print("X TEST:  " +  str(X_test.shape))
        print(y_train)
        print("###################")
        print(y_test)

        train_network(X_train, X_test, y_train, y_test, model_type, TEST, LABEL_SUBJECTS, LABEL, subject)



