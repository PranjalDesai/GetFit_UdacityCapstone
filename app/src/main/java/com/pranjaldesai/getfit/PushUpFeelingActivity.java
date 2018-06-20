package com.pranjaldesai.getfit;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PushUpFeelingActivity extends AppCompatActivity {

    @BindView(R.id.pushup_schedule_layout)
    ConstraintLayout scheduleLayout;
    @BindView(R.id.easyBtn)
    CardView easyBtn;
    @BindView(R.id.rightBtn)
    CardView rightBtn;
    @BindView(R.id.homeBtn)
    CardView homeBtn;
    @BindView(R.id.finish_pushup_description)
    TextView descriptionPushUp;
    @BindView(R.id.finish_pushup_title)
    TextView titlePushUp;
    @BindView(R.id.progressBarDifficulty)
    ProgressBar progressBar;

    String pushUpLevel, pushUpSubLevel;
    int numberOfPushups=0;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_up_feeling);
        ButterKnife.bind(this);

        setupWindowAnimations();

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            startActivity( new Intent(this, LoginActivity.class));
        }
        currentLevel();

        Intent intent= getIntent();
        numberOfPushups= intent.getExtras().getInt(getString(R.string.numberOfPushUp));

        rightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               routeRight();
            }
        });

        easyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeEasy();
            }
        });

        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeHome();
            }
        });

    }

    private void setupWindowAnimations(){
        Explode explode= new Explode();
        explode.setDuration(1000);
        Fade fade= new Fade();
        fade.setDuration(1000);
        getWindow().setEnterTransition(explode);
        getWindow().setExitTransition(explode);
    }

    private void routeEasy(){
        PushUpLevelCalculator pushUpLevelCalculator= new PushUpLevelCalculator();
        pushUpLevelCalculator.levelUp(Integer.parseInt(pushUpLevel),Integer.parseInt(pushUpSubLevel));
        int newLevel= pushUpLevelCalculator.getPushUpLevel();
        int newSubLevel= pushUpLevelCalculator.getSubPushUpLevel();
        pushUpLevelUpdate(newLevel,newSubLevel);
    }

    private void routeHome(){
        PushUpLevelCalculator pushUpLevelCalculator= new PushUpLevelCalculator();
        if(pushUpLevel.equals("-1")){
            pushUpLevelCalculator.firstTimePushUpLevel(numberOfPushups);
        }else if(pushUpSubLevel!=null && pushUpSubLevel.equals("0")){
            pushUpLevelCalculator.testPushUpLevel(Integer.parseInt(pushUpLevel),numberOfPushups);
        }
        int newLevel= pushUpLevelCalculator.getPushUpLevel();
        int newSubLevel= pushUpLevelCalculator.getSubPushUpLevel();
        pushUpLevelUpdate(newLevel,newSubLevel);
    }

    private void pushUpLevelUpdate(final int newLevel, final int newSubLevel){
        final DocumentReference sfDocRef = db.collection(getString(R.string.collections_user)).document(currentUser.getUid());

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                transaction.update(sfDocRef, getString(R.string.pushup_level), String.valueOf(newLevel));
                transaction.update(sfDocRef, getString(R.string.pushup_sub_level), String.valueOf(newSubLevel));
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                routeRight();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.pushup_feeling), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void routeRight(){
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void currentLevel(){
        if(currentUser!=null) {
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot documentSnapshot = task.getResult();

                                if (documentSnapshot.contains(getString(R.string.pushup_level))) {
                                    pushUpLevel=documentSnapshot.get(getString(R.string.pushup_level)).toString();
                                    pushUpSubLevel= documentSnapshot.get(getString(R.string.pushup_sub_level)).toString();
                                } else {
                                    pushUpLevel="-1";
                                }

                                updateLevel();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);
                            Snackbar.make(findViewById(R.id.pushup_feeling), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void updateLevel(){
        if(pushUpLevel.equals("-1")){
            scheduleLayout.setVisibility(View.GONE);
            homeBtn.setVisibility(View.VISIBLE);
            titlePushUp.setText(getString(R.string.training));
            descriptionPushUp.setText(getString(R.string.firsttime_test));
        }else if(pushUpSubLevel!=null && pushUpSubLevel.equals("0")){
            scheduleLayout.setVisibility(View.GONE);
            homeBtn.setVisibility(View.VISIBLE);
            titlePushUp.setText(getString(R.string.training));
            descriptionPushUp.setText(getString(R.string.firsttime_test));
        }else{
            scheduleLayout.setVisibility(View.VISIBLE);
            homeBtn.setVisibility(View.GONE);
            titlePushUp.setText(getString(R.string.pushup_set_title));
            descriptionPushUp.setText(getString(R.string.set_finished));
        }

        progressBar.setVisibility(View.GONE);
    }
}
