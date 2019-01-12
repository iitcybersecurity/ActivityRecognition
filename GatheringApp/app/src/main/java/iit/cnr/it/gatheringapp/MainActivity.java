package iit.cnr.it.gatheringapp;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TextView;
import com.facebook.*;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import iit.cnr.it.gatheringapp.sensors.Accelerometer;
import iit.cnr.it.gatheringapp.utils.UserActivitiesHandler;
import iit.cnr.it.gatheringapp.service.BackgroundDetectedActivitiesService;
import iit.cnr.it.gatheringapp.utils.FbUtils;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    //Facebook variables
    private CallbackManager mFacebookCallbackManager;
    private ProfileTracker mProfileTracker;

    //Accelerometer variables
    private Accelerometer accelerometerFragment = null;

    //GUI variables
    private Toolbar toolbar;
    TextView loginText;


    //Activity recognition
    BroadcastReceiver broadcastReceiver;
    public static final String BROADCAST_DETECTED_ACTIVITY = "activity_intent";
    public UserActivitiesHandler userActivitiesHandler = null;
    private PowerManager.WakeLock mWakeLock = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //to keep the app alive in background and also with screen off
        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "activityMain");
        mWakeLock.acquire();

        loginText = findViewById(iit.cnr.it.gatheringapp.R.id.login_text);


        //Facebook sdk initialization
        FacebookSdk.sdkInitialize(getApplicationContext());

        setContentView(iit.cnr.it.gatheringapp.R.layout.activity_main);
        createToolbar();

        mFacebookCallbackManager = CallbackManager.Factory.create();
        LoginButton mFacebookSignInButton = findViewById(iit.cnr.it.gatheringapp.R.id.login_button);
        mFacebookSignInButton.setReadPermissions("email");

        //See if the user is already logged
        final AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if(isLoggedIn){
            setProfile();
        }

        // Callback registration
        mFacebookSignInButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                setProfile();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException exception) {
            }
        });

        //Facebook tracker
        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {

                if (currentAccessToken == null){
                    //User logged out
                    createToolbar();
                }
            }
        };


        //Activity recognition
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    if(userActivitiesHandler !=null)
                        userActivitiesHandler.handleUserActivity(type,confidence, context);
                }
            }


        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BROADCAST_DETECTED_ACTIVITY));
        System.out.println("ON CREATE");
        startTracking();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    void setProfile() {
        Profile profile;

        if (Profile.getCurrentProfile() == null) mProfileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                getFbInfo(currentProfile.getId(), currentProfile.getName());
                mProfileTracker.stopTracking();
            }
        };
        else {
            profile = Profile.getCurrentProfile();
            getFbInfo(profile.getId(), profile.getName());
        }

    }


    private void getFbInfo(final String userID, final String userName){
        accelerometerFragment = new Accelerometer(this.getApplicationContext(), userName, this);
        FbUtils utilsFb = new FbUtils(userID, userName, this);
        utilsFb.execute();
        userActivitiesHandler = new UserActivitiesHandler(this, userName);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /*LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BROADCAST_DETECTED_ACTIVITY));*/
    }


    private void startTracking() {

        Intent intent1 = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(intent1);


    }




    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }


    void createToolbar(){
        AppBarLayout barLayout = findViewById(iit.cnr.it.gatheringapp.R.id.appBar);
        barLayout.setExpanded(true);

        toolbar = findViewById(iit.cnr.it.gatheringapp.R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(iit.cnr.it.gatheringapp.R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, iit.cnr.it.gatheringapp.R.string.navigation_drawer_open, iit.cnr.it.gatheringapp.R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(iit.cnr.it.gatheringapp.R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toolbar.setVisibility(View.INVISIBLE);
        //activityTextView = findViewById(R.id.ActivityTextView);
        //activityTextView.setText("Login before use GatheringAPP");
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer =  findViewById(iit.cnr.it.gatheringapp.R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
        TableLayout activitiesTable = findViewById(iit.cnr.it.gatheringapp.R.id.ActivitiesTable);
        activitiesTable.setVisibility(View.VISIBLE);
        //activityTextView.setVisibility(View.VISIBLE);
        System.out.println("BACK PRESSED");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(iit.cnr.it.gatheringapp.R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == iit.cnr.it.gatheringapp.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == iit.cnr.it.gatheringapp.R.id.nav_sensors) {


            getSupportFragmentManager().beginTransaction()
                    .replace(iit.cnr.it.gatheringapp.R.id.content_frame, accelerometerFragment)
                    .addToBackStack(null)
                    .commit();


        }/*
        else if (id == R.id.nav_gallery) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content_frame, new MainActivity)
                    .addToBackStack(null)
                    .commit();

        } else if (id == R.id.nav_slideshow) {
            // Handle the SlideShow Fragment

        } else if (id == R.id.nav_manage) {
            // Handle the Tools Fragment

        } else if (id == R.id.nav_share) {
            // Handle the Share Fragment

        } else if (id == R.id.nav_send) {
            // Handle the Send Fragment

        }*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.mWakeLock.release();
        Log.e("OnDestroy", "ondestroy");

    }


}
