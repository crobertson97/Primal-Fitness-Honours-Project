package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
    private EditText exercise, sets, reps, restSets, restReps, name, weight;
    private List<String[]> array;
    private ServiceHandler sh;
    private String planName, planType;
    private boolean planPrivate;
    private String [] coachingLinks, emailLinks;
    private MobileServiceTable<UserItem> mUserTable;
    private MobileServiceTable<PlanLinkItem> mLinkTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_creation);
        sh = new ServiceHandler(this);
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
            mUserTable = mClient.getTable(UserItem.class);
            mLinkTable = mClient.getTable(PlanLinkItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
    }

    @SuppressLint("SetTextI18n")
    private void callPopup() {

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View popupView = layoutInflater.inflate(R.layout.popup, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT,
                true);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);
        popupView.setBackgroundColor(Color.parseColor("#ffffff"));

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        exercise = popupView.findViewById(R.id.exerciseName);
        sets = popupView.findViewById(R.id.suggestedSets);
        reps = popupView.findViewById(R.id.suggestedReps);
        restSets = popupView.findViewById(R.id.suggestedRestSets);
        restReps = popupView.findViewById(R.id.suggestedRestReps);
        weight =  popupView.findViewById(R.id.weightInput);

        TextView exerciseLabel = popupView.findViewById(R.id.exerciseNameLabel);

        if (FitnessFragment.planType.equals("Cardio")) {
            exerciseLabel.setText("Distance");
            weight.setVisibility(View.GONE);
            popupView.findViewById(R.id.weightInputLabel).setVisibility(View.GONE);
        } else if (FitnessFragment.planType.equals("Weights")) {

        }
        (popupView.findViewById(R.id.add))
                .setOnClickListener(arg0 -> {
                    if (checkInputs()) {
                        switch (FitnessFragment.planType) {
                            case "Cardio":
                                array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), restSets.getText().toString(), restReps.getText().toString()});
                                break;
                            case "Weights":
                                array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), restSets.getText().toString(), restReps.getText().toString(), weight.getText().toString()});
                                break;
                        }
                    }

                    popupWindow.dismiss();
                });
        (popupView.findViewById(R.id.cancel))
                .setOnClickListener(arg0 -> popupWindow.dismiss());
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

    public void onCreateDialog(String [] names, String [] email) {
        ArrayList<Integer> selectedItems = new ArrayList<>();  // Where we track the selected items
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Select Athletes")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(names, null,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                //addPlan();
                                selectedItems.add(which);
                            } else{// if (selectedItems.contains(which)) {
                                // Else, if the item is already in the array, remove it
                                selectedItems.remove(Integer.valueOf(which));
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Link", (dialog, id) -> {
                    // User clicked OK, so save the selectedItems results somewhere
                    // or return them to the component that opened the dialog
                    addItemLinks(selectedItems, names, email);
                    planPrivate = true;
                    addPlan();
                })
                .setNegativeButton("Make public", (dialog, id) -> {
                    planPrivate = false;
                    addPlan();
                });

            builder.create().show();
    }

    public void addItemLinks(ArrayList<Integer> links, String [] names, String [] email) {
        if (mClient == null) {
            return;
        }
        PlanLinkItem item;// = new PlanLinkItem();
int i= 0;

        for(Object arrad: links) {

            item = new PlanLinkItem();
            //test(arrad, names, email, item);
            Log.i("TAG", "" + i);
            i++;
            try {
                Log.i("TAG", "" + names[(int) arrad] + " NEW:" + arrad.toString());
                Log.i("TAG", "" + email[(int) arrad] + " NEW:" + arrad.toString());
                item.setPlanName(name.getText().toString());
                item.setId(sh.createTransactionID());
                item.setUsername(email[(int) arrad]);
                item.setComplete(planPrivate);
                item.setPlanType(FitnessFragment.planType);
                item.setType("Fitness");
            } catch (Exception e) {
                e.printStackTrace();
            }

            test(item);
        }
//
//
//            PlanLinkItem finalItem = item;
//            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    addLinkItemInTable(finalItem);
//                } catch (final Exception e) {
//                    sh.createAndShowDialogFromTask(e);
//                }
//                return null;
//            }
//        };
//        sh.runAsyncTask(task);

    }

    private void test(PlanLinkItem item){

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

    public void addLinkItemInTable(PlanLinkItem item) throws ExecutionException, InterruptedException {
        mLinkTable.insert(item).get();
    }

    private void addPlan(){
        for (String[] arra : array) {
            planName = name.getText().toString();
            planType = FitnessFragment.planType;
            addItem(arra);
            Toast.makeText(this, "Plan Added", Toast.LENGTH_LONG).show();
            this.finish();
        }
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
                    getCoachLinks();
                }
                break;

            case R.id.addExercise:
                callPopup();
                break;

        }
    }

    private boolean checkInputs() {
        switch (FitnessFragment.planType) {
            case "Cardio":
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
            case "Weights":
                if(exercise.getText().toString().isEmpty() || exercise.getText().toString().isEmpty() || sets.getText().toString().isEmpty() || reps.getText().toString().isEmpty() || restSets.getText().toString().isEmpty() || restReps.getText().toString().isEmpty()){
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
            default:
                return false;
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
                    tableDefinition.put("weight", ColumnDataType.String);

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
            if(FitnessFragment.planType.equals("Weights")){
                item.setWeight(exercises[5]);
            }
            item.setCreatedBy(LoginActivity.loggedInUser);
            item.setPrivate(planPrivate);
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
