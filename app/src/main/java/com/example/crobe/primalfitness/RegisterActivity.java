package com.example.crobe.primalfitness;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText firstNameInput, surnameInput, emailAddressInput, passwordInput, coachInput, ageInput, weightInput, heightInput;
    private TextView coachLabel, ageLabel, weightLabel, heightLabel;
    private Spinner type;
    private Button submit;
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private boolean completeRegistration;


    private static boolean inputValid(String email) {
        if ((!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())) {
            return true;
        } else {
            return false;
        }

    }

    private static boolean checkString(String str) {
        char ch;
        boolean capitalFlag = false;
        boolean lowerCaseFlag = false;
        boolean numberFlag = false;
        if (str.length() < 6) {
            return false;
        } else {
            for (int i = 0; i < str.length(); i++) {
                ch = str.charAt(i);
                if (Character.isDigit(ch)) {
                    numberFlag = true;
                } else if (Character.isUpperCase(ch)) {
                    capitalFlag = true;
                } else if (Character.isLowerCase(ch)) {
                    lowerCaseFlag = true;
                }
                if (numberFlag && capitalFlag && lowerCaseFlag)
                    return true;
            }
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Spinner spinner = (Spinner) findViewById(R.id.userTypeChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        firstNameInput = (EditText) findViewById(R.id.firstName);
        surnameInput = (EditText) findViewById(R.id.surname);
        emailAddressInput = (EditText) findViewById(R.id.emailAddress);
        passwordInput = (EditText) findViewById(R.id.password);
        coachInput = (EditText) findViewById(R.id.coachLink);
        ageInput = (EditText) findViewById(R.id.age);
        weightInput = (EditText) findViewById(R.id.weight);
        heightInput = (EditText) findViewById(R.id.height);

        coachLabel = (TextView) findViewById(R.id.coachIDLabel);
        ageLabel = (TextView) findViewById(R.id.ageLabel);
        weightLabel = (TextView) findViewById(R.id.weightLabel);
        heightLabel = (TextView) findViewById(R.id.heightLabel);

        type = (Spinner) findViewById(R.id.userTypeChoice);
        type.setOnItemSelectedListener(this);

        submit = (Button) this.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",
                    this);
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
            createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error at 106");
        } catch (Exception e) {
            createAndShowDialog(e, "Error at 108");
        }

    }

    @Override
    public void onClick(View v) {
        if (!inputValid(emailAddressInput.getText().toString()) && !checkString(passwordInput.getText().toString())) {
            Toast.makeText(this, "Invalid Email & Password", Toast.LENGTH_SHORT).show();
        } else if (!checkString(passwordInput.getText().toString())) {
            Toast.makeText(this, "Invalid Password", Toast.LENGTH_SHORT).show();
        } else if (!inputValid(emailAddressInput.getText().toString())) {
            Toast.makeText(this, "Invalid Email", Toast.LENGTH_SHORT).show();
        } else {
            addItem();
            if (completeRegistration) {
                Toast.makeText(this, "Registered", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
            }

        }
    }

    public void addItem() {
        if (mClient == null) {
            return;
        }

        // Create a new item
        final UserItem item = new UserItem();

        try {

            item.setFirstName(AESCrypt.encrypt(firstNameInput.getText().toString()));
            item.setSurname(AESCrypt.encrypt(surnameInput.getText().toString()));
            item.setEmail(emailAddressInput.getText().toString());
            item.setPassword(AESCrypt.encrypt(passwordInput.getText().toString()));
            item.setProfileType(type.getSelectedItem().toString());
            item.setLoggedIn(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final UserItem entity = addItemInTable(item);
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error at 203");
                }
                return null;
            }
        };

        runAsyncTask(task);
        completeRegistration = true;
    }

    private AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
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

    public UserItem addItemInTable(UserItem item) throws ExecutionException, InterruptedException {
        UserItem entity = mUserTable.insert(item).get();
        return entity;
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
                    tableDefinition.put("profileType", ColumnDataType.String);
                    tableDefinition.put("loggedIn", ColumnDataType.String);

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

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserItem> results = refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    createAndShowDialogFromTask(e, "Error at 314");
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

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        if (parent.getItemAtPosition(pos).toString().equals("Athlete")) {
            coachLabel.setVisibility(View.VISIBLE);
            coachInput.setVisibility(View.VISIBLE);
            ageLabel.setVisibility(View.VISIBLE);
            ageInput.setVisibility(View.VISIBLE);
            weightLabel.setVisibility(View.VISIBLE);
            weightInput.setVisibility(View.VISIBLE);
            heightLabel.setVisibility(View.VISIBLE);
            heightInput.setVisibility(View.VISIBLE);
        } else if (parent.getItemAtPosition(pos).toString().equals("Coach")) {
            coachLabel.setVisibility(View.GONE);
            coachInput.setVisibility(View.GONE);
            ageLabel.setVisibility(View.GONE);
            ageInput.setVisibility(View.GONE);
            weightLabel.setVisibility(View.GONE);
            weightInput.setVisibility(View.GONE);
            heightLabel.setVisibility(View.GONE);
            heightInput.setVisibility(View.GONE);
        } else {
            coachLabel.setVisibility(View.GONE);
            coachInput.setVisibility(View.GONE);
            ageLabel.setVisibility(View.VISIBLE);
            ageInput.setVisibility(View.VISIBLE);
            weightLabel.setVisibility(View.VISIBLE);
            weightInput.setVisibility(View.VISIBLE);
            heightLabel.setVisibility(View.VISIBLE);
            heightInput.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}