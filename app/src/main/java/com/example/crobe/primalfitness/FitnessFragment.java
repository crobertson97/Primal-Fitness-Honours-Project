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
public class FitnessFragment extends Fragment implements View.OnClickListener {


    public static String planType;
    public static Boolean fitness;

    public FitnessFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_plan_fitness, container, false);

        TextView calisthetics = view.findViewById(R.id.calisthetics);
        calisthetics.setOnClickListener(this);
        TextView cardio = view.findViewById(R.id.cardio);
        cardio.setOnClickListener(this);
        TextView weights = view.findViewById(R.id.weights);
        weights.setOnClickListener(this);




        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.calisthetics:
                planType = "Calisthetics";
                fitness = true;
                NutritionFragment.nutrition = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
            case R.id.cardio:
                planType = "Cardio";
                fitness = true;
                NutritionFragment.nutrition = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
            case R.id.weights:
                planType = "Weights";
                fitness = true;
                NutritionFragment.nutrition = false;
                startActivity(new Intent(getActivity(), PlansToScreen.class));
                break;
        }
    }
}
