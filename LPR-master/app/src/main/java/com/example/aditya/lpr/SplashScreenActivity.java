package com.example.aditya.lpr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.aditya.lpr.constants.MyAppPreferences;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide the notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_splash_screen);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // This is the amount of time the SplashScreen will be displayed to the user
        final int SPLASH_TIME_OUT_MILLISECONDS = 2500;

        // Create an Handler object and set the timeout
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                // This method will be executed once the timer is over

                // Get the SharedPreferences object and check whether the user is logged in
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                Intent intent;
                boolean logged_in_via_facebook = sharedPreferences.getBoolean(MyAppPreferences.LOGGED_IN_VIA_FACEBOOK,false);
                boolean logged_in_via_google = sharedPreferences.getBoolean(MyAppPreferences.LOGGED_IN_VIA_GOOGLE,false);

                if(logged_in_via_facebook || logged_in_via_google){
                    // If the user is logged in then redirect to MainActivity
                    intent = new Intent(SplashScreenActivity.this,MainActivity.class);
                }
                else{
                    // Else redirect to LoginActivity
                    intent = new Intent(SplashScreenActivity.this,LoginActivity.class);
                }
                startActivity(intent);
                finish(); // Destroy this(SplashScreen) activity
            }

        }, SPLASH_TIME_OUT_MILLISECONDS);
    }

}
