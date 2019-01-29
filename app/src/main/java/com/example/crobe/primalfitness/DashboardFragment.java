package com.example.crobe.primalfitness;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends Fragment implements OnClickListener {

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        ImageButton plansButton = (ImageButton) view.findViewById(R.id.plansImage);
        plansButton.setOnClickListener(this);
        ImageButton messageButton = (ImageButton) view.findViewById(R.id.messageImage);
        messageButton.setOnClickListener(this);
        ImageButton nutritionButton = (ImageButton) view.findViewById(R.id.nutritionImage);
        nutritionButton.setOnClickListener(this);
        ImageButton fitnessButton = (ImageButton) view.findViewById(R.id.fitnessImage);
        fitnessButton.setOnClickListener(this);

        return view;
    }

    public void onClick(View v) {
        switch(v.getId()){
            case R.id.plansImage:
                startActivity(new Intent(getActivity(), PlansActivity.class));
                break;
            case R.id.fitnessImage:
                Toast.makeText(getActivity(), "Yeah! Fitness my dude!", Toast.LENGTH_LONG).show();
                break;
            case R.id.nutritionImage:
                Toast.makeText(getActivity(), "Yeah! Nutrition my dude!", Toast.LENGTH_LONG).show();
                break;
            case R.id.messageImage:
                Toast.makeText(getActivity(), "Yeah! Messages my dude!", Toast.LENGTH_LONG).show();
                break;
        }
    }


}
