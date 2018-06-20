package com.pranjaldesai.getfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ProgressBar progressBar;
    int click=0;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    @BindView(R.id.user_profile_pic)
    CircleImageView profilePic;
    @BindView(R.id.navigation)
    BottomNavigationView navigation;
    @BindView(R.id.navigation_title)
    TextView appTitle;
    FragmentManager fragmentManager;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Explode explode= new Explode();
            explode.setDuration(500);
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    appTitle.setText(getString(R.string.app_name));
                    FitnessFragment fitnessFragment = new FitnessFragment();
                    fitnessFragment.setEnterTransition(explode);
                    fragmentManager.beginTransaction().replace(R.id.navigationContainer,fitnessFragment).commit();
                    return true;
                case R.id.navigation_settings:
                    appTitle.setText(getString(R.string.title_settings));
                    SettingFragment settingFragment = new SettingFragment();
                    settingFragment.setEnterTransition(explode);
                    fragmentManager.beginTransaction().replace(R.id.navigationContainer,settingFragment).commit();
                    return true;
                case R.id.navigation_diary:
                    appTitle.setText(getString(R.string.title_diary));
                    DiaryFragment diaryFragment = new DiaryFragment();
                    diaryFragment.setEnterTransition(explode);
                    fragmentManager.beginTransaction().replace(R.id.navigationContainer,diaryFragment).commit();
                    return true;
            }
            return false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setupWindowAnimations();

        FitnessFragment fitnessFragment = new FitnessFragment();
        Explode explode= new Explode();
        explode.setDuration(500);
        fitnessFragment.setEnterTransition(explode);
        fragmentManager= getSupportFragmentManager();
        appTitle.setText(getString(R.string.app_name));
        fragmentManager.beginTransaction().replace(R.id.navigationContainer,fitnessFragment).commit();
        navigation.setSelectedItemId(R.id.navigation_home);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        setCurrentUser();

    }

    private void setupWindowAnimations(){
        Fade fade= new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(fade);
    }



    private void setCurrentUser(){
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            startActivity( new Intent(this, LoginActivity.class));
        }else {
            Glide.with(this)
                    .load(currentUser.getPhotoUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.ic_account)
                            .fitCenter())
                    .into(profilePic);
            getTotalPushups();
        }
    }

    private void getTotalPushups(){
        db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot= task.getResult();
                            if(documentSnapshot.contains(getString(R.string.total_pushups))){
                                updateWidget(documentSnapshot.get(getString(R.string.total_pushups)).toString());
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.container), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void updateWidget(String result){
        SharedPreferences.Editor sharedPreferencesEditor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferencesEditor.putString(
                getString(R.string.widget_pushup), result);
        sharedPreferencesEditor.apply();
        PushUpWidgetService.startActionPushUp(this, result);

    }



}
