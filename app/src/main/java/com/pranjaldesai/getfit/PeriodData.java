package com.pranjaldesai.getfit;

public class PeriodData {

    private long date;

    private String typeOfPeriod;

    private String periodDescription;

    public PeriodData(){

    }

    public String getTypeOfPeriod() {
        return typeOfPeriod;
    }

    public void setTypeOfPeriod(String typeOfPeriod) {
        this.typeOfPeriod = typeOfPeriod;
    }

    public void setDate(long date){
        this.date=date;
    }

    public long getDate(){
        return date;
    }

    public String getPeriodDescription() {
        return periodDescription;
    }

    public void setPeriodDescription(String periodDescription) {
        this.periodDescription = periodDescription;
    }

}
