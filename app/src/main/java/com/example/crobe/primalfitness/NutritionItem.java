package com.example.crobe.primalfitness;

public class NutritionItem {

    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("createdBy")
    private String mCreatedBy;

    @com.google.gson.annotations.SerializedName("private")
    private Boolean mPrivate;

    @com.google.gson.annotations.SerializedName("foodName")
    private String mFoodName;

    @com.google.gson.annotations.SerializedName("recipeType")
    private String mRecipeType;

    @com.google.gson.annotations.SerializedName("recipeName")
    private String mRecipeName;

    @com.google.gson.annotations.SerializedName("portions")
    private String mPortions;

    @com.google.gson.annotations.SerializedName("calories")
    private String mCalories;

    public NutritionItem() {

    }

    public String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public Boolean getPrivate() {
        return mPrivate;
    }

    public final void setPrivate(Boolean mPrivate) {
        this.mPrivate = mPrivate;
    }

    public String getCreatedBy() {
        return mCreatedBy;
    }

    public final void setCreatedBy(String mCreatedBy) {
        this.mCreatedBy = mCreatedBy;
    }

    public String getFoodName() {
        return mFoodName;
    }

    public final void setFoodName(String mFoodName) {
        this.mFoodName = mFoodName;
    }

    public String getRecipeType() {
        return mRecipeType;
    }

    public final void setRecipeType(String mRecipeType) {
        this.mRecipeType = mRecipeType;
    }

    public String getRecipeName() {
        return mRecipeName;
    }

    public final void setRecipeName(String mRecipeName) {
        this.mRecipeName = mRecipeName;
    }

    public String getPortions() {
        return mPortions;
    }

    public final void setPortions(String mPortions) {
        this.mPortions = mPortions;
    }

    public String getCalories() {
        return mCalories;
    }

    public final void setCalories(String mCalories) {
        this.mCalories = mCalories;
    }
}