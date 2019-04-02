package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;
import com.squareup.okhttp.OkHttpClient;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NutritionCreationActivity extends AppCompatActivity implements View.OnClickListener {

    private MobileServiceClient mClient;
    private MobileServiceTable<NutritionItem> mNutritionTable;
    private Dialog myDialog;
    private EditText ingredient, calories, name;
    private List<String[]> array;
    private ServiceHandler sh;
    private String planName, planType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nutrition_creation);
        sh = new ServiceHandler(this);
        myDialog = new Dialog(this);
        array = new ArrayList<>();
        name = findViewById(R.id.recipeName);

        Button createPlan = findViewById(R.id.createRecipe);
        createPlan.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net",this);
            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });
            mNutritionTable = mClient.getTable(NutritionItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
    }

    public void ShowPopup(View v) {
        myDialog.setContentView(R.layout.pop_nutrition);
        Button submitRecipe = myDialog.findViewById(R.id.submitRecipe);
        submitRecipe.setOnClickListener(this);
        ingredient = myDialog.findViewById(R.id.ingredientName);
        calories = myDialog.findViewById(R.id.calories);
        myDialog.show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.createRecipe:
                if (name.getText().toString().isEmpty()) {
                    Toast.makeText(this, "Please enter a name and type", Toast.LENGTH_LONG).show();
                } else if (array.isEmpty()) {
                    Toast.makeText(this, "Please add ingredients", Toast.LENGTH_LONG).show();
                } else {
                    for (String[] arra : array) {
                        planName = name.getText().toString();
                        planType = NutritionFragment.planType;
                        addItem(arra);
                        Toast.makeText(this, "Recipe Added", Toast.LENGTH_LONG).show();
                        this.finish();
                    }
                }
                break;

            case R.id.submitRecipe:
                if (checkInputs()) {
                    array.add(new String[]{ingredient.getText().toString(), calories.getText().toString()});
                    myDialog.dismiss();
                }
                break;
        }
    }

    private boolean checkInputs() {
        if (ingredient.getText().toString().isEmpty() || calories.getText().toString().isEmpty()) {
            Toast.makeText(this, "Please enter values into all fields", Toast.LENGTH_LONG).show();
            return false;
        } else {
            LinearLayout linearLayout = findViewById(R.id.ingredientsLayout);
            TextView newIngredient = new TextView(this);
            newIngredient.setText(ingredient.getText().toString());
            newIngredient.setTextSize(24);
            newIngredient.setBackground(ContextCompat.getDrawable(this, R.drawable.border));
            newIngredient.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            newIngredient.setTextColor(Color.parseColor("#ff000000"));
            linearLayout.addView(newIngredient);
            return true;
        }
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
                    tableDefinition.put("foodName", ColumnDataType.String);
                    tableDefinition.put("recipeType", ColumnDataType.String);
                    tableDefinition.put("recipeName", ColumnDataType.String);
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("portions", ColumnDataType.String);
                    tableDefinition.put("calories", ColumnDataType.String);
                    tableDefinition.put("rest", ColumnDataType.String);
                    tableDefinition.put("private", ColumnDataType.String);

                    localStore.defineTable("nutritionitem", tableDefinition);

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();


                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }

                return null;
            }
        };

        return sh.runAsyncTask(task);
    }

    private void refreshItemsFromTable() {
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    refreshItemsFromMobileServiceTable();
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };

        sh.runAsyncTask(task);
    }

    private void refreshItemsFromMobileServiceTable() {
        mNutritionTable.where().execute();
    }

    public void addItem(String[] ingredients) {
        if (mClient == null) {
            return;
        }

        final NutritionItem item = new NutritionItem();
        try {
            item.setRecipeName(planName);
            item.setRecipeType(planType);
            item.setId(sh.createTransactionID());
            item.setCalories(ingredients[1]);
            item.setCreatedBy(LoginActivity.loggedInUser);

            item.setPrivate(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the new item
        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    addItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public void addItemInTable(NutritionItem item) {
        mNutritionTable.insert(item);
    }
}

