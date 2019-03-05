package com.example.crobe.primalfitness;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;


/**
 * A simple {@link Fragment} subclass.
 */
public class FitnessScheduleFragment extends Fragment {


    private LinearLayout layoutPlans;
    private MobileServiceClient mClient;

    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private ServiceHandler sh;

    public FitnessScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_fitness_schedule, container, false);
//        sh = new ServiceHandler(getActivity());
//
//        layoutPlans = view.findViewById(R.id.scheduledPlans);
//
//        try {
//            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", getActivity());
//
//            mClient.setAndroidHttpClientFactory(() -> {
//                OkHttpClient client = new OkHttpClient();
//                client.setReadTimeout(20, TimeUnit.SECONDS);
//                client.setWriteTimeout(20, TimeUnit.SECONDS);
//                return client;
//            });
//
//            mLinkTable = mClient.getTable(PlanLinkItem.class);
//
//        } catch (MalformedURLException e) {
//            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
//        } catch (Exception e) {
//            sh.createAndShowDialog(e, "Error");
//        }
//
//        getCreatedPlans();


        return view;

    }

//    private void getCreatedPlans() {
//        if (mClient == null) {
//            return;
//        }
//
//        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    final List<PlanLinkItem> links = mLinkTable.where().field("username").eq(LoginActivity.loggedInUser).execute().get();
//                    getActivity().runOnUiThread(() -> {
//                        for (PlanLinkItem itemLinks : links) {
//                            addPlanToScreen(itemLinks);
//                        }
//                    });
//                } catch (final Exception e) {
//                    sh.createAndShowDialogFromTask(e, "Error");
//                }
//                return null;
//            }
//        };
//        sh.runAsyncTask(task);
//    }
//
//    public void addPlanToScreen(PlanLinkItem item) {
//        final TextView planOnScreen = new TextView(getActivity());
//        planOnScreen.setText(item.getPlanName());
//        planOnScreen.setTextSize(36);
//        planOnScreen.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.border));
//        planOnScreen.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
//        planOnScreen.setTextColor(Color.parseColor("#ff000000"));
//        layoutPlans.addView(planOnScreen);
//    }
}
