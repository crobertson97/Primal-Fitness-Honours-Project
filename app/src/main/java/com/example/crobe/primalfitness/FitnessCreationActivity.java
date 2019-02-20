package com.example.crobe.primalfitness;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class FitnessCreationActivity extends AppCompatActivity implements View.OnClickListener{

    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private Dialog myDialog;
    private Button submit;
    private EditText exercise, sets, reps, rest;
    private List<String[]> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_creation);
        myDialog = new Dialog(this);
        array = new ArrayList<String[]>();
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.pop_fitness);
        submit = (Button) myDialog.findViewById(R.id.submitExercise);
        submit.setOnClickListener(this);
        exercise = (EditText) myDialog.findViewById(R.id.exerciseName);
        sets = (EditText) myDialog.findViewById(R.id.suggestedSets);
        reps = (EditText) myDialog.findViewById(R.id.suggestedReps);
        rest = (EditText) myDialog.findViewById(R.id.suggestedRest);
        myDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.submitExercise:
                if(checkInputs()){
                    array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), rest.getText().toString()});

                    myDialog.dismiss();
                    for (String[] arra : array){
                        for(int i=0; i< arra.length; i++)
                        Log.d("FitnessCreationActivity","here: " + arra[i]);
                    }
                }
                break;
            case R.id.createPlan:
                break;
        }
    }

    private boolean checkInputs() {
        if(exercise.getText().toString().isEmpty() || sets.getText().toString().isEmpty() || reps.getText().toString().isEmpty() || rest.getText().toString().isEmpty()){
            Toast.makeText(this, "Please enter values into all fields", Toast.LENGTH_LONG).show();
            return false;
        }else {
            return true;
        }
    }
}
