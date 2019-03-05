package com.example.crobe.primalfitness;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

public class FitnessActivityW extends AppCompatActivity {

    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_diary:
                fragmentManager.beginTransaction().replace(R.id.content, new FitnessDiaryFragment()).commit();
                return true;
            case R.id.navigation_schedule:
                fragmentManager.beginTransaction().replace(R.id.content, new FitnessScheduleFragment()).commit();
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitnessw);

        fragmentManager.beginTransaction().replace(R.id.content, new FitnessFragment()).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation_fitness);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
