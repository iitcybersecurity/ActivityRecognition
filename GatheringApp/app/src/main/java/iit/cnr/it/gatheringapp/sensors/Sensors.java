package iit.cnr.it.gatheringapp.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import iit.cnr.it.gatheringapp.Classifier.TensorFlowClassifier;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.dbutils.DbManage;
import iit.cnr.it.gatheringapp.utils.Utils;
import iit.cnr.it.gatheringapp.Classifier.HARClassifier;

import static iit.cnr.it.gatheringapp.Utils.round;
import static java.lang.Float.POSITIVE_INFINITY;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by giacomo on 24/10/18.
 */

public class Sensors implements SensorEventListener {
    //Sensor variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;
    private Sensor countSensor;
    private float x_acc, y_acc, z_acc, x_gyr, y_gyr, z_gyr;

    //Main activity
    private AppCompatActivity activity;

    //batch size
    private final int batch_size = 50;
    private int countInfluxData = 0;
    private String influxData = "";

    private String username = "";

    private String activityLabel = "unknown";

    private int accelerometerSensibility;
    private int gyroscopeSensibility;
    private int stepCounterSensibility;
    private float baseFrequency;

    private TensorFlowClassifier classifier;
    private HARClassifier HAR;
    private TextView decision;
    private TextView frequency;
    private float[] predictions;
    private View probabilityFragment;
    ArrayList<String> labels = new ArrayList<>();
    ArrayList<Long> time = new ArrayList<>();
    private float rate;

    boolean HAR_set;
    boolean test = false;//true;
    boolean ready = false;
    boolean pred = false;

    //Variable to compute frequency
    long prev_time = 0;
    long current_time = 0;
    float current_sampling_rate = 0;


    int ACCEL_SENSOR_DELAY = 20;
    long lastAccelSensorChange = 0;



    public Sensors() {

    }

    public Sensors(FragmentActivity _activity, String username, boolean set, View fragmentView) {
        this();
        this.activity = (AppCompatActivity) _activity;
        this.username = username;
        probabilityFragment = fragmentView;


        //Accelerometer sensor initialization
        senSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        countSensor = senSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        HAR_set = set;
        if(HAR_set == true) {
            classifier = new TensorFlowClassifier(_activity.getApplicationContext());
            HAR = new HARClassifier(classifier);
            decision = (TextView) probabilityFragment.findViewById(R.id.decision);
        }
    }

    private void compute_sampling_rate() {
        current_time = System.currentTimeMillis();
        current_sampling_rate = (float) ((current_time - prev_time) * pow(10, -3));

        frequency = probabilityFragment.findViewById(R.id.frequency_text2);
        if(prev_time != 0)
            frequency.setText(current_sampling_rate + " Hz");
        prev_time = current_time;
    }

