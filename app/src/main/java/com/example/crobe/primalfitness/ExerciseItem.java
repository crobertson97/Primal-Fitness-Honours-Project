package com.example.crobe.primalfitness;

public class ExerciseItem {

    //@com.google.gson.annotations.SerializedName("exerciseName")
    @com.google.gson.annotations.SerializedName("id")
    private String mExerciseName;

    @com.google.gson.annotations.SerializedName("exercisePlanType")
    private String mPlanType;

    @com.google.gson.annotations.SerializedName("planName")
    private String mPlanName;

    @com.google.gson.annotations.SerializedName("rest")
    private String mRest;

    @com.google.gson.annotations.SerializedName("setsSuggested")
    private String mSetsSuggested;

    @com.google.gson.annotations.SerializedName("repsSuggested")
    private String mRepsSuggested;

    public ExerciseItem() {

    }

    public ExerciseItem(String mExerciseName, String mPlanType, String mPlanName, String mRest, String mSetsSuggested, String mRepsSuggested) {
        this.setExerciseName(mExerciseName);
        this.setPlanType(mPlanType);
        this.setPlanName(mPlanName);
        this.setRest(mRest);
        this.setSetsSuggested(mSetsSuggested);
        this.setRepsSuggested(mRepsSuggested);
    }

    public String getExerciseName() {
        return mExerciseName;
    }

    public final void setExerciseName(String mExerciseName) {
        this.mExerciseName = mExerciseName;
    }

    public String getPlanType() {
        return mPlanType;
    }

    public final void setPlanType(String mPlanType) {
        this.mPlanType = mPlanType;
    }

    public String getPlanName() {
        return mPlanName;
    }

    public final void setPlanName(String mPlanName) {
        this.mPlanName = mPlanName;
    }

    public String getRest() {
        return mRest;
    }

    public final void setRest(String mRest) {
        this.mRest = mRest;
    }

    public String getSetsSuggested() {
        return mSetsSuggested;
    }

    public final void setSetsSuggested(String mSetsSuggested) {
        this.mSetsSuggested = mSetsSuggested;
    }

    public String getRepsSuggested() {
        return mRepsSuggested;
    }

    public final void setRepsSuggested(String mRepsSuggested) {
        this.mRepsSuggested = mRepsSuggested;
    }


}
