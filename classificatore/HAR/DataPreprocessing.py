from scipy import stats
import pandas as pd
import numpy as np
from sklearn.preprocessing import RobustScaler, OneHotEncoder


def read_wisdm_dataset(dataset_path):
    column_names = [
        'user_id',
        'activity',
        'timestamp',
        'x_axis',
        'y_axis',
        'z_axis'
    ]

    df = pd.read_csv(
        dataset_path,
        header=None,
        names=column_names
    )

    df.z_axis.replace(regex=True, inplace=True, to_replace=r';', value=r'')
    df['z_axis'] = df.z_axis.astype(np.float64)
    df.dropna(axis=0, how='any', inplace=True)

    df_train = df[df['user_id'] <= 30]
    df_test = df[df['user_id'] > 30]
    scale_columns = ['x_axis', 'y_axis', 'z_axis']

    scaler = RobustScaler()
    scaler = scaler.fit(df_train[scale_columns])

    df_train.loc[:, scale_columns] = scaler.transform(
        df_train[scale_columns].to_numpy()
    )

    df_test.loc[:, scale_columns] = scaler.transform(
        df_test[scale_columns].to_numpy()
    )
    return df_train, df_test

def read_wisdm_dataset_1(dataset_path):
    column_names = [
        'user_id',
        'activity',
        'timestamp',
        'x_axis',
        'y_axis',
        'z_axis'
    ]

    df = pd.read_csv(
        dataset_path,
        header=None,
        names=column_names
    )

    df.z_axis.replace(regex=True, inplace=True, to_replace=r';', value=r'')
    df['z_axis'] = df.z_axis.astype(np.float64)
    df.dropna(axis=0, how='any', inplace=True)


    scale_columns = ['x_axis', 'y_axis', 'z_axis']

    scaler = RobustScaler()
    scaler = scaler.fit(df[scale_columns])

    df.loc[:, scale_columns] = scaler.transform(
        df[scale_columns].to_numpy()
    )

    return df



def create_dataset(X, y, time_steps=1, step=1):
    Xs, ys = [], []
    for i in range(0, len(X) - time_steps, step):
        v = X.iloc[i:(i + time_steps)].values
        labels = y.iloc[i: i + time_steps]
        Xs.append(v)
        ys.append(stats.mode(labels)[0][0])
    return np.array(Xs), np.array(ys).reshape(-1, 1)

def create_train_test(df_train, df_test=None, TIME_STEPS=200, STEP=40):


    X_train, y_train = create_dataset(
        df_train[['x_axis', 'y_axis', 'z_axis']],
        df_train.activity,
        TIME_STEPS,
        STEP
    )
    if df_test.empty == False:
        X_test, y_test = create_dataset(
            df_test[['x_axis', 'y_axis', 'z_axis']],
            df_test.activity,
            TIME_STEPS,
            STEP
        )

    enc = OneHotEncoder(handle_unknown='ignore', sparse=False)

    enc = enc.fit(y_train)

    y_train = enc.transform(y_train)
    if df_test.empty == False:
        y_test = enc.transform(y_test)
        return X_train, y_train, X_test, y_test
    else:
        return X_train, y_train, None, None


    print(X_train.shape, y_train.shape)



