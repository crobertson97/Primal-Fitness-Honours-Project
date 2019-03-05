package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static String loggedInUser, loggedInUserType;
    public EditText passwordLogin, emailAddress;
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private ServiceHandler sh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        sh = new ServiceHandler(this);

        Button signIn = this.findViewById(R.id.signIn);
        signIn.setOnClickListener(this);
        Button testCoach = this.findViewById(R.id.testCoach);
        testCoach.setOnClickListener(this);
        Button testAthlete = this.findViewById(R.id.testAthlete);
        testAthlete.setOnClickListener(this);
        Button testStandard = this.findViewById(R.id.testStandard);
        testStandard.setOnClickListener(this);
        Button register = this.findViewById(R.id.register);
        register.setOnClickListener(this);
        emailAddress = findViewById(R.id.username);
        emailAddress.setText("");
        passwordLogin = findViewById(R.id.password);
        passwordLogin.setText("");

        try {
            // Create the Mobile Service Client instance, using the provided

            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",
                    this);

            // Extend timeout from default of 10s to 20s
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
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.testCoach:
                loggedInUserType = "Coach";
                startActivity(new Intent(this, NavigationActivity.class));
                break;

            case R.id.testAthlete:
                loggedInUserType = "Athlete";
                startActivity(new Intent(this, NavigationActivity.class));
                break;

            case R.id.testStandard:
                loggedInUserType = "Standard";
                startActivity(new Intent(this, NavigationActivity.class));
                break;

            case R.id.register:
                startActivity(new Intent(this, RegisterActivity.class));
                break;

            case R.id.signIn:
                checkItem();
                break;
        }
    }

    public void checkItem() {
        if (mClient == null) {
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    final List<UserItem> results = mUserTable.where().field("email").eq(AESCrypt.encrypt(emailAddress.getText().toString())).execute().get();
                    runOnUiThread(() -> {
                        Boolean number = false;
                        for (UserItem item : results) {
                            try {
                                if (item.getPassword().equals(AESCrypt.encrypt(passwordLogin.getText().toString()))) {
                                    number = true;
                                    loggedInUser = item.getEmail();
                                    loggedInUserType = item.getProfileType();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        newActivity(number);
                    });
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    private void newActivity(Boolean logIn) {
        if (logIn) {
            startActivity(new Intent(this, NavigationActivity.class));
        } else {
            Toast.makeText(this, "Invalid Email/Password.", Toast.LENGTH_SHORT).show();
        }

    }
}
