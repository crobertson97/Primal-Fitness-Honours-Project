package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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

public class ExerciseActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mPlanTable;
    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;
    private String exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        sh = new ServiceHandler(this);

        Button addToSchedule = findViewById(R.id.addToSchedule);
        addToSchedule.setOnClickListener(this);


        layoutPlans = findViewById(R.id.planExercises);
        if (ScheduleFragment.schedule) {
            this.setTitle(ScheduleFragment.planSchedule);
            addToSchedule.setVisibility(View.GONE);
        } else {

        }

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", this);

            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mPlanTable = mClient.getTable(ExerciseItem.class);
            mLinkTable = mClient.getTable(PlanLinkItem.class);
            initLocalStore().get();
            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
        getCreatedPlans();
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
                    sh.createAndShowDialogFromTask(e, "Error at 278");
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
        }

    }

    private void getCreatedPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<ExerciseItem> results;
                    if (ScheduleFragment.schedule) {
                        results = mPlanTable.where().field("planName").eq(ScheduleFragment.planSchedule).and(mLinkTable.where().field("complete").eq(false)).execute().get();
                    } else {
                        results = mPlanTable.where().field("exercisePlanType").eq(FitnessFragment.planType).and(mPlanTable.where().field("planName").eq(FitnessPlans.plan)).execute().get();
                    }

                    runOnUiThread(() -> {
                        for (ExerciseItem item : results) {
                            addPlanToScreen(item);
                        }
                    });
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public void addPlanToScreen(ExerciseItem item) {
        final TextView planOnScreen = new TextView(this);
        planOnScreen.setText(item.getExerciseName());
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
        planOnScreen.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            exercise = "Exercise: " + planOnScreen.getText().toString();
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
        // Set the dialog title
        builder.setTitle(exercise).setItems(stuff, null).setPositiveButton("Ok", null);
        builder.create().show();
    }

    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final PlanLinkItem item = new PlanLinkItem();
        try {
            item.setPlanName(FitnessPlans.plan);
            item.setPlanType(FitnessFragment.planType);
            item.setId(sh.createTransactionID());
            item.setUsername(LoginActivity.loggedInUser);
            item.setComplete(false);

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
                    sh.createAndShowDialogFromTask(e, "Error at 203");
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
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
    }

    private void refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        mLinkTable.execute().get();
    }
}
