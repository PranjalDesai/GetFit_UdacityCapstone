package com.pranjaldesai.getfit;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;

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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class FitnessFragment extends Fragment {

    View view;
    @BindView(R.id.pushup_card)
    CardView mPushCard;
    @BindView(R.id.period_card)
    CardView mPeriodCard;
    @BindView(R.id.progressBarFitness)
    ProgressBar progressBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    String userName;


    public FitnessFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_fitness, container, false);
        ButterKnife.bind(this, view);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            startActivity( new Intent(getContext(), LoginActivity.class));
        }
        getGender();

        userName=currentUser.getDisplayName();
        String[] str= userName.split(" ");
        if(str.length>1){
            userName= str[0];
        }

        mPushCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routePushUp();
            }
        });
        mPeriodCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routePeriod();
            }
        });

        return view;
    }

    private void getGender(){
        db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot= task.getResult();
                            if(documentSnapshot.contains(getResources().getString(R.string.user_gender))){
                                if(documentSnapshot.get(getResources().getString(R.string.user_gender)).toString().equals("Male")){
                                    mPeriodCard.setVisibility(View.GONE);
                                }else{
                                    mPeriodCard.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(view.findViewById(R.id.fitness_fragment), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                    }
                });
    }


    private void routePushUp(){
        if(currentUser!=null){
            progressBar.setVisibility(View.VISIBLE);
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot= task.getResult();
                                progressBar.setVisibility(View.GONE);

                                Intent pushup= new Intent(getContext(),PushUpIntro.class);
                                pushup.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);


                                if(documentSnapshot.contains(getResources().getString(R.string.pushup_level))){
                                    pushup.putExtra(getResources().getString(R.string.pushup_level),documentSnapshot.get(getResources().getString(R.string.pushup_level)).toString());
                                    pushup.putExtra(getResources().getString(R.string.pushup_sub_level),documentSnapshot.get(getResources().getString(R.string.pushup_sub_level)).toString());
                                }else{
                                    pushup.putExtra(getResources().getString(R.string.pushup_level),"-1");
                                }

                                pushup.putExtra(getResources().getString(R.string.user_name), userName);

                                startActivity(pushup);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(view.findViewById(R.id.fitness_fragment), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void routePeriod(){
        if(currentUser!=null) {
            progressBar.setVisibility(View.VISIBLE);
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot documentSnapshot= task.getResult();
                                progressBar.setVisibility(View.GONE);

                                if(documentSnapshot.contains(getResources().getString(R.string.last_period))){
                                    startActivity(new Intent(getContext(), PeriodActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                }else{
                                    startActivity(new Intent(getContext(), FirstimePeriodActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(view.findViewById(R.id.fitness_fragment), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }

    }

}
