package com.example.voteboat.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.voteboat.R;
import com.example.voteboat.adapters.ElectionAdapter;
import com.example.voteboat.databinding.ActivityMainBinding;
import com.example.voteboat.fragments.ElectionFragment;
import com.example.voteboat.fragments.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.parse.ParseObject;

import org.parceler.Parcels;


public class MainActivity extends AppCompatActivity implements ElectionAdapter.ElectionAdapterListener {

    public static final String TAG = "MainActivity";
    ActivityMainBinding binding;
    final FragmentManager fragmentManager = getSupportFragmentManager();
    final ElectionFragment electionFragment = new ElectionFragment();
    final ProfileFragment profileFragment = new ProfileFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View Binding. Inflating XML file
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Setting listener for bottom navigation
        setBottomNavigationListener();

        // Location
        // Checking if key is null
        if (TextUtils.isEmpty(getResources().getString(R.string.google_maps_api_key))) {
            throw new IllegalStateException("You forgot to supply a Google Maps API key");
        }

    }

    private void setBottomNavigationListener() {
        binding.bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            Fragment fragment;

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        fragment = electionFragment;
                        break;
                    case R.id.action_calendar:
                        fragment = electionFragment;
                        break;
                    case R.id.action_profile:
                        fragment = profileFragment;
                        break;
                    default:
                        fragment = electionFragment;
                        break;
                }
                fragmentManager.beginTransaction().replace(R.id.flContainer, fragment).commit();
                return true;
            }
        });

        binding.bottomNavigation.setSelectedItemId(R.id.action_home);
    }

    /* Listens for when election is clicked on, to go to that elections races
    *  object is the data to be passed into the new fragment
    *  fragment is which fragment we are going to
    *  type is what data type the object is
    */
    @Override
    public void setElectionListener(Object object, Fragment fragment, String type) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(type, Parcels.wrap(object));
        fragment.setArguments(bundle);
        // Replace frame layout with fragment
        fragmentManager.beginTransaction().replace(binding.flContainer.getId(),fragment).commit();
    }
}