package com.sonunayan48.android.covidtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class DistrictStateActivity extends AppCompatActivity {
    private TextView mTotal;
    private TextView mActive;
    private TextView mRecovered;
    private TextView mDeath;
    private TextView mAcrossDistrict;
    private StateClass district;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_district_state);
        setup();
        Intent startingIntent = getIntent();
        district = (StateClass) startingIntent.getSerializableExtra("district");
        if (district != null) {
            setData(district);
        }
    }

    private void setData(StateClass district) {
        mTotal.setText(district.getmConfirmed());
        mActive.setText(district.getmActive());
        mRecovered.setText(district.getmRecovered());
        mDeath.setText(district.getmDeath());
        mAcrossDistrict.setText("Across " + district.getmName());
    }

    private void setup() {
        mTotal = findViewById(R.id.total_cases_count);
        mActive = findViewById(R.id.active_cases_count);
        mRecovered = findViewById(R.id.recovered_cases_count);
        mDeath = findViewById(R.id.death_cases_count);
        mAcrossDistrict = findViewById(R.id.across_district);
    }
}
