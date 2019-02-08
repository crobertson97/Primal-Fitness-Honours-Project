package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    public static EditText passwordLogin;
    private Button signIn, register, test;
    private EditText emailAddress;
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private Boolean loggedIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Configuration config = getResources().getConfiguration();

        signIn = (Button) this.findViewById(R.id.signIn);
        signIn.setOnClickListener(this);
        test = (Button) this.findViewById(R.id.test);
        test.setOnClickListener(this);
        register = (Button) this.findViewById(R.id.register);
        register.setOnClickListener(this);
        emailAddress = (EditText) findViewById(R.id.username);
        emailAddress.setText("");
        passwordLogin = (EditText) findViewById(R.id.password);
        passwordLogin.setText("");
        loggedIn = false;

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

            mUserTable = mClient.getTable(UserItem.class);

            initLocalStore().get();

            refreshItemsFromTable();

        } catch (MalformedURLException e) {
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            createAndShowDialog(e, "Error");
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.test:
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

        // Set the item as completed and update it in the table
        //loggedIn = true;

        //final UserItem item = new UserItem();
        // emailAddress.getText().toString().equals(item.getEmail()) &&


        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final List<UserItem> results = mUserTable.where().field("id").eq(emailAddress.getText().toString()).execute().get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (UserItem item : results) {
                                try {
                                    if ((AESCrypt.encrypt(passwordLogin.getText().toString()).equals(item.getPassword()))) {
                                        newActivity(true);
                                        return;
                                    } else {
                                        newActivity(false);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

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

    private void newActivity(Boolean logIn) {
        if (logIn) {
            startActivity(new Intent(this, NavigationActivity.class));
        } else {
            Toast.makeText(this, "Invalid Email/Password.", Toast.LENGTH_SHORT).show();
        }

    }

    private void newActivity() {
        Toast.makeText(this, "Invalid Email/Password.", Toast.LENGTH_SHORT).show();
    }

    public void checkItemInTable(UserItem item) throws ExecutionException, InterruptedException {
        mUserTable.update(item).get();
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
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("password", ColumnDataType.String);
                    tableDefinition.put("profileType", ColumnDataType.String);


                    localStore.defineTable("useritem", tableDefinition);

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

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
            }
        });
    }

    private void refreshItemsFromTable() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserItem> results = refreshItemsFromMobileServiceTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (UserItem item : results) {
                                //mAdapter.add(item);
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

    private List<UserItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException {
        return mUserTable.where().field("complete").
                eq(val(false)).execute().get();
    }

}
