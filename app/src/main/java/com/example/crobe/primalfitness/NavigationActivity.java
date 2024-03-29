package com.example.crobe.primalfitness;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;

public class NavigationActivity extends AppCompatActivity {

    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragmentManager.beginTransaction().replace(R.id.content, new HomeFragment()).commit();
                return true;
            case R.id.navigation_notifications:
                fragmentManager.beginTransaction().replace(R.id.content, new NotificationFragment()).commit();
                return true;
        }
        return false;
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        fragmentManager.beginTransaction().replace(R.id.content, new HomeFragment()).commit();

        BottomNavigationView navigation = findViewById(R.id.navigation_main);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }



}
