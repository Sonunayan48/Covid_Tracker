package com.sonunayan48.android.covidtracker;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.sonunayan48.android.covidtracker.Network.NetworkUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

public class StateActivity extends AppCompatActivity {
    public static final String BANNER_AD_PERIOD_KEY = "banner_ad_period";
    private static final String SHOW_ADS_KEY = "show_ads";
    private static final String SHOW_DISTRICT_DATA_KEY = "show_district_data";
    public static int show_district_data = 0;
    private static final String URLSTRINGINDIA = "https://covid-19india-api.herokuapp.com/v2.0/country_data";
    private static final String COUNTRY_DATA_API = "country_data_api";
    private static final String STATE_DATA_API = "state_data_api";
    public static int ADS_PERIOD = 1000;
    private static final String URLSTRINGSTATE = "https://covid-19india-api.herokuapp.com/v2.0/state_data";
    private static final String IS_LANGUAGE_SELECTED = "is_language_selected";
    private static String urlCountry = "";
    private static final String SHARE_URL = "https://covidtracker48.page.link/downlaod";
    private static final String LATEST_VERSION_KEY = "latest_version";
    private static final String SELECTED_LANGUAGE_INDEX = "selected_language_label";
    private static final String SELECTED_LANGUAGE = "selected_language_value";
    private static String urlState = "";
    public static ArrayList<Object> stateList;
    private FirebaseRemoteConfig remoteConfig = FirebaseRemoteConfig.getInstance();
    private RecyclerView stateListRecycler;
    private StateListAdapter adapter;
    private ProgressBar progressBar;
    private ProgressBar progressBar2;
    private TextView mConnectionText;
    private TextView totalCases;
    private TextView activeCase;
    private TextView recovered;
    private TextView totalDeaths;
    private TextView lastUpdate;
    private TextView dConfirmed, dActive, dRecovered, dDeath;
    private JSONObject obj;
    private FirebaseAnalytics mAnalytics;
    private SharedPreferences preferences;
    private SlidingUpPanelLayout sliding;
    private View slider;
    private LinearLayout dc, da, dr, dd;
    private View arrowActive;
    private AdView adView;
    public static int toShowAds = 0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            MenuItem item = menu.getItem(1);
            item.setVisible(false);
        }
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
                break;
            case R.id.download_ArogyaSetu:
                downloadArogyaSetuApp();
                break;
            case R.id.change_language:
                //Toast.makeText(this, "Feature comming soon", Toast.LENGTH_SHORT).show();
                openLangDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    private void openLangDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.choose_language));
        String[] items = {"English", "हिन्दी"};
        int selectedLanguage = preferences.getInt(SELECTED_LANGUAGE_INDEX, 0);
        builder.setSingleChoiceItems(items, selectedLanguage, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which){
                    case 0:
                        changePreferences(0, "");
                        setAppLanguage("");
                        recreate();
                        dialog.cancel();
                        break;
                    case 1:
                        //translateToHindi();
                        changePreferences(1, "hi");
                        setAppLanguage("hi");
                        recreate();
                        dialog.cancel();
                        break;
                }
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setAppLanguage(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        getBaseContext().getResources().updateConfiguration(config, getBaseContext()
                .getResources().getDisplayMetrics());
    }

    private void loadLocale() {
        String language = preferences.getString(SELECTED_LANGUAGE, "");
        setAppLanguage(language);
    }

    /*private void translateToHindi() {
        translator.downloadModelIfNeeded()
                .addOnSuccessListener(
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                translator.translate("Done").addOnSuccessListener(
                                        new OnSuccessListener<String>() {
                                            @Override
                                            public void onSuccess(String s) {
                                                Toast.makeText(StateActivity.this,
                                                        s, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                );
                            }
                        }
                );
    }*/

    private void changePreferences(int lanIndex, String language) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(SELECTED_LANGUAGE_INDEX, lanIndex);
        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    private void shareApp() {
        String messageText = getString(R.string.share_app_msg, SHARE_URL);
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, messageText);
        sendIntent.setType("text/plain");
        Intent shareIntent = Intent.createChooser(sendIntent, getString(R.string.share_app_title));
        startActivity(shareIntent);


    }

    public void downloadArogyaSetuApp() {
        Uri arogyaSetuApp = Uri.parse("https://play.google.com/store/apps/details?id=nic.goi.aarogyasetu");
        Uri arogyaSetuAppSam = Uri.parse("https://www.mygov.in/aarogya-setu-app/");
        Intent downloadApp = new Intent(Intent.ACTION_VIEW, arogyaSetuAppSam);
        if (downloadApp.resolveActivity(getPackageManager()) != null) {
            startActivity(downloadApp);
        }
    }

    public void initAds() {
        MobileAds.initialize(this);
    }

    public void addBannerAds() {
        for (int i = ADS_PERIOD; i < stateList.size(); i += ADS_PERIOD) {
            AdView adView = new AdView(StateActivity.this);
            adView.setAdSize(AdSize.BANNER);
            adView.setAdUnitId(getString(R.string.banner_ad_unit_id_release));
            stateList.add(i, adView);
        }
        loadBannerAd();
    }

    private void loadBannerAd() {
        int i = ADS_PERIOD;
        while (i < stateList.size()) {
            if (!(stateList.get(i) instanceof AdView)) {
                return;
            }
            AdView adView = (AdView) stateList.get(i);
            adView.loadAd(new AdRequest.Builder().build());
            i += ADS_PERIOD;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        loadLocale();
        setContentView(R.layout.activity_state);
        initAds();
        mAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());
        Log.v("Analytics", "analytics fetched");
        /*FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(FirebaseTranslateLanguage.EN)
                .setTargetLanguage(FirebaseTranslateLanguage.HI)
                .build();*/
        /*translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);*/
        Bundle data = getIntent().getExtras();
        if (data != null) {
            String websiteStr = data.getString("website");
            if (websiteStr != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(websiteStr));
                startActivity(intent);
            }
        }
        remoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(true)
                .build());
        HashMap<String, Object> defaults = new HashMap<>();
        defaults.put(LATEST_VERSION_KEY, 1.0);
        defaults.put(COUNTRY_DATA_API, "");
        defaults.put(STATE_DATA_API, "");
        defaults.put(SHOW_ADS_KEY, 0);
        defaults.put(BANNER_AD_PERIOD_KEY, 1000);
        defaults.put(SHOW_DISTRICT_DATA_KEY, 0);
        remoteConfig.setDefaults(defaults);
        remoteConfig.fetch().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                remoteConfig.activateFetched();
                try {
                    checkForUpdates();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        stateListRecycler = findViewById(R.id.state_list);
        progressBar = findViewById(R.id.progress_circular);
        progressBar2 = findViewById(R.id.progress_circular2);
        mConnectionText = findViewById(R.id.connection_text);
        totalCases = findViewById(R.id.total_cases_count);
        activeCase = findViewById(R.id.active_cases_count);
        recovered = findViewById(R.id.recovered_cases_count);
        totalDeaths = findViewById(R.id.death_cases_count);
        lastUpdate = findViewById(R.id.last_update);
        dConfirmed = findViewById(R.id.delta_confirmed);
        dActive = findViewById(R.id.delta_active);
        dRecovered = findViewById(R.id.delta_recovered);
        dDeath = findViewById(R.id.delta_deaths);
        stateList = new ArrayList<>();
        sliding = findViewById(R.id.sliding);
        slider = findViewById(R.id.dragger);
        dc = findViewById(R.id.layout_dc);
        da = findViewById(R.id.layout_da);
        dr = findViewById(R.id.layout_dr);
        dd = findViewById(R.id.layout_dd);
        arrowActive = findViewById(R.id.arrow_a);
        setupListener();
        startNetworkCall();
    }

    private void newUserLanguageSelection() {
        boolean isLanguageSelected = preferences.getBoolean(IS_LANGUAGE_SELECTED, false);
        if (!isLanguageSelected && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            openLangDialog();
            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean(IS_LANGUAGE_SELECTED, true);
            editor.apply();
        }
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

    private void checkForUpdates() throws PackageManager.NameNotFoundException {
        Double latestVersion = (Double) remoteConfig.getDouble(LATEST_VERSION_KEY);
        toShowAds = Integer.parseInt(remoteConfig.getString(SHOW_ADS_KEY));
        ADS_PERIOD = Integer.parseInt(remoteConfig.getString(BANNER_AD_PERIOD_KEY));
        show_district_data = Integer.parseInt(remoteConfig.getString(SHOW_DISTRICT_DATA_KEY));
        PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(), 0);
        String versionStr = pInfo.versionName;
        Double version = Double.parseDouble(versionStr);
        if (latestVersion > version) {
            createUpdateDialog();
        }
        Log.d("TAG", versionStr);
    }

    private void createUpdateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.new_update_message));
        builder.setTitle(getString(R.string.new_update_title));
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_update_black_24dp);
        builder.setPositiveButton(getString(R.string.update_now_dialog), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(SHARE_URL));
                startActivity(intent);
            }
        });
        builder.setNegativeButton(R.string.update_later_dialog, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog = builder.show();
        alertDialog.show();
    }

    private void startNetworkCall() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            progressBar2.setVisibility(View.VISIBLE);
            remoteConfig.fetch().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    remoteConfig.activateFetched();
                    urlCountry = remoteConfig.getString(COUNTRY_DATA_API);
                    urlState = remoteConfig.getString(STATE_DATA_API);
                    if (urlCountry.length() != 0 && urlState.length() != 0) {
                        new GetStateResults().execute();
                    } else {
                        progressBar.setVisibility(View.INVISIBLE);
                        progressBar2.setVisibility(View.INVISIBLE);
                        showServerErrorDialog();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.INVISIBLE);
                    progressBar2.setVisibility(View.INVISIBLE);
                    showServerErrorDialog();
                }
            });
            mConnectionText.setVisibility(View.INVISIBLE);
        } else {
            mConnectionText.setVisibility(View.VISIBLE);
            lastUpdate.setText("No Internet Connection");
            createNetworkErrorDialog();
        }
    }

    private void showServerErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.server_error_title));
        builder.setMessage(getString(R.string.server_error_message));
        builder.setCancelable(false);
        builder.setIcon(R.drawable.ic_error_black_24dp);
        builder.setNegativeButton(getString(R.string.dialog_exit_app),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finishAffinity();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void setIndiaData() {
        try {
            String active = obj.getString("active_cases");
            String confirmed = obj.getString("confirmed_cases");
            String recoverd = obj.getString("recovered_cases");
            String death = obj.getString("death_cases");
            String update = obj.getString("last_updated");
            String deltaActive = obj.getString("delta_change_active_cases");
            String deltaRecovered = obj.getString("delta_change_recovered_cases");
            String deltaDeath = obj.getString("delta_change_death_cases");
            int dCnf = Integer.parseInt(deltaActive) + Integer.parseInt(deltaRecovered)
                    + Integer.parseInt(deltaDeath);
            if (Integer.parseInt(deltaActive) <= 0) {
                arrowActive.setBackground(getDrawable(R.drawable.ic_arrow_down_green));
                deltaActive = String.valueOf(Integer.parseInt(deltaActive) * (-1));
            }
            totalCases.setText(confirmed);
            activeCase.setText(active);
            recovered.setText(recoverd);
            totalDeaths.setText(death);
            dActive.setText(deltaActive);
            dRecovered.setText(deltaRecovered);
            dDeath.setText(deltaDeath);
            dConfirmed.setText(String.valueOf(dCnf));
            lastUpdate.setText("Last Updated: " + update);
            dc.setVisibility(View.VISIBLE);
            da.setVisibility(View.VISIBLE);
            dr.setVisibility(View.VISIBLE);
            dd.setVisibility(View.VISIBLE);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            Toast.makeText(this, "There might be some issue!", Toast.LENGTH_SHORT).show();
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

    private class GetStateResults extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Void... voids) {
            NetworkUtils networkUtils = new NetworkUtils();
            String jsonStr = networkUtils.makeRequestCall(urlState);
            String jsonStrIndia = networkUtils.makeRequestCall(urlCountry);
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
                } catch (Exception e) {
                    showServerErrorDialog();
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

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setVisibility(View.INVISIBLE);
            progressBar2.setVisibility(View.INVISIBLE);
            super.onPostExecute(aVoid);
            setIndiaData();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stateList.sort(new Comparator<Object>() {
                    @Override
                    public int compare(Object o1, Object o2) {
                        return Integer.parseInt(((StateClass) o2).getmActive()) - Integer.parseInt(((StateClass) o1).getmActive());
                    }
                });
            }
            if (toShowAds == 1) {
                addBannerAds();
            }
            LinearLayoutManager manager = new LinearLayoutManager(StateActivity.this);
            stateListRecycler.setLayoutManager(manager);
            adapter = new StateListAdapter(stateList, new StateListAdapter.ListItemClickListner() {
                @Override
                public void onListClick(int itemIndex) {
                    Intent intent = new Intent(getApplicationContext(), StateDetailWithDistrictActivity.class);
                    StateClass clickedState = (StateClass) stateList.get(itemIndex);
                    intent.putExtra("state", clickedState);
                    startActivity(intent);
                }
            }, R.anim.rv_item_falldown_animation);
            stateListRecycler.setAdapter(adapter);
            newUserLanguageSelection();
        }
    }

}
