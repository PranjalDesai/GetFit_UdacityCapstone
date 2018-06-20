package com.pranjaldesai.getfit;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.ConfigClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessOptions;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingFragment extends Fragment implements SettingListAdaptor.ItemClickListener {

    View view;
    private SettingListAdaptor settingListAdaptor;
    @BindView(R.id.setting_recycler_view)
    RecyclerView mRecylerView;
    @BindView(R.id.sign_out_button)
    Button signOutButton;
    private LinearLayoutManager linearLayoutManager;
    ArrayList<String> title, subtitle;
    String description, positiveButton;
    ArrayList<Drawable> drawables;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public SettingFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view= inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        loadRecyclerView();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentUser=mAuth.getCurrentUser();
                if(currentUser!=null) {
                    mAuth.signOut();
                    signOutButton.setText(getResources().getString(R.string.sign_in));
                }else {
                    getActivity().startActivity(new Intent(getContext(), LoginActivity.class));
                }
            }
        });
        return view;
    }

    public void loadRecyclerView(){
            linearLayoutManager= new LinearLayoutManager(getContext());
            mRecylerView.setLayoutManager(linearLayoutManager);
            mRecylerView.setHasFixedSize(true);
            title= new ArrayList<>();
            subtitle =new ArrayList<>();
            title.addAll(Arrays.asList(getResources().getStringArray(R.array.setting_titles)));
            addDrawables();
            addSubtitles();
            settingListAdaptor = new SettingListAdaptor(this, title, subtitle,drawables);
            mRecylerView.setAdapter(settingListAdaptor);
    }

    public void addDrawables(){
        drawables= new ArrayList<>();
        drawables.add(getResources().getDrawable(R.drawable.history));
        drawables.add(getResources().getDrawable(R.drawable.ic_permission));
        drawables.add(getResources().getDrawable(R.drawable.google_fit));
        drawables.add(getResources().getDrawable(R.drawable.help_circle));

    }
    public void addSubtitles(){
        subtitle =new ArrayList<>();
        subtitle.add(getResources().getString(R.string.clear_data_subtitle));
        subtitle.add(getResources().getString(R.string.enabled_subtitle)+ getPermissions() + getResources().getString(R.string.permission_subtitle));
        subtitle.add(getResources().getString(R.string.googlefit_subtitle)+getGoogleFit());
        subtitle.add(getResources().getString(R.string.version_subtitle)+ getString(R.string.app_version));
    }

    public String getPermissions(){
        String permission= "0";
        int granted=0;
        try {
            PackageInfo pi = getActivity().getPackageManager().getPackageInfo(getResources().getString(R.string.packageName), PackageManager.GET_PERMISSIONS);
            for (int i = 0; i < pi.requestedPermissions.length; i++) {
                if ((pi.requestedPermissionsFlags[i] & PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0) {
                    granted++;
                }
            }
            permission= granted +" /"+ pi.requestedPermissions.length;
        } catch (Exception e) {
        }

        return permission;
    }

    private FitnessOptions getFitnessOptions(){
        FitnessOptions fitnessOptions = FitnessOptions.builder()
                .addDataType(DataType.AGGREGATE_NUTRITION_SUMMARY, FitnessOptions.ACCESS_READ)
                .addDataType(DataType.TYPE_WORKOUT_EXERCISE, FitnessOptions.ACCESS_WRITE)
                .build();
        return fitnessOptions;
    }

    public String getGoogleFit(){
        String googleFit= "disabled";

        FitnessOptions fitnessOptions= getFitnessOptions();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(sharedPreferences.getBoolean(
                getResources().getString(R.string.googleFit), false)){
            googleFit= "enabled";
        }
        return googleFit;
    }

    public void signOut(View v){
        mAuth.signOut();
    }

    public void performAction(int clickedItemIndex){
        if(clickedItemIndex==0){
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Snackbar.make(view.findViewById(R.id.settingView), getResources().getString(R.string.clear_data_snackbar), Snackbar.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(view.findViewById(R.id.settingView), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_SHORT).show();
                        }
                    });
        }else if(clickedItemIndex==1){
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", getActivity().getPackageName(), null);
            intent.setData(uri);
            getContext().startActivity(intent);
        }else if(clickedItemIndex==2){
            SharedPreferences.Editor sharedPreferencesEditor =
                    PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
            if(getGoogleFit().equals("enabled")){
                sharedPreferencesEditor.putBoolean(
                        getResources().getString(R.string.googleFit), false);
            }else {
                sharedPreferencesEditor.putBoolean(
                        getResources().getString(R.string.googleFit), true);
            }
            sharedPreferencesEditor.apply();
            addSubtitles();
            settingListAdaptor.removeData();
            settingListAdaptor.updateList(title,drawables,subtitle);
            settingListAdaptor.notifyDataSetChanged();
        }

    }

    public void getDescription(int itemIndex){
        if(itemIndex==0){
            description= getResources().getString(R.string.clear_data_description);
            positiveButton= getResources().getString(R.string.yes_positiveBtn);
        }else if (itemIndex==1){
            description= getResources().getString(R.string.permission_description);
            positiveButton= getResources().getString(R.string.yes_positiveBtn);
        }else if (itemIndex==2){
            if(getGoogleFit().equals("enabled")){
                description= getResources().getString(R.string.google_fit_enabled_description);
                positiveButton=getResources().getString(R.string.disable_positiveBtn);
            }else {
                description= getResources().getString(R.string.google_fit_disabled_description);
                positiveButton=getResources().getString(R.string.enable_positiveBtn);
            }

        }else if (itemIndex==3){
            description= getResources().getString(R.string.version_subtitle)+getString(R.string.app_version)+getResources().getString(R.string.about_me_description);
            positiveButton= getResources().getString(R.string.close_positiveBtn);
        }

    }

    @Override
    public void onListItemClick(final int clickedItemIndex) {
        getDescription(clickedItemIndex);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
        builder.setTitle(title.get(clickedItemIndex))
                .setMessage(description)
                .setPositiveButton(positiveButton, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(clickedItemIndex==3){
                            dialog.dismiss();
                        }else {
                            performAction(clickedItemIndex);
                        }
                    }
                });
        if(clickedItemIndex!=3) {
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
        }
        builder.create();
        builder.show();
    }
}
