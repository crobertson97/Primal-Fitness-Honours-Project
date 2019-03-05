package com.example.crobe.primalfitness;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;



/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inflate the layout for this fragment

        Button plansButton = (Button) view.findViewById(R.id.plans);
        plansButton.setOnClickListener(this);
        Button messageButton = (Button) view.findViewById(R.id.messages);
        messageButton.setOnClickListener(this);
        Button nutritionButton = (Button) view.findViewById(R.id.nutritionTracking);
        nutritionButton.setOnClickListener(this);
        Button fitnessButton = (Button) view.findViewById(R.id.fitnessTracking);
        fitnessButton.setOnClickListener(this);

        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plans:
                startActivity(new Intent(getActivity(), PlansActivity.class));
                break;
            case R.id.fitnessTracking:
                startActivity(new Intent(getActivity(), FitnessActivity.class));
                break;
            case R.id.nutritionTracking:
                startActivity(new Intent(getActivity(), NutritionActivity.class));
                break;
            case R.id.messages:
                Toast.makeText(getActivity(), "Yeah! Messages my dude!", Toast.LENGTH_LONG).show();
                break;
        }
    }

}
