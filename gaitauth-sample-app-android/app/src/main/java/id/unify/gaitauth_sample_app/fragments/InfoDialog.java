/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;

import id.unify.gaitauth_sample_app.MainActivity;
import id.unify.gaitauth_sample_app.Preferences;
import id.unify.gaitauth_sample_app.databinding.DialogInfoBinding;


public class InfoDialog extends AlertDialog {
    private DialogInfoBinding mBinding;

    public InfoDialog(Activity activity) {
        super(activity);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DialogInfoBinding.inflate(LayoutInflater.from(getContext()));
        setContentView(mBinding.getRoot());
    }

    @Override
    protected void onStart() {
        super.onStart();

        String sdkKey = "SDK Key: " + Preferences.getString(Preferences.SDK_KEY);
        mBinding.sdkKey.setText(sdkKey);

        String user = "User: " + MainActivity.USER;
        mBinding.user.setText(user);

        String clientId = "Client ID: " + Preferences.getString(Preferences.CLIENT_ID);
        mBinding.clientId.setText(clientId);

        String installId = "Install ID: " + Preferences.getString(Preferences.INSTALL_ID);
        mBinding.installId.setText(installId);

        String customerId = "Customer ID: " + Preferences.getString(Preferences.CUSTOMER_ID);
        mBinding.customerId.setText(customerId);

        String modelId = "Model ID: " + Preferences.getString(Preferences.MODEL_ID);
        mBinding.modelId.setText(modelId);
    }
}
