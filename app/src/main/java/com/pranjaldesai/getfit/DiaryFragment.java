package com.pranjaldesai.getfit;


import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LegendEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.shrikanthravi.collapsiblecalendarview.data.Day;
import com.shrikanthravi.collapsiblecalendarview.widget.CollapsibleCalendar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class DiaryFragment extends Fragment {

    View view;
    @BindView(R.id.diary_calendar_view)
    CollapsibleCalendar collapsibleCalendar;
    @BindView(R.id.nutrition_card)
    CardView nutritionCard;
    @BindView(R.id.pushup_card)
    CardView pushupCard;
    @BindView(R.id.period_card)
    CardView periodCard;
    @BindView(R.id.nutritionChart)
    PieChart nutritionChart;
    @BindView(R.id.nutrition_food_secondary)
    TextView calorieCount;
    @BindView(R.id.pushup_active_time)
    TextView pushupActiveTime;
    @BindView(R.id.pushup_total_done)
    TextView pushupTotalDone;
    @BindView(R.id.pushup_calories)
    TextView pushupCalorieCount;
    @BindView(R.id.pushup_fitness_secondary)
    TextView pushupTimeSecondary;
    @BindView(R.id.diary_progressBar)
    ProgressBar diaryProgressBar;
    @BindView(R.id.period_date)
    TextView periodDate;
    @BindView(R.id.period_flow)
    TextView periodFlow;
    @BindView(R.id.period_mood)
    TextView periodMood;
    @BindView(R.id.period_symptoms)
    TextView periodSymptoms;
    @BindView(R.id.period_fitness_secondary)
    TextView periodTimeSecondary;
    Float carbs, protien, calories, fat;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    boolean googleFit;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public DiaryFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view= inflater.inflate(R.layout.fragment_diary, container, false);
        ButterKnife.bind(this, view);

        mAuth = FirebaseAuth.getInstance();
        currentUser= mAuth.getCurrentUser();
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(sharedPreferences.getBoolean(
                getString(R.string.googleFit), false)){
            googleFit= true;
        }

        final Calendar cal = Calendar.getInstance();
        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH); // Note: zero based!
        final int day = cal.get(Calendar.DAY_OF_MONTH);
        collapsibleCalendar.setCalendarListener(new CollapsibleCalendar.CalendarListener() {
            @Override
            public void onDaySelect() {

                Day selectedDay= collapsibleCalendar.getSelectedDay();
                if(selectedDay.getYear()<year && selectedDay.getMonth()<=month && selectedDay.getDay()<=day){
                    getData();
                }else if(selectedDay.getYear()==year){
                    if(selectedDay.getMonth()<month){
                        getData();
                    }else if(selectedDay.getMonth()==month){
                        if(selectedDay.getDay()<=day){
                            getData();
                        }else{
                            nutritionCard.setVisibility(View.GONE);
                            pushupCard.setVisibility(View.GONE);
                            periodCard.setVisibility(View.GONE);
                        }
                    }else{
                        nutritionCard.setVisibility(View.GONE);
                        pushupCard.setVisibility(View.GONE);
                        periodCard.setVisibility(View.GONE);
                    }
                }
                else{
                    nutritionCard.setVisibility(View.GONE);
                    pushupCard.setVisibility(View.GONE);
                    periodCard.setVisibility(View.GONE);
                }
            }

            @Override
            public void onItemClick(View v) {
            }

            @Override
            public void onDataUpdate() {
            }

            @Override
            public void onMonthChange() {
            }

            @Override
            public void onWeekChange(int position) {
            }
        });
        Day currentDay= new Day(year,month,day);
        if(googleFit) {
            new NutritionData().execute(currentDay);
        }
        getFitnessData(currentDay);
        getPeriodData(currentDay);
        diaryProgressBar.setVisibility(View.VISIBLE);
        return view;
    }


    private void getData(){
        if(googleFit) {
            new NutritionData().execute(collapsibleCalendar.getSelectedDay());
        }
        getFitnessData(collapsibleCalendar.getSelectedDay());
        getPeriodData(collapsibleCalendar.getSelectedDay());
        diaryProgressBar.setVisibility(View.VISIBLE);
    }

    private class NutritionData extends AsyncTask<Day, Void, Void> {

        @Override
        protected Void doInBackground(Day... params) {
            readHistoryData(params[0].getDay(),params[0].getMonth(),params[0].getYear());
            return null;
        }
        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    public void getFitnessData(Day currentDay){
        if(currentUser!=null) {
            diaryProgressBar.setVisibility(View.VISIBLE);
            String date = (currentDay.getMonth()+1) + "-" + currentDay.getDay() + "-" + currentDay.getYear();
            final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid()).collection(getString(R.string.collections_activity)).document(date).collection(getString(R.string.collections_pushups))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.isEmpty()){
                                pushupCard.setVisibility(View.GONE);
                                diaryProgressBar.setVisibility(View.GONE);
                            }else {
                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    try {


                                        int activeTime = Integer.parseInt(querySnapshot.getData().get(getString(R.string.activetime)).toString());
                                        if (activeTime < 60) {
                                            pushupActiveTime.setText(activeTime + "s");
                                        } else {
                                            pushupActiveTime.setText((activeTime / 60) + "m " + (activeTime % 60) + "s");
                                        }
                                        pushupCalorieCount.setText(querySnapshot.getData().get(getString(R.string.caloriecount)).toString());
                                        pushupTotalDone.setText(querySnapshot.getData().get(getString(R.string.totalDone)).toString());
                                        Date date1 = new Date(Long.parseLong(querySnapshot.getData().get(getString(R.string.time)).toString()));
                                        String time = sdf.format(date1);
                                        pushupTimeSecondary.setText(time);
                                        pushupCard.setVisibility(View.VISIBLE);
                                        diaryProgressBar.setVisibility(View.GONE);
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pushupCard.setVisibility(View.GONE);
                            diaryProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    public void getPeriodData(Day currentDay){
        if(currentUser!=null) {
            diaryProgressBar.setVisibility(View.VISIBLE);
            String date = (currentDay.getMonth()+1) + "-" + currentDay.getDay() + "-" + currentDay.getYear();
            final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy hh:mm a", Locale.US);
            db.collection(getString(R.string.collections_user)).document(currentUser.getUid()).collection(getString(R.string.collections_activity)).document(date).collection(getString(R.string.collections_period))
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if(queryDocumentSnapshots.isEmpty()){
                                periodCard.setVisibility(View.GONE);
                                diaryProgressBar.setVisibility(View.GONE);
                            }else {
                                for (QueryDocumentSnapshot querySnapshot : queryDocumentSnapshots) {
                                    try {
                                        periodDate.setText(querySnapshot.getData().get(getString(R.string.date)).toString());
                                        periodFlow.setText(querySnapshot.getData().get(getString(R.string.flow)).toString());
                                        periodMood.setText(querySnapshot.getData().get(getString(R.string.mood)).toString());
                                        Date date1 = new Date(Long.parseLong(querySnapshot.getData().get(getString(R.string.time)).toString()));
                                        periodSymptoms.setText(querySnapshot.getData().get(getString(R.string.symptoms)).toString());
                                        String time = sdf.format(date1);
                                        periodTimeSecondary.setText(time);
                                        periodCard.setVisibility(View.VISIBLE);
                                        diaryProgressBar.setVisibility(View.GONE);
                                    }catch ( Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            periodCard.setVisibility(View.GONE);
                            diaryProgressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }

    protected Task<DataReadResponse> readHistoryData(int selectedDay, int selectedMonth, int selectedYear) {

        Calendar cal = Calendar.getInstance();
        cal.set(selectedYear,selectedMonth,selectedDay+1,23,59);
        long endTime = cal.getTimeInMillis();
        cal.add(Calendar.DAY_OF_YEAR, -1);
        long startTime = cal.getTimeInMillis();

        DataReadRequest readRequest = new DataReadRequest.Builder()
                .aggregate(DataType.TYPE_NUTRITION, DataType.AGGREGATE_NUTRITION_SUMMARY)
                .bucketByTime(1,TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS).build();

        // Invoke the History API to fetch the data with the query
        return Fitness.getHistoryClient(getActivity(), GoogleSignIn.getLastSignedInAccount(getActivity()))
                .readData(readRequest)
                .addOnSuccessListener(
                        new OnSuccessListener<DataReadResponse>() {
                            @Override
                            public void onSuccess(DataReadResponse dataReadResponse) {
                                printData(dataReadResponse);
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Snackbar.make(view.findViewById(R.id.fragment_diary), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                            }
                        });
    }

    public void printData(DataReadResponse dataReadResult){

        if (dataReadResult.getBuckets().size() > 0) {
            for (Bucket bucket : dataReadResult.getBuckets()) {
                List<DataSet> dataSets = bucket.getDataSets();
                for (DataSet dataSet : dataSets) {
                    dumpDataSet(dataSet);
                }
            }
        } else if (dataReadResult.getDataSets().size() > 0) {
            for (DataSet dataSet : dataReadResult.getDataSets()) {
                dumpDataSet(dataSet);
            }
        }

    }

    private void dumpDataSet(DataSet dataSet) {
        if(!dataSet.isEmpty()) {
            carbs = dataSet.getDataPoints().get(0).getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_TOTAL_CARBS);
            protien = dataSet.getDataPoints().get(0).getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_PROTEIN);
            fat = dataSet.getDataPoints().get(0).getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_TOTAL_FAT);
            calories = dataSet.getDataPoints().get(0).getValue(Field.FIELD_NUTRIENTS).getKeyValue(Field.NUTRIENT_CALORIES);
            showNutritionCard();
        }else{
            nutritionCard.setVisibility(View.GONE);
            diaryProgressBar.setVisibility(View.GONE);
        }
    }

    public void showNutritionCard(){
        if(carbs!=null && protien!=null && fat!=null && calories!=null) {
            nutritionChart.clear();
            ArrayList<PieEntry> entries1 = new ArrayList<PieEntry>();
            entries1.add(new PieEntry(fat, getString(R.string.fat)));
            entries1.add(new PieEntry(carbs, getString(R.string.carb)));
            entries1.add(new PieEntry(protien, getString(R.string.protein)));
            PieDataSet ds1 = new PieDataSet(entries1, "");
            ds1.setColors(ColorTemplate.MATERIAL_COLORS);
            ds1.setSliceSpace(2f);
            ds1.setValueTextColor(Color.WHITE);
            ds1.setValueTextSize(12f);

            PieData d = new PieData(ds1);

            nutritionChart.getDescription().setEnabled(false);
            nutritionChart.setHoleRadius(25f);
            nutritionChart.setTransparentCircleRadius(30f);
            Legend legend = nutritionChart.getLegend();
            legend.setTextColor(Color.WHITE);
            legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
            legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            legend.setOrientation(Legend.LegendOrientation.VERTICAL);
            legend.setDrawInside(false);

            nutritionChart.setData(d);
            calorieCount.setText(getString(R.string.calorie_consumed)+calories.intValue());
            nutritionCard.setVisibility(View.VISIBLE);
            diaryProgressBar.setVisibility(View.GONE);
        }

    }

}
