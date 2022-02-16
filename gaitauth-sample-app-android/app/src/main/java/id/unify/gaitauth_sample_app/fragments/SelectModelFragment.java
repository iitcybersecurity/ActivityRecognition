/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.common.base.Strings;

import id.unify.gaitauth_sample_app.databinding.FragmentSelectModelBinding;

public class SelectModelFragment extends Fragment {

    public static final String SCREEN_TITLE = "Select Model";

    private FragmentSelectModelBinding viewBinding;
    private SelectModelListener callback;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        viewBinding = FragmentSelectModelBinding.inflate(inflater, container, false);

        setupCreateModelBtnCallback();
        setupLoadModelBtnCallback();

        return viewBinding.getRoot();
    }

    private void showModelIdInputDialog(Context context) {
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Model ID");

        EditText input = new EditText(context);
        alert.setView(input);

        alert.setPositiveButton("Load Model", (dialog, whichButton) -> {
            String modelId = input.getText().toString();
            if (Strings.isNullOrEmpty(modelId)) {
                Toast.makeText(context, "Please input a model ID", Toast.LENGTH_LONG).show();
                return;
            }
            modelId = modelId.trim();
            callback.onModelIdInputted(modelId);
            dialog.cancel();
        });
        alert.show();
    }

    private void setupCreateModelBtnCallback() {
        viewBinding.createModelButton.setOnClickListener(v -> callback.onCreateModelPressed());
    }

    private void setupLoadModelBtnCallback() {
        viewBinding.loadModelButton.setOnClickListener(v -> {
            showModelIdInputDialog(v.getContext());
        });
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        callback = (SelectModelListener) context;
    }

    public interface SelectModelListener {
        void onCreateModelPressed();

        void onModelIdInputted(String modelId);
    }
}
