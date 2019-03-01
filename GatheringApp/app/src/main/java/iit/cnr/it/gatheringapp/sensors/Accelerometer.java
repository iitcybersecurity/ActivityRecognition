package iit.cnr.it.gatheringapp.sensors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.MainActivity;
import iit.cnr.it.gatheringapp.utils.Utils;


import java.util.Calendar;

import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by giacomo on 05/02/18.
 */

@SuppressLint("ValidFragment")
public class Accelerometer extends android.support.v4.app.Fragment implements SensorEventListener, AdapterView.OnItemSelectedListener, View.OnClickListener {

    //Sensor variables
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;
    private Sensor senGyroscope;
    private Sensor countSensor;

    //Graph variables
    GraphView graph_x;
    GraphView graph_y;
    GraphView graph_z;

    //data series variable
    LineGraphSeries<DataPoint> series_x;
    LineGraphSeries<DataPoint> series_y;
    LineGraphSeries<DataPoint> series_z;

    //Graph choose variable: 0--> accelerometer, 1--> gyroscope, 2--> Magnitude
    int sensor_choosed = 0;

    //Variable to compute frequency
    long prev_time = 0;
    long current_time = 0;
    float current_sampling_rate = 0;
    TextView frequency_text;

    //Start and Stop button
    Button start_stop_btn;

    //Start Stop sensors variable
    public boolean run;

    //sensors variables
    float x_acc, y_acc, z_acc, x_gyr, y_gyr, z_gyr;

    //streaming button
    private Button connectPhones;

    private Context context;
    private String userName = "";
    private Activity activity;
    private Fragment parentFragment;

    public Accelerometer (Context context, Fragment parentFragment) {
        this.context = context;
        this.parentFragment = parentFragment;
    }

    public Accelerometer(Context context, String userName, Activity _activity){
        this.context = context;
        this.userName = userName;
        this.activity = _activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.accelerometer_fragment,container,false);

        TableLayout activitiesTable = parentFragment.getActivity().findViewById(R.id.ActivitiesTable);
        activitiesTable.setVisibility(View.INVISIBLE);

        //Accelerometer sensor initialization
        senSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        countSensor = senSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        start_stop_btn = v.findViewById(R.id.button_start_stop);
        start_stop_btn.setOnClickListener(this);


        startSensors();

        //get the Frequency textView
        frequency_text =  v.findViewById(R.id.frequency_text);

        //Spinner creation to select the sensor to plot
        Spinner spinner = (Spinner) v.findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sensors_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        //Graph x initialization
        graph_x = (GraphView) v.findViewById(R.id.graph_x);
        // activate horizontal zooming and scrolling
        graph_x.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph_x.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph_x.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph_x.getViewport().setScrollableY(true);

        graph_x.getGridLabelRenderer().setHorizontalLabelsVisible(false);


        series_x = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_x.addSeries(series_x);
        graph_x.setTitle("X axis");

        //Graph y initialization
        graph_y = (GraphView) v.findViewById(R.id.graph_y);
        // activate horizontal zooming and scrolling
        graph_y.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph_y.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph_y.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph_y.getViewport().setScrollableY(true);
        graph_y.getGridLabelRenderer().setHorizontalLabelsVisible(false);
        series_y = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_y.addSeries(series_y);
        graph_y.setTitle("Y axis");


        //Graph z initialization
        graph_z = (GraphView) v.findViewById(R.id.graph_z);
        // activate horizontal zooming and scrolling
        graph_z.getViewport().setScalable(true);
        // activate horizontal scrolling
        graph_z.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph_z.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph_z.getViewport().setScrollableY(true);
        graph_z.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        series_z = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_z.addSeries(series_z);
        graph_z.setTitle("Z axis");

        graph_x.setVisibility(View.INVISIBLE);
        graph_y.setVisibility(View.INVISIBLE);
        graph_z.setVisibility(View.VISIBLE);
        //startSensors();

