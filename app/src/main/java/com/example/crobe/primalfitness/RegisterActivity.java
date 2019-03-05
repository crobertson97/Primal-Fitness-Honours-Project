package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
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
import com.microsoft.windowsazure.mobileservices.MobileServiceException;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


public class RegisterActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private EditText firstNameInput, surnameInput, emailAddressInput, passwordInput, coachInput, ageInput, weightInput, heightInput;
    private TextView coachLabel, ageLabel, weightLabel, heightLabel;
    private Spinner spinner;
    private MobileServiceClient mClient;
    private MobileServiceTable<UserItem> mUserTable;
    private boolean completeRegistration;
    private ServiceHandler sh;


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
        sh = new ServiceHandler(this);

        spinner = findViewById(R.id.userTypeChoice);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.user_type_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        firstNameInput = findViewById(R.id.firstName);
        surnameInput = findViewById(R.id.surname);
        emailAddressInput = findViewById(R.id.emailAddress);
        passwordInput = findViewById(R.id.password);
        coachInput = findViewById(R.id.coachLink);
        ageInput = findViewById(R.id.age);
        weightInput = findViewById(R.id.weight);
        heightInput = findViewById(R.id.height);

        coachLabel = findViewById(R.id.coachIDLabel);
        ageLabel = findViewById(R.id.ageLabel);
        weightLabel = findViewById(R.id.weightLabel);
        heightLabel = findViewById(R.id.heightLabel);

        spinner.setOnItemSelectedListener(this);

        Button submit = this.findViewById(R.id.submit);
        submit.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient(
                    "https://primalfitnesshonours.azurewebsites.net",
                    this);
            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });

            mUserTable = mClient.getTable(UserItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
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
            item.setId(sh.createTransactionID());
            item.setFirstName(AESCrypt.encrypt(firstNameInput.getText().toString()));
            item.setSurname(AESCrypt.encrypt(surnameInput.getText().toString()));
            item.setEmail(AESCrypt.encrypt(emailAddressInput.getText().toString()));
            item.setPassword(AESCrypt.encrypt(passwordInput.getText().toString()));
            item.setProfileType(spinner.getSelectedItem().toString());
            if (spinner.getSelectedItem().toString().equals("Standard")) {
                item.setAge(AESCrypt.encrypt(ageInput.getText().toString()));
                item.setHeight(AESCrypt.encrypt(heightInput.getText().toString()));
                item.setWeight(AESCrypt.encrypt(weightInput.getText().toString()));
            } else if (spinner.getSelectedItem().toString().equals("Athlete")) {
                item.setAge(AESCrypt.encrypt(ageInput.getText().toString()));
                item.setHeight(AESCrypt.encrypt(heightInput.getText().toString()));
                item.setWeight(AESCrypt.encrypt(weightInput.getText().toString()));
                item.setCoachLink(AESCrypt.encrypt(coachInput.getText().toString()));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    final UserItem entity = addItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
        completeRegistration = true;
    }

    public UserItem addItemInTable(UserItem item) throws ExecutionException, InterruptedException {
        return mUserTable.insert(item).get();
    }

    private AsyncTask<Void, Void, Void> initLocalStore() {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    Map<String, ColumnDataType> tableDefinition = new HashMap<>();
                    tableDefinition.put("firstName", ColumnDataType.String);
                    tableDefinition.put("surname", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("email", ColumnDataType.String);
                    tableDefinition.put("password", ColumnDataType.String);
                    tableDefinition.put("profileType", ColumnDataType.String);
                    tableDefinition.put("age", ColumnDataType.String);
                    tableDefinition.put("weight", ColumnDataType.String);
                    tableDefinition.put("height", ColumnDataType.String);

                    localStore.defineTable("useritem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();


                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }

                return null;
            }
        };

        return sh.runAsyncTask(task);
    }

    private void refreshItemsFromTable() {

        // Get the items that weren't marked as completed and add them in the
        // adapter

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserItem> results = refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e, "Error");
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
    }

    private List<UserItem> refreshItemsFromMobileServiceTable() throws ExecutionException, InterruptedException, MobileServiceException {
        return mUserTable.execute().get();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (parent.getItemAtPosition(pos).toString()) {
            case "Athlete":
                coachLabel.setVisibility(View.VISIBLE);
                coachInput.setVisibility(View.VISIBLE);
                ageLabel.setVisibility(View.VISIBLE);
                ageInput.setVisibility(View.VISIBLE);
                weightLabel.setVisibility(View.VISIBLE);
                weightInput.setVisibility(View.VISIBLE);
                heightLabel.setVisibility(View.VISIBLE);
                heightInput.setVisibility(View.VISIBLE);
                break;
            case "Coach":
                coachLabel.setVisibility(View.GONE);
                coachInput.setVisibility(View.GONE);
                ageLabel.setVisibility(View.GONE);
                ageInput.setVisibility(View.GONE);
                weightLabel.setVisibility(View.GONE);
                weightInput.setVisibility(View.GONE);
                heightLabel.setVisibility(View.GONE);
                heightInput.setVisibility(View.GONE);
                break;
            default:
                coachLabel.setVisibility(View.GONE);
                coachInput.setVisibility(View.GONE);
                ageLabel.setVisibility(View.VISIBLE);
                ageInput.setVisibility(View.VISIBLE);
                weightLabel.setVisibility(View.VISIBLE);
                weightInput.setVisibility(View.VISIBLE);
                heightLabel.setVisibility(View.VISIBLE);
                heightInput.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

}