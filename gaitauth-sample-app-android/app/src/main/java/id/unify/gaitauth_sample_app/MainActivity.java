/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.common.base.Strings;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import id.unify.gaitauth_sample_app.databinding.ActivityMainBinding;
import id.unify.gaitauth_sample_app.fragments.FeatureCollectionFragment;
import id.unify.gaitauth_sample_app.fragments.InfoDialog;
import id.unify.gaitauth_sample_app.fragments.ModelErrorFragment;
import id.unify.gaitauth_sample_app.fragments.ModelPendingFragment;
import id.unify.gaitauth_sample_app.fragments.ScoreFragment;
import id.unify.gaitauth_sample_app.fragments.SelectModelFragment;
import id.unify.gaitauth_sample_app.fragments.SendFeaturesFragment;
import id.unify.gaitauth_sample_app.fragments.TestingFragment;
import id.unify.gaitauth_sample_app.models.CosineData;
import id.unify.sdk.core.CompletionHandler;
import id.unify.sdk.core.UnifyID;
import id.unify.sdk.core.UnifyIDConfig;
import id.unify.sdk.core.UnifyIDException;
import id.unify.sdk.gaitauth.AuthenticationListener;
import id.unify.sdk.gaitauth.AuthenticationResult;
import id.unify.sdk.gaitauth.Authenticator;
import id.unify.sdk.gaitauth.GaitAuth;
import id.unify.sdk.gaitauth.GaitAuthException;
import id.unify.sdk.gaitauth.GaitFeature;
import id.unify.sdk.gaitauth.GaitModel;
import id.unify.sdk.gaitauth.GaitModelException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

