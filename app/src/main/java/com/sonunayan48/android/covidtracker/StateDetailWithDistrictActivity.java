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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.sonunayan48.android.covidtracker.Network.NetworkUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;

public class StateDetailWithDistrictActivity extends AppCompatActivity {
    public static final int ADS_PERIOD = StateActivity.ADS_PERIOD;
    private static final String DISTRICT_URL = "https://api.covid19india.org/state_district_wise.json";
    private static ArrayList<Object> districtList;
    StateClass state;
    private TextView mTotal;
    private TextView mActive;
    private TextView mRecovered;
    private TextView mDeath;
    private TextView mAcrossState;
    private ProgressBar mProgressBar;
    private ProgressBar progressBar;
    private RecyclerView districtView;
    private StateListAdapter adapter;
    private TextView dataNotAvailable;
    private SlidingUpPanelLayout sliding;
    private View slider;
    private boolean showDataUnavailable = false;

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
        setupListener();
        startNetworkCall();
    }

    public void addBannerAds() {
        for (int i = ADS_PERIOD; i < districtList.size(); i += ADS_PERIOD) {
            AdView adView = new AdView(StateDetailWithDistrictActivity.this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.banner_ad_unit_id));
            districtList.add(i, adView);
        }
        loadBannerAd();
    }

    private void loadBannerAd() {
        int i = ADS_PERIOD;
        while (i < districtList.size()) {
            if (!(districtList.get(i) instanceof AdView)) {
                return;
            }
            AdView adView = (AdView) districtList.get(i);
            adView.loadAd(new AdRequest.Builder().build());
            i += ADS_PERIOD;
        }
    }

    private void setup() {
        mTotal = findViewById(R.id.total_cases_count);
        mActive = findViewById(R.id.active_cases_count);
        mRecovered = findViewById(R.id.recovered_cases_count);
        mDeath = findViewById(R.id.death_cases_count);
        mAcrossState = findViewById(R.id.across_state);
        mProgressBar = findViewById(R.id.progress_horizontal);
        progressBar = findViewById(R.id.progress_circular2);
        districtList = new ArrayList<>();
        districtView = findViewById(R.id.district_list);
        dataNotAvailable = findViewById(R.id.data_not_available);
        sliding = findViewById(R.id.sliding);
        slider = findViewById(R.id.dragger);
    }

    private void setupListener() {
        sliding.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if ("Collapsed".equalsIgnoreCase(newState.name())) {
                    slider.setBackground(getDrawable(R.drawable.ic_expand_less_black_24dp));
                } else if ("Expanded".equalsIgnoreCase(newState.name())) {
                    slider.setBackground(getDrawable(R.drawable.ic_expand_more_black_24dp));
                }
            }
        });
    }

    private void setData(StateClass state) {
        mTotal.setText(state.getmConfirmed());
        mActive.setText(state.getmActive());
        mRecovered.setText(state.getmRecovered());
        mDeath.setText(state.getmDeath());
        String acrossStr = getString(R.string.across_var, state.getmName());
        mAcrossState.setText(acrossStr);
    }

    private void startNetworkCall() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            new GetDistrictData().execute();
        } else {
            createNetworkErrorDialog();
        }
    }

    private void createNetworkErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.no_internet_msg));
        builder.setTitle(getString(R.string.no_internet_connection_title));
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_warning_black_24dp);
        builder.setPositiveButton(getString(R.string.dialog_try_again), new DialogInterface.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                startNetworkCall();
            }
        });
        builder.setNegativeButton(getString(R.string.dialog_exit_app), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finishAffinity();
            }
        });
        AlertDialog alertDialog = builder.show();
        alertDialog.show();
    }

    private class GetDistrictData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils networkUtils = new NetworkUtils();
            String jsonStr = networkUtils.makeRequestCall(DISTRICT_URL);
            if (jsonStr != null) {
                try {
                    JSONObject parentObj = new JSONObject(jsonStr);
                    JSONObject stateObject;
                    if ((state.getmName()).equals("Telengana")) {
                        stateObject = parentObj.getJSONObject("Telangana");
                    } else {
                        stateObject = parentObj.getJSONObject(state.getmName());
                    }
                    JSONObject districtObject = stateObject.getJSONObject("districtData");
                    Iterator e = districtObject.keys();
                    while (e.hasNext()) {
                        String districtName = (String) e.next();
                        if (!districtName.equals("Unknown")) {
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
                    showDataUnavailable = true;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressBar.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
            if (showDataUnavailable) {
                dataNotAvailable.setText(getString(R.string.district_data_unavailable, state.getmName()));
                dataNotAvailable.setVisibility(View.VISIBLE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                districtList.sort(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return Integer.parseInt(((StateClass) o2).getmActive()) - Integer.parseInt(((StateClass) o1).getmActive());
                    }
                });
            }
            if (StateActivity.toShowAds == 1) {
                addBannerAds();
            }
            LinearLayoutManager manager = new LinearLayoutManager(StateDetailWithDistrictActivity.this);
            districtView.setLayoutManager(manager);
            adapter = new StateListAdapter(districtList, new StateListAdapter.ListItemClickListner() {
                @Override
                public void onListClick(int itemIndex) {
                    Intent intent = new Intent(getApplicationContext(), DistrictStateActivity.class);
                    StateClass clickedDistrict = (StateClass) districtList.get(itemIndex);
                    intent.putExtra("district", clickedDistrict);
                    startActivity(intent);
                }
            }, R.anim.fade_transition_animation);
            districtView.setAdapter(adapter);

        }
    }
}