    private void compute_sampling_rate(float sampling) {
        current_time = System.currentTimeMillis();
        current_sampling_rate = (float) ((current_time - prev_time) * pow(10, -3));

        frequency = probabilityFragment.findViewById(R.id.frequency_text2);
        if(prev_time != 0)
            frequency.setText(Float.toString(sampling) + " Hz");
        prev_time = current_time;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        if(test == false){

            if (mySensor.getType() == Sensor.TYPE_GYROSCOPE && HAR_set == false) {
                //Get the gyroscope values
                x_gyr = sensorEvent.values[0];
                y_gyr = sensorEvent.values[1];
                z_gyr = sensorEvent.values[2];

                prepareInfluxData(x_gyr, y_gyr, z_gyr, "gyroscope", activityLabel, -1);

            }

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                long now = System.currentTimeMillis();
                if (now-lastAccelSensorChange > ACCEL_SENSOR_DELAY) {

                    //Get the accelerometer values
                    x_acc = sensorEvent.values[0];
                    y_acc = sensorEvent.values[1];
                    z_acc = sensorEvent.values[2];
                    //compute_sampling_rate(now-lastAccelSensorChange);
                    lastAccelSensorChange = now;

                }

                float predicted = -1;
                if (HAR_set == true) {
                    HAR.setElement(x_acc, y_acc, z_acc);
                    predictions = HAR.activityPrediction();

                    if (predictions != null) {
                        Log.d("PREDICTION", Integer.toString(predictions.length));

                        predicted = writeResults(predictions);
                        pred = true;
                        //compute_sampling_rate();
                    }
/*
                    time.add(System.currentTimeMillis());

                    if(time.size() == 200) {
                        rate = calculateFrequency(time);
                        time.clear();
                    }
                    ready = true;
                    frequency = probabilityFragment.findViewById(R.id.frequency_text2);
                    if(rate != POSITIVE_INFINITY)
                        frequency.setText(rate + " Hz");*/
                }
                prepareInfluxData(x_acc, y_acc, z_acc, "accelerometer", activityLabel, predicted);
            }
            //sensorValue.setText(" " + sensorEvent.values[0]);
        }
    }

    private float writeResults(float[] predictions) {
        float max = -1;
        int idx = -1;
        for (int i = 0; i < predictions.length; i++) {
            final TextView probability = (TextView) probabilityFragment.findViewById(i);
            probability.setText(round(predictions[i] * 100, 2) + "%" );

            if (predictions[i] > max) {
                idx = i;
                max = predictions[i];
            }
        }

        //lettura delle labels di classificazione da file di testo di configurazione
        BufferedReader reader;
        try{
            final InputStream file = activity.getAssets().open("labels_config.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                labels.add(line);
                line = reader.readLine();
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        activityLabel = labels.get(idx);
        decision.setText(labels.get(idx));
        idx++;
        int id = activity.getBaseContext().getResources().getIdentifier("ic" + idx, "drawable", activity.getBaseContext().getPackageName());
        decision.setCompoundDrawablesWithIntrinsicBounds( id, 0, 0, 0);

        return predictions[idx-1];
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startSensors(String activityLabel, Context context) {
        this.activityLabel = activityLabel;
        initSensorsSensibility(context);
        Log.d("SENSORS",
                "Running sensors with following values: " +
                        "Accelerometer {" + accelerometerSensibility + "} - " +
                        "Gyroscope {" + gyroscopeSensibility + "} - " +
                        "StepCounter {" + stepCounterSensibility + "}");

        try {
            if (senGyroscope != null)
                senSensorManager.registerListener(this, senGyroscope, accelerometerSensibility);
            else
                Toast.makeText(this.activity, "Gyroscope not available!", Toast.LENGTH_SHORT).show();
            if (senAccelerometer != null)
                senSensorManager.registerListener(this, senAccelerometer, gyroscopeSensibility);
            else
                Toast.makeText(this.activity, "Accelerometer not available!", Toast.LENGTH_SHORT).show();

            if (countSensor != null)
                senSensorManager.registerListener(this, countSensor, stepCounterSensibility);
            else
                Toast.makeText(this.activity, "Step Counter not available!", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    public void startSensorsHAR(Context context) {
        accelerometerSensibility = 2;// computeSensibility(context);

        try {
            if (senAccelerometer != null)
                senSensorManager.registerListener(this, senAccelerometer, accelerometerSensibility);
            else
                Toast.makeText(this.activity, "Accelerometer not available!", Toast.LENGTH_SHORT).show();
        } catch (Exception exception) {
            exception.printStackTrace();
            throw exception;
        }
    }

    public void stopSensors(Sensors sensors) {
        senSensorManager.unregisterListener(sensors);
        Toast.makeText(this.activity, "Sensors Unregistered", Toast.LENGTH_SHORT).show();
    }

    public void prepareInfluxData(float x, float y, float z, String sensor, String activityLabel,float probability) {
        long timestamp = System.currentTimeMillis();
        String timeStamp = timestamp + "000000";
        String x_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=x,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + ",probability=" + probability +" value=" + x + " " + timeStamp + "\n";
        String y_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=y,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + ",probability=" + probability +" value=" + y + " " + timeStamp + "\n";
        String z_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=z,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + ",probability=" + probability +" value=" + z + " " + timeStamp + "\n";
        String magn_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=magnitude_" + sensor + ",activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + ",probability=" + probability + " " + " value=" + (sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2)))+ " " + timeStamp + "\n";
        String predict_to_write = "activityPrediction,label=" + activityLabel + " probability=" + probability + " " + timeStamp + "\n";
        influxData = influxData + x_to_write + y_to_write + z_to_write + magn_to_write;
        if(pred == true) {
            influxData = influxData + predict_to_write;
            pred = false;
        }
        countInfluxData++;
        if (countInfluxData == batch_size) {
            countInfluxData = 0;
            final String data_to_write = influxData;
            influxData = "";
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    try {

                        DbManage influx = new DbManage();
                        try {
                            System.out.println("Write data " + data_to_write);
                            influx.write_string(activity.getApplicationContext(), data_to_write);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();
        }
    }


    private void initSensorsSensibility(Context context) {
        try {
            accelerometerSensibility = Integer.valueOf(Utils.getConfigValue(context, "accelerometer.sensibility"));
            gyroscopeSensibility = Integer.valueOf(Utils.getConfigValue(context, "gyroscope.sensibility"));
            stepCounterSensibility = Integer.valueOf(Utils.getConfigValue(context, "step.counter.sensibility"));

        } catch (Exception exception) {
            exception.printStackTrace();
            Log.d("SENSORS_ERROR", "Unable to retrieve configuration values, setting to default...");
            accelerometerSensibility = SensorManager.SENSOR_DELAY_NORMAL;
            gyroscopeSensibility = SensorManager.SENSOR_DELAY_NORMAL;
            stepCounterSensibility = SensorManager.SENSOR_DELAY_NORMAL;
        }
    }

 /*   private int computeSensibility(Context context){
        baseFrequency = Integer.valueOf(Utils.getConfigValue(context,"baseFrequency"));
        int delay = 0;
        float diff;
        senSensorManager.registerListener(this, senAccelerometer, 0);
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        diff = abs(baseFrequency - rate);
        senSensorManager.unregisterListener(this, senAccelerometer);
        ready = false;
        for(int i = 1; i < 4; i++){
            accelerometerSensibility = i;
            senSensorManager.registerListener(this, senAccelerometer, accelerometerSensibility);
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if(diff < abs(baseFrequency - rate)){
                diff = abs(baseFrequency - rate);
                delay = i;
            }
            senSensorManager.unregisterListener(this,senAccelerometer);
            ready = false;
        }
        test = false;
        return delay;
    }*/

   /* public float computeSamplingRate() {
        float sampling_rate = 0;
        for(int i=0; i<200; i++){
            time.add(System.currentTimeMillis());
        }
        if(time.size() == 200) {
            sampling_rate = calculateFrequency(time);
            time.clear();
        }
        return sampling_rate;
    }

    private float calculateFrequency(ArrayList<Long> time){
        ArrayList<Float> frequencies = new ArrayList<>();
        for (int i = 1; i<time.size(); i++){
            Log.d("TIME",Float.toString(time.get(i)));
                Float freq = (float) (1 / ((time.get(i) - time.get(i-1)) * pow(10, -3)));
                if(freq < 250.0){
                    frequencies.add(freq);
                }
        }

        Float total = Float.valueOf(0);

        for(int j=0; j<frequencies.size(); j++){
            total += frequencies.get(j);
        }
        Log.d("TOTAL",Float.toString(total));
        return total/frequencies.size();
    }*/
}
