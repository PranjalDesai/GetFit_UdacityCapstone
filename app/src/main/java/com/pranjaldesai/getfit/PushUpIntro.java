package com.pranjaldesai.getfit;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PushUpIntro extends AppCompatActivity {

    @BindView(R.id.pushup_description)
    TextView descriptionPushUp;
    @BindView(R.id.pushup_description_schedule)
    TextView descriptionSchedulePushUp;
    @BindView(R.id.fifth_pushup_day)
    TextView fifthDay;
    @BindView(R.id.fourth_pushup_day)
    TextView fourthDay;
    @BindView(R.id.three_pushup_day)
    TextView thirdDay;
    @BindView(R.id.second_pushup_day)
    TextView secondDay;
    @BindView(R.id.first_pushup_day)
    TextView firstDay;
    @BindView(R.id.pushup_intro_title)
    TextView introTitlePushUp;
    @BindView(R.id.intro_continue)
    CardView mContinue;
    @BindView(R.id.pushup_firsttime_layout)
    RelativeLayout firstTimePushUp;
    @BindView(R.id.pushup_schedule_layout)
    CardView schedulePushUp;

    String pushupLevel="", level="", pushupSubLevel="", userName="";
    String[] pushupLevels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_up_intro);
        ButterKnife.bind(this);

        setupWindowAnimations();
        Resources res = getResources();
        pushupLevels= getResources().getStringArray(R.array.pushup_levels);
        pushupLevel= (String) getIntent().getExtras().get(getString(R.string.pushup_level));
        userName= (String) getIntent().getExtras().get(getString(R.string.user_name));
        pushupSubLevel= (String) getIntent().getExtras().get(getString(R.string.pushup_sub_level));
        if(pushupLevel.equals("-1")){
            String title= getString(R.string.lets_start, userName);
            introTitlePushUp.setText(title);
            firstTimePushUp.setVisibility(View.VISIBLE);
            level="firstTimeTest";
            schedulePushUp.setVisibility(View.GONE);
            descriptionPushUp.setText(getString(R.string.first_time_description));
        }else if(pushupSubLevel!=null && pushupSubLevel.equals("0")){
            String title= getString(R.string.test_title, userName);
            introTitlePushUp.setText(title);
            firstTimePushUp.setVisibility(View.VISIBLE);
            schedulePushUp.setVisibility(View.GONE);
            level="test";
            descriptionPushUp.setText(getString(R.string.test_description));
        }else{
            String title= getString(R.string.welcome_back, userName);
            introTitlePushUp.setText(title);
            firstTimePushUp.setVisibility(View.GONE);
            schedulePushUp.setVisibility(View.VISIBLE);
            descriptionPushUp.setText(getString(R.string.set_description));
            level= pushupLevels[Integer.parseInt(pushupLevel)];
            String[] pushupSet= level.split("–");
            int totalPushup=0;
            for (String aPushupSet : pushupSet) {
                totalPushup += Integer.parseInt(aPushupSet);
            }
            descriptionSchedulePushUp.setText(getString(R.string.total)+String.valueOf(totalPushup));
            firstDay.setText(pushupSet[0]);
            secondDay.setText(pushupSet[1]);
            thirdDay.setText(pushupSet[2]);
            fourthDay.setText(pushupSet[3]);
            fifthDay.setText(pushupSet[4]);
        }

        mContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routePushUpTracker();
            }
        });
    }

    private void setupWindowAnimations(){
        Slide slide= new Slide();
        slide.setDuration(1000);
        getWindow().setEnterTransition(slide);
        getWindow().setExitTransition(slide);
    }

    public void routePushUpTracker(){
        Intent intent= new Intent(this, PushUpTrackerActivity.class);
        intent.putExtra(getString(R.string.currentLevel),level);
        intent.putExtra(getString(R.string.userPrimaryLevel), pushupLevel);
        startActivity(intent);
    }
}
