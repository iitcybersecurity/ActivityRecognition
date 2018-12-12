package iit.cnr.it.gatheringapp;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by giacomo on 18/10/18.
 */

public class Utils {

    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            InputStream rawResource = resources.openRawResource(R.raw.config);
            Properties properties = new Properties();
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e("Properties", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Properties", "Failed to open config file.");
        }

        return null;
    }
}
