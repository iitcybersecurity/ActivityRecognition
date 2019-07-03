package iit.cnr.it.gatheringapp.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.fragments.HistoryFragment;
import iit.cnr.it.gatheringapp.fragments.HomeFragment;
import iit.cnr.it.gatheringapp.fragments.ProbabilityFragment;
import iit.cnr.it.gatheringapp.fragments.SensorsFragment;
import iit.cnr.it.gatheringapp.fragments.TrainingFragment;
import iit.cnr.it.gatheringapp.service.BackgroundDetectedActivitiesService;
import iit.cnr.it.gatheringapp.utils.BottomNavigationViewHelper;
import iit.cnr.it.gatheringapp.utils.FbUtils;
import iit.cnr.it.gatheringapp.utils.UserActivitiesHandler;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        SensorsFragment.OnFragmentInteractionListener,
        TrainingFragment.OnFragmentInteractionListener,
        ProbabilityFragment.OnFragmentInteractionListener{

    private HomeFragment homeFragment;
    private SensorsFragment sensorsFragment;
    private TrainingFragment trainingFragment;
    private HistoryFragment historyFragment;
    private ProbabilityFragment probabilityFragment;

    private SettingsActivity settingsActivity;
    private LoginActivity loginActivity;

    private static final String HOME_TAG = "F_HOME";
    private static final String SENSORS_TAG = "F_SENSORS";
    private static final String TRAINING_TAG = "F_TRAINING";
    private static final String HISTORY_TAG = "F_HISTORY";
    private static final String PROBABILITY_TAG = "F_PROBABILITY";


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
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ACTIVITY_MAIN");
        mWakeLock.acquire();

        setContentView(R.layout.activity_main);

        //loading the default fragment
        homeFragment = new HomeFragment();
        settingsActivity = new SettingsActivity();
        loginActivity = new LoginActivity();

        loadFragment(homeFragment, HOME_TAG);

        BottomNavigationView bottomNavigation = findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        userActivitiesHandler = new UserActivitiesHandler(this, FbUtils.getUserName());
        //Activity recognition
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(BROADCAST_DETECTED_ACTIVITY)) {
                    int type = intent.getIntExtra("type", -1);
                    int confidence = intent.getIntExtra("confidence", 0);
                    if (userActivitiesHandler != null)
                        userActivitiesHandler.handleUserActivity(type, confidence, context);
                }
            }


        };

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(BROADCAST_DETECTED_ACTIVITY));
        startTracking();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.navigation_home:
                if (homeFragment == null) {
                    homeFragment = new HomeFragment();
                }
                displayFragment(homeFragment, HOME_TAG, sensorsFragment, trainingFragment, historyFragment,probabilityFragment);
                break;

            case R.id.navigation_sensors:
                if (sensorsFragment == null) {
                    sensorsFragment = new SensorsFragment();
                }
                displayFragment(sensorsFragment, SENSORS_TAG, trainingFragment, historyFragment,probabilityFragment, homeFragment);
                break;

            case R.id.navigation_training:
                if (trainingFragment == null) {
                    trainingFragment = new TrainingFragment();
                }
                displayFragment(trainingFragment, TRAINING_TAG, historyFragment, probabilityFragment, homeFragment, sensorsFragment);
                break;
            case R.id.navigation_history:
                if (historyFragment == null) {
                    historyFragment = new HistoryFragment();
                }
                displayFragment(historyFragment, HISTORY_TAG, probabilityFragment, trainingFragment, homeFragment, sensorsFragment);
                break;
            case R.id.navigation_probability:
                if (probabilityFragment == null) {
                    probabilityFragment = new ProbabilityFragment();
                }
                displayFragment(probabilityFragment, PROBABILITY_TAG, trainingFragment, historyFragment, homeFragment, sensorsFragment);
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == iit.cnr.it.gatheringapp.R.id.action_settings) {
            goToActivity(settingsActivity);
        }
        if (id == R.id.action_logout) {
            //TODO implement a generic call to logout system
            loginActivity.performLogout();
            goToActivity(loginActivity);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        this.mWakeLock.release();
        Log.e("OnDestroy", "Main Activity destroyed!");

    }


    private void startTracking() {
        Intent trackingIntent = new Intent(MainActivity.this, BackgroundDetectedActivitiesService.class);
        startService(trackingIntent);

    }

    private void goToActivity(Activity activity) {
        Intent intent = new Intent(this, activity.getClass());
        startActivity(intent);
    }

    private void loadFragment(Fragment fragment, String tag) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment, tag)
                    .commit();
        }
    }

    protected void displayFragment(Fragment newFragment, String tag, Fragment... fragments) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (newFragment.isAdded()) { // if the fragment is already in container
            fragmentTransaction.show(newFragment);
        } else { // fragment needs to be added to frame container
            fragmentTransaction.add(R.id.main_container, newFragment, tag);
        }
        for (Fragment existingFragment : fragments) {
            if (existingFragment != null && existingFragment.isAdded()) {
                fragmentTransaction.hide(existingFragment);
            }
        }

        // Commit changes
        fragmentTransaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
