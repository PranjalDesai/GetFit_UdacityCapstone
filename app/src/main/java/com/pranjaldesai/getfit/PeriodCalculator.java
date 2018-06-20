package com.pranjaldesai.getfit;


import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;

public class PeriodCalculator {

    long lastPeriodDate;
    int periodCycleLength, periodLength;
    Context context;

    public PeriodCalculator(String lastPeriodDate, String periodCycleLength, String periodLength, Context context){
        this.lastPeriodDate=Long.parseLong(lastPeriodDate);
        this.periodCycleLength=Integer.parseInt(periodCycleLength);
        this.periodLength=Integer.parseInt(periodLength);
        this.context= context;
    }

    public ArrayList<PeriodData> getPeriodData(){
        ArrayList<PeriodData> periodData= new ArrayList<>();
        long periodDate= lastPeriodDate;
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(periodDate);
        periodDate= c.getTimeInMillis();
        for (int i = 1; i <= 36; i++) {
            for (int k = 0; k <= 1; k++) {
                c.add(Calendar.DATE, -1);
                PeriodData data= new PeriodData();
                data.setDate(c.getTimeInMillis());
                data.setTypeOfPeriod(context.getResources().getString(R.string.pre_period));
                data.setPeriodDescription(context.getResources().getString(R.string.pre_period));
                periodData.add(data);
            }
            c.add(Calendar.DATE, 2);
            for (int j = 0; j < periodLength; j++) {
                PeriodData data= new PeriodData();
                data.setTypeOfPeriod(context.getResources().getString(R.string.period_day));
                data.setPeriodDescription(context.getResources().getString(R.string.period_day_description));
                data.setDate(c.getTimeInMillis());
                periodData.add(data);
                c.add(Calendar.DATE, 1);
            }
            for (int l = 1; l <= 2; l++) {
                PeriodData data= new PeriodData();
                data.setTypeOfPeriod(context.getResources().getString(R.string.post_period));
                data.setPeriodDescription(context.getResources().getString(R.string.post_period_description));
                data.setDate(c.getTimeInMillis());
                periodData.add(data);
                c.add(Calendar.DATE,1);
            }
            if(periodLength>8){
                c.add(Calendar.DATE, 5);
                for (int m = 0; m < 3; m++) {
                    PeriodData data= new PeriodData();
                    data.setTypeOfPeriod(context.getResources().getString(R.string.peak_ovulation));
                    data.setPeriodDescription(context.getResources().getString(R.string.peak_ovulation_description));
                    data.setDate(c.getTimeInMillis());
                    periodData.add(data);
                    c.add(Calendar.DATE,1);
                }
            }else{
                c.add(Calendar.DATE, 3);
                for (int m = 0; m < 5; m++) {
                    PeriodData data= new PeriodData();
                    data.setTypeOfPeriod(context.getResources().getString(R.string.peak_ovulation));
                    data.setPeriodDescription(context.getResources().getString(R.string.peak_ovulation_description));
                    data.setDate(c.getTimeInMillis());
                    periodData.add(data);
                    c.add(Calendar.DATE,1);
                }
            }

            c.setTimeInMillis(periodDate);
            c.add(Calendar.DATE,((periodCycleLength)*i));
        }

        return periodData;
    }
}
