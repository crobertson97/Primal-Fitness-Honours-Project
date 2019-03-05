package com.example.crobe.primalfitness;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

public class PlansActivity extends AppCompatActivity {

    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_fitness:
                fragmentManager.beginTransaction().replace(R.id.content, new FitnessFragment()).commit();
                return true;
            case R.id.navigation_nutrition:
                fragmentManager.beginTransaction().replace(R.id.content, new NutritionFragment()).commit();
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);

        fragmentManager.beginTransaction().replace(R.id.content, new FitnessFragment()).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation_plans);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

}
