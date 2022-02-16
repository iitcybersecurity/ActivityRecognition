/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.common.base.Strings;

import id.unify.gaitauth_sample_app.R;
import id.unify.gaitauth_sample_app.databinding.FragmentModelErrorBinding;

public class ModelErrorFragment extends Fragment {

    public static final String SCREEN_TITLE = "Model Error";
    private static final String ERROR_REASON = "errorReason";

    private String errorReason;

    public static ModelErrorFragment newInstance(String errorReason) {
        ModelErrorFragment fragment = new ModelErrorFragment();
        Bundle args = new Bundle();
        args.putString(ERROR_REASON, errorReason);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            errorReason = getArguments().getString(ERROR_REASON);
        }

        if (Strings.isNullOrEmpty(errorReason)) {
            errorReason = getString(R.string.model_error_default_reason);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        id.unify.gaitauth_sample_app.databinding.FragmentModelErrorBinding viewBinding =
                FragmentModelErrorBinding.inflate(inflater, container, false);
        viewBinding.modelErrorMessage.setText(errorReason);
        return viewBinding.getRoot();
    }
}
