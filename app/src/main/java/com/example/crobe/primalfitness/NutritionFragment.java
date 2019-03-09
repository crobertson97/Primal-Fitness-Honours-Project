package com.example.crobe.primalfitness;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class NutritionFragment extends Fragment implements View.OnClickListener {

    public static String planType;
    public static Boolean nutrition;

    public NutritionFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plan_nutrition, container, false);

        TextView calisthetics = view.findViewById(R.id.breakfast);
        calisthetics.setOnClickListener(this);
        TextView cardio = view.findViewById(R.id.lunch);
        cardio.setOnClickListener(this);
        TextView weights = view.findViewById(R.id.dinner);
        weights.setOnClickListener(this);
        TextView newPlan = view.findViewById(R.id.createPlanFood);
        newPlan.setOnClickListener(this);

        if (!LoginActivity.loggedInUserType.equals("Coach")) {
            newPlan.setVisibility(View.GONE);
        }

        return view;
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.breakfast:
                planType = "Breakfast";
                nutrition = true;
                FitnessFragment.fitness = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
            case R.id.lunch:
                planType = "Lunch";
                nutrition = true;
                FitnessFragment.fitness = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
            case R.id.dinner:
                planType = "Dinner";
                nutrition = true;
                FitnessFragment.fitness = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
            case R.id.createPlanFood:
                startActivity(new Intent(getActivity(), NutritionCreationActivity.class));
                break;
        }
    }
}

