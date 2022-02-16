package id.unify.gaitauth_sample_app.fragments;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.Locale;

import id.unify.gaitauth_sample_app.GaitAuthService;
import id.unify.gaitauth_sample_app.Preferences;
import id.unify.gaitauth_sample_app.R;
import id.unify.gaitauth_sample_app.databinding.FragmentSendFeaturesBinding;


public class SendFeaturesFragment extends Fragment {

    private static final String TAG = "SendFeaturesFragment";

    public static final  String SCREEN_TITLE = "Send Features";
    public static final String ARG_KEY_IS_TRAINING = "IS_TRAINING";

    private SendFeaturesListener callback;
    private boolean collecting = false;
    private FragmentSendFeaturesBinding viewBinding;

    public static SendFeaturesFragment build(boolean isTraining) {
        Bundle arg = new Bundle();
        arg.putBoolean(ARG_KEY_IS_TRAINING, isTraining);
        SendFeaturesFragment ret = new SendFeaturesFragment();
        ret.setArguments(arg);
        return ret;
    }

    // Handle broadcasts from FeatureCollectionService to bump collectedCount
    private BroadcastReceiver featureStatsUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (getActivity() != null) {
                int count = intent.getIntExtra(
                        GaitAuthService.BROADCAST_EXTRA_FEATURE_COUNT, 0);
                updateCollectedCountUI(count);
            }
        }
    };

    private void updateCollectedCountUI(int count) {
        runOnUiThread(() -> viewBinding.collectedCount.setText(String.format(Locale.US, "%d",
                count)));
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }

    private void parseArgumentsAndUpdateCollectingState() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            collecting = arguments.getBoolean(ARG_KEY_IS_TRAINING, false);
        }
    }

    // Setup broadcast receiver to get collectedCount from foreground service
    private void setupBroadcastReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GaitAuthService.ACTION_FEATURE_COLLECTED_COUNT_UPDATE);
        LocalBroadcastManager.getInstance(context).registerReceiver(featureStatsUpdateReceiver,
                intentFilter);
    }


    // refresh UI component based on current state
    private void refreshUI() {
        updateCollectedCountUI(Preferences.getInt(Preferences.FEATURE_COLLECTED_COUNT));
        if (collecting) {
            viewBinding.startPauseCollectionButton.setText(R.string.stop_collection_button);
        } else {
            viewBinding.startPauseCollectionButton.setText(R.string.start_collection_button);
        }
    }


    private void setupStartOrStopBtnCallback() {
        viewBinding.startPauseCollectionButton.setOnClickListener(v -> {
            if (collecting) {
                callback.onStopCollectionBtnPressed();
                collecting = false;
            } else {
                callback.onStartCollectionBtnPressed();
                collecting = true;
            }
            refreshUI();
        });
    }

    private void setupSendFeaturesBtnCallback(Context context) {
        viewBinding.sendHttpFeaturesButton.setOnClickListener(v -> {
            if (collecting) {
                Toast.makeText(context, "Need to stop collecting to add features.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // this call can be blocking
            AsyncTask.execute(() -> {
                int totalUploaded = callback.onSendFeaturesPressed(viewBinding.editTextHttpServer.getText().toString());
                if (totalUploaded == 0) {
                    return;
                }
                // update UI needs to be in main thread
                runOnUiThread(() -> {
                    Preferences.put(Preferences.FEATURE_COLLECTED_COUNT, 0);
                    updateCollectedCountUI(0);
                });
            });
        });
    }

    @SuppressLint("DefaultLocale")
    private void setupCompareFeaturesBtnCallback(Context context) {
        viewBinding.compareHttpFeaturesButton.setOnClickListener(v -> {
            if (collecting) {
                Toast.makeText(context, "Need to stop collecting to add features.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // this call can be blocking
            AsyncTask.execute(() -> {
                double cosineSimilarity = callback.onCompareFeaturesPressed(viewBinding.editTextHttpServer.getText().toString());
                // update UI needs to be in main thread
                runOnUiThread(() -> {
                    Toast.makeText(context, String.format("Comparison result: %f", cosineSimilarity),
                            Toast.LENGTH_LONG).show();
                    // Updates count
                    Preferences.put(Preferences.FEATURE_COLLECTED_COUNT, 0);
                    updateCollectedCountUI(0);
                });
            });
        });
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentSendFeaturesBinding.inflate(inflater, container,
                false);
        View view = viewBinding.getRoot();

        Context context = getContext();
        if (context == null) {
            Log.i(TAG, "Context is null, early exit onCreateView");
            return view;
        }

        parseArgumentsAndUpdateCollectingState();
        setupBroadcastReceiver(context);
        refreshUI();
        setupStartOrStopBtnCallback();
        setupSendFeaturesBtnCallback(context);
        setupCompareFeaturesBtnCallback(context);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (SendFeaturesListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    public interface SendFeaturesListener {

        void onStartCollectionBtnPressed();

        void onStopCollectionBtnPressed();

        int onSendFeaturesPressed(String serverUrl);

        double onCompareFeaturesPressed(String serverUrl);
    }
}