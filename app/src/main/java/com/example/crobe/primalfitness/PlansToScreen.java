package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

public class PlansToScreen extends AppCompatActivity implements View.OnClickListener {

    public static String plan;
    public static Boolean plans;
    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mFitnessTable;
    private MobileServiceTable<NutritionItem> mNutritionTable;

    private ServiceHandler sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_plans);
        sh = new ServiceHandler(this);

        layoutPlans = findViewById(R.id.createdPlans);

        TextView newPlan = findViewById(R.id.createPlan);
        newPlan.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", this);

            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mFitnessTable = mClient.getTable(ExerciseItem.class);
            mNutritionTable = mClient.getTable(NutritionItem.class);

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
        if (FitnessFragment.fitness) {
            getFitnessPlans();
            PlanItemActivity.planView = "Fitness View";
        } else if (NutritionFragment.nutrition) {
            getNutritionPlans();
            PlanItemActivity.planView = "Nutrition View";
        }
        if (!LoginActivity.loggedInUserType.equals("Coach")) {
            newPlan.setVisibility(View.GONE);
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createPlan:
                if(FitnessFragment.fitness){
                    startActivity(new Intent(this, FitnessCreationActivity.class));
                }else if (NutritionFragment.nutrition) {
                    startActivity(new Intent(this, NutritionCreationActivity.class));
                }
                break;
        }
    }

    private void getFitnessPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<ExerciseItem> results = mFitnessTable.where().field("exercisePlanType").eq(FitnessFragment.planType).execute().get();
                    final ArrayList<String> stuff = new ArrayList<>();


                    runOnUiThread(() -> {
                        for (ExerciseItem item : results) {
                            stuff.add(item.getPlanName());
                        }
                        final Collection<String> stuff2 = stuff.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
                        for (String item : stuff2) {
                            addPlanToScreen(item);
                        }
                    });
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    private void getNutritionPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<NutritionItem> results = mNutritionTable.where().field("recipeType").eq(NutritionFragment.planType).execute().get();
                    final ArrayList<String> stuff = new ArrayList<>();


                    runOnUiThread(() -> {
                        for (NutritionItem item : results) {
                            stuff.add(item.getRecipeName());
                        }
                        final Collection<String> stuff2 = stuff.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
                        for (String item : stuff2) {
                            addPlanToScreen(item);
                        }
                    });
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public void addPlanToScreen(String item) {
        final TextView planOnScreen = new TextView(this);
        planOnScreen.setText(item);
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
        planOnScreen.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            plan = planOnScreen.getText().toString();
            startActivity(new Intent(getApplicationContext(), PlanItemActivity.class));
        });
        layoutPlans.addView(planOnScreen);
    }


}

