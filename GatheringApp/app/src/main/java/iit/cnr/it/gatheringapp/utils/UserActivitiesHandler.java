package iit.cnr.it.gatheringapp.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.DetectedActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.activities.MainActivity;
import iit.cnr.it.gatheringapp.sensors.Sensors;

/**
 * Created by giacomo on 25/10/18.
 */

public class UserActivitiesHandler {
    private static final int CONFIDENCE = 70;
    private static final int QUEUE_SIZE = 4;

    private static ArrayList<Integer> detectedArray;
    private boolean walking;

    private AppCompatActivity activity;
    private Sensors sensors;
    private String username;
    private int currentActivity;
    private int pastActivity;
    private int CURRENT_ACTIVITIES_TO_SHOW = 6;


    public UserActivitiesHandler(FragmentActivity _activity, String username){
        detectedArray = new ArrayList<>();
        walking = false;
        this.activity = (AppCompatActivity) _activity;
        this.username = username;
        sensors = new Sensors(this.activity, username);
    }

    public String ActivityToString(int type){
        String label = "";
        switch (type) {
            case DetectedActivity.IN_VEHICLE: {
                label = "IN_VEHICLE";
                break;
            }
            case DetectedActivity.ON_BICYCLE: {
                label = "ON_BICYCLE";
                break;
            }
            case DetectedActivity.ON_FOOT: {
                label = "WALKING";
                break;
            }
            case DetectedActivity.RUNNING: {
                label = "RUNNING";
                break;
            }
            case DetectedActivity.STILL: {
                label = "STILL";
                break;
            }
            case DetectedActivity.TILTING: {
                label = "TILTING";
                break;
            }
            case DetectedActivity.WALKING: {
                label = "WALKING";
                break;
            }
            case DetectedActivity.UNKNOWN: {
                label = "UNKNOWN";
                break;
            }
        }
        return label;
    }

    public void notifyThis(String title, String message, String label) {
        Intent notificationIntent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0,
                notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder b = new NotificationCompat.Builder(this.activity.getApplicationContext());
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setTicker("Activity detected")
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent).build();



        switch (label) {
            case "WALKING": {
                b.setSmallIcon(R.drawable.ic_directions_walk_black_24dp);
                break;
            }
            case "IN_VEHICLE": {
                b.setSmallIcon(R.drawable.ic_directions_car_black_24dp);
                break;
            }
            case "RUNNING": {
                b.setSmallIcon(R.drawable.ic_directions_run_black_24dp);
                break;
            }
            case "STILL": {
                b.setSmallIcon(R.drawable.ic_still_black_24dp);
                break;
            }
            case "ON_BICYCLE": {
                b.setSmallIcon(R.drawable.ic_directions_bike_black_24dp);
                break;
            }
        }

        //TODO verificare comportamento anomalo
        NotificationManager nm = (NotificationManager) this.activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1337, b.build());



    }

    public void handleUserActivity(int type, int confidence, Context context) {

        if (confidence > CONFIDENCE) {

            if (detectedArray.size() == QUEUE_SIZE){
                detectedArray.remove(0);
            }
            if(type == DetectedActivity.ON_FOOT)
                detectedArray.add(DetectedActivity.WALKING);
            else
                detectedArray.add(type);

            currentActivity = evaluateActivity();

            if(evaluateActivity()==DetectedActivity.WALKING && !walking){
                //writeOnDb
                sensors.startSensors(ActivityToString(DetectedActivity.WALKING), context);
                walking = true;
            }
            if(evaluateActivity()!=DetectedActivity.WALKING && walking) {
                //StopWriteOnDb
                sensors.stopSensors(sensors);
                walking = false;
            }

            if(currentActivity != pastActivity){
                if(!ActivityToString(currentActivity).equals(""))
                    addRow(ActivityToString(currentActivity), new SimpleDateFormat("HH:mm:ss", Locale.US).format(new Date()));
                pastActivity = currentActivity;
            }
        }
    }

    private void addRow(String label, String time){
        TableLayout activitiesTable = this.activity.findViewById(R.id.activities_table);

        if(activitiesTable.getChildCount()>CURRENT_ACTIVITIES_TO_SHOW) {
            activitiesTable.removeViewAt(0);
        }
        LayoutInflater inflater = (LayoutInflater) this.activity.getApplicationContext().getSystemService
                (Context.LAYOUT_INFLATER_SERVICE);

        TableRow row = (TableRow) inflater.inflate(R.layout.row_table, activitiesTable);
        TextView activityText = row.findViewById(R.id.activityText);

        activityText.setText(label);
        TextView timeText = row.findViewById(R.id.timeText);
        timeText.setText(time);
        ImageView imUpload = row.findViewById(R.id.uploadIcon);
        ImageView imActivity = row.findViewById(R.id.activityIcon);
        switch (label) {
            case "WALKING": {
                imActivity.setImageResource(R.drawable.ic_directions_walk_black_24dp);
                imUpload.setImageResource(R.drawable.ic_upload);
                break;
            }
            case "IN_VEHICLE": {
                imActivity.setImageResource(R.drawable.ic_directions_car_black_24dp);
                break;
            }
            case "RUNNING": {
                imActivity.setImageResource(R.drawable.ic_directions_run_black_24dp);
                break;
            }
            case "STILL": {
                imActivity.setImageResource(R.drawable.ic_still_black_24dp);
                break;
            }
            case "ON_BICYCLE": {
                imActivity.setImageResource(R.drawable.ic_directions_bike_black_24dp);
                break;
            }
        }
        activitiesTable.addView(row);
        notifyThis("GatheringApp", label + " activity", label);

    }

    private int evaluateActivity() {
        int max = 0, count = 0;
        for (int i = 0; i < detectedArray.size(); i++) {
            int num = detectedArray.get(i);
            if (num == max) {
                count++;
            } else if (num > max) {
                max = num;
                count = 1;
            }
        }
        if(count > detectedArray.size()*0.75)
            return max;
        else
            return -1;
    }
}
