package iit.cnr.it.gatheringapp.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;
import iit.cnr.it.gatheringapp.dbutils.DbManage;
import iit.cnr.it.gatheringapp.MainActivity;
import iit.cnr.it.gatheringapp.utils.Utils;

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
    private MainActivity activity;

    //batch size
    private int batch_size = 50;
    private int countInfluxData = 0;
    private String influxData = "";

    private String username = "";

    private String activityLabel = "";


    public Sensors(MainActivity _activity, String username) {
        this.activity = _activity;
        this.username = username;

        //Accelerometer sensor initialization
        senSensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        countSensor = senSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Get the gyroscope values
            x_gyr = sensorEvent.values[0];
            y_gyr = sensorEvent.values[1];
            z_gyr = sensorEvent.values[2];

            prepareInfluxData(x_gyr, y_gyr, z_gyr, "gyroscope");

        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            //Get the accelerometer values
            x_acc = sensorEvent.values[0];
            y_acc = sensorEvent.values[1];
            z_acc = sensorEvent.values[2];
            prepareInfluxData(x_acc, y_acc, z_acc, "accelerometer");

            //sensorValue.setText(" " + sensorEvent.values[0]);
        }
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void startSensors(String activityLabel, Context context) {
        this.activityLabel = activityLabel;
        int accelerometerSensibility = Integer.getInteger(Utils.getConfigValue(context, "accelerometer.sensibility"));
        int gyroscopeSensibility = Integer.getInteger(Utils.getConfigValue(context, "gyroscope.sensibility"));
        int stepCounterSensibility = Integer.getInteger(Utils.getConfigValue(context, "step.counter.sensibility"));

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

    public void stopSensors() {
        senSensorManager.unregisterListener(this);
        Toast.makeText(this.activity, "Sensors Unregistered", Toast.LENGTH_SHORT).show();
    }

    public void prepareInfluxData(float x, float y, float z, String sensor) {
        long timestamp = System.currentTimeMillis();
        String timeStamp = Long.toString(timestamp) + "000000";
        String x_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=x,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + " value=" + x + " " + timeStamp + "\n";
        String y_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=y,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + " value=" + y + " " + timeStamp + "\n";
        String z_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=z,activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + " value=" + z + " " + timeStamp + "\n";
        String magn_to_write = "activities,sensor=" + sensor + ",device=smartphone,axes=magnitude_" + sensor + ",activity=" + activityLabel + ",user=" + username.replaceAll("\\s", "") + " value=" + (sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2))) + " " + timeStamp + "\n";
        influxData = influxData + x_to_write + y_to_write + z_to_write + magn_to_write;
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
}
