package com.sonunayan48.android.covidtracker;

import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sonunayan48.android.covidtracker.Network.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;

public class StateActivity extends AppCompatActivity {
    private static final String URLSTRING = "https://covid-19india-api.herokuapp.com/v2.0/state_data";
    private RecyclerView stateListRecycler;
    private StateListAdapter adapter;
    public static ArrayList<StateClass> stateList;
    private ProgressBar progressBar;
    private TextView mConnectionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        stateListRecycler = findViewById(R.id.state_list);
        progressBar = findViewById(R.id.progress_circular);
        mConnectionText = findViewById(R.id.connection_text);
        stateList = new ArrayList<>();
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()){
            new GetStateResults().execute();
        }
        else {
            mConnectionText.setVisibility(View.VISIBLE);
        }


    }

    private class GetStateResults extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils networkUtils = new NetworkUtils();
            String jsonStr = networkUtils.makeRequestCall(URLSTRING);
            JSONArray array;

            if (jsonStr != null) {
                try {
                    array = new JSONArray(jsonStr);
                    JSONObject parentObject = array.getJSONObject(1);
                    JSONArray stateArray = parentObject.getJSONArray("state_data");
                    for (int i = 0; i < stateArray.length(); i++) {
                        JSONObject tempObject = stateArray.getJSONObject(i);
                        String name = tempObject.getString("state");
                        String active = tempObject.getString("active");
                        String confirmed = tempObject.getString("confirmed");
                        String recoverd = tempObject.getString("recovered");
                        String death = tempObject.getString("deaths");
                        stateList.add(new StateClass(name, confirmed, active, recoverd, death));
                    }
                    Log.d("TAG", "Fetched");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
            stateList.sort(new Comparator<StateClass>() {
                @Override
                public int compare(StateClass o1, StateClass o2) {
                    return Integer.parseInt(o2.getmActive()) - Integer.parseInt(o1.getmActive());
                }
            });
            LinearLayoutManager manager = new LinearLayoutManager(StateActivity.this);
            stateListRecycler.setLayoutManager(manager);
            adapter = new StateListAdapter(stateList);
            stateListRecycler.setAdapter(adapter);
        }
    }


}
