package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

public class FitnessPlans extends AppCompatActivity {

    public static String plan;
    List<String> plans;
    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mPlanTable;
    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_plans);
        sh = new ServiceHandler(this);

        layoutPlans = findViewById(R.id.createdPlans);

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

    private void getCreatedPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<ExerciseItem> results = mPlanTable.where().field("exercisePlanType").eq(FitnessFragment.planType).execute().get();
                    final ArrayList<String> stuff = new ArrayList<>();


                    runOnUiThread(() -> {
                        for (ExerciseItem item : results) {
                            stuff.add(item.getPlanName());
                        }
                        final Collection<String> stuff2 = stuff.stream().distinct().collect(Collectors.toCollection(LinkedList::new));
                        for (String item5 : stuff2) {
                            addPlanToScreen(item5);
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

    public void addPlanToScreen(String item) {
        final TextView planOnScreen = new TextView(this);
        planOnScreen.setText(item);
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
        planOnScreen.setTextAlignment(TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            onCreateDialog();
            plan = planOnScreen.getText().toString();
            Log.i("TAG", "" + plan);
        });
        layoutPlans.addView(planOnScreen);
    }

    public void onCreateDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("View exercises in this plan or link it to your account?")
                .setPositiveButton("Link", (dialog, id) -> addItem())
                .setNegativeButton("View", (dialog, id) -> startActivity(new Intent(getApplicationContext(), ExerciseActivity.class)));
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final PlanLinkItem item = new PlanLinkItem();
        try {
            item.setPlanName(plan);
            item.setPlanType(FitnessFragment.planType);
            item.setId(sh.createTransactionID());
            item.setUsername(LoginActivity.loggedInUser);

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final PlanLinkItem entity = addItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error at 203");
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public PlanLinkItem addItemInTable(PlanLinkItem item) throws ExecutionException, InterruptedException {
        return mLinkTable.insert(item).get();
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

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<PlanLinkItem> results = refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
    }

    private List<PlanLinkItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        return mLinkTable.execute().get();
    }

}

