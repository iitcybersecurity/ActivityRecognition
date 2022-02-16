/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import id.unify.sdk.gaitauth.Authenticator;
import id.unify.sdk.gaitauth.FeatureEventListener;
import id.unify.sdk.gaitauth.GaitAuth;
import id.unify.sdk.gaitauth.GaitAuthException;
import id.unify.sdk.gaitauth.GaitFeature;
import id.unify.sdk.gaitauth.GaitModel;
import id.unify.sdk.gaitauth.GaitQuantileConfig;

/**
 * A Foreground service to handle the collection, training, and testing of collected GaitFeatures
 * using UnifyID GaitAuth SDK
 * <p>
 *  More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/
 * <p>
 * */
public class GaitAuthService extends Service implements FeatureEventListener {
    private static final String TAG = "GaitAuthService";

    private static final double QUANTILE_THRESHOLD = .8;

    static final int NOTIFICATION_ID = 9527;
    static final String CHANNEL_ID = "id.unify.gaitauth_sample_app.foreground-service.channel-id";
    static final String CHANNEL_NAME = "id.unify.gaitauth_sample_app.foreground-service.channel-name";

    private static final String NOTIFICATION_CONTENT_READY = "GaitAuth service is ready.";
    private static final String NOTIFICATION_CONTENT_COLLECTION = "GaitAuth service is collecting features.";

    public static final String BROADCAST_EXTRA_FEATURE_COUNT = "BROADCAST_EXTRA_FEATURE_COUNT";
    public static final String ACTION_FEATURE_COLLECTED_COUNT_UPDATE =
            "ACTION_FEATURE_COLLECTED_COUNT_UPDATE";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void initNotificationChannel(Context context) {
        NotificationManager manager = context.getSystemService(NotificationManager.class);
        if (manager == null) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        manager.createNotificationChannel(channel);
    }

    public class GaitAuthServiceBinder extends Binder {
        GaitAuthService getService() {
            return GaitAuthService.this;
        }
    }

    private PowerManager.WakeLock wakeLock;
    private final IBinder binder = new GaitAuthServiceBinder();
    private FeatureStore featureStore;
    private Authenticator authenticator;
    private boolean training = false;
    private boolean testing = false;

    @Override
    public void onCreate() {
        super.onCreate();
        startForeground(NOTIFICATION_ID, makeNotification(NOTIFICATION_CONTENT_READY));
        initiateWakeLock();
        featureStore = FeatureStore.getInstance(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // Acquire a partial wakelock to help extending the active time of data collection. It is
    // optional when using GaitAuth SDK depending on your application.
    @SuppressLint("WakelockTimeout")
    private void initiateWakeLock() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            // PARTIAL_WAKE_LOCK: Ensures that the CPU is running;
            // the screen and keyboard backlight will be allowed to go off.
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "GaitAuth-Sample:" + TAG);
            if (wakeLock != null) {
                wakeLock.acquire();
            }
        } else {
            Log.e(TAG, "Failed to get an instance of PowerManager");
        }
    }

    private void updateNotificationContent(String contentText) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.w(TAG, "NotificationManager is null, stopping GaitAuth service.");
            stopSelf();
            return;
        }
        Notification notification = makeNotification(contentText);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }

    private Notification makeNotification(String contentText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return makeNotificationPostOreo(contentText);
        }
        return makeNotificationPreOreo(contentText);
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private Notification makeNotificationPostOreo(String contentText) {
        return new Notification.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(contentText)
                .setSound(null)
                .setOngoing(true)
                .build();
    }

    private Notification makeNotificationPreOreo(String contentText) {
        return new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentText(contentText)
                .setSound(null)
                .setOngoing(true)
                .build();
    }

    /**
     * A callback that receives new {@link GaitFeature}
     * after {@link #startFeatureCollectionForTraining()} is called
     * <p>
     * Every new GaitFeature will be buffered to local disk
     * and Broadcast is sent to update the number of collected Features to the UI
     */
    @Override
    public void onNewFeature(GaitFeature feature) {
        featureStore.add(feature);
        int count = Preferences.getInt(Preferences.FEATURE_COLLECTED_COUNT) + 1;
        Preferences.put(Preferences.FEATURE_COLLECTED_COUNT, count);
        // Broadcast increased collected count
        Intent intent = new Intent(ACTION_FEATURE_COLLECTED_COUNT_UPDATE);
        intent.putExtra(BROADCAST_EXTRA_FEATURE_COUNT, count);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    public boolean isTraining() {
        return training;
    }

    /**
     * Start collecting gait features for Training
     * <p>
     * New collected feature will be return in {@link #onNewFeature(GaitFeature)} of this class
     * <p>
     * More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/model-training/#stop-feature-collection
     */
    public void startFeatureCollectionForTraining() throws GaitAuthException {
        GaitAuth.getInstance().registerListener(this);
        updateNotificationContent(NOTIFICATION_CONTENT_COLLECTION);
        training = true;
    }

    /**
     * Stop collecting gait features for Training
     * <p>
     * {@link #onNewFeature(GaitFeature)} will stop receive new GaitFeatures
     * <p>
     * More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/model-training/#start-feature-collection
     */
    public void stopFeatureCollectionForTraining() {
        GaitAuth.getInstance().unregisterAllListeners();
        updateNotificationContent(NOTIFICATION_CONTENT_READY);
        training = false;
    }

    public boolean isTesting() {
        return testing;
    }

    /**
     * Start collecting gait features for Testing
     * <p>
     * NOTE: No callback is needed for collecting features in testing phrase
     * <p>
     */
    public void startFeatureCollectionForTesting(GaitModel model) throws GaitAuthException {
        GaitQuantileConfig config = new GaitQuantileConfig(QUANTILE_THRESHOLD);
        authenticator = GaitAuth.getInstance().createAuthenticator(config, model);
        updateNotificationContent(NOTIFICATION_CONTENT_COLLECTION);
        testing = true;
    }

    /**
     * Stop collecting gait features for Testing
     */
    public void stopFeatureCollectionForTesting() {
        if (authenticator != null) {
            authenticator.stop();
            updateNotificationContent(NOTIFICATION_CONTENT_READY);
            testing = false;
        }
    }

    @Nullable
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public void reset() {
        if (training) {
            stopFeatureCollectionForTraining();
        }

        if (testing) {
            stopFeatureCollectionForTesting();
        }

        training = false;
        testing = false;
        featureStore.empty();
        authenticator = null;
    }
}
