/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Calendar;
import java.util.Locale;

import id.unify.gaitauth_sample_app.Preferences;
import id.unify.gaitauth_sample_app.R;
import id.unify.gaitauth_sample_app.databinding.FragmentModelPendingBinding;

public class ModelPendingFragment extends Fragment {

    public static final String SCREEN_TITLE = "Model Pending";

    private ModelPendingListener callback;
    private FragmentModelPendingBinding viewBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentModelPendingBinding.inflate(inflater, container, false);
        View view = viewBinding.getRoot();
        updateRefreshedText(Preferences.getLong(Preferences.LAST_MODEL_PULL_TS));
        setupRefreshBtnCallback();
        return view;
    }

    private void updateRefreshedText(long millis) {
        String date = getString(R.string.refreshed_status_default);
        if (millis > 0) {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(millis);
            date = DateFormat.format("MMM d, yyyy 'at' H:mm:ss a", cal).toString();
        }
        viewBinding.refreshStatus.setText(date);
    }

    private void setupRefreshBtnCallback() {
        viewBinding.refreshButton.setOnClickListener(v -> {
            long refreshedAt = System.currentTimeMillis();
            Preferences.put(Preferences.LAST_MODEL_PULL_TS, refreshedAt);
            updateRefreshedText(refreshedAt);

            callback.onRefreshPressed();
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (ModelPendingListener) context;
    }

    public interface ModelPendingListener {
        void onRefreshPressed();
    }
}
