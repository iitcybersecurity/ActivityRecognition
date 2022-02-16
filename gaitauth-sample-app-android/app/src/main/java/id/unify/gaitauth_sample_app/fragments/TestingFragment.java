/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import id.unify.gaitauth_sample_app.R;
import id.unify.gaitauth_sample_app.databinding.FragmentTestingBinding;

public class TestingFragment extends Fragment {
    private static final String TAG = "TestingFragment";
    public static final String SCREEN_TITLE = "Testing";
    public static final String ARG_KEY_IS_TESTING = "IS_TESTING";

    public static TestingFragment build(boolean isTesting) {
        Bundle arg = new Bundle();
        arg.putBoolean(ARG_KEY_IS_TESTING, isTesting);
        TestingFragment ret = new TestingFragment();
        ret.setArguments(arg);
        return ret;
    }

    private FragmentTestingBinding viewBinding;
    private TestingListener callbacks;
    private boolean collecting = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        viewBinding = FragmentTestingBinding.inflate(inflater, container, false);
        View view = viewBinding.getRoot();

        Context context = getContext();
        if (context == null) {
            Log.i(TAG, "Context is null, early exit onCreateView");
            return view;
        }

        parseArgumentsAndUpdateCollectingState();
        refreshUI();
        setupStartOrStopBtnCallback();
        setupScoreFeaturesBtnCallback(context);

        return view;
    }

    private void parseArgumentsAndUpdateCollectingState() {
        Bundle arguments = getArguments();
        if (arguments != null) {
            collecting = arguments.getBoolean(ARG_KEY_IS_TESTING, false);
        }
    }

    // refresh UI based on collection state
    private void refreshUI() {
        if (collecting) {
            viewBinding.startPauseCollectionButton.setText(R.string.stop_collection_button);
            viewBinding.watchAnimation.playAnimation();
        } else {
            viewBinding.startPauseCollectionButton.setText(R.string.start_collection_button);
            viewBinding.watchAnimation.pauseAnimation();
        }
    }

    private void setupStartOrStopBtnCallback() {
        viewBinding.startPauseCollectionButton.setOnClickListener(v -> {
            if (collecting) {
                callbacks.onStopCollectionPressed();
                collecting = false;
            } else {
                callbacks.onStartCollectionPressed();
                collecting = true;
            }
            refreshUI();
        });
    }

    private void setupScoreFeaturesBtnCallback(Context context) {
        viewBinding.scoreFeaturesBtn.setOnClickListener(v -> {
            if (collecting) {
                Toast.makeText(context, "Need to stop collecting to score.",
                        Toast.LENGTH_LONG).show();
                return;
            }
            callbacks.onScoreFeaturesPressed();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (TestingListener) context;
    }

    public interface TestingListener {
        void onStartCollectionPressed();
        void onStopCollectionPressed();
        void onScoreFeaturesPressed();
    }
}
