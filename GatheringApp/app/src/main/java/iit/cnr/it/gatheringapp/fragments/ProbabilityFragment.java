package iit.cnr.it.gatheringapp.fragments;

import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import iit.cnr.it.gatheringapp.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import iit.cnr.it.gatheringapp.sensors.Sensors;
import iit.cnr.it.gatheringapp.Classifier.TensorFlowClassifier;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class ProbabilityFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    private int number_labels;
    private TensorFlowClassifier classifier;
    private Sensors sensor;
    ArrayList<String> labels = new ArrayList<>();
    private TextView decisionTextView;
    private Context context;
    TextView frequency_text;

    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Activity mainActivity;
    private ProbabilityFragment.OnFragmentInteractionListener mListener;

    public ProbabilityFragment(){

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrainingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProbabilityFragment newInstance(String param1, String param2) {
        ProbabilityFragment fragment = new ProbabilityFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View fragmentView = inflater.inflate(R.layout.fragment_probability, container, false);

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

        decisionTextView = (TextView)fragmentView.findViewById(R.id.decision);
        TableLayout table = (TableLayout)fragmentView.findViewById(R.id.table);
        CreateTable(table);
        context = fragmentView.getContext();
        sensor = new Sensors(getActivity(), "MarcoBongiovanni",true,fragmentView);
        sensor.startSensorsHAR(getActivity().getApplicationContext());
        return fragmentView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof ProbabilityFragment.OnFragmentInteractionListener) {
            mListener = (ProbabilityFragment.OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    //creazione della tabella dinamica
    private void CreateTable(TableLayout table){

        for(int i=1; i <= number_labels; i++){
            final TableRow ValueRow = new TableRow(getActivity());
            final TextView  activity = new TextView(getActivity());
            final TextView probability = new TextView(getActivity());

            TableRow.LayoutParams layoutParams = new TableRow.LayoutParams();
            layoutParams.weight = 1;

            int id = getActivity().getBaseContext().getResources().getIdentifier("ic" + i, "drawable", getActivity().getBaseContext().getPackageName());

            activity.setCompoundDrawablesWithIntrinsicBounds( id, 0, 0, 0);
            activity.setText("  " + labels.get(i - 1));
            activity.setTextAppearance(getActivity(), android.R.style.TextAppearance_Material_Display1);
            activity.setTextSize(20);
            activity.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            activity.setLayoutParams(layoutParams);

            probability.setId(i-1);
            probability.setTextAppearance(getActivity(), android.R.style.TextAppearance_Material_Display1);
            probability.setTextSize(20);
            probability.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            probability.setLayoutParams(layoutParams);

            ValueRow.addView(activity);
            ValueRow.addView(probability);
            ValueRow.setPadding(40,40,40,40);
            TableRow.LayoutParams lp = new TableRow.LayoutParams();
            lp.width = MATCH_PARENT;
            lp.height = MATCH_PARENT;

            ValueRow.setLayoutParams(lp);
            table.addView(ValueRow);
        }
    }

    private SensorManager getSensorManager() {
        return (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
