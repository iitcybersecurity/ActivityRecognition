package iit.cnr.it.gatheringapp.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.widget.CircularProgressDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Option;
import com.bumptech.glide.request.RequestOptions;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.sensors.Sensors;
import iit.cnr.it.gatheringapp.utils.Utils;

import java.security.MessageDigest;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ActionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ActionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ActionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    private static final String LABEL = "label";
    private static final String DESCRIPTION = "description";
    private static final String INSTRUCTIONS = "instructions";

    private Sensors sensors;
    private OnFragmentInteractionListener mListener;
    CountDownTimer mCountDownTimer;
    private TextView mTitleText;
    private TextView mInstructionsText;
    private TextView mInstructionsCopy;
    private EditText mTrainingInput;
    private ProgressBar mProgressBar;
    private ImageView mInstructionsView;
    private Button mRecordButton;
    private Button mDoneButton;
    private String label;
    private String description;
    private String instructions;
    private int timerCounter;
    private int millisInFuture;
    private int countDownInterval;

    public ActionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param label Parameter 1.
     * @param description Parameter 2.
     * @return A new instance of fragment ActionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ActionFragment newInstance(String label, String description, String instructions) {
        ActionFragment fragment = new ActionFragment();
        Bundle args = new Bundle();
        args.putString(LABEL, label);
        args.putString(DESCRIPTION, description);
        args.putString(INSTRUCTIONS, instructions);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            label = getArguments().getString(LABEL);
            description = getArguments().getString(DESCRIPTION);
            instructions = getArguments().getString(INSTRUCTIONS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_action, container, false);
        final Context context = fragmentView.getContext();
        sensors = new Sensors(getActivity(), "");

        // Setup text
        mTitleText = fragmentView.findViewById(R.id.action_title);
        mTitleText.setText(description);
        mInstructionsText = fragmentView.findViewById(R.id.instructions_record_text);
        mInstructionsCopy = fragmentView.findViewById(R.id.instructions_copy_text);
        mTrainingInput = fragmentView.findViewById(R.id.training_copy_input);


        // Setup buttons and visibility
        mRecordButton = fragmentView.findViewById(R.id.record_action_button);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                recordAction(label, context);
            }
        });
        mDoneButton = fragmentView.findViewById(R.id.done_action_button);
        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopAction();
            }
        });
        mDoneButton.setVisibility(View.GONE);

        // Setup Progress Bar and timer
        setupProgressBarTimer(fragmentView);

        // Setup instructions gif
        setupInstructionsGif(fragmentView, context);

        return fragmentView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onActionFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
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

    private void recordAction(String label, Context context) {
        mDoneButton.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecordButton.setVisibility(View.GONE);
        mInstructionsText.setVisibility(View.INVISIBLE);
        mInstructionsView.setVisibility(View.INVISIBLE);

        try {
            sensors.startSensors(label, context);
            mCountDownTimer.start();

            if(label.matches("TEXT")) {
                Log.i("INFO", "I should present and input text box ");
                mInstructionsView.setVisibility(View.GONE);
                mInstructionsCopy.setVisibility(View.VISIBLE);
                mTrainingInput.setVisibility(View.VISIBLE);
            }
            if (label.matches("UNLOCK")) {
                Log.i("INFO", "I should check if the phone gets locked and the send a notification 10 seconds later");
                sendSoundNotification();
            }

            if (label.matches("WEB")) {
                Log.i("INFO", "I should open the browser and wait 40 sec, then notify the user to come back");
            }
            sensors.stopSensors();
            Log.i("SUCCESS", "Recorded activity: " + label);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopAction() {
        mDoneButton.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.INVISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        mInstructionsText.setVisibility(View.VISIBLE);
        mInstructionsView.setVisibility(View.VISIBLE);
        mInstructionsCopy.setVisibility(View.GONE);
        mTrainingInput.setVisibility(View.GONE);
        mTrainingInput.setText(null);
        try {
            sensors.stopSensors();
            mCountDownTimer.cancel();
            mProgressBar.setProgress(0);
            timerCounter = 0;

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSoundNotification() {

    }

    private void setupInstructionsGif(View fragmentView, Context context) {
        mInstructionsView = fragmentView.findViewById(R.id.instructions_view);
        int instructionsId = context.getResources().getIdentifier(instructions, "raw", context.getPackageName());

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(context);
        circularProgressDrawable.setStrokeWidth(5f);
        circularProgressDrawable.setCenterRadius(30f);
        circularProgressDrawable.start();
        try {
            Option.memory("com.bumptech.glide.load.model.stream.HttpGlideUrlLoader.Timeout", 2500).update(20_000, MessageDigest.getInstance("SHA-512"));
            Glide.with(this)
                    .load(instructionsId)
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(circularProgressDrawable)
                    .into(mInstructionsView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupProgressBarTimer(View fragmentView) {
        timerCounter = 0;
        millisInFuture = 60000;//Integer.valueOf(Utils.getConfigValue(context, "timer.counter"));
        countDownInterval = 1000;//Integer.valueOf(Utils.getConfigValue(context, "timer.interval"));
        mProgressBar = fragmentView.findViewById(R.id.action_progress_bar);
        mProgressBar.setProgress(timerCounter);
        mProgressBar.setVisibility(View.INVISIBLE);
        mCountDownTimer=new CountDownTimer(millisInFuture,countDownInterval) {

            @Override
            public void onTick(long millisUntilFinished) {
                Log.v("Log_tag", "Tick of Progress"+ timerCounter+ millisUntilFinished);
                timerCounter++;
                mProgressBar.setProgress((int)timerCounter*100/(millisInFuture/countDownInterval));

            }

            @Override
            public void onFinish() {
                //Do what you want
                timerCounter++;
                mProgressBar.setProgress(100);
                stopAction();
            }
        };

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onActionFragmentInteraction(Uri uri);
    }
}
