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
import iit.cnr.it.gatheringapp.R;
import iit.cnr.it.gatheringapp.fragments.HistoryFragment;
import iit.cnr.it.gatheringapp.fragments.HomeFragment;
import iit.cnr.it.gatheringapp.fragments.SensorsFragment;
import iit.cnr.it.gatheringapp.fragments.TrainingFragment;

public class Main2Activity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener,
        HomeFragment.OnFragmentInteractionListener,
        HistoryFragment.OnFragmentInteractionListener,
        SensorsFragment.OnFragmentInteractionListener,
        TrainingFragment.OnFragmentInteractionListener {

    private TextView mTextMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        //loading the default fragment
        loadFragment(new HomeFragment());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_menu, menu);
        return true;
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new HomeFragment();
                break;

            case R.id.navigation_sensors:
                fragment = new SensorsFragment();
                break;

            case R.id.navigation_training:
                fragment = new TrainingFragment();
                break;

            case R.id.navigation_history:
                fragment = new HistoryFragment();
                break;
        }

        return loadFragment(fragment);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
