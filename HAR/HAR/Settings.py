#DATASET_PATH = "/home/giacomo/CNR/WISDM_dataset/WISDM_ar_v1.1_raw.txt"
DATASET_PATH = "/home/giacomo/CNR/WISDM_dataset/WISDM_ar_v1.1_raw_1_new_sample.txt"

TIME_STEPS = 200
STEP = 40
num_sensors = 3
num_classes = 6

# Hyper-parameters
BATCH_SIZE = 32
EPOCHS = 50
model_name = "lstm_model"
weights_name = "lstm_weights"