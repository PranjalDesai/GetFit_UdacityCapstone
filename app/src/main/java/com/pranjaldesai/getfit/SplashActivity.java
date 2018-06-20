package com.pranjaldesai.getfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        if (!sharedPreferences.getBoolean(
                getString(R.string.onboarding_wizard), false)) {
            // The user hasn't seen the OnboardingFragment yet, so show it
            startActivity(new Intent(this, OnboardingWizard.class));
        } else {
            startActivity(new Intent(this, MainActivity.class));
        }
    }
}
