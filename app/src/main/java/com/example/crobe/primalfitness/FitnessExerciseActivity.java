package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class FitnessExerciseActivity extends AppCompatActivity implements View.OnClickListener {

    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mPlanTable;
    private ServiceHandler sh;
    private String exercise;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        sh = new ServiceHandler(this);

        layoutPlans = findViewById(R.id.planExercises);

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", this);

            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mPlanTable = mClient.getTable(ExerciseItem.class);

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
        getCreatedPlans();
    }

    @Override
    public void onClick(View view) {

    }

    private void getCreatedPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<ExerciseItem> results = mPlanTable.where().field("planName").eq(ScheduleFragment.planSchedule).execute().get();
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

}
