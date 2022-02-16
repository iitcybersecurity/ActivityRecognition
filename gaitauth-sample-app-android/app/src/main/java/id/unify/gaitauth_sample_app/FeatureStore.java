/*
 * Copyright (c) 2020 UnifyID, Inc. All rights reserved.
 * Unauthorized copying or excerpting via any medium is strictly prohibited.
 * Proprietary and confidential.
 */

package id.unify.gaitauth_sample_app;

import android.content.Context;
import android.util.Log;

import org.apache.commons.compress.utils.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import id.unify.sdk.gaitauth.FeatureCollectionException;
import id.unify.sdk.gaitauth.GaitAuth;
import id.unify.sdk.gaitauth.GaitFeature;

public class FeatureStore {

    private static final String TAG = FeatureStore.class.getSimpleName();
    private static final int UPLOAD_SIZE = 200;
    private static final String STORE_FILE_NAME = "id.unify.sdk.gaitauth.FeatureStore";

    private static FeatureStore instance;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private File file;

    private FeatureStore(String path) {
        file = new File(path);
        try {
            file.createNewFile();
        } catch (IOException e) {
            Log.e(TAG, "Failed to create a new file", e);
        }
    }

    public static synchronized FeatureStore getInstance(Context context) {
        if (instance == null) {
            String path = context.getFilesDir().getAbsolutePath() + File.separator + STORE_FILE_NAME;
            instance = new FeatureStore(path);
        }
        return instance;
    }

    public synchronized void add(GaitFeature feature) {
        // Add data via executor to ensure it is done in a single-threaded manner
        executor.submit(() -> {
            // Deserialize any GaitFeatures already in file
            List<GaitFeature> gaitFeatures = new ArrayList<>();
            try (InputStream inputStream = new FileInputStream(file)) {
                // Read all of the serialized data from the file
                byte[] existingData = IOUtils.toByteArray(inputStream);
                // Deserialize bytes into GaitFeatures
                if (existingData.length > 0) {
                    gaitFeatures = GaitAuth.deserializeFeatures(existingData);
                }
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to find file to read from", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to read from file", e);
            } catch (FeatureCollectionException e) {
                Log.e(TAG, "Failed to deserialize features", e);
            }
            // Add the new GaitFeature
            gaitFeatures.add(feature);

            // Serialize the GaitFeatures to bytes
            byte[] data = new byte[0];
            try {
                data = GaitAuth.serializeFeatures(gaitFeatures);
            } catch (FeatureCollectionException e) {
                Log.e(TAG, "Failed to serialize GaitFeatures", e);
            }

            // Append the bytes to the file
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(file);
                fos.write(data);
            } catch (Exception e) {
                Log.e(TAG, "Failed to store features in file", e);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close a FileOutputStream", e);
                    }
                }
            }
        });
    }

    // retrieve data in chunks
    public synchronized List<List<GaitFeature>> getAll() {
        List<List<GaitFeature>> chunks = new ArrayList<>();
        try (InputStream inputStream = new FileInputStream(file)) {
            // Read all of the serialized data from the file
            byte[] data = IOUtils.toByteArray(inputStream);
            if (data.length == 0) {
                return chunks;
            }

            // Deserialize bytes into GaitFeatures
            List<GaitFeature> gaitFeatures = GaitAuth.deserializeFeatures(data);

            // Split features into chunks of UPLOAD_SIZE
            chunks = partition(gaitFeatures);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Failed to find file to read from", e);
        } catch (IOException e) {
            Log.e(TAG, "Failed to read from file", e);
        } catch (FeatureCollectionException e) {
            Log.e(TAG, "Failed to deserialize features", e);
        }
        return chunks;
    }

    private static List<List<GaitFeature>> partition(List<GaitFeature> members) {
        List<List<GaitFeature>> res = new ArrayList<>();
        List<GaitFeature> internal = new ArrayList<>();

        for (GaitFeature member : members) {
            internal.add(member);

            if (internal.size() == UPLOAD_SIZE) {
                res.add(internal);
                internal = new ArrayList<>();
            }
        }

        if (!internal.isEmpty()) {
            res.add(internal);
        }

        return res;
    }

    // empty backing file content
    public void empty() {
        executor.submit(() -> {
            try (PrintWriter pw = new PrintWriter(file)) {
            } catch (FileNotFoundException e) {
                Log.e(TAG, "Fail to empty FeatureStore backing file", e);
            }
        });
    }
}
