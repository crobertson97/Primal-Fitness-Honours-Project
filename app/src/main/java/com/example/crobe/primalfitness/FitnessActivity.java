package com.example.crobe.primalfitness;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class FitnessActivity extends AppCompatActivity {

    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = (MenuItem item) -> {
        switch (item.getItemId()) {
            case R.id.navigation_diary_fitness:
                fragmentManager.beginTransaction().replace(android.R.id.content, new DiaryFragment()).commit();
                return true;
            case R.id.navigation_schedule_fitness:
                fragmentManager.beginTransaction().replace(android.R.id.content, new ScheduleFragment()).commit();
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness);
        this.setTitle("Fitness Tracking");

        fragmentManager.beginTransaction().replace(android.R.id.content, new DiaryFragment()).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation_fitness);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }
}

