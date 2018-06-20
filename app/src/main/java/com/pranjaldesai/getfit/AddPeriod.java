package com.pranjaldesai.getfit;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.abdeveloper.library.MultiSelectDialog;
import com.abdeveloper.library.MultiSelectModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AddPeriod extends AppCompatActivity {

    @BindView(R.id.period_date_picker)
    Button datePicker;
    @BindView(R.id.period_flow_picker)
    Button flowPicker;
    @BindView(R.id.period_mood_picker)
    Button moodPicker;
    @BindView(R.id.period_symptoms_picker)
    Button symptomsPicker;
    @BindView(R.id.period_log_submit)
    Button submitButton;
    @BindView(R.id.period_length_spinner)
    Spinner periodLength;
    @BindView(R.id.period_log_cancel)
    Button cancelButton;
    @BindView(R.id.progressPeriodBar)
    ProgressBar progressBar;

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;

    private ArrayList<MultiSelectModel> flowList, moodList, symptomsList;
    MultiSelectDialog flowDialog, moodDialog, symptomsDialog;
    private int mYear, mMonth, mDay, periodLengthIndex;
    String[] periodLengthdays;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_period);

        setupWindowAnimations();

        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        toolbar.setTitle(getString(R.string.log_period));
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
        initializeLists();

        flowPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowDialog.show(getSupportFragmentManager(), "FlowDialog");
            }
        });

        moodPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moodDialog.show(getSupportFragmentManager(), "MoodDialog");
            }
        });

        symptomsPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                symptomsDialog.show(getSupportFragmentManager(), "SymptomsDialog");
            }
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDate();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateFields();
            }
        });

        progressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        periodLengthdays= getResources().getStringArray(R.array.period_length_days);
        ArrayAdapter<String> periodLenghtDataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.period_length_days));
        periodLenghtDataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        periodLength.setAdapter(periodLenghtDataAdapter);
        periodLength.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                periodLengthIndex= position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupWindowAnimations(){
        Slide slide= new Slide();
        slide.setDuration(1000);
        Explode explode= new Explode();
        explode.setDuration(1000);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(explode);
    }

    private void validateFields(){
        if(datePicker.getText().equals(getString(R.string.select_start_date))){
            Snackbar.make(findViewById(R.id.period_details),getString(R.string.please_select_date),Snackbar.LENGTH_LONG).show();
        } else if(periodLengthIndex==0) {
            Snackbar.make(findViewById(R.id.period_details),getString(R.string.please_select_period_length),Snackbar.LENGTH_LONG).show();
        } else if(flowPicker.getText().equals(getString(R.string.select_flow))){
            Snackbar.make(findViewById(R.id.period_details),getString(R.string.please_select_flow),Snackbar.LENGTH_LONG).show();
        }else if(moodPicker.getText().equals(getString(R.string.select_mood))){
            Snackbar.make(findViewById(R.id.period_details),getString(R.string.please_select_mood),Snackbar.LENGTH_LONG).show();
        }else if(symptomsPicker.getText().equals(getString(R.string.select_symptoms))){
            Snackbar.make(findViewById(R.id.period_details),getString(R.string.please_select_symptoms),Snackbar.LENGTH_LONG).show();
        }else{
            progressBar.setVisibility(View.VISIBLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            String date= datePicker.getText().toString();
            try {
                Date selectedDate= new SimpleDateFormat("MM/dd/yyyy").parse(date);
                updateLastPeriod(selectedDate, date);

            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateLastPeriod(final Date date, final String dateString){
        final DocumentReference sfDocRef = db.collection(getString(R.string.collections_user)).document(currentUser.getUid());

        db.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(sfDocRef);
                transaction.update(sfDocRef, getString(R.string.last_period), String.valueOf(date.getTime()));
                transaction.update(sfDocRef, getString(R.string.periodLength), periodLengthdays[periodLengthIndex]);
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                logPeriod(date,dateString );
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void logPeriod(Date date, String dateString){
        Map<String, Object> period = new HashMap<>();
        final Calendar c = Calendar.getInstance();
        period.put(getString(R.string.date), dateString);
        period.put(getString(R.string.flow), flowPicker.getText().toString());
        period.put(getString(R.string.mood), moodPicker.getText().toString());
        period.put(getString(R.string.symptoms), symptomsPicker.getText().toString());
        period.put(getString(R.string.time), c.getTimeInMillis());
        String dashedDate= dateString.replace("/","-");
        db.collection(getString(R.string.collections_user)).document(currentUser.getUid()).collection(getString(R.string.collections_activity)).document(dashedDate).collection(getString(R.string.collections_period)).document(String.valueOf(date.getTime()))
                .set(period)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        redirectMainActivity();
                        progressBar.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(findViewById(R.id.period_details), getResources().getString(R.string.error_snackbar), Snackbar.LENGTH_LONG).show();
                    }
                });
    }

    private void redirectMainActivity(){
        startActivity(new Intent(this, MainActivity.class));
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

    private void initializeLists(){
        flowList= new ArrayList<>();
        moodList= new ArrayList<>();
        symptomsList= new ArrayList<>();

        flowList.add(new MultiSelectModel(1,getString(R.string.light)));
        flowList.add(new MultiSelectModel(2,getString(R.string.medium)));
        flowList.add(new MultiSelectModel(3,getString(R.string.heavy)));
        flowDialog = new MultiSelectDialog()
                .title(getString(R.string.select_flow_lower))
                .titleSize(25)
                .positiveText(getString(R.string.done))
                .negativeText(getString(R.string.cancel))
                .setMinSelectionLimit(1)
                .setMaxSelectionLimit(1)
                .multiSelectList(flowList)
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                        flowPicker.setText(dataString);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        moodList.add(new MultiSelectModel(1,getString(R.string.normal)));
        moodList.add(new MultiSelectModel(2,getString(R.string.happy)));
        moodList.add(new MultiSelectModel(3,getString(R.string.frisky)));
        moodList.add(new MultiSelectModel(4,getString(R.string.mood_swings)));
        moodList.add(new MultiSelectModel(5,getString(R.string.angry)));
        moodList.add(new MultiSelectModel(6,getString(R.string.sad)));
        moodList.add(new MultiSelectModel(7,getString(R.string.panicky)));
        moodDialog = new MultiSelectDialog()
                .title(getString(R.string.select_mood_lower))
                .titleSize(25)
                .positiveText(getString(R.string.done))
                .negativeText(getString(R.string.cancel))
                .setMinSelectionLimit(1)
                .setMaxSelectionLimit(moodList.size())
                .multiSelectList(moodList)
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                        moodPicker.setText(dataString);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

        symptomsList.add(new MultiSelectModel(1,getString(R.string.everything_fine)));
        symptomsList.add(new MultiSelectModel(2,getString(R.string.cramps)));
        symptomsList.add(new MultiSelectModel(3,getString(R.string.tender_breasts)));
        symptomsList.add(new MultiSelectModel(4,getString(R.string.headache)));
        symptomsList.add(new MultiSelectModel(5,getString(R.string.acne)));
        symptomsList.add(new MultiSelectModel(6,getString(R.string.backache)));
        symptomsList.add(new MultiSelectModel(7,getString(R.string.nausea)));
        symptomsList.add(new MultiSelectModel(8,getString(R.string.fatigue)));
        symptomsList.add(new MultiSelectModel(9,getString(R.string.bloating)));
        symptomsList.add(new MultiSelectModel(10,getString(R.string.cravings)));
        symptomsList.add(new MultiSelectModel(11,getString(R.string.insomnia)));
        symptomsList.add(new MultiSelectModel(12,getString(R.string.constipation)));
        symptomsList.add(new MultiSelectModel(13,getString(R.string.diarrhea)));
        symptomsDialog = new MultiSelectDialog()
                .title(getString(R.string.select_symptoms_lower))
                .titleSize(25)
                .positiveText(getString(R.string.done))
                .negativeText(getString(R.string.cancel))
                .setMinSelectionLimit(1)
                .setMaxSelectionLimit(symptomsList.size())
                .multiSelectList(symptomsList)
                .onSubmit(new MultiSelectDialog.SubmitCallbackListener() {
                    @Override
                    public void onSelected(ArrayList<Integer> selectedIds, ArrayList<String> selectedNames, String dataString) {
                        symptomsPicker.setText(dataString);
                    }

                    @Override
                    public void onCancel() {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent= new Intent(this, PeriodActivity.class);
            navigateUpTo(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
