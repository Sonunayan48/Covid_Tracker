package com.sonunayan48.android.covidtracker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class SplashScreenActivity extends AppCompatActivity {

    private ImageView appIconImg;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_splash_screen);
        RotateAnimation rotate = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotate.setDuration(3000);
        rotate.setInterpolator(new LinearInterpolator());
        appIconImg = findViewById(R.id.covid19);
        appIconImg.startAnimation(rotate);
        /* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*/

        /** Duration of wait **/
        int SPLASH_DISPLAY_LENGTH = 2000;
        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {
                /* Create an Intent that will start the Menu-Activity. */
                Intent startStateActivity = new Intent(SplashScreenActivity.this, StateActivity.class);
                Bundle data = getIntent().getExtras();
                if (data != null){
                    String websiteStr = data.getString("website");
                    if (websiteStr != null){
                        startStateActivity.putExtra("website", websiteStr);
                    }
                }
                SplashScreenActivity.this.startActivity(startStateActivity);
                SplashScreenActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGTH);
    }
}