        return v;
    }

    private Button.OnClickListener connectListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    public void computeSamplingRate(){
        current_time = System.currentTimeMillis();
        current_sampling_rate = (float) (1/((current_time - prev_time) * pow(10,-3)));
        prev_time = current_time;
        frequency_text.setText(current_sampling_rate + " Hz");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if(mySensor.getType() == Sensor.TYPE_ACCELEROMETER){
            //Get the accelerometer values
            x_acc = sensorEvent.values[0];
            y_acc = sensorEvent.values[1];
            z_acc = sensorEvent.values[2];
        }

        if(mySensor.getType() == Sensor.TYPE_GYROSCOPE){
            //Get the gyroscope values
            x_gyr = sensorEvent.values[0];
            y_gyr = sensorEvent.values[1];
            z_gyr = sensorEvent.values[2];

        }

        if (sensor_choosed!=1) {
            //Sampling rate computation
            computeSamplingRate();
            //Accelerometer
            if (sensor_choosed==0) {
                series_x.appendData(new DataPoint(Calendar.getInstance().getTime(), x_acc), true, 40);
                series_y.appendData(new DataPoint(Calendar.getInstance().getTime(), y_acc), true, 40);
                series_z.appendData(new DataPoint(Calendar.getInstance().getTime(), z_acc), true, 40);
            }
            //Magnitude
            if(sensor_choosed==2) {
                series_y.appendData(new DataPoint(Calendar.getInstance().getTime(), sqrt(pow(x_acc,2) + pow(y_acc,2) + pow(z_acc,2))), true, 40);
            }
        }
        //Gyroscope
        if (sensor_choosed==1) {
            computeSamplingRate();
            Log.v("Gyr: ",  System.currentTimeMillis()/1000 + " " + x_gyr + " " + y_gyr + " " + z_gyr + " " + graph_x.getViewport().getMinX(true));
            series_x.appendData(new DataPoint(Calendar.getInstance().getTime(),x_gyr), true,40 );
            series_y.appendData(new DataPoint(Calendar.getInstance().getTime(),y_gyr), true,40 );
            series_z.appendData(new DataPoint(Calendar.getInstance().getTime(),z_gyr), true,40 );
        }
        /*
        if (mySensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d("Steps ", String.valueOf(sensorEvent.values[0]) + "");
            step_text.setText(String.valueOf((int)sensorEvent.values[0]) + " steps");
        }*/
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("Item selected: ","position " + i);
        sensor_choosed = i;
        if (sensor_choosed == 0){
            graph_x.setVisibility(View.VISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.VISIBLE);
            graph_y.setTitle("Y axis");
        }
        if (sensor_choosed == 1){
            graph_x.setVisibility(View.VISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.VISIBLE);
            graph_y.setTitle("Y axis");

        }
        if (sensor_choosed == 2){
            graph_x.setVisibility(View.INVISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.INVISIBLE);
            graph_y.setTitle("Magnitude");

        }

        graph_x.removeAllSeries();
        graph_y.removeAllSeries();
        graph_z.removeAllSeries();
        series_x = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_x.addSeries(series_x);
        series_y = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_y.addSeries(series_y);
        series_z = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_z.addSeries(series_z);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void stopSensors() {
        if(run == true)
            senSensorManager.unregisterListener(this);
        run = false;
    }

    public void startSensors() {

        int accelerometerSensibility;
        int gyroscopeSensibility;
        int stepCounterSensibility;
        try {
            accelerometerSensibility = Integer.getInteger(Utils.getConfigValue(parentFragment.getActivity().getApplicationContext(), "accelerometer.sensibility"));
            gyroscopeSensibility = Integer.getInteger(Utils.getConfigValue(parentFragment.getActivity().getApplicationContext(), "gyroscope.sensibility"));
            stepCounterSensibility = Integer.getInteger(Utils.getConfigValue(parentFragment.getActivity().getApplicationContext(), "step.counter.sensibility"));

        } catch (Exception exception){
            exception.printStackTrace();
            Log.d("SENSORS_ERROR", "Unable to retrieve configuration values, setting to default...");
            accelerometerSensibility = SensorManager.SENSOR_DELAY_NORMAL;
            gyroscopeSensibility = SensorManager.SENSOR_DELAY_NORMAL;
            stepCounterSensibility = SensorManager.SENSOR_DELAY_NORMAL;
        }

        Log.d("SENSORS_CONF",
                "Running sensors with following values: " +
                        "Accelerometer {" + accelerometerSensibility + "} - " +
                        "Gyroscope {" + gyroscopeSensibility + "} - " +
                        "StepCounter {" + stepCounterSensibility + "}");

        senSensorManager.registerListener(this, senGyroscope, gyroscopeSensibility);
        senSensorManager.registerListener(this, senAccelerometer, accelerometerSensibility);

        if (countSensor != null) {
            senSensorManager.registerListener(this, countSensor, stepCounterSensibility);
        } else {
            Toast.makeText(getActivity(), "Count sensor not available!", Toast.LENGTH_LONG).show();
        }
        run = true;
    }

    @Override
    public void onClick(View view) {
        Button b = (Button) view;
        if (view.getId() == R.id.button_start_stop) {
            Log.e("id", b.getText().toString());
            if (b.getText().toString().equals("Start")) {
                b.setText("Stop");
                startSensors();

            } else {
                b.setText("Start");
                stopSensors();
            }
        }
    }
}

