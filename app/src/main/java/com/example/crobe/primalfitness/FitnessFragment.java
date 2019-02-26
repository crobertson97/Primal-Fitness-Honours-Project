package com.example.crobe.primalfitness;


import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FitnessFragment extends Fragment implements View.OnClickListener {


    private TextView newPlan;
    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<ExerciseItem> mPlanTable;
    List<String> plans;

    public FitnessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        View view = inflater.inflate(R.layout.fragment_plan_fitness, container, false);

        newPlan = (TextView) view.findViewById(R.id.createPlan);
        newPlan.setOnClickListener(this);
        layoutPlans = (LinearLayout) view.findViewById(R.id.createdPlans);

        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net", getActivity());

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

            mPlanTable = mClient.getTable(ExerciseItem.class);

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

        if (!LoginActivity.loggedInUserType.equals("Coach")) {
            newPlan.setVisibility(View.INVISIBLE);
            getCreatedPlans();
        } else {
            checkItem();
        }


        return view;
    }

    private void getCreatedPlans() {
        if (mClient == null) {
            return;
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<ExerciseItem> results = mPlanTable.select("planName").execute().get();
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (ExerciseItem item : results) {
                                TextView plan = new TextView(getActivity());
                                plan.setText(item.getPlanName());
                                plan.setTextSize(36);
                                plan.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border));
                                plan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                plan.setTextColor(Color.parseColor("#ff000000"));
                                layoutPlans.addView(plan);
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        runAsyncTask(task);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createPlan:
                startActivity(new Intent(getActivity(), FitnessCreationActivity.class));
                break;
        }
    }

    public void checkItem() {
        if (mClient == null) {
            return;
        }

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<ExerciseItem> results = mPlanTable.where().field("createdBy").eq(LoginActivity.loggedInUser).execute().get();

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (ExerciseItem item : results) {
                                TextView plan = new TextView(getActivity());
                                plan.setText(item.getPlanName());
                                plan.setTextSize(36);
                                plan.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border));
                                plan.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                plan.setTextColor(Color.parseColor("#ff000000"));
                                layoutPlans.addView(plan);
                            }
                        }
                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        runAsyncTask(task);
    }

    public void createAndShowDialogFromTask(final Exception exception, String title) {
        getActivity().runOnUiThread(new Runnable() {
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
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

}
