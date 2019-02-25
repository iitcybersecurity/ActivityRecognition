package iit.cnr.it.gatheringapp.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by giacomo on 23/10/18.
 */

@SuppressLint("StaticFieldLeak")
public class FbUtils extends AsyncTask<String, Void, Bitmap> {
    private String userID;
    private String userName;
    private Activity activity;

    public FbUtils(String userId, String userName, Activity _activity){
        this.userID = userId;
        this.userName = userName;
        this.activity = _activity;
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
       // TODO riattiva quando te la senti bro
        // profilePicture.setImageBitmap(bitmap);
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
}

