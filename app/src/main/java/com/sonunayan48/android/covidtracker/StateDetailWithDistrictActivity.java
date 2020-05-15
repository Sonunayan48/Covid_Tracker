package com.sonunayan48.android.covidtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class StateDetailWithDistrictActivity extends AppCompatActivity {

    private TextView mTotal;
    private TextView mActive;
    private TextView mRecovered;
    private TextView mDeath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_detail_with_district);
        setup();
        Intent startingIntent = getIntent();
        StateClass state = (StateClass) startingIntent.getSerializableExtra("state");
        if (state != null){
            setData(state);
        }
    }

    private void setup(){
        mTotal = findViewById(R.id.total_cases_count);
        mActive = findViewById(R.id.active_cases_count);
        mRecovered = findViewById(R.id.recovered_cases_count);
        mDeath = findViewById(R.id.death_cases_count);
    }

    private void setData(StateClass state){
        mTotal.setText(state.getmConfirmed());
        mActive.setText(state.getmActive());
        mRecovered.setText(state.getmRecovered());
        mDeath.setText(state.getmDeath());
    }
}
