package com.example.crobe.primalfitness;

public class ExerciseItem {

    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("createdBy")
    private String mCreatedBy;

    @com.google.gson.annotations.SerializedName("private")
    private Boolean mPrivate;

    @com.google.gson.annotations.SerializedName("exerciseName")
    private String mExerciseName;

    @com.google.gson.annotations.SerializedName("exercisePlanType")
    private String mPlanType;

    @com.google.gson.annotations.SerializedName("planName")
    private String mPlanName;

    @com.google.gson.annotations.SerializedName("restSets")
    private String mRestSets;

    @com.google.gson.annotations.SerializedName("restReps")
    private String mRestReps;

    @com.google.gson.annotations.SerializedName("setsSuggested")
    private String mSetsSuggested;

    @com.google.gson.annotations.SerializedName("repsSuggested")
    private String mRepsSuggested;

    public ExerciseItem() {

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

    public String getRestSets() {
        return mRestSets;
    }

    public final void setRestSets(String mRestSets) {
        this.mRestSets = mRestSets;
    }

    public String getRestReps() {
        return mRestReps;
    }

    public final void setRestReps(String mRestReps) {
        this.mRestReps = mRestReps;
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