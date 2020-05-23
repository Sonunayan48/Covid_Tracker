package com.sonunayan48.android.covidtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sonunayan48.android.covidtracker.Network.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class StateDetailWithDistrictActivity extends AppCompatActivity {

    private static final String DISTRICT_URL = "https://api.covid19india.org/state_district_wise.json";
    private TextView mTotal;
    private TextView mActive;
    private TextView mRecovered;
    private TextView mDeath;
    private TextView mAcrossState;
    private ProgressBar mProgressBar;
    private static ArrayList<StateClass> districtList;
    StateClass state;
    private RecyclerView districtView;
    private StateListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state_detail_with_district);
        setup();
        Intent startingIntent = getIntent();
        state = (StateClass) startingIntent.getSerializableExtra("state");
        if (state != null) {
            setData(state);
        }
        startNetworkCall();
    }

    private void setup(){
        mTotal = findViewById(R.id.total_cases_count);
        mActive = findViewById(R.id.active_cases_count);
        mRecovered = findViewById(R.id.recovered_cases_count);
        mDeath = findViewById(R.id.death_cases_count);
        mAcrossState = findViewById(R.id.across_state);
        mProgressBar = findViewById(R.id.progress_horizontal);
        districtList = new ArrayList<>();
        districtView = findViewById(R.id.district_list);
    }

    private void setData(StateClass state){
        mTotal.setText(state.getmConfirmed());
        mActive.setText(state.getmActive());
        mRecovered.setText(state.getmRecovered());
        mDeath.setText(state.getmDeath());
        mAcrossState.setText("Across " + state.getmName());
    }

    private void startNetworkCall(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            new GetDistrictData().execute();
        } else {
            createNetworkErrorDialog();
        }
    }

    private void createNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please connect to Internet and retry.");
        builder.setTitle("No Internet Connection!");
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_warning_black_24dp);
        builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startNetworkCall();
            }
        });
        builder.setNegativeButton("Exit App", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        AlertDialog alertDialog = builder.show();
        alertDialog.show();
    }

    private class GetDistrictData extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils networkUtils = new NetworkUtils();
            String jsonStr = networkUtils.makeRequestCall(DISTRICT_URL);
            if (jsonStr != null){
                try{
                    JSONObject parentObj = new JSONObject(jsonStr);
                    JSONObject stateObject = parentObj.getJSONObject(state.getmName());
                    JSONObject districtObject = stateObject.getJSONObject("districtData");
                    Iterator e = districtObject.keys();
                    while (e.hasNext()){
                        String districtName = (String) e.next();
                        if (!districtName.equals("Unknown")){
                            JSONObject obj = districtObject.getJSONObject(districtName);
                            String confirmed = obj.getString("confirmed");
                            String active = obj.getString("active");
                            String recovered = obj.getString("recovered");
                            String death = obj.getString("deceased");
                            StateClass dist = new StateClass(districtName, confirmed, active, recovered, death);
                            districtList.add(dist);
                        }
                    }
                    Log.d("TAG", "debug");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
            districtList.sort(new Comparator<StateClass>() {
                @Override
                public int compare(StateClass o1, StateClass o2) {
                    return Integer.parseInt(o2.getmActive()) - Integer.parseInt(o1.getmActive());
                }
            });
            LinearLayoutManager manager = new LinearLayoutManager(StateDetailWithDistrictActivity.this);
            districtView.setLayoutManager(manager);
            adapter = new StateListAdapter(districtList, new StateListAdapter.ListItemClickListner() {
                @Override
                public void onListClick(int itemIndex) {
                    Intent intent = new Intent(getApplicationContext(), DistrictStateActivity.class);
                    StateClass clickedDistrict = districtList.get(itemIndex);
                    intent.putExtra("district", clickedDistrict);
                    startActivity(intent);
                }
            }, R.anim.fade_transition_animation);
            districtView.setAdapter(adapter);

        }
    }
}
