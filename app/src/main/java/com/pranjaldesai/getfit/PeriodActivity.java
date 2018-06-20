package com.pranjaldesai.getfit;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.github.sundeepk.compactcalendarview.CompactCalendarView;
import com.github.sundeepk.compactcalendarview.domain.Event;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PeriodActivity extends AppCompatActivity {

    @BindView(R.id.compactcalendar_view)
    CompactCalendarView compactCalendarView;
    @BindView(R.id.month_name_period)
    TextView mMonthTitle;
    @BindView(R.id.period_card_calendar_view)
    CardView mSelectedEventCardView;
    @BindView(R.id.period_icon_calendar)
    ImageView periodIcon;
    @BindView(R.id.period_calendar_title)
    TextView periodTitle;
    @BindView(R.id.period_calendar_description)
    TextView periodDescription;
    @BindView(R.id.periodFloatingActionButton)
    FloatingActionButton reportPeriod;

    String lastPeriodDate, periodLength, periodCycleLength;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_period);

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
        }else{
            getPeriodDetails();
        }


        final SimpleDateFormat dateFormatForMonth = new SimpleDateFormat("MMMM - yyyy", Locale.getDefault());
        mMonthTitle.setText(dateFormatForMonth.format(compactCalendarView.getFirstDayOfCurrentMonth()));

        compactCalendarView.setListener(new CompactCalendarView.CompactCalendarViewListener() {
            @Override
            public void onDayClick(Date dateClicked) {
                List<Event> events = compactCalendarView.getEvents(dateClicked);
                setCardView(events);
            }

            @Override
            public void onMonthScroll(Date firstDayOfNewMonth) {
                mMonthTitle.setText(dateFormatForMonth.format(firstDayOfNewMonth));
            }
        });

        reportPeriod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity( new Intent(PeriodActivity.this, AddPeriod.class));
            }
        });
    }

    private void setCardView(List<Event> events){
        if(events.isEmpty()){
            mSelectedEventCardView.setVisibility(View.GONE);
        }else{
            for(Event event: events) {
                PeriodData periodData= (PeriodData) event.getData();
                mSelectedEventCardView.setVisibility(View.VISIBLE);
                if (periodData.getTypeOfPeriod().equals(getString(R.string.pre_period))) {
                    mSelectedEventCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPurple));
                    periodIcon.setImageDrawable(getResources().getDrawable(R.drawable.emoticon_sad));
                    periodTitle.setText(periodData.getTypeOfPeriod());
                } else if (periodData.getTypeOfPeriod().equals(getString(R.string.period_day))) {
                    mSelectedEventCardView.setCardBackgroundColor(getResources().getColor(R.color.colorRed));
                    periodIcon.setImageDrawable(getResources().getDrawable(R.drawable.water));
                    periodTitle.setText(periodData.getTypeOfPeriod());
                } else if (periodData.getTypeOfPeriod().equals(getString(R.string.post_period))) {
                    mSelectedEventCardView.setCardBackgroundColor(getResources().getColor(R.color.colorDeepPurple));
                    periodIcon.setImageDrawable(getResources().getDrawable(R.drawable.emoticon_happy));
                    periodTitle.setText(periodData.getTypeOfPeriod());
                } else {
                    mSelectedEventCardView.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    periodIcon.setImageDrawable(getResources().getDrawable(R.drawable.baby_buggy));

                }
                periodTitle.setText(periodData.getTypeOfPeriod());
                periodDescription.setText(periodData.getPeriodDescription());
            }

        }

    }

    private void getPeriodDetails(){
        db.collection(getString(R.string.collections_user)).document(currentUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot= task.getResult();
                            if(documentSnapshot.contains(getString(R.string.last_period))){
                                lastPeriodDate= documentSnapshot.get(getString(R.string.last_period)).toString();
                                periodCycleLength= documentSnapshot.get(getString(R.string.periodCycleLength)).toString();
                                periodLength= documentSnapshot.get(getString(R.string.periodLength)).toString();
                                setEvents();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.period_activity), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    public void setEvents(){
        periodCycleLength= periodCycleLength.replace(getString(R.string.days),"");
        periodLength= periodLength.replace(getString(R.string.days),"");
        PeriodCalculator periodCalculator= new PeriodCalculator(lastPeriodDate,periodCycleLength,periodLength,this);
        ArrayList<PeriodData> periodData= periodCalculator.getPeriodData();
        for(PeriodData p: periodData){
            Event ev;
            if(p.getTypeOfPeriod().equals(getString(R.string.pre_period))){
                ev= new Event(getResources().getColor(R.color.colorPurple), p.getDate(), p);
            } else if(p.getTypeOfPeriod().equals(getString(R.string.period_day))){
                ev= new Event(getResources().getColor(R.color.colorRed), p.getDate(), p);
            } else if(p.getTypeOfPeriod().equals(getString(R.string.post_period))){
                ev= new Event(getResources().getColor(R.color.colorDeepPurple), p.getDate(), p);
            } else {
                ev= new Event(getResources().getColor(R.color.colorPrimary), p.getDate(), p);
            }
            compactCalendarView.addEvent(ev);
        }
        Calendar c = Calendar.getInstance();
        List<Event> events= compactCalendarView.getEvents(c.getTimeInMillis());
        setCardView(events);
    }

    private void setupWindowAnimations(){
        Explode explode= new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(explode);
        getWindow().setExitTransition(explode);
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
