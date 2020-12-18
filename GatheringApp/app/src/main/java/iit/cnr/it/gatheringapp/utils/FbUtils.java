package iit.cnr.it.gatheringapp.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.facebook.Profile;
import com.facebook.ProfileTracker;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import iit.cnr.it.gatheringapp.R;

/**
 * Created by giacomo on 23/10/18.
 */

@SuppressLint("StaticFieldLeak")
public class FbUtils extends AsyncTask<String, Void, Bitmap> {
    private String userID;
    private static String userName;
    private AppCompatActivity activity;
    private static ProfileTracker mProfileTracker;

    public FbUtils(String userId, String userName, FragmentActivity _activity){
        this.userID = userId;
        this.userName = userName;
        this.activity = (AppCompatActivity) _activity;
    }


    @Override
    protected Bitmap doInBackground(String... params) {
        Log.d("do in background", "BACKGROUND");
        Bitmap bitmap = null;
        try {
            String profileImgUrl = "https://graph.facebook.com/" + userID + "/picture?type=large";
            bitmap = getBitmapFromURL(profileImgUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        TextView username = this.activity.findViewById(R.id.facebook_username);
        username.setText(userName);
        ImageView profilePicture = this.activity.findViewById(R.id.facebook_profile_picture);
        profilePicture.setImageBitmap(bitmap);
//        View appBarLayout = this.activity.findViewById( R.id.app_bar_main );
//        View appBar = appBarLayout.findViewById(R.id.appBar);
//        View toolbar = appBar.findViewById(R.id.toolbar);
//        toolbar.setVisibility(View.VISIBLE);
//        //TextView activityTextView = this.activity.findViewById(R.id.ActivityTextView);
        //activityTextView.setText("Activities:");


    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }


    private static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getUserName() {
        Profile profile;

        if (Profile.getCurrentProfile() == null) mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                mProfileTracker.stopTracking();
                userName = currentProfile.getName();
            }
        };
        else {
            profile = Profile.getCurrentProfile();
            userName = profile.getName();
        }
        return userName;
    }

}

