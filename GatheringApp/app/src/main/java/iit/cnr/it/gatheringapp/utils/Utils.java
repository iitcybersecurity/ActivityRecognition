package iit.cnr.it.gatheringapp.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import iit.cnr.it.gatheringapp.R;

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
            String property = properties.getProperty(name);
            return property;
        } catch (Resources.NotFoundException e) {
            Log.e("Properties", "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e("Properties", "Failed to open config file.");
        }

        return null;
    }


    //TODO understand why it's so inefficient
    public static boolean loadFragment(FragmentManager fragmentManager, int viewId, Fragment fragment, String tag) {
        //switching fragment
        if (fragment != null) {
            fragmentManager
                    .beginTransaction()
                    .replace(viewId, fragment, tag)
                    .commit();
            return true;
        }
        return false;
    }
}