public class MainActivity
        extends AppCompatActivity
        implements SelectModelFragment.SelectModelListener,
        FeatureCollectionFragment.FeatureCollectionListener,
        SendFeaturesFragment.SendFeaturesListener,
        ModelPendingFragment.ModelPendingListener,
        TestingFragment.TestingListener,
        ScoreFragment.ScoreListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String USER = UUID.randomUUID().toString();

    private OkHttpClient client = new OkHttpClient();

    private ActivityMainBinding viewBinding;
    private GaitModel model;
    private GaitAuthService gaitAuthService;
    private AtomicBoolean gaitAuthServiceBound = new AtomicBoolean(false);
    private ServiceConnection gaitAuthServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            GaitAuthService.GaitAuthServiceBinder binder =
                    (GaitAuthService.GaitAuthServiceBinder) service;
            gaitAuthService = binder.getService();
            gaitAuthServiceBound.set(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            gaitAuthServiceBound.set(false);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        Context context = getApplicationContext();
        setupModelDeleteBtn();
        setupGetInfoBtn();
        Preferences.initialize(context);

        String sdkKey = Preferences.getString(Preferences.SDK_KEY);
        if (Strings.isNullOrEmpty(sdkKey)) {
            showSdkKeyInputAlertDialog();
        } else {
            initializeAfterSdkKeyIsSet(context, sdkKey);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (gaitAuthServiceBound.get()) {
            unbindService(gaitAuthServiceConnection);
            gaitAuthServiceBound.set(false);
        }
    }

    private void showSdkKeyInputAlertDialog() {
        Context activityContext = MainActivity.this;
        String msg = "Please enter a valid UnifyID SDK Key";
        AlertDialog.Builder alert = new AlertDialog.Builder(activityContext);
        alert.setTitle("SDK Key");
        alert.setMessage(msg);

        EditText input = new EditText(activityContext);
        alert.setView(input);

        alert.setPositiveButton("Confirm", (dialog, whichButton) -> {
            String sdkKey = input.getText().toString();
            if (Strings.isNullOrEmpty(sdkKey)) {
                Toast.makeText(activityContext, msg, Toast.LENGTH_LONG).show();
                showSdkKeyInputAlertDialog();
                return;
            }
            sdkKey = sdkKey.trim();
            Preferences.put(Preferences.SDK_KEY, sdkKey);
            initializeAfterSdkKeyIsSet(getApplicationContext(), sdkKey);
        });
        alert.setCancelable(false);
        alert.show();
    }

    /*
     * Initialize an instance of the GaitAuth SDK
     * <p>
     * OnCompletion: start GaitAuth Foreground Service
     * <p>
     * OnFailure: show an AlertDialog for failure's reasons
     * */
    private void initializeGaitAuth(Context context, String sdkKey) {
        showProgressBar();
        UnifyID.initialize(context, sdkKey, USER, new CompletionHandler() {
            @Override
            public void onCompletion(UnifyIDConfig config) {
                GaitAuth.initialize(context, config);
                Preferences.put(Preferences.CLIENT_ID, config.getClientId());
                Preferences.put(Preferences.INSTALL_ID, config.getInstallId());
                Preferences.put(Preferences.CUSTOMER_ID, config.getCustomerId());
                startGaitAuthService(context);
                route();
                hideProgressBar();
            }

            @Override
            public void onFailure(UnifyIDException e) {
                String msg = "Failed to initialize UnifyID SDK";
                runOnUiThread(() -> new AlertDialog.Builder(MainActivity.this)
                        .setTitle(msg)
                        .setMessage(String.format("You might try restarting the app\nerror: %s",
                                e.getMessage()))
                        .setCancelable(false)
                        .show());
                Log.e(TAG, msg, e);
                hideProgressBar();
            }
        });
    }

    // call this method to continue to setup when SDK key is set
    private void initializeAfterSdkKeyIsSet(Context context, String sdkKey) {
        if (GaitAuth.getInstance() == null) {
            // If GaitAuth is null let's init SDK
            initializeGaitAuth(context, sdkKey);
        } else {
            startGaitAuthService(context);
            route();
        }
    }

    // display fragment based on model ID existence
    private void route() {
        String modelId = Preferences.getString(Preferences.MODEL_ID);
        boolean noModel = Strings.isNullOrEmpty(modelId);
        if (noModel) {
            showFragment(new SelectModelFragment(), SelectModelFragment.SCREEN_TITLE);
        } else {
            loadModel(modelId);
        }
    }

    // start a service and then bind the app to it, so it can run after the app is closed
    private void startGaitAuthService(Context context) {
        Intent intent = new Intent(context, GaitAuthService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            GaitAuthService.initNotificationChannel(context);
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        bindService(intent, gaitAuthServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void deleteModel() {
        Preferences.remove(Preferences.MODEL_ID);
        Preferences.remove(Preferences.FEATURE_COLLECTED_COUNT);
        Preferences.remove(Preferences.FEATURE_UPLOADED_COUNT);
        if (gaitAuthService != null) {
            gaitAuthService.reset();
        }
        route();
    }

    private void setupModelDeleteBtn() {
        viewBinding.toolbar.deleteButton.setOnClickListener((v) ->
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.reset)
                        .setMessage(R.string.reset_message)
                        .setPositiveButton(R.string.reset, (dialog, which) -> deleteModel())
                        .setNegativeButton(android.R.string.cancel, null)
                        .show()
        );
    }

    private void setupGetInfoBtn() {
        viewBinding.toolbar.infoButton.setOnClickListener((v ->
                new InfoDialog(MainActivity.this).show())
        );
    }

    private void showFragment(final Fragment fragment, final String title) {
        runOnUiThread(() -> {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.mainFragment, fragment);
            ft.commit();
            viewBinding.toolbar.title.setText(title);
        });
    }

    private void showToast(final String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show());
    }

    private void showProgressBar() {
        runOnUiThread(() -> viewBinding.mainPb.setVisibility(View.VISIBLE));
    }

    private void hideProgressBar() {
        runOnUiThread(() -> viewBinding.mainPb.setVisibility(View.INVISIBLE));
    }

    private void renderFragmentBasedOnModelStatus(GaitModel.Status status) {
        switch (status) {
            case CREATED:
                showFragment(SendFeaturesFragment.build(gaitAuthService.isTraining()),
                        SendFeaturesFragment.SCREEN_TITLE);
                break;
            default:
                // treat it as a failure
                showFragment(ModelErrorFragment.newInstance("unknown model status"),
                        ModelErrorFragment.SCREEN_TITLE);
                break;
        }
    }

    /**
     * Load a model given the modelId.
     * <p>
     * Update the UI accordingly based on model status if success.
     * Otherwise, update the UI to notify failure's reasons
     * <p>
     * More detailed at: https://developer.unify.id/docs/gaitauth/model-training/#loading-a-model
     */
    private void loadModel(String modelId) {
        showProgressBar();
        AsyncTask.execute(() -> {
            try {
                try {
                    model = GaitAuth.getInstance().loadModel(modelId);
                    Preferences.put(Preferences.MODEL_ID, modelId);
                    renderFragmentBasedOnModelStatus(model.getStatus());
                } catch (GaitModelException e) {
                    Log.e(TAG, "Failed to load gaitModel", e);
                    String modelFailureMsg = "model load failed with error: " + e.getMessage();
                    showFragment(ModelErrorFragment.newInstance(modelFailureMsg),
                            ModelErrorFragment.SCREEN_TITLE);
                }
            } finally {
                hideProgressBar();
            }
        });
    }


    /**
     * Handle onClick of CreateModelBtn:
     * Go to FeatureCollectionFragment if succeeded, otherwise show toast to notify the failure
     * <p>
     * More detailed at: https://developer.unify.id/docs/gaitauth/model-training/#create-a-gait-model
     */
    @Override
    public void onCreateModelPressed() {
        showProgressBar();
        AsyncTask.execute(() -> {
            try {
                model = GaitAuth.getInstance().createModel();
                String modelId = model.getId();
                Preferences.put(Preferences.MODEL_ID, modelId);
                showFragment(new FeatureCollectionFragment(),
                        FeatureCollectionFragment.SCREEN_TITLE);
            } catch (GaitModelException e) {
                showToast(getString(R.string.create_model_failure_toast));
            } finally {
                hideProgressBar();
            }
        });
    }

    @Override
    public void onModelIdInputted(String modelId) {
        loadModel(modelId);
    }

    @Override
    public void onStartCollectionBtnPressed() {
        try {
            gaitAuthService.startFeatureCollectionForTraining();
        } catch (GaitAuthException e) {
            String msg = "Failed to start feature collection";
            showToast(msg);
            Log.e(TAG, msg, e);
        }
    }

    @Override
    public void onStopCollectionBtnPressed() {
        gaitAuthService.stopFeatureCollectionForTraining();
    }

    /**
     * Send features to HTTP server
     *
     * @return number of features updated
     */
    @Override
    public int onSendFeaturesPressed(String serverUrl) {
        showProgressBar();
        try {
            FeatureStore featureStore = FeatureStore.getInstance(this);
            List<List<GaitFeature>> chunks = featureStore.getAll();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (List<GaitFeature> chunk : chunks) {
                for (GaitFeature f : chunk) {
                    try {
                        outputStream.write(f.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            String json = String.format("{\"features\":\"%s\"}", base64String);
            System.out.println(json);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    MediaType.parse("application/json"), json);

            okhttp3.Request request = new okhttp3.Request.Builder().url(String.format("%s/api/feature/store", serverUrl)).post(body).build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                okhttp3.Headers responseHeaders = response.headers();
                for (int i = 0; i < responseHeaders.size(); i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }

                System.out.println(response.body().string());
                return chunks.size();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                featureStore.empty();
            }
            return 0;
        } finally {
            hideProgressBar();
        }
    }

    @Override
    public double onCompareFeaturesPressed(String serverUrl) {
        showProgressBar();
        try {
            FeatureStore featureStore = FeatureStore.getInstance(this);
            List<List<GaitFeature>> chunks = featureStore.getAll();

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            for (List<GaitFeature> chunk : chunks) {
                for (GaitFeature f : chunk) {
                    try {
                        outputStream.write(f.toString().getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            String base64String = Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT);
            String json = String.format("{\"features\":\"%s\"}", base64String);
            System.out.println(json);
            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    MediaType.parse("application/json"), json);

            okhttp3.Request request = new okhttp3.Request.Builder().url(String.format("%s/api/feature/compare", serverUrl)).post(body).build();
            try {
                okhttp3.Response response = client.newCall(request).execute();
                if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                okhttp3.Headers responseHeaders = response.headers();
                for (int i = 0; i < responseHeaders.size(); i++) {
                    System.out.println(responseHeaders.name(i) + ": " + responseHeaders.value(i));
                }
                Gson gson = new Gson();
                CosineData fd = gson.fromJson(response.body().string(), CosineData.class);
                System.out.printf("Received cosine: %f%n", fd.getCosine());
                return fd.getCosine();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                featureStore.empty();
            }
            return -1F;
        } finally {
            hideProgressBar();
        }
    }


    /**
     * Add buffered features to {@link #model}
     * <p>
     * Update the UI accordingly if success.
     * Otherwise, showToast of the failure message
     * <p>
     * More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/model-training/#loading-a-model
     */
    @Override
    public int onAddFeaturesPressed() {
        showProgressBar();
        try {
            FeatureStore featureStore = FeatureStore.getInstance(this);
            List<List<GaitFeature>> chunks = featureStore.getAll();
            int uploadedCounter = 0;

            for (List<GaitFeature> chunk : chunks) {
                try {
                    model.add(chunk);
                    uploadedCounter += chunk.size();
                } catch (GaitModelException e) {
                    showToast("Encountered an exception when adding features to the model, "
                            + "collected features are partially uploaded, please try again");
                    Log.e(TAG, "Failed to add data to Gait Model", e);
                    uploadedCounter = 0;
                }
            }
            if (uploadedCounter > 0) { // only truncate file after we uploaded something
                featureStore.empty();
            }
            return uploadedCounter;
        } finally {
            hideProgressBar();
        }
    }

    /**
     * Start training model if users press `Okay` button of the dialog,
     * otherwise dismiss the dialog
     * <p>
     * More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/model-training/#training-a-model
     */
    @Override
    public void onTrainModelPressed() {
        // Create a dialog to confirm action
        new AlertDialog
                .Builder(MainActivity.this)
                .setTitle(R.string.train_model_confirmation_dialog_title)
                .setMessage(R.string.train_model_confirmation_dialog_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    // Enforce that collected features must be uploaded
                    boolean canNotTrain = Preferences.getInt(Preferences.FEATURE_COLLECTED_COUNT) > 0;
                    if (canNotTrain) {
                        showToast("Must upload all features before training.");
                        return;
                    }

                    showProgressBar();
                    AsyncTask.execute(() -> {
                        try {
                            model.train();
                            showFragment(new ModelPendingFragment(),
                                    ModelPendingFragment.SCREEN_TITLE);
                        } catch (GaitModelException e) {
                            String msg = "Failed to start training model";
                            Log.e(TAG, msg, e);
                            showToast(msg);
                        } finally {
                            hideProgressBar();
                        }
                    });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Refresh a model status, and update the UI accordingly the status.
     * <p>
     * More detailed documentation of our SDK API via: https://developer.unify.id/docs/gaitauth/model-training/#training-a-model
     */
    @Override
    public void onRefreshPressed() {
        showProgressBar();
        AsyncTask.execute(() -> {
            try {
                try {
                    model.refresh();
                    renderFragmentBasedOnModelStatus(model.getStatus());
                } catch (GaitModelException e) {
                    showToast("Failed to refresh model status.");
                }
            } finally {
                hideProgressBar();
            }
        });
    }

    @Override
    public void onStartCollectionPressed() {
        try {
            gaitAuthService.startFeatureCollectionForTesting(model);
        } catch (GaitAuthException e) {
            String msg = "Failed to start feature collection for testing";
            showToast(msg);
            Log.e(TAG, msg, e);
        }
    }

    @Override
    public void onStopCollectionPressed() {
        gaitAuthService.stopFeatureCollectionForTesting();
    }

    @Override
    public void onScoreFeaturesPressed() {
        Authenticator authenticator = gaitAuthService.getAuthenticator();
        if (authenticator == null) {
            showToast("Scores are unavailable, have you collected features yet?");
            return;
        }

        showProgressBar();
        authenticator.getStatus(new AuthenticationListener() {
            @Override
            public void onComplete(AuthenticationResult authenticationResult) {
                Bundle fragmentArguments = new Bundle();
                fragmentArguments.putSerializable(ScoreFragment.ARG_KEY_AUTH_RESULT,
                        authenticationResult);
                ScoreFragment scoreFragment = new ScoreFragment();
                scoreFragment.setArguments(fragmentArguments);
                showFragment(scoreFragment, ScoreFragment.SCREEN_TITLE);
                hideProgressBar();
            }

            @Override
            public void onFailure(GaitAuthException e) {
                String msg = String.format("Failed to get scores, error: %s", e.getMessage());
                showToast(msg);
                Log.e(TAG, msg, e);
                hideProgressBar();
            }
        });
    }

    @Override
    public void onDismissPressed() {
        renderFragmentBasedOnModelStatus(model.getStatus());
    }
}
