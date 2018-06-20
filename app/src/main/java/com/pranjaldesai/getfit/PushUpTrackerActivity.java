package com.pranjaldesai.getfit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static java.lang.Thread.sleep;

public class PushUpTrackerActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sm;
    private Sensor proximitySensor;

    @BindView(R.id.countDisplay)
    TextView countDisplay;
    @BindView(R.id.continuebtn)
    CardView continueBtn;
    @BindView(R.id.countDisplayButton)
    CardView pushupCounter;
    @BindView(R.id.pushup_description_schedule)
    TextView totalPushups;
    @BindView(R.id.first_pushup_day)
    TextView firstDay;
    @BindView(R.id.second_pushup_day)
    TextView secondDay;
    @BindView(R.id.three_pushup_day)
    TextView thirdDay;
    @BindView(R.id.fourth_pushup_day)
    TextView fourthDay;
    @BindView(R.id.fifth_pushup_day)
    TextView fifthDay;
    @BindView(R.id.restText)
    TextView restText;
    @BindView(R.id.complete_button_text)
    TextView completeTextView;
    @BindView(R.id.finishWorkout)
    ProgressBar progressBar;
    @BindView(R.id.pushup_test_title)
    TextView pushupTestTitle;
    @BindView(R.id.pushup_schedule_layout)
    CardView pushupScheduleLayout;

    private long startTime, currentTime, totalTime=0;

    private int numberOfPushUps=0, setNumber=0, setNumberOfPushUp=0;
    private String extra, pushUpLevel;
    private boolean pushupTest;
    String[] pushupSets;
    CountDownTimer timer;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_up_tracker);
        ButterKnife.bind(this);

        setupWindowAnimations();

        mAuth = FirebaseAuth.getInstance();
        currentUser= mAuth.getCurrentUser();
        Intent currentIntent= getIntent();
        extra= currentIntent.getStringExtra(getString(R.string.currentLevel));
        pushUpLevel= currentIntent.getStringExtra(getString(R.string.userPrimaryLevel));
        if(extra!=null) {
            if (extra.equals(getString(R.string.firstTimeTest))) {
                pushupTest=false;
                countDisplay.setText("0");
                pushupTestTitle.setVisibility(View.VISIBLE);
                pushupScheduleLayout.setVisibility(View.GONE);

            } else if (extra.equals("test")) {
                pushupTest=false;
                countDisplay.setText("0");
                pushupTestTitle.setVisibility(View.VISIBLE);
                pushupScheduleLayout.setVisibility(View.GONE);
            } else {
                pushupTest=true;
                pushupTestTitle.setVisibility(View.GONE);
                pushupScheduleLayout.setVisibility(View.VISIBLE);
                pushupSets= extra.split("–");
                int total=0;
                for (int i = 0; i < pushupSets.length; i++) {
                    total+=Integer.parseInt(pushupSets[i]);
                    setSelector(pushupSets[i], i);
                }
                totalPushups.setText(getString(R.string.total)+total);
                firstDay.setTextColor(getResources().getColor(R.color.colorYellow));
                setNumberOfPushUp=Integer.parseInt(pushupSets[setNumber]);
                countDisplay.setText(String.valueOf(setNumberOfPushUp));
            }
        }

        setDisplayClickListener();
        startTime= System.currentTimeMillis();
        startSensor();

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(completeTextView.getText().toString().equals(getString(R.string.complete_activity))){
                    onComplete();
                }else if(timer!=null){
                    timer.cancel();
                    countDisplay.setText(String.valueOf(setNumberOfPushUp));
                    completeTextView.setText(getString(R.string.complete_activity));
                    restText.setVisibility(View.GONE);
                    pushupCounter.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                    startSensor();
                    startTime= System.currentTimeMillis();
                    setDisplayClickListener();
                }
            }
        });
    }

    private void setupWindowAnimations(){
        Explode explode= new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);
        getWindow().setExitTransition(explode);
    }

    private void completePushUp(){
        progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> workout = new HashMap<>();
        final Calendar c = Calendar.getInstance();
        workout.put(getString(R.string.activetime), String.valueOf(totalTime/1000));
        workout.put(getString(R.string.caloriecount), String.valueOf(((totalTime/1000)*388)/3600));
        workout.put(getString(R.string.totalDone), numberOfPushUps);
        workout.put(getString(R.string.time), c.getTimeInMillis());
        String dashedDate= (c.get(Calendar.MONTH)+1)+"-"+c.get(Calendar.DAY_OF_MONTH)+"-"+c.get(Calendar.YEAR);
        db.collection(getString(R.string.collections_user)).document(currentUser.getUid()).collection(getString(R.string.collections_activity)).document(dashedDate).collection(getString(R.string.collections_pushups)).document(String.valueOf(c.getTimeInMillis()))
                .set(workout)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateTotalPushUp();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.pushup_tracker), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void updateTotalPushUp()
    {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        String oldPushup= sharedPreferences.getString(getString(R.string.widget_pushup),null);
        final String updatedPushup;
        if(oldPushup!=null){
            int newPushUp=Integer.parseInt(oldPushup)+numberOfPushUps;
            updatedPushup= String.valueOf(newPushUp);
        }else{
            updatedPushup= String.valueOf(numberOfPushUps);
        }

        final DocumentReference sfDocRef = db.collection(getString(R.string.collections_user)).document(currentUser.getUid());

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                transaction.update(sfDocRef, getString(R.string.total_pushups), updatedPushup);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                redirectPushUpFinalActivity(updatedPushup);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Snackbar.make(findViewById(R.id.pushup_tracker), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
            }
        });
    }
    private void redirectPushUpFinalActivity(String updatedPushup){
        updateWidget(updatedPushup);
        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        Intent intent= new Intent(this,PushUpFeelingActivity.class);
        intent.putExtra(getString(R.string.numberOfPushUp), numberOfPushUps);
        startActivity(intent);
    }

    private void updateWidget(String result){
        SharedPreferences.Editor sharedPreferencesEditor =
                PreferenceManager.getDefaultSharedPreferences(this).edit();
        sharedPreferencesEditor.putString(
                getString(R.string.widget_pushup), result);
        sharedPreferencesEditor.apply();
        PushUpWidgetService.startActionPushUp(this, result);
    }

    public void setDisplayClickListener(){
        pushupCounter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countUpButton();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        for (int i = 0; i < 1; i++) {
            if (event.values[0] <= 3.0f) {

                if (proximitySensor != null) {
                    if(pushupTest) {
                        pushupSetData();
                    }
                }
            }
        }
    }

    private void pushupSetData() {

        numberOfPushUps++;
        setNumberOfPushUp--;
        countDisplay.setText(String.valueOf(setNumberOfPushUp));

        if(setNumberOfPushUp==0){
            setNumber++;
            if(setNumber>4){
                onComplete();
            }else {
                setNumberOfPushUp = Integer.parseInt(pushupSets[setNumber]);
                proximitySensor=null;
                sm=null;
                levelSelector();
                pushupCounter.setOnClickListener(null);
                restTime();
            }
        }
    }

    private void levelSelector(){
        firstDay.setTextColor(getResources().getColor(R.color.white));
        secondDay.setTextColor(getResources().getColor(R.color.white));
        thirdDay.setTextColor(getResources().getColor(R.color.white));
        fourthDay.setTextColor(getResources().getColor(R.color.white));
        fifthDay.setTextColor(getResources().getColor(R.color.white));
        if(setNumber==0){
            firstDay.setTextColor(getResources().getColor(R.color.colorYellow));
        }else if(setNumber==1){
            secondDay.setTextColor(getResources().getColor(R.color.colorYellow));
        }else if (setNumber==2){
            thirdDay.setTextColor(getResources().getColor(R.color.colorYellow));
        }else if (setNumber==3){
            fourthDay.setTextColor(getResources().getColor(R.color.colorYellow));
        }else if (setNumber==4){
            fifthDay.setTextColor(getResources().getColor(R.color.colorYellow));
        }
    }

    private void setSelector(String pushup, int i){
        if(i==0){
            firstDay.setText(pushup);
        }else if(i==1){
            secondDay.setText(pushup);
        }else if (i==2){
            thirdDay.setText(pushup);
        }else if (i==3){
            fourthDay.setText(pushup);
        }else if (i==4){
            fifthDay.setText(pushup);
        }
    }

    private void restTime(){
        if(pushUpLevel!=null) {
            int time;
            int currentLevel= Integer.parseInt(pushUpLevel);
            if(currentLevel<14){
                time=30000;
            }else if(currentLevel<28){
                time=60000;
            }else if(currentLevel<42){
                time=90000;
            }else{
                time=120000;
            }
            currentTime= System.currentTimeMillis();
            totalTime+= currentTime-startTime;
            restText.setVisibility(View.VISIBLE);
            completeTextView.setText(getString(R.string.skip_rest));
            pushupCounter.setBackgroundTintList(getResources().getColorStateList(R.color.colorYellow));
            timer= new CountDownTimer(time, 1000){

                @Override
                public void onTick(long millisUntilFinished) {
                    countDisplay.setText(String.valueOf(millisUntilFinished/1000));
                }

                @Override
                public void onFinish() {
                    countDisplay.setText(String.valueOf(setNumberOfPushUp));
                    startSensor();
                    completeTextView.setText(getString(R.string.complete_activity));
                    pushupCounter.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary));
                    restText.setVisibility(View.INVISIBLE);
                    startTime=System.currentTimeMillis();
                    setDisplayClickListener();
                }
            }.start();
        }
    }

    private void startSensor(){
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        proximitySensor = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sm.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void onComplete(){
        proximitySensor=null;
        sm=null;
        pushupCounter.setOnClickListener(null);
        currentTime=System.currentTimeMillis();
        totalTime+= currentTime-startTime;
        completePushUp();

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void countUpButton(){
        numberOfPushUps++;
        setNumberOfPushUp--;
        if(pushupTest){
            countDisplay.setText(String.valueOf(setNumberOfPushUp));
            if(setNumberOfPushUp==0){
                setNumber++;
                if(setNumber>4){
                    onComplete();
                }else {
                    setNumberOfPushUp = Integer.parseInt(pushupSets[setNumber]);
                    proximitySensor=null;
                    sm=null;
                    pushupCounter.setOnClickListener(null);
                    levelSelector();
                    restTime();
                }
            }
        }else{
            countDisplay.setText(String.valueOf(numberOfPushUps));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        sm=null;
        proximitySensor = null;
        pushupCounter.setOnClickListener(null);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
