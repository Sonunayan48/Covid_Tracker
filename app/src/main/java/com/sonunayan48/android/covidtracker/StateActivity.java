package com.sonunayan48.android.covidtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
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
    private static final String URLSTRINGINDIA = "https://covid-19india-api.herokuapp.com/v2.0/country_data";
    private static final String URLSTRINGSTATE = "https://covid-19india-api.herokuapp.com/v2.0/state_data";
    private static final String SHARE_URL = "https://covidtracker48.page.link/downlaod";
    public static ArrayList<StateClass> stateList;
    private RecyclerView stateListRecycler;
    private StateListAdapter adapter;
    private ProgressBar progressBar;
    private TextView mConnectionText;
    private TextView totalCases;
    private TextView activeCase;
    private TextView recovered;
    private TextView totalDeaths;
    private TextView lastUpdate;
    private JSONObject obj;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(getApplicationContext(), AboutActivity.class));
                break;
            case R.id.share:
                shareApp();
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareApp() {
        String messageText = "Hey There, Our lives have been impacted very badly due to the spread of *Novel " +
                "Corona Virus* in India, but we are ready to face and win this challenge. " +
                "Download the *Covid Tracker* android app to know the current data related to *COVID 19* in" +
                " your *State or District* to stay alert. Download the app free from here: " +
                SHARE_URL;
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, messageText);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, "Share the App using");
        startActivity(shareIntent);


    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_state);
        Bundle data = getIntent().getExtras();
        if (data != null) {
            String websiteStr = data.getString("website");
            if (websiteStr != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(websiteStr));
                startActivity(intent);
            }
        }
        stateListRecycler = findViewById(R.id.state_list);
        progressBar = findViewById(R.id.progress_circular);
        mConnectionText = findViewById(R.id.connection_text);
        totalCases = findViewById(R.id.total_cases_count);
        activeCase = findViewById(R.id.active_cases_count);
        recovered = findViewById(R.id.recovered_cases_count);
        totalDeaths = findViewById(R.id.death_cases_count);
        lastUpdate = findViewById(R.id.last_update);
        stateList = new ArrayList<>();
        startNetworkCall();
    }

    private void startNetworkCall(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            new GetStateResults().execute();
            mConnectionText.setVisibility(View.INVISIBLE);
        } else {
            mConnectionText.setVisibility(View.VISIBLE);
            lastUpdate.setText("No Internet Connection");
            createNetworkErrorDialog();
        }
    }

    private void setIndiaData() {
        try {
            String active = obj.getString("active_cases");
            String confirmed = obj.getString("confirmed_cases");
            String recoverd = obj.getString("recovered_cases");
            String death = obj.getString("death_cases");
            String update = obj.getString("last_updated");
            totalCases.setText(confirmed);
            activeCase.setText(active);
            recovered.setText(recoverd);
            totalDeaths.setText(death);
            lastUpdate.setText("Last Updated: " + update);
        } catch (JSONException e) {
            e.printStackTrace();
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

    private class GetStateResults extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils networkUtils = new NetworkUtils();
            String jsonStr = networkUtils.makeRequestCall(URLSTRINGSTATE);
            String jsonStrIndia = networkUtils.makeRequestCall(URLSTRINGINDIA);

            if (jsonStr != null) {
                try {
                    JSONArray array = new JSONArray(jsonStr);
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

            if (jsonStrIndia != null) {
                try {
                    JSONArray arrayIndia = new JSONArray(jsonStrIndia);
                    obj = arrayIndia.getJSONObject(1);
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
            setIndiaData();
            stateList.sort(new Comparator<StateClass>() {
                @Override
                public int compare(StateClass o1, StateClass o2) {
                    return Integer.parseInt(o2.getmActive()) - Integer.parseInt(o1.getmActive());
                }
            });
            LinearLayoutManager manager = new LinearLayoutManager(StateActivity.this);
            stateListRecycler.setLayoutManager(manager);
            adapter = new StateListAdapter(stateList, new StateListAdapter.ListItemClickListner() {
                @Override
                public void onListClick(int itemIndex) {
                    Intent intent = new Intent(getApplicationContext(), StateDetailWithDistrictActivity.class);
                    StateClass clickedState = stateList.get(itemIndex);
                    intent.putExtra("state", clickedState);
                    startActivity(intent);
                }
            }, R.anim.rv_item_falldown_animation);
            stateListRecycler.setAdapter(adapter);
        }
    }

}
