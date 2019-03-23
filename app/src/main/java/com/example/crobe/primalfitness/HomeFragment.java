package com.example.crobe.primalfitness;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements View.OnClickListener {

    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private ServiceHandler sh;
    private String [] coachingLinks, emailLinks;
    public static String coachingUserLinkEmail;
    private String buttonType;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Inflate the layout for this fragment

        Button plansButton = view.findViewById(R.id.plans);
        plansButton.setOnClickListener(this);
        Button messageButton = view.findViewById(R.id.messages);
        messageButton.setOnClickListener(this);
        Button nutritionButton = view.findViewById(R.id.nutritionTracking);
        nutritionButton.setOnClickListener(this);
        Button fitnessButton = view.findViewById(R.id.fitnessTracking);
        fitnessButton.setOnClickListener(this);
        sh = new ServiceHandler(getActivity());


        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net", getActivity());

            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mUserTable = mClient.getTable(UserItem.class);

        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
        return view;
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plans:
                startActivity(new Intent(getActivity(), PlansActivity.class));
                break;
            case R.id.fitnessTracking:
                if(LoginActivity.loggedInUserType.equals("Coach")){
                    buttonType = "Fitness";
                    getCoachLinks();
                    break;
                }else{
                    startActivity(new Intent(getActivity(), FitnessActivity.class));
                }
            case R.id.nutritionTracking:
                if(LoginActivity.loggedInUserType.equals("Coach")){
                    buttonType = "Nutrition";
                    getCoachLinks();
                    break;
                }else{
                    startActivity(new Intent(getActivity(), NutritionActivity.class));
                }

            case R.id.messages:
                Toast.makeText(getActivity(), "Yeah! Messages my dude!", Toast.LENGTH_LONG).show();
                break;
        }
    }

    public void onCreateDialog(String [] names, String [] email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select an Athlete")
                .setItems(names, (dialog, which) -> {
                    // The 'which' argument contains the index position
                    // of the selected item
                    coachingUserLinkEmail = email[which];
                    Log.i("TAG",""+ coachingUserLinkEmail);
                    if(buttonType.equals("Fitness")){
                        startActivity(new Intent(getActivity(), FitnessActivity.class));
                    }else{
                        startActivity(new Intent(getActivity(), NutritionActivity.class));
                    }


                });
        builder.setNegativeButton("cancel", (dialog, which) ->
                dialog.dismiss()
        );
        builder.create().show();
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
                    getActivity().runOnUiThread(() -> {
                        int i = 0;
                        for (UserItem coachLinks : links) {
                            try {
                                Log.i("TAG",""+ AESCrypt.decrypt(coachLinks.getFirstName()) + " " + AESCrypt.decrypt(coachLinks.getSurname()));
                                coachingLinks[i] = AESCrypt.decrypt(coachLinks.getFirstName()) + " " + AESCrypt.decrypt(coachLinks.getSurname());
                                emailLinks[i] = coachLinks.getEmail();
                                Log.i("TAG",""+ coachingLinks[i]);
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

}
