package com.example.crobe.primalfitness;

public class UserItem {

    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    @com.google.gson.annotations.SerializedName("firstName")
    private String mFirstName;

    @com.google.gson.annotations.SerializedName("surname")
    private String mSurname;

    @com.google.gson.annotations.SerializedName("email")
    private String mEmail;

    @com.google.gson.annotations.SerializedName("password")
    private String mPassword;

    @com.google.gson.annotations.SerializedName("profileType")
    private String mProfileType;

    @com.google.gson.annotations.SerializedName("age")
    private String mAge;

    @com.google.gson.annotations.SerializedName("weight")
    private String mWeight;

    @com.google.gson.annotations.SerializedName("height")
    private String mHeight;

    @com.google.gson.annotations.SerializedName("coachLink")
    private String mCoachLink;

    public UserItem() {

    }

    public String getId() {
        return mId;
    }

    public final void setId(String mId) {
        this.mId = mId;
    }

    public String getFirstName() {
        return mFirstName;
    }

    public final void setFirstName(String mFirstName) {
        this.mFirstName = mFirstName;
    }

    public String getSurname() {
        return mSurname;
    }

    public final void setSurname(String mSurname) {
        this.mSurname = mSurname;
    }

    public String getEmail() {
        return mEmail;
    }

    public final void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String getPassword() {
        return mPassword;
    }

    public final void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getProfileType() {
        return mProfileType;
    }

    public final void setProfileType(String mProfileType) {
        this.mProfileType = mProfileType;
    }

    public String getAge() {
        return mAge;
    }

    public final void setAge(String mAge) {
        this.mAge = mAge;
    }

    public String getWeight() {
        return mWeight;
    }

    public final void setWeight(String mWeight) {
        this.mWeight = mWeight;
    }

    public String getHeight() {
        return mHeight;
    }

    public final void setHeight(String mHeight) {
        this.mHeight = mHeight;
    }

    public String getCoachLink() {
        return mCoachLink;
    }

    public final void setCoachLink(String mCoachLink) {
        this.mCoachLink = mCoachLink;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserItem && ((UserItem) o).mEmail == mEmail;
    }


}