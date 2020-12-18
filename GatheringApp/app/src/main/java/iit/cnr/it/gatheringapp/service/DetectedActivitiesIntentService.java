package iit.cnr.it.gatheringapp.service;
import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;

/**
 * Created by giacomo on 24/10/18.
 */

   public class DetectedActivitiesIntentService  extends IntentService {

        public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";


        protected static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

        public DetectedActivitiesIntentService() {
            // Use the TAG to name the worker thread.
            super(TAG);
        }

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void onHandleIntent(Intent intent) {

            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the list of the probable activities associated with the current state of the
            // device. Each activity is associated with a confidence level, which is an int between
            // 0 and 100.
            ArrayList<DetectedActivity> detectedActivities = (ArrayList) result.getProbableActivities();
            System.out.println("DETECTED ACTIVITY");
            for (DetectedActivity activity : detectedActivities) {
                Log.e(TAG, "Detected activity: " + activity.getType() + ", " + activity.getConfidence());
                /*Toast.makeText(getApplicationContext(),
                        "Requesting activity updates failed to start",
                        Toast.LENGTH_SHORT)
                        .show();*/
                broadcastActivity(activity);
            }
        }

        private void broadcastActivity(DetectedActivity activity) {
            Intent intent = new Intent(BROADCAST_DETECTED_ACTIVITY);
            intent.putExtra("type", activity.getType());
            intent.putExtra("confidence", activity.getConfidence());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
    }

