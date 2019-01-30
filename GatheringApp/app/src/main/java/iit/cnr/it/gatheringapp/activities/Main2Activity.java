package iit.cnr.it.gatheringapp.activities;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.fragments.HistoryFragment;
import iit.cnr.it.gatheringapp.fragments.HomeFragment;
import iit.cnr.it.gatheringapp.fragments.SensorsFragment;
import iit.cnr.it.gatheringapp.fragments.TrainingFragment;
import iit.cnr.it.gatheringapp.sensors.Accelerometer;
import iit.cnr.it.gatheringapp.utils.BottomNavigationViewHelper;
import iit.cnr.it.gatheringapp.utils.FbUtils;
import iit.cnr.it.gatheringapp.utils.UserActivitiesHandler;

public class Main2Activity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        SensorsFragment.OnFragmentInteractionListener,
        TrainingFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;
    private ProfileTracker mProfileTracker;
    private UserActivitiesHandler userActivitiesHandler = null;


    private static final String HOME_TAG ="F_HOME";
    private static final String SENSORS_TAG ="F_SENSORS";
    private static final String TRAINING_TAG ="F_TRAINING";
    private static final String HISTORY_TAG ="F_HISTORY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //loading the default fragment
        loadFragment(new HomeFragment(), HOME_TAG);

        BottomNavigationView bottomNavigation = (BottomNavigationView) findViewById(R.id.navigation);
        BottomNavigationViewHelper.removeShiftMode(bottomNavigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    private boolean loadFragment(Fragment fragment, String tag) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment, tag)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String tag = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                tag = HOME_TAG;
                break;

            case R.id.navigation_sensors:
                fragment = new SensorsFragment();
                tag = SENSORS_TAG;
                break;

            case R.id.navigation_training:
                fragment = new TrainingFragment();
                tag = TRAINING_TAG;
                break;

            case R.id.navigation_history:
                fragment = new HistoryFragment();
                tag = HISTORY_TAG;
                break;
        }

        return loadFragment(fragment, tag);
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
        FbUtils utilsFb = new FbUtils(userID, userName, this);
        utilsFb.execute();
        userActivitiesHandler = new UserActivitiesHandler(this, userName);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
