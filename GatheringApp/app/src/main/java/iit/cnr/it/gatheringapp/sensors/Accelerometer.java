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
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import iit.cnr.it.gatheringapp.Classifier.HARClassifier;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.Classifier.TensorFlowClassifier;
import iit.cnr.it.gatheringapp.utils.Utils;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static iit.cnr.it.gatheringapp.Utils.round;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;

/**
 * Created by giacomo on 05/02/18.
 */

@SuppressLint("ValidFragment")
public class Accelerometer extends android.support.v4.app.Fragment implements SensorEventListener, AdapterView.OnItemSelectedListener, View.OnClickListener {
    private static final int N_SAMPLES = 200;
    private static List<Float> x;
    private static List<Float> y;
    private static List<Float> z;

    private int number_labels;
    private TextView decisionTextView;
    private float[] results;

    private TensorFlowClassifier classifier;
    ArrayList<String> labels = new ArrayList<>();
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
    private int accelerometerSensibility;
    private int gyroscopeSensibility;
    private int stepCounterSensibility;
    private HARClassifier HAR;
    private float[] predictions;

    public Accelerometer(Context context, Fragment parentFragment) {
        this.context = context;
        this.parentFragment = parentFragment;
    }

    public Accelerometer(Context context, String userName, Activity _activity) {
        this.context = context;
        this.userName = userName;
        this.activity = _activity;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.accelerometer_fragment, container, false);

        //Accelerometer sensor initialization
        senSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        countSensor = senSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        initSensorsSensibility();

        start_stop_btn = v.findViewById(R.id.button_start_stop);
        start_stop_btn.setOnClickListener(this);

        startSensors();

