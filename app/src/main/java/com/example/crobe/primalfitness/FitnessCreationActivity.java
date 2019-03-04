package com.example.crobe.primalfitness;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class FitnessCreationActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mExerciseTable;
    private Dialog myDialog;
    private Button submitExercise, createPlan;
    private Spinner type;
    private EditText exercise, sets, reps, rest, name;
    private List<String[]> array;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fitness_creation);
        myDialog = new Dialog(this);
        array = new ArrayList<String[]>();
        name = (EditText) findViewById(R.id.planName);

        createPlan = (Button) findViewById(R.id.createPlan);
        createPlan.setOnClickListener(this);

        Spinner spinner = (Spinner) findViewById(R.id.planType);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.plan_type_array_fitness, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        type = (Spinner) findViewById(R.id.planType);
        type.setOnItemSelectedListener(this);


        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",
                    this);

            // Extend timeout from default of 10s to 20s
            mClient.setAndroidHttpClientFactory(new OkHttpClientFactory() {
                @Override
                public OkHttpClient createOkHttpClient() {
                    OkHttpClient client = new OkHttpClient();
                    client.setReadTimeout(20, TimeUnit.SECONDS);
                    client.setWriteTimeout(20, TimeUnit.SECONDS);
                    return client;
                }
            });
            mExerciseTable = mClient.getTable(ExerciseItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.pop_fitness);
        submitExercise = (Button) myDialog.findViewById(R.id.submitExercise);
        submitExercise.setOnClickListener(this);
        exercise = (EditText) myDialog.findViewById(R.id.exerciseName);
        sets = (EditText) myDialog.findViewById(R.id.suggestedSets);
        reps = (EditText) myDialog.findViewById(R.id.suggestedReps);
        rest = (EditText) myDialog.findViewById(R.id.suggestedRest);
        myDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.createPlan:
                if (name.getText().toString().isEmpty() || type.getSelectedItem().toString().isEmpty() || array.isEmpty()) {
                    Toast.makeText(this, "Please enter a name and type", Toast.LENGTH_LONG).show();
                } else {
                    for (String[] arra : array) {
                        addItem(arra);
                    }
                }
                break;

            case R.id.submitExercise:
                if(checkInputs()){
                    array.add(new String[]{exercise.getText().toString(), sets.getText().toString(), reps.getText().toString(), rest.getText().toString()});
                    myDialog.dismiss();
                }
                break;
        }
    }

    private boolean checkInputs() {
        if(exercise.getText().toString().isEmpty() || sets.getText().toString().isEmpty() || reps.getText().toString().isEmpty() || rest.getText().toString().isEmpty()){
            Toast.makeText(this, "Please enter values into all fields", Toast.LENGTH_LONG).show();
            return false;
        }else {
            LinearLayout linearLayout = (LinearLayout) findViewById(R.id.exercisesLayout);
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

    public void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error at 229");
            }
        });
    }

    public void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    public void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("planName", ColumnDataType.String);
                    tableDefinition.put("exercisePlanType", ColumnDataType.String);
                    tableDefinition.put("exerciseName", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("setsSuggested", ColumnDataType.String);
                    tableDefinition.put("repsSuggested", ColumnDataType.String);
                    tableDefinition.put("rest", ColumnDataType.String);
                    tableDefinition.put("createdBy", ColumnDataType.String);

                    localStore.defineTable("exerciseitem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();


                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error at 278");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<ExerciseItem> results = refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error at 314");
                }
                return null;
            }
        };

        runAsyncTask(task);
    }

    private List<ExerciseItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mExerciseTable.where().field("complete").
                eq(val(false)).execute().get();
    }

    public void addItem(String[] exercises) {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final ExerciseItem item = new ExerciseItem();
        try {
            item.setPlanName(name.getText().toString());
            item.setPlanType(type.getSelectedItem().toString());
            item.setId(createTransactionID());
            item.setExerciseName(exercises[0]);
            item.setSetsSuggested(exercises[1]);
            item.setRepsSuggested(exercises[2]);
            item.setRest(exercises[3]);
            item.setCreatedBy(LoginActivity.loggedInUser);
            item.setPrivate(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final ExerciseItem entity = addItemInTable(item);
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error at 203");
                }
                return null;
            }
        };
        runAsyncTask(task);
    }

    public ExerciseItem addItemInTable(ExerciseItem item) throws ExecutionException, InterruptedException {
        ExerciseItem entity = mExerciseTable.insert(item).get();
        return entity;
    }

    public String createTransactionID() throws Exception {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
