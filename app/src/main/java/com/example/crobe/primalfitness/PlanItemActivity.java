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
import java.util.ArrayList;
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
    private MobileServiceTable<UserItem> mUserTable;
    private ServiceHandler sh;
    private String title;
    public static String planView;
    private String[] coachingLinks, emailLinks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        sh = new ServiceHandler(this);

        Button addToSchedule = findViewById(R.id.addToSchedule);
        addToSchedule.setOnClickListener(this);
        if (LoginActivity.loggedInUserType.equals("Coach")) {
            addToSchedule.setText("Add To Athletes Schedule");
        }

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
            mUserTable = mClient.getTable(UserItem.class);
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

        if (LoginActivity.loggedInUserType.equals("Coach")){
            completePlan.setVisibility(View.GONE);
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

    public void addItemLinks(ArrayList<Integer> links, String[] names, String[] email) {
        if (mClient == null) {
            return;
        }
        PlanLinkItem item;// = new PlanLinkItem();
        int i = 0;

        for (Object arrad : links) {

            item = new PlanLinkItem();
            //test(arrad, names, email, item);
            Log.i("TAG", "" + i);
            i++;
            try {
                Log.i("TAG", "" + PlansToScreen.plan);
                item.setPlanName(PlansToScreen.plan);
                item.setId(sh.createTransactionID());
                item.setUsername(email[(int) arrad]);
                item.setComplete(false);
                item.setPlanType(FitnessFragment.planType);
                item.setType("Fitness");
            } catch (Exception e) {
                e.printStackTrace();
            }

            test(item);
        }

    }

    private void test(PlanLinkItem item) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    addLinkItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);

    }

    public void addLinkItemInTable(PlanLinkItem item) {
        mLinkTable.insert(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToSchedule:
                if (LoginActivity.loggedInUserType.equals("Coach")) {
                    getCoachLinks();
                    Toast.makeText(this, "Plan Added to Athletes Schedule", Toast.LENGTH_SHORT).show();
                    //this.finish();
                    break;
                } else {
                    addItem();
                    this.finish();
                    break;
                }


            case R.id.completePlan:
                checkItem();
                Toast.makeText(this, "Plan Completed", Toast.LENGTH_SHORT).show();
                this.finish();
                break;
        }
    }

    private void getCoachLinks() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<UserItem> links = mUserTable.where().field("coachLink").eq(LoginActivity.loggedInUser).execute().get();
                    coachingLinks = new String[links.size()];
                    emailLinks = new String[links.size()];
                    runOnUiThread(() -> {
                        int i = 0;
                        for (UserItem coachLinks : links) {
                            try {
                                coachingLinks[i] = AESCrypt.decrypt(coachLinks.getFirstName()) + " " + AESCrypt.decrypt(coachLinks.getSurname());
                                emailLinks[i] = coachLinks.getEmail();
                                i++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        onCreateDialog(coachingLinks, emailLinks);
                    });
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);

    }

    public void onCreateDialog(String[] names, String[] email) {
        ArrayList<Integer> selectedItems = new ArrayList<>();  // Where we track the selected items
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Select Athletes");
        // Specify the list array, the items to be selected by default (null for none),
        // and the listener through which to receive callbacks when items are selected
        builder.setMultiChoiceItems(names, null,
                (dialog, which, isChecked) -> {
                    if (isChecked) {
                        //addPlan();
                        selectedItems.add(which);
                    } else {// if (selectedItems.contains(which)) {
                        // Else, if the item is already in the array, remove it
                        //selectedItems.remove(Integer.valueOf(which));
                    }
                });
        // Set the action buttons
        builder.setPositiveButton("Link", (dialog, id) -> {
            addItemLinks(selectedItems, names, email);
        });
        builder.setNegativeButton("Cancel", (dialog, id) -> {
            ;
            dialog.dismiss();
        });

        builder.create().show();
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
            if(FitnessFragment.planType.equals("Weights")){
                title = "Exercise: " + planOnScreen.getText().toString();
                String[] meh = new String[5];
                meh[0] = "Weight: " + item.getWeight() + "Kg";
                meh[1] = "Sets: " + item.getSetsSuggested();
                meh[2] = "Reps: " + item.getRepsSuggested();
                meh[3] = "Rest Sets: " + item.getRestSets() + "\n(mm:ss)";
                meh[4] = "Rest Reps: " + item.getRestReps() + "\n(mm:ss)";
                onCreateDialog(meh);
            } else if(FitnessFragment.planType.equals("Cardio")){
                title = "Distance: " + planOnScreen.getText().toString();
                String[] meh = new String[4];
                meh[0] = "Sets: " + item.getSetsSuggested();
                meh[1] = "Reps: " + item.getRepsSuggested();
                meh[2] = "Rest Sets: " + item.getRestSets() + "\n(mm:ss)";
                meh[3] = "Rest Reps: " + item.getRestReps() + "\n(mm:ss)";;
                onCreateDialog(meh);
            } else if(FitnessFragment.planType.equals("Calisthetics")){
                title = "Exercise: " + planOnScreen.getText().toString();
                String[] meh = new String[4];
                meh[0] = "Sets: " + item.getSetsSuggested();
                meh[1] = "Reps: " + item.getRepsSuggested();
                meh[2] = "Rest Sets: " + item.getRestSets() + "\n(mm:ss)";
                meh[3] = "Rest Reps: " + item.getRestReps() + "\n(mm:ss)";;
                onCreateDialog(meh);
            }

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

    private void refreshItemsFromMobileServiceTable() throws MobileServiceException {
        mLinkTable.execute();
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

    public void checkItemInTable(PlanLinkItem item) {
        mLinkTable.update(item);
    }
}
