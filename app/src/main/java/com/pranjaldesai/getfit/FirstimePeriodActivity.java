package com.pranjaldesai.getfit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FirstimePeriodActivity extends AppCompatActivity {

    @BindView(R.id.last_period_date_picker)
    Button datePicker;
    @BindView(R.id.period_length_spinner)
    Spinner periodLength;
    @BindView(R.id.period_cycle_spinner)
    Spinner periodCycle;
    @BindView(R.id.period_firsttime_finish)
    FloatingActionButton finishFab;
    @BindView(R.id.period_starter_title)
    TextView starterTitle;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private int mYear, mMonth, mDay, periodLengthIndex, periodCycleIndex;
    String[] periodLengthdays, periodCycledays;
    boolean length,cycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firstime_period);

        setupWindowAnimations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(getString(R.string.period_tracker));
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser==null) {
            startActivity( new Intent(this, LoginActivity.class));
        }


        String userName=currentUser.getDisplayName();
        String[] str= userName.split(" ");
        if(str.length>1){
            userName= str[0];
        }
        String title= getString(R.string.lets_start, userName);
        starterTitle.setText(title);

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });

        periodLengthdays= getResources().getStringArray(R.array.period_length_days);
        periodCycledays= getResources().getStringArray(R.array.period_cycle_days);
        ArrayAdapter<String> periodLenghtDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.period_length_days));
        periodLenghtDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodLength.setAdapter(periodLenghtDataAdapter);
        periodLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodLengthIndex= position;
                length=true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                length=false;
            }
        });

        ArrayAdapter<String> periodCycleDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.period_cycle_days));
        periodCycleDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodCycle.setAdapter(periodCycleDataAdapter);
        periodCycle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodCycleIndex= position;
                cycle=true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                cycle=false;
            }
        });

        finishFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFields();
            }
        });
    }

    private void setupWindowAnimations(){
        Slide slide= new Slide();
        slide.setDuration(1000);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

    private void validateFields(){
        if(datePicker.getText().equals(getString(R.string.select_date))){
            Snackbar.make(findViewById(R.id.relativeLayout6),getString(R.string.please_select_date),Snackbar.LENGTH_LONG).show();
        }else if(periodLengthIndex==0){
            Snackbar.make(findViewById(R.id.relativeLayout6),getString(R.string.please_select_period_length),Snackbar.LENGTH_LONG).show();
        }else if(periodCycleIndex==0){
            Snackbar.make(findViewById(R.id.relativeLayout6),getString(R.string.please_select_period_cycle),Snackbar.LENGTH_LONG).show();
        }else{
            Map<String, Object> periodData = new HashMap<>();
            String date= datePicker.getText().toString();
            try {
                Date selectedDate= new SimpleDateFormat("MM/dd/yyyy").parse(date);
                periodData.put(getString(R.string.last_period), String.valueOf(selectedDate.getTime()));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            periodData.put(getString(R.string.periodCycleLength), periodCycledays[periodCycleIndex]);
            periodData.put(getString(R.string.periodLength), periodLengthdays[periodLengthIndex]);

            db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                    .set(periodData, SetOptions.merge())
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            routeToPeriodActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Snackbar.make(findViewById(R.id.firsttime_period), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void routeToPeriodActivity(){
        startActivity(new Intent(this, PeriodActivity.class));
    }

    private void getDate(){
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog datePickerDialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                datePicker.setText((month+1)+"/"+dayOfMonth+"/"+year);
            }
        }, mYear,mMonth, mDay);
        Calendar max= Calendar.getInstance();
        max.set(mYear,mMonth,mDay,23,59,59);
        datePickerDialog.getDatePicker().setMaxDate(max.getTimeInMillis());
        datePickerDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent= new Intent(this, MainActivity.class);
            navigateUpTo(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
