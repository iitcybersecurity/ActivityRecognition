/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import id.unify.gaitauth_sample_app.databinding.FragmentScoreBinding;
import id.unify.sdk.gaitauth.AuthenticationResult;
import id.unify.sdk.gaitauth.GaitScore;

public class ScoreFragment extends Fragment {
    private static final String TAG = "ScoreFragment";
    public static final String SCREEN_TITLE = "Scores";
    public static final String ARG_KEY_AUTH_RESULT = "AUTH_RESULT";
    private static final String FEATURE_SCORE_CONTEXT_KEY = "featureScores";

    private ScoreListener callbacks;
    private FragmentScoreBinding viewBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentScoreBinding.inflate(inflater, container, false);
        View view = viewBinding.getRoot();
        Context context = getContext();
        if (context == null) {
            Log.i(TAG, "Context is null, early exit onCreateView");
            return view;
        }

        setupDismissBtnCallback();
        Bundle arguments = getArguments();
        if (arguments == null) {
            Toast.makeText(context, "No scores to display, please try again",
                    Toast.LENGTH_LONG).show();
            return view;
        }

        AuthenticationResult authResult = (AuthenticationResult) arguments.getSerializable(
                ARG_KEY_AUTH_RESULT);
        updateChart(authResult);
        updateStatus(authResult);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callbacks = (ScoreListener) context;
    }

    private int getColor(float score) {
        final int highScore = Color.argb(255, 76, 175, 80);
        final int mediumHighScore = Color.argb(255, 255, 255, 59);
        final int mediumLowScore = Color.argb(255, 255, 152, 0);
        final int lowScore = Color.argb(255, 255, 87, 34);

        if (score > 0.85) {
            return highScore;
        } else if (score > 0) {
            return mediumHighScore;
        } else if (score > -0.5) {
            return mediumLowScore;
        }

        return lowScore;
    }

    private void updateStatus(AuthenticationResult result) {
        viewBinding.authStatus.setText(result.getStatus().name().toLowerCase());
    }

    private void updateChart(AuthenticationResult result) {
        Map<String, Object> context = result.getContext();
        List<GaitScore> scores = (List<GaitScore>) context.get(FEATURE_SCORE_CONTEXT_KEY);

        ArrayList<BarEntry> scoreEntries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (!scores.isEmpty()) {
            GaitScore firstScore = scores.get(0);
            long current_x_index = 0;
            long last_score_timestamp = firstScore.getTimestamp().getTime();
            for (GaitScore gaitScore : scores) {

                // make blank space between the score bars if the timestamp of the current score
                // is more than 2 seconds away from
                // the previous score
                long tsOffset = (gaitScore.getTimestamp().getTime() - last_score_timestamp) / 1000L;
                if (tsOffset >= 2) {
                    current_x_index += tsOffset - 1;
                }

                BarEntry barEntry = new BarEntry(current_x_index, gaitScore.getScore());
                scoreEntries.add(barEntry);
                colors.add(getColor(gaitScore.getScore()));

                // always increase x_index for the next score entry to avoid overlap score
                // if multiple consecutive scores have the same ts in second resolution
                current_x_index++;
                last_score_timestamp = gaitScore.getTimestamp().getTime();
            }
        }

        BarDataSet scoreSet = new BarDataSet(scoreEntries, "Scores");
        scoreSet.setColors(colors);
        scoreSet.setDrawValues(false);

        BarData scoresData = new BarData(scoreSet);
        viewBinding.scoreChart.setData(scoresData);

        int maxScore = 1, minScore = -1;
        YAxis yAxis = viewBinding.scoreChart.getAxisLeft();
        yAxis.setAxisMaximum(maxScore);
        yAxis.setAxisMinimum(minScore);
        yAxis.setLabelCount(5, true);
        // disable AxisRight since it is for other type of chart
        viewBinding.scoreChart.getAxisRight().setEnabled(false);

        viewBinding.scoreChart.getXAxis().setEnabled(false);
        viewBinding.scoreChart.getLegend().setEnabled(false);
        viewBinding.scoreChart.getDescription().setEnabled(false);
    }

    private void setupDismissBtnCallback() {
        viewBinding.dismissBtn.setOnClickListener(v -> callbacks.onDismissPressed());
    }

    public interface ScoreListener {
        void onDismissPressed();
    }
}
