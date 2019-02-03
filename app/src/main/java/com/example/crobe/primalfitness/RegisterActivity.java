package com.example.crobe.primalfitness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.concurrent.ExecutionException;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.OkHttpClientFactory;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.*;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText firstName, surname, emailAddress, password;
    private Spinner type;
    private Button submit;
    private ProgressBar mProgressBar;
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private RegisterAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Spinner spinner = (Spinner) findViewById(R.id.userTypeChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        firstName = (EditText)findViewById(R.id.firstName);
        surname = (EditText)findViewById(R.id.surname);
        emailAddress = (EditText)findViewById(R.id.emailAddress);
        password = (EditText)findViewById(R.id.password);
        type =(Spinner) findViewById(R.id.userTypeChoice);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);
        mProgressBar.setVisibility(ProgressBar.GONE);

        submit = (Button) this.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",
                    this).withFilter(new ProgressFilter());
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
        String firstName_str = firstName.getText().toString();
        String surname_str = surname.getText().toString();
        String emailAddress_str = emailAddress.getText().toString();
        String password_str = password.getText().toString();
        String type_str = type.getSelectedItem().toString();
        if (!inputValid(emailAddress_str) && !checkString(password_str)){
            Toast.makeText(this, "Invalid Email & Password", Toast.LENGTH_SHORT).show();
        } else if(!checkString(password_str)){
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        } else if(!inputValid(emailAddress_str)){
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
        } else{
            addItem();
            Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
            //startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private static boolean inputValid(String email){
        if((!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())){
            return true;
        }else{
            return false;
        }

    }

    private static boolean checkString(String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        if(str.length() < 6){
            return false;
        }else{
            for(int i=0;i < str.length();i++) {
                ch = str.charAt(i);
                if( Character.isDigit(ch)) {
                    numberFlag = true;
                }
                else if (Character.isUpperCase(ch)) {
                    capitalFlag = true;
                } else if (Character.isLowerCase(ch)) {
                    lowerCaseFlag = true;
                }
                if(numberFlag && capitalFlag && lowerCaseFlag)
                    return true;
            }
        }
        return false;
    }

    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final UserItem item = new UserItem();
        String password_str = password.getText().toString();

        item.setFirstName(firstName.getText().toString());
        item.setSurname(surname.getText().toString());
        item.setEmail(emailAddress.getText().toString());
        item.setPassword(password.getText().toString());
        try {
            item.setPassword(AESCrypt.encrypt(password.getText().toString()));
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final UserItem entity = addItemInTable(item);

//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if(!entity.isComplete()){
//                                mAdapter.add(entity);
//                            }
//                        }
//                    });
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        runAsyncTask(task);

    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
    }

    public UserItem addItemInTable(UserItem item) throws ExecutionException, InterruptedException {
        UserItem entity = mUserTable.insert(item).get();
        return entity;
    }

    private void createAndShowDialogFromTask(final Exception exception, String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(exception, "Error");
                return;
            }
        });
    }

    private void createAndShowDialog(Exception exception, String title) {
        Throwable ex = exception;
        if (exception.getCause() != null) {
            ex = exception.getCause();
        }
        createAndShowDialog(ex.getMessage(), title);
        return;
    }

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
        return;
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
                    tableDefinition.put("firstName", ColumnDataType.String);
                    tableDefinition.put("surname", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("password", ColumnDataType.String);

                    localStore.defineTable("UserItem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();


                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return runAsyncTask(task);
    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserItem> results = refreshItemsFromMobileServiceTable();

                    //Offline Sync
                    //final List<ToDoItem> results = refreshItemsFromMobileServiceTableSyncTable();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //mAdapter.clear();

                            for (UserItem item : results) {
                                mAdapter.add(item);
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

    private class ProgressFilter implements ServiceFilter {

        @Override
        public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

            final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.VISIBLE);
                }
            });

            ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

            Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
                @Override
                public void onFailure(Throwable e) {
                    resultFuture.setException(e);
                }

                @Override
                public void onSuccess(ServiceFilterResponse response) {
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            if (mProgressBar != null) mProgressBar.setVisibility(ProgressBar.GONE);
                        }
                    });

                    resultFuture.set(response);
                }
            });

            return resultFuture;
        }
    }
}