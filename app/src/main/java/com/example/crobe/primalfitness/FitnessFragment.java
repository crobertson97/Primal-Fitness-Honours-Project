package com.example.crobe.primalfitness;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class FitnessFragment extends Fragment implements View.OnClickListener {


    private TextView newPlan;

    public FitnessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_plan_fitness, container, false);

        newPlan = (TextView) view.findViewById(R.id.createPlan);
        newPlan.setOnClickListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createPlan:
                startActivity(new Intent(getActivity(), FitnessCreationActivity.class));
                break;
        }
    }
}
