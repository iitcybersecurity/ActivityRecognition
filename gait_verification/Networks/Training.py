import numpy as np
from sklearn import metrics
from sklearn.preprocessing import LabelBinarizer
from keras.callbacks import EarlyStopping, Callback, ModelCheckpoint
from Networks.Models import lstm_keras, rcnn_keras, lstm_keras1, autoencoder_model

import matplotlib
matplotlib.use('Agg')

import seaborn as sns

def compute_fp(confusion_matrix, id_subj):
	idx = 0
	fp = 0
	for subject_line in confusion_matrix:
		if idx != id_subj:
			fp +=subject_line[id_subj]
		idx +=1
	return fp

def compute_tn(tp, fn, fp, confusion_matrix):
	tn = 0
	total = 0
	for subject_line in confusion_matrix:
		for value in subject_line:
			total += value
	return total - tp - fn - fp

def compute_metrics(confusion_matrix):
	fpr_arr = []
	fnr_arr = []
	accuracy_arr = []
	eer_arr = []

	id_subj = 0
	for subject_line in confusion_matrix:
		tp = subject_line[id_subj]
		fn = 0
		#print(subject_line)
		if id_subj != len(subject_line):
			for value in subject_line[id_subj+1:]:
				print(value)
				fn +=value
		#print("fn1 " + str(fn))
		if id_subj != 0:
			for value in subject_line[0:id_subj]:
				print(value)
				fn +=value
		fp = compute_fp(confusion_matrix, id_subj)
		tn = compute_tn(tp, fn, fp, confusion_matrix)
		id_subj +=1
		accuracy = (tp + tn) / (tp + tn + fp + fn)
		fpr = fp/(fp + tn)
		#print("fn2 " + str(fn))
		#print(tp)
		fnr = fn/(fn + tp)
		#print(fnr)
		#sys.exit(0)
		eer = 0
		#print("MEtrics:" + str(fpr) + " "  + str(fnr) + " " + str(accuracy))
		fpr_arr.append(fpr)
		fnr_arr.append(fnr)
		accuracy_arr.append(accuracy)
		#eer_threshold = threshold(np.nanargmin(np.absolute((fnr - fpr))))
		#EER = fpr(np.nanargmin(np.absolute((fnr - fpr))))
		#eer_arr.append(EER)
	return np.mean(np.array(fpr_arr)), np.mean(np.array(fnr_arr)), np.mean(np.array(accuracy_arr))


