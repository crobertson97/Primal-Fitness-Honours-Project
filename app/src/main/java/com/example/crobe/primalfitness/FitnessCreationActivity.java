package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class FitnessCreationActivity extends AppCompatActivity implements View.OnClickListener {

    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mExerciseTable;
    private Dialog myDialog;
    private EditText exercise, sets, reps, restSets, restReps, name, weight;
    private List<String[]> array;
    private ServiceHandler sh;
    private String planName, planType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_creation);
        sh = new ServiceHandler(this);
        myDialog = new Dialog(this);
        myDialog.setContentView(R.layout.pop_fitness);
        array = new ArrayList<>();
        name = findViewById(R.id.planName);


        Button createPlan = findViewById(R.id.createPlan);
        createPlan.setOnClickListener(this);

        Button addExercise = findViewById(R.id.addExercise);
        addExercise.setOnClickListener(this);

        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",this);

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });
            mExerciseTable = mClient.getTable(ExerciseItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.pop_fitness);
        exercise = myDialog.findViewById(R.id.exerciseName);
        sets = myDialog.findViewById(R.id.suggestedSets);
        reps = myDialog.findViewById(R.id.suggestedReps);
        restSets = myDialog.findViewById(R.id.suggestedRestSets);
        restReps = myDialog.findViewById(R.id.suggestedRestReps);

        myDialog.show();
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    public Dialog onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Get the layout inflater
        LayoutInflater inflater = getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        builder.setView(inflater.inflate(R.layout.pop_fitness, null));
        exercise = myDialog.findViewById(R.id.exerciseName);
        weight = myDialog.findViewById(R.id.weightInput);
        Log.i("TAG", "" + FitnessFragment.planType);
        TextView test = myDialog.findViewById(R.id.exerciseNameLabel);
        if (FitnessFragment.planType.equals("Cardio")) {
            Log.i("TAG", "" + test.getText().toString());
            exercise.setText("Distance");
            myDialog.findViewById(R.id.weightInput).setVisibility(View.GONE);
            myDialog.findViewById(R.id.weightInputLabel).setVisibility(View.GONE);
        } else if (FitnessFragment.planType.equals("Weights")) {
            exercise.setText("Exercise");
        }


        sets = myDialog.findViewById(R.id.suggestedSets);
        reps = myDialog.findViewById(R.id.suggestedReps);
        restSets = myDialog.findViewById(R.id.suggestedRestSets);
        restReps = myDialog.findViewById(R.id.suggestedRestReps);

        builder.setPositiveButton("Add", (dialog, id) -> {

            if (checkInputs()) {
                switch (FitnessFragment.planType) {
                    case "Cardio":
                        array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), restSets.getText().toString(), restReps.getText().toString()});
                        break;
                    case "Weights":
                        array.add(new String[]{exercise.getText().toString(), weight.getText().toString(), sets.getText().toString(), reps.getText().toString(), restSets.getText().toString(), restReps.getText().toString()});
                        break;
                }
                array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), restSets.getText().toString(), restReps.getText().toString()});
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) ->
                dialog.dismiss()
        );
        return builder.create();
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createPlan:
                if (name.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please enter a name and type", Toast.LENGTH_LONG).show();
                } else if (array.isEmpty()) {
                    Toast.makeText(this, "Please add exercises", Toast.LENGTH_LONG).show();
                } else {
                    for (String[] arra : array) {
                        planName = name.getText().toString();
                        planType = FitnessFragment.planType;
                        addItem(arra);
                        Toast.makeText(this, "Plan Added", Toast.LENGTH_LONG).show();
                        this.finish();
                    }
                }
                break;

            case R.id.addExercise:
                onCreateDialog().show();
                break;

        }
    }

    private boolean checkInputs() {
        if(exercise.getText().toString().isEmpty() || sets.getText().toString().isEmpty() || reps.getText().toString().isEmpty() || restSets.getText().toString().isEmpty() || restReps.getText().toString().isEmpty()){
            Toast.makeText(this, "Please enter values into all fields", Toast.LENGTH_LONG).show();
            return false;
        }else {
            LinearLayout linearLayout = findViewById(R.id.exercisesLayout);
            TextView newExercise = new TextView(this);
            newExercise.setText(exercise.getText().toString());
            newExercise.setTextSize(24);
            newExercise.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
            newExercise.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            newExercise.setTextColor(Color.parseColor("#ff000000"));
            linearLayout.addView(newExercise);
            return true;
        }
    }

    private AsyncTask<Void, Void, Void> initLocalStore() {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                    tableDefinition.put("planName", ColumnDataType.String);
                    tableDefinition.put("exercisePlanType", ColumnDataType.String);
                    tableDefinition.put("exerciseName", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("setsSuggested", ColumnDataType.String);
                    tableDefinition.put("repsSuggested", ColumnDataType.String);
                    tableDefinition.put("restSets", ColumnDataType.String);
                    tableDefinition.put("restReps", ColumnDataType.String);
                    tableDefinition.put("createdBy", ColumnDataType.String);

                    localStore.defineTable("exerciseitem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();


                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }

                return null;
            }
        };

        return sh.runAsyncTask(task);
    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
    }

    private void refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        mExerciseTable.where().execute().get();
    }

    public void addItem(String[] exercises) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ExerciseItem item = new ExerciseItem();
        try {
            item.setPlanName(planName);
            item.setPlanType(planType);
            item.setId(sh.createTransactionID());
            item.setExerciseName(exercises[0]);
            item.setSetsSuggested(exercises[1]);
            item.setRepsSuggested(exercises[2]);
            item.setRestSets(exercises[3]);
            item.setRestReps(exercises[4]);
            item.setCreatedBy(LoginActivity.loggedInUser);
            item.setPrivate(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    addItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public void addItemInTable(ExerciseItem item) throws ExecutionException, InterruptedException {
        mExerciseTable.insert(item).get();
    }
}
