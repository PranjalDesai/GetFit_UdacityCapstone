package com.pranjaldesai.getfit;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;

import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;

public class OnboardingWizard extends AppIntro2 {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SignInFragment signInFragment= new SignInFragment();
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_one_title), getString(R.string.intro_one_description), R.drawable.background_zero, getResources().getColor(R.color.colorYellowDark)));
        addSlide(AppIntro2Fragment.newInstance(getString(R.string.intro_two_title), getString(R.string.intro_two_description), R.drawable.background_one, getResources().getColor(R.color.colorSecondaryDark)));
        addSlide(signInFragment);
        showSkipButton(false);
        askForPermissions(new String[]{Manifest.permission.BODY_SENSORS}, 2);

    }


    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        SharedPreferences.Editor sharedPreferencesEditor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferencesEditor.putBoolean(
                getString(R.string.onboarding_wizard), true);
        sharedPreferencesEditor.apply();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
