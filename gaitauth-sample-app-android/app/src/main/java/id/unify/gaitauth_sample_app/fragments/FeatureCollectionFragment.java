/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.List;
import java.util.Locale;

import id.unify.gaitauth_sample_app.FeatureStore;
import id.unify.gaitauth_sample_app.GaitAuthService;
import id.unify.gaitauth_sample_app.MainActivity;
import id.unify.gaitauth_sample_app.Preferences;
import id.unify.gaitauth_sample_app.R;
import id.unify.gaitauth_sample_app.databinding.FragmentFeatureCollectionBinding;
import id.unify.sdk.gaitauth.GaitFeature;

public class FeatureCollectionFragment extends Fragment {
    private static final String TAG = "FeatureCollectionFragment";
    public static final String SCREEN_TITLE = "Feature Collection";
    public static final String ARG_KEY_IS_TRAINING = "IS_TRAINING";

    public static FeatureCollectionFragment build(boolean isTraining) {
        Bundle arg = new Bundle();
        arg.putBoolean(ARG_KEY_IS_TRAINING, isTraining);
        FeatureCollectionFragment ret = new FeatureCollectionFragment();
        ret.setArguments(arg);
        return ret;
    }

    private FeatureCollectionListener callback;
    private FragmentFeatureCollectionBinding viewBinding;
    private boolean collecting = false;

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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentFeatureCollectionBinding.inflate(inflater, container,
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
        setupAddFeatureBtnCallback(context);
        setupTrainBtnCallback();

        return view;
    }

    private void parseArgumentsAndUpdateCollectingState() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            collecting = arguments.getBoolean(ARG_KEY_IS_TRAINING, false);
        }
    }

    // refresh UI component based on current state
    private void refreshUI() {
        updateCollectedCountUI(Preferences.getInt(Preferences.FEATURE_COLLECTED_COUNT));
        updateUploadedCountUI(Preferences.getInt(Preferences.FEATURE_UPLOADED_COUNT));
        if (collecting) {
            viewBinding.startPauseCollectionButton.setText(R.string.stop_collection_button);
            viewBinding.watchAnimation.playAnimation();
        } else {
            viewBinding.startPauseCollectionButton.setText(R.string.start_collection_button);
            viewBinding.watchAnimation.pauseAnimation();
        }
    }

    // Setup broadcast receiver to get collectedCount from foreground service
    private void setupBroadcastReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GaitAuthService.ACTION_FEATURE_COLLECTED_COUNT_UPDATE);
        LocalBroadcastManager.getInstance(context).registerReceiver(featureStatsUpdateReceiver,
                intentFilter);
    }

    private void runOnUiThread(Runnable action) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(action);
        }
    }

    private void updateCollectedCountUI(int count) {
        runOnUiThread(() -> viewBinding.collectedCount.setText(String.format(Locale.US, "%d",
                count)));
    }

    private void updateUploadedCountUI(int count) {
        runOnUiThread(() -> viewBinding.uploadedCount.setText(String.format(Locale.US, "%d",
                count)));
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

    private void setupAddFeatureBtnCallback(Context context) {
        viewBinding.addFeaturesButton.setOnClickListener(v -> {
            if (collecting) {
                Toast.makeText(context, "Need to stop collecting to add features.",
                        Toast.LENGTH_LONG).show();
                return;
            }

            // this call can be blocking
            AsyncTask.execute(() -> {
                int totalUploaded = callback.onAddFeaturesPressed();
                if (totalUploaded == 0) {
                    return;
                }
                // update UI needs to be in main thread
                runOnUiThread(() -> {
                    int currentUploadedCount = Preferences.getInt(Preferences.FEATURE_UPLOADED_COUNT);
                    int newUploadedCount = currentUploadedCount + totalUploaded;
                    Preferences.put(Preferences.FEATURE_UPLOADED_COUNT, newUploadedCount);
                    Preferences.put(Preferences.FEATURE_COLLECTED_COUNT, 0);
                    updateCollectedCountUI(0);
                    updateUploadedCountUI(newUploadedCount);
                });
            });
        });
    }

    private void setupTrainBtnCallback() {
        viewBinding.trainModelButton.setOnClickListener(v -> callback.onTrainModelPressed());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (FeatureCollectionListener) context;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshUI();
    }

    public interface FeatureCollectionListener {

        void onStartCollectionBtnPressed();

        void onStopCollectionBtnPressed();

        int onAddFeaturesPressed();

        void onTrainModelPressed();
    }
}
