package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class DiaryFragment extends Fragment implements View.OnClickListener {

    public static String planSchedule;

    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;
    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;
    private String[] plans;

    public DiaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_diary, container, false);

        getActivity().setTitle("Fitness Tracking - Diary");
        sh = new ServiceHandler(getActivity());

        layoutPlans = view.findViewById(R.id.scheduledPlans);

        plans = new String[]{"Calisthetics", "Cardio", "Weights"};

        Button addToDiary = view.findViewById(R.id.addToDiary);
        addToDiary.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", getActivity());

            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mLinkTable = mClient.getTable(PlanLinkItem.class);

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }

        getCreatedPlans();

        return view;
    }

    private void getCreatedPlans() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<PlanLinkItem> links;
                    if(LoginActivity.loggedInUserType.equals("Coach")){
                        links = mLinkTable.where().field("username").eq(HomeFragment.coachingUserLinkEmail).and(mLinkTable.where().field("complete").eq(true)).and(mLinkTable.where().field("type").eq("Fitness")).execute().get();
                    }else {
                        links = mLinkTable.where().field("username").eq(LoginActivity.loggedInUser).and(mLinkTable.where().field("complete").eq(true)).and(mLinkTable.where().field("type").eq("Fitness")).execute().get();
                    }
                    getActivity().runOnUiThread(() -> {
                        for (PlanLinkItem itemLinks : links) {
                            addPlanToScreen(itemLinks);
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

    public void addPlanToScreen(PlanLinkItem item) {
        final TextView planOnScreen = new TextView(getActivity());
        planOnScreen.setText(item.getPlanName());
        planOnScreen.setTextSize(36);
        planOnScreen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border));
        planOnScreen.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
        planOnScreen.setOnClickListener(view -> {
            planSchedule = planOnScreen.getText().toString();
            PlanItemActivity.planView = "Fitness Diary";
            FitnessFragment.planType = item.getPlanType();
            startActivity(new Intent(getActivity(), PlanItemActivity.class));
        });
        layoutPlans.addView(planOnScreen);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.addToDiary:
                onCreateDialog();
                break;
        }
    }


    public void onCreateDialog() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        builder.setTitle("Select Type of Program");
        builder.setSingleChoiceItems(plans, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                FitnessFragment.planType = plans[i];
                startActivity(new Intent(getActivity(), FitnessCreationActivity.class));
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }
}
