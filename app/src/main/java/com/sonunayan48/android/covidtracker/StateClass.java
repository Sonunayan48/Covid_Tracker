package com.sonunayan48.android.covidtracker;

public class StateClass {
    private String mName;
    private String mConfirmed;
    private String mActive;
    private String mRecovered;
    private String mDeath;

    public StateClass(String name, String confirmed, String active,
                      String recovered, String death) {
        mName = name;
        mConfirmed = confirmed;
        mActive = active;
        mRecovered = recovered;
        mDeath = death;
    }

    public String getmName() {
        return mName;
    }

    public String getmConfirmed() {
        return mConfirmed;
    }

    public String getmActive() {
        return mActive;
    }

    public String getmRecovered() {
        return mRecovered;
    }

    public String getmDeath() {
        return mDeath;
    }
}
