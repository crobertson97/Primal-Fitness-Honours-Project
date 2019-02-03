package com.example.crobe.primalfitness;

public class UserItem {

    @com.google.gson.annotations.SerializedName("firstName")
    private String mFirstName;

    @com.google.gson.annotations.SerializedName("surname")
    private String mSurname;

    @com.google.gson.annotations.SerializedName("id")
    private String mEmail;

    @com.google.gson.annotations.SerializedName("password")
    private String mPassword;

    public UserItem() {

    }

    public UserItem(String mFirstName, String mSurname, String mEmail, String mPassword) {
        this.setFirstName(mFirstName);
        this.setSurname(mSurname);
        this.setEmail(mEmail);
        this.setPassword(mPassword);
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

    public final void setPassword(String mSurname) {
        this.mPassword = mPassword;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserItem && ((UserItem) o).mEmail == mEmail;
    }


}