        //get the Frequency textView
        frequency_text = v.findViewById(R.id.frequency_text);

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
        series_x = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });

        initGraph(graph_x, series_x, "X Axis");

        //Graph y initialization
        graph_y = (GraphView) v.findViewById(R.id.graph_y);
        series_y = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        initGraph(graph_y, series_y, "Y Axis");

        //Graph z initialization
        graph_z = (GraphView) v.findViewById(R.id.graph_z);
        series_z = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        initGraph(graph_z, series_z, "Z Axis");

        graph_x.setVisibility(View.INVISIBLE);
        graph_y.setVisibility(View.INVISIBLE);
        graph_z.setVisibility(View.VISIBLE);
        //startSensors();

        x = new ArrayList<>();
        y = new ArrayList<>();
        z = new ArrayList<>();

        //lettura delle labels di classificazione da file di testo di configurazione
        BufferedReader reader;
        try{
            final InputStream file = getActivity().getAssets().open("labels_config.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                labels.add(line);
                line = reader.readLine();
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        number_labels = labels.size();
        decisionTextView = (TextView)v.findViewById(R.id.decision);
        classifier = new TensorFlowClassifier(getActivity().getApplicationContext());
        HAR = new HARClassifier(classifier);

        return v;
    }

    private Button.OnClickListener connectListener = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };


    public void computeSamplingRate() {
        current_time = System.currentTimeMillis();
        current_sampling_rate = (float) (1 / ((current_time - prev_time) * pow(10, -3)));
        prev_time = current_time;
        frequency_text.setText(current_sampling_rate + " Hz");
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //Get the accelerometer values
            x_acc = sensorEvent.values[0];
            y_acc = sensorEvent.values[1];
            z_acc = sensorEvent.values[2];
        }

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            //Get the gyroscope values
            x_gyr = sensorEvent.values[0];
            y_gyr = sensorEvent.values[1];
            z_gyr = sensorEvent.values[2];

        }

        if (sensor_choosed != 1) {
            //Sampling rate computation
            computeSamplingRate();
            //Accelerometer
            if (sensor_choosed == 0) {
                series_x.appendData(new DataPoint(Calendar.getInstance().getTime(), x_acc), true, 40);
                series_y.appendData(new DataPoint(Calendar.getInstance().getTime(), y_acc), true, 40);
                series_z.appendData(new DataPoint(Calendar.getInstance().getTime(), z_acc), true, 40);
            }
            //Magnitude
            if (sensor_choosed == 2) {
                series_y.appendData(new DataPoint(Calendar.getInstance().getTime(), sqrt(pow(x_acc, 2) + pow(y_acc, 2) + pow(z_acc, 2))), true, 40);
            }
        }
        //Gyroscope
        if (sensor_choosed == 1) {
            computeSamplingRate();
            Log.v("Gyr: ", System.currentTimeMillis() / 1000 + " " + x_gyr + " " + y_gyr + " " + z_gyr + " " + graph_x.getViewport().getMinX(true));
            series_x.appendData(new DataPoint(Calendar.getInstance().getTime(), x_gyr), true, 40);
            series_y.appendData(new DataPoint(Calendar.getInstance().getTime(), y_gyr), true, 40);
            series_z.appendData(new DataPoint(Calendar.getInstance().getTime(), z_gyr), true, 40);
        }
        /*
        if (mySensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            Log.d("Steps ", String.valueOf(sensorEvent.values[0]) + "");
            step_text.setText(String.valueOf((int)sensorEvent.values[0]) + " steps");
        }*/
        HAR.setElement(x_acc, y_acc, z_acc);
        predictions = HAR.activityPrediction();
        if(predictions != null)
            writeResults(predictions);
    }

    private void writeResults(float[] predictions) {
        float max = -1;
        int idx = -1;
        for (int i = 0; i < predictions.length; i++) {
            if (predictions[i] > max) {
                idx = i;
                max = predictions[i];
            }
        }

        //lettura delle labels di classificazione da file di testo di configurazione
        BufferedReader reader;
        try{
            final InputStream file = getActivity().getAssets().open("labels_config.txt");
            reader = new BufferedReader(new InputStreamReader(file));
            String line = reader.readLine();
            while(line != null){
                labels.add(line);
                line = reader.readLine();
            }
        } catch(IOException ioe){
            ioe.printStackTrace();
        }
        decisionTextView.setText(labels.get(idx));
        idx++;
        int id = getActivity().getBaseContext().getResources().getIdentifier("ic" + idx, "drawable", getActivity().getBaseContext().getPackageName());
        decisionTextView.setCompoundDrawablesWithIntrinsicBounds( id, 0, 0, 0);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Log.d("Item selected: ", "position " + i);
        sensor_choosed = i;
        if (sensor_choosed == 0) {
            graph_x.setVisibility(View.VISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.VISIBLE);
            graph_y.setTitle("Y axis");
        }
        if (sensor_choosed == 1) {
            graph_x.setVisibility(View.VISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.VISIBLE);
            graph_y.setTitle("Y axis");

        }
        if (sensor_choosed == 2) {
            graph_x.setVisibility(View.INVISIBLE);
            graph_y.setVisibility(View.VISIBLE);
            graph_z.setVisibility(View.INVISIBLE);
            graph_y.setTitle("Magnitude");

        }

        graph_x.removeAllSeries();
        graph_y.removeAllSeries();
        graph_z.removeAllSeries();
        series_x = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_x.addSeries(series_x);
        series_y = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_y.addSeries(series_y);
        series_z = new LineGraphSeries<>(new DataPoint[]{
                new DataPoint(Calendar.getInstance().getTime(), 0)
        });
        graph_z.addSeries(series_z);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void stopSensors(Accelerometer sensor) {
        if (run == true)
            senSensorManager.unregisterListener(sensor);
        run = false;
    }

    public void startSensors() {
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

    private void initGraph(GraphView graph, LineGraphSeries series, String label) {
        graph.getViewport().setScrollable(true);
        // activate horizontal and vertical zooming and scrolling
        graph.getViewport().setScalableY(true);
        // activate vertical scrolling
        graph.getViewport().setScrollableY(true);

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(-30);
        graph.getViewport().setMaxY(50);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(100);
        graph.getViewport().setMaxX(500);

        graph.getGridLabelRenderer().setHorizontalLabelsVisible(false);

        graph.addSeries(series);
        graph.setTitle(label);
    }

    private void initSensorsSensibility() {
        try {
            Context context = getContext();
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
                stopSensors(this);
            }
        }
    }
}

