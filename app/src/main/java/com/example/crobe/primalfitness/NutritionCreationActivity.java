package com.example.crobe.primalfitness;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
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
    private MobileServiceTable<ExerciseItem> mExerciseTable;
    private EditText ingredient, calories, name;
    private List<String[]> array;
    private ServiceHandler sh;
    private String planName, planType;
    private String[] coachingLinks, emailLinks;
    private MobileServiceTable<UserItem> mUserTable;
    private MobileServiceTable<PlanLinkItem> mLinkTable;
    private MobileServiceTable<NutritionItem> mNutritionTable;
    private Dialog myDialog;


    @SuppressLint("SetTextI18n")
    private void callPopup() {

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);

        @SuppressLint("InflateParams") View popupView = layoutInflater.inflate(R.layout.popup_nutrition, null);

        PopupWindow popupWindow = new PopupWindow(popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
                true);

        popupWindow.setTouchable(true);
        popupWindow.setFocusable(true);

        popupView.setBackgroundColor(Color.parseColor("#ffffff"));

        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        ingredient = popupView.findViewById(R.id.ingredientName);
        calories = popupView.findViewById(R.id.calories);


        (popupView.findViewById(R.id.add))
                .setOnClickListener(arg0 -> {
                    if (checkInputs()) {
                        array.add(new String[]{ingredient.getText().toString(), calories.getText().toString()});
                    }

                    popupWindow.dismiss();
                });
        (popupView.findViewById(R.id.cancel))
                .setOnClickListener(arg0 -> popupWindow.dismiss());
    }

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

        Button addExercise = findViewById(R.id.addFood);
        addExercise.setOnClickListener(this);

        try {
            mClient = new MobileServiceClient("https://primalfitnesshonours.azurewebsites.net",this);
            mClient.setAndroidHttpClientFactory(() -> {
                OkHttpClient client = new OkHttpClient();
                client.setReadTimeout(20, TimeUnit.SECONDS);
                client.setWriteTimeout(20, TimeUnit.SECONDS);
                return client;
            });
            mNutritionTable = mClient.getTable(NutritionItem.class);
            mUserTable = mClient.getTable(UserItem.class);
            mLinkTable = mClient.getTable(PlanLinkItem.class);
            initLocalStore().get();
            refreshItemsFromTable();
        } catch (MalformedURLException e) {
            sh.createAndShowDialog(new Exception("There was an error creating the Mobile Service. Verify the URL"), "Error");
        } catch (Exception e) {
            sh.createAndShowDialog(e, "Error");
        }
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
                    if (LoginActivity.loggedInUserType.equals("Coach")) {
                        getCoachLinks();
                    } else {
                        addItemToDiary();
                        addPlan();
                    }
                }
                break;



            case R.id.addFood:
                callPopup();
                break;
        }
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
                    runOnUiThread(() -> {
                        int i = 0;
                        for (UserItem coachLinks : links) {
                            try {
                                coachingLinks[i] = AESCrypt.decrypt(coachLinks.getFirstName()) + " " + AESCrypt.decrypt(coachLinks.getSurname());
                                emailLinks[i] = coachLinks.getEmail();
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

    public void onCreateDialog(String[] names, String[] email) {
        ArrayList<Integer> selectedItems = new ArrayList<>();  // Where we track the selected items
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
        // Set the dialog title
        builder.setTitle("Select Athletes")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(names, null,
                        (dialog, which, isChecked) -> {
                            if (isChecked) {
                                selectedItems.add(which);
                            } else {// if (selectedItems.contains(which)) {
                                // Else, if the item is already in the array, remove it
                                selectedItems.remove(Integer.valueOf(which));
                            }
                        })
                // Set the action buttons
                .setPositiveButton("Link", (dialog, id) -> {
                    // User clicked OK, so save the selectedItems results somewhere
                    // or return them to the component that opened the dialog
                    addItemLinks(selectedItems, names, email);
                    addPlan();
                })
                .setNegativeButton("Make public", (dialog, id) -> {
                    addPlan();
                });

        builder.create().show();
    }

    public void addItemToDiary() {
        if (mClient == null) {
            return;
        }
        PlanLinkItem item = new PlanLinkItem();

        try {
            item.setPlanName(name.getText().toString());
            item.setId(sh.createTransactionID());
            item.setUsername(LoginActivity.loggedInUser);
            item.setComplete(true);
            item.setPlanType(NutritionFragment.planType);
            item.setType("Nutrition");
        } catch (Exception e) {
            e.printStackTrace();
        }
        test(item);
    }

    public void addItemLinks(ArrayList<Integer> links, String[] names, String[] email) {
        if (mClient == null) {
            return;
        }
        PlanLinkItem item;

        for (Object arrad : links) {

            item = new PlanLinkItem();

            try {
                item.setPlanName(name.getText().toString());
                item.setId(sh.createTransactionID());
                item.setUsername(email[(int) arrad]);
                item.setComplete(false);
                item.setPlanType(NutritionFragment.planType);
                item.setType("Nutrition");
            } catch (Exception e) {
                e.printStackTrace();
            }

            test(item);
        }

    }

    private void test(PlanLinkItem item) {

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {


            @Override
            protected Void doInBackground(Void... params) {
                try {
                    addLinkItemInTable(item);
                } catch (final Exception e) {
                    sh.createAndShowDialogFromTask(e);
                }
                return null;
            }
        };
        sh.runAsyncTask(task);
    }

    public void addLinkItemInTable(PlanLinkItem item) {
        mLinkTable.insert(item);
    }

    private void addPlan() {
        for (String[] arra : array) {
            planName = name.getText().toString();
            planType = NutritionFragment.planType;
            addItem(arra);
            Toast.makeText(this, "Plan Added", Toast.LENGTH_LONG).show();
            this.finish();
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
            item.setFoodName(ingredients[0]);
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