saved_weights_path = "Weights/"
confusion_matrix_path = "confusion_matrix/"
def train_network(X_train, X_test, y_train, y_test, model_type, TEST, LABELS, activity, subject):

    batch_size = 150
    
    epochs = 300

    # Some debugging info
    print("Some useful info to get an insight on dataset's shape and normalisation:")
    print("(X shape, y shape, every X's mean, every X's standard deviation)")
    print(X_test.shape, y_test.shape, np.mean(X_test), np.std(X_test))
    print("The dataset is therefore properly normalised, as expected, but not yet one-hot encoded.")
    input_shape = (X_train.shape[1], X_train.shape[2])
    if model_type == "LSTM":
        n_classes = 27
        model = lstm_keras(input_shape, n_classes)
        model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])
    if model_type == "LSTM_binary":
        n_classes = 1
        model = lstm_keras1(input_shape, n_classes)
        model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['accuracy'])
    if model_type == "LSTM_ae":
        model = autoencoder_model(X_train)
        model.compile(optimizer='adam', loss='mae')
        model.summary()

    #model.compile(loss='categorical_crossentropy', optimizer='adam', metrics=['categorical_accuracy'])
    #model.compile(loss='binary_crossentropy', optimizer='adam', metrics=['binary_crossentropy'])

    # perform one-hot encoding on the labels
    if model_type != "LSTM_ae":
        lb = LabelBinarizer()
        y_test = lb.fit_transform(y_test)
        y_train = lb.fit_transform(y_train)
    if model_type == "LSTM_binary" or model_type == "LSTM_ae":
        checkpoint = ModelCheckpoint(saved_weights_path + model_type + activity +  "_" + str(subject) + ".hdf5", monitor='val_loss', verbose=1,
                                     save_best_only=True, mode='auto', period=1)
    else:
        checkpoint = ModelCheckpoint(saved_weights_path + model_type  + activity + ".hdf5", monitor='val_loss', verbose=1,
                                 save_best_only=True, mode='auto', period=1)

    if TEST == False:
        if model_type == "LSTM_ae":
            history = model.fit(
                X_train,
                X_train,
                validation_data=(X_test, X_test),
                epochs=epochs,
                batch_size=batch_size,
                callbacks=[checkpoint]
            )
        else:
            history = model.fit(
                X_train,
                y_train,
                validation_data=(X_test, y_test),
                epochs=epochs,
                batch_size=batch_size,
                callbacks=[checkpoint]
            )

    if model_type == "LSTM" :
        model.load_weights(saved_weights_path + model_type + activity + ".hdf5")
        y_pred_ohe = model.predict(X_test)
        y_pred_labels = np.argmax(y_pred_ohe, axis=1)
        y_true_labels = np.argmax(y_test, axis=1)
        confusion_matrix = metrics.confusion_matrix(y_true=y_true_labels, y_pred=y_pred_labels)
        #fpr, fnr, eer, accuracy = compute_metrics(confusion_matrix)
        matplotlib.pyplot.figure(figsize=(16, 14))
        sns.set(style='whitegrid', palette='muted', font_scale=1.5)
        sns.heatmap(confusion_matrix, xticklabels=LABELS, yticklabels=LABELS, annot=True, fmt="d");
        matplotlib.pyplot.title("Confusion matrix")
        matplotlib.pyplot.ylabel('True label')
        matplotlib.pyplot.xlabel('Predicted label')
        matplotlib.pyplot.savefig(confusion_matrix_path + model_type + "_" + activity)
        fpr, fnr, accuracy = compute_metrics(confusion_matrix)
        print(activity)
        f = open(confusion_matrix_path + model_type + "_" + activity + ".txt", "w")
        f.write("FPR: " + str(fpr) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy))
        f.close()
        print("FPR: " + str(fpr) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy))
    if model_type == "LSTM_binary" :
        model.load_weights(saved_weights_path + model_type + activity +  "_" + str(subject) + ".hdf5")

        y_pred_ohe = model.predict(X_test)
        y_pred = []
        for y in y_pred_ohe:
            if y > 0.5:
                y_pred.append(1)
            else:
                y_pred.append(0)
        y_true = []
        for y in y_test:
            if y > 0.5:
                y_true.append(1)
            else:
                y_true.append(0)
        id = 0
       
        unknown_correct = 0
        unknown_uncorrect = 0
        for i in y_true:
            if i == y_pred[id]:
                unknown_correct +=1
            else:
                unknown_uncorrect +=1
            id +=1
        f = open(confusion_matrix_path + model_type + "_" + activity + "_" + str(subject) + "unknown.txt", "w")
        f.write("correct: " + str(unknown_correct) +  " uncorrect: " + str(unknown_uncorrect))
        f.close()
        

        confusion_matrix = metrics.confusion_matrix(y_true=y_true, y_pred=y_pred)
        fpr, fnr, accuracy = compute_metrics(confusion_matrix)
        print(activity)
        #f = open(confusion_matrix_path + model_type + "_" + activity + "_" + str(subject) + ".txt", "w")
        #f.write("FPR: " + str(fpr) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy))
        #f.close()
        #print("FPR: " + str(fpr) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy))

        matplotlib.pyplot.figure(figsize=(16, 14))
        sns.set(style='whitegrid', palette='muted', font_scale=1.5)
        sns.heatmap(confusion_matrix, xticklabels=LABELS, yticklabels=LABELS, annot=True, fmt="d");
        matplotlib.pyplot.title("Confusion matrix")
        matplotlib.pyplot.ylabel('True label')
        matplotlib.pyplot.xlabel('Predicted label')
        matplotlib.pyplot.savefig(confusion_matrix_path + model_type + "_" + activity + "_" + str(subject))


    if model_type == "LSTM_ae":
        model.load_weights(saved_weights_path + model_type + activity +  "_" + str(subject) + ".hdf5")

        # plot the loss distribution of the training set
        X_pred = model.predict(X_train)
        print(len(X_pred))
        X_pred = X_pred.reshape(X_pred.shape[0], X_pred.shape[1] * X_pred.shape[2])
        Xtrain = X_train.reshape(X_pred.shape[0], X_train.shape[1] * X_train.shape[2])
        scored = np.mean(np.abs(X_pred - Xtrain), axis=1)
        matplotlib.pyplot.figure(figsize=(16, 9), dpi=80)
        matplotlib.pyplot.title('Loss Distribution', fontsize=16)
        sns.distplot(scored, bins=20, kde=True, color='blue');
        matplotlib.pyplot.xlim([0.0, .5])
        matplotlib.pyplot.savefig("loss_distr.png")
        X_pred = model.predict(X_test)
        X_pred = X_pred.reshape(X_pred.shape[0], X_pred.shape[1] * X_pred.shape[2])
        X_test = X_test.reshape(X_test.shape[0], X_test.shape[1] * X_test.shape[2])

        mse = np.mean(np.abs(X_test - X_pred), axis=1)
        
        thresholds = [0.1, 0.15, 0.2, 0.21, 0.22, 0.23, 0.24, 0.25, 0.26, 0.27, 0.28, 0.29, 0.3,0.35]

        for threshold in thresholds:
            print(threshold)
            anomalies = mse > threshold
            id = 0
            corrects_positive = 0
            total_positive = 0
            total_negative = 0
            corrects_negative = 0
            wrong_positive = 0
            wrong_negative = 0

            for y in y_test:
                if int(y[0]) == 1:
                    total_positive +=1
                    if anomalies[id] == False:
                        corrects_positive +=1
                    else:
                        wrong_positive +=1
                else:
                    total_negative +=1
                    if anomalies[id] == True:
                        corrects_negative +=1
                    else:
                        wrong_negative +=1
                id +=1
            print(str(corrects_positive) + "/" + str(total_positive))
            print(str(corrects_negative) + "/" + str(total_negative))
            if wrong_positive == 0 and corrects_negative == 0:
                far = 10000
            else:
                far = wrong_positive/(wrong_positive + corrects_negative)
            if wrong_negative == 0 and corrects_positive == 0:
                fnr = 10000
            else:
                fnr = wrong_negative/(wrong_negative + corrects_positive)
            if total_positive == 0 and total_negative == 0:
                accuracy = 0
            else:
                accuracy = (corrects_positive + corrects_negative)/(total_positive + total_negative)
            print("FPR: " + str(far) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy))
            f = open(confusion_matrix_path + model_type + "_" + activity + "_" + str(subject) + ".txt", "a")
            f.write("FPR" + str(threshold) + ": " + str(far) + " FNR: " + str(fnr) + " Accuracy: " + str(accuracy) + "\n")
            f.close()

