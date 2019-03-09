package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class PlanItemActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mFitnessTable;
    private MobileServiceTable<NutritionItem> mNutritionTable;
    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;
    private String title;
    public static String planView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        sh = new ServiceHandler(this);

        Button addToSchedule = findViewById(R.id.addToSchedule);
        addToSchedule.setOnClickListener(this);

        Button completePlan = findViewById(R.id.completePlan);
        completePlan.setOnClickListener(this);

        layoutPlans = findViewById(R.id.planExercises);
        this.setTitle(planView);

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
            mLinkTable = mClient.getTable(PlanLinkItem.class);
            initLocalStore().get();
            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }

        switch (planView) {
            case "Fitness Schedule":
                addToSchedule.setVisibility(View.GONE);
                getFitnessPlans();
                break;
            case "Nutrition Schedule":
                addToSchedule.setVisibility(View.GONE);
                getNutritionPlans();
                break;
            case "Fitness Diary":
                addToSchedule.setVisibility(View.GONE);
                completePlan.setVisibility(View.GONE);
                getFitnessPlans();
                break;
            case "Nutrition Diary":
                addToSchedule.setVisibility(View.GONE);
                completePlan.setVisibility(View.GONE);
                getNutritionPlans();
                break;
            case "Fitness View":
                this.setTitle(PlansToScreen.plan);
                completePlan.setVisibility(View.GONE);
                getFitnessPlans();
                break;
            case "Nutrition View":
                this.setTitle(PlansToScreen.plan);
                completePlan.setVisibility(View.GONE);
                getNutritionPlans();
                break;
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
                    tableDefinition.put("planType", ColumnDataType.String);
                    tableDefinition.put("username", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);

                    localStore.defineTable("planlinkitem", tableDefinition);

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

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToSchedule:
                addItem();
                Toast.makeText(this, "Plan Added to your Schedule", Toast.LENGTH_SHORT).show();
                break;
            case R.id.completePlan:
                checkItem();
                Toast.makeText(this, "Plan Completed", Toast.LENGTH_SHORT).show();
                this.finish();
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
                    final List<ExerciseItem> results;
                    if (planView.equals("Fitness Schedule") || planView.equals("Fitness Diary")) {
                        results = mFitnessTable.where().field("planName").eq(ScheduleFragment.planSchedule).or(mFitnessTable.where().field("planName").eq(DiaryFragment.planSchedule)).execute().get();
                    } else {
                        results = mFitnessTable.where().field("exercisePlanType").eq(FitnessFragment.planType).and(mFitnessTable.where().field("planName").eq(PlansToScreen.plan)).execute().get();
                    }

                    runOnUiThread(() -> {
                        for (ExerciseItem item : results) {
                            addFitnessPlanToScreen(item);
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
                    final List<NutritionItem> results;
                    if (planView.equals("Nutrition Schedule") || planView.equals("Nutrition Diary")) {
                        results = mNutritionTable.where().field("recipeName").eq(NutritionScheduleFragment.planSchedule).or(mNutritionTable.where().field("recipeName").eq(NutritionDiaryFragment.planSchedule)).execute().get();
                    } else {
                        results = mNutritionTable.where().field("recipeType").eq(NutritionFragment.planType).and(mNutritionTable.where().field("recipeName").eq(PlansToScreen.plan)).execute().get();
                    }

                    runOnUiThread(() -> {
                        for (NutritionItem item : results) {
                            Log.i("TAG", "HELLLLLLp: " + item.getFoodName());
                            addNutritionPlanToScreen(item);
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

    public void addNutritionPlanToScreen(NutritionItem item) {
        final TextView planOnScreen = new TextView(this);
        planOnScreen.setText(item.getFoodName());
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
        planOnScreen.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            title = "Ingredient: " + planOnScreen.getText().toString();
            String[] meh = new String[1];
            meh[0] = "Calories: " + item.getCalories();
            onCreateDialog(meh);
        });
        layoutPlans.addView(planOnScreen);
    }

    public void addFitnessPlanToScreen(ExerciseItem item) {
        final TextView planOnScreen = new TextView(this);
        planOnScreen.setText(item.getExerciseName());
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
        planOnScreen.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            title = "Exercise: " + planOnScreen.getText().toString();
            String[] meh = new String[3];
            meh[0] = "Sets: " + item.getSetsSuggested();
            meh[1] = "Reps: " + item.getRepsSuggested();
            meh[2] = "Rest: " + item.getRest() + "(mm:ss)";
            onCreateDialog(meh);
        });
        layoutPlans.addView(planOnScreen);
    }

    public void onCreateDialog(String[] stuff) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title).setItems(stuff, null).setPositiveButton("Ok", null);
        builder.create().show();
    }

    public void addItem() {
        if (mClient == null) {
            return;
        }

        final PlanLinkItem item = new PlanLinkItem();
        try {
            item.setPlanName(PlansToScreen.plan);
            item.setId(sh.createTransactionID());
            item.setUsername(LoginActivity.loggedInUser);
            item.setComplete(false);
            if (FitnessFragment.fitness) {
                item.setPlanType(FitnessFragment.planType);
                item.setType("Fitness");
            } else if (NutritionFragment.nutrition) {
                item.setPlanType(NutritionFragment.planType);
                item.setType("Nutrition");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

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

    public void addItemInTable(PlanLinkItem item) throws ExecutionException, InterruptedException {
        mLinkTable.insert(item).get();
    }

    private void refreshItemsFromTable() {

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

    private void refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        mLinkTable.execute().get();
    }

    public void checkItem() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<PlanLinkItem> results = mLinkTable.where().field("complete").eq(val(false)).execute().get();

                    runOnUiThread(() -> {
                        for (PlanLinkItem item : results) {
                            try {
                                if (planView.equals("Fitness Schedule")) {
                                    if (item.getPlanName().equals(ScheduleFragment.planSchedule)) {
                                        item.setComplete(true);
                                        checkItemInTable(item);
                                    }
                                } else {
                                    if (item.getPlanName().equals(NutritionScheduleFragment.planSchedule)) {
                                        item.setComplete(true);
                                        checkItemInTable(item);
                                    }
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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

    public void checkItemInTable(PlanLinkItem item) throws ExecutionException, InterruptedException {
        mLinkTable.update(item).get();
    }
}
