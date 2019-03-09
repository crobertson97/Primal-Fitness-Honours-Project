package com.example.crobe.primalfitness;

public class PlanLinkItem {

    @com.google.gson.annotations.SerializedName("id")
    private String id;

    @com.google.gson.annotations.SerializedName("username")
    private String mUsername;

    @com.google.gson.annotations.SerializedName("planName")
    private String mPlanName;

    @com.google.gson.annotations.SerializedName("planType")
    private String mPlanType;

    @com.google.gson.annotations.SerializedName("complete")
    private Boolean mComplete;

    @com.google.gson.annotations.SerializedName("type")
    private String mType;


    public PlanLinkItem() {

    }

    public String getId() {
        return id;
    }

    public final void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return id;
    }

    public final void setUsername(String mUsername) {
        this.mUsername = mUsername;
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

    public Boolean getComplete() {
        return mComplete;
    }

    public final void setComplete(Boolean mComplete) {
        this.mComplete = mComplete;
    }

    public String getType() {
        return mType;
    }

    public final void setType(String mType) {
        this.mType = mType;
    }

}