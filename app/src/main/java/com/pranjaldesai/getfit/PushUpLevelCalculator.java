package com.pranjaldesai.getfit;

public class PushUpLevelCalculator {

    public int pushUpLevel, pushUpSubLevel;



    public PushUpLevelCalculator(){

    }

    public void firstTimePushUpLevel(int numberOfPushUps){
        if(numberOfPushUps<11){
            pushUpLevel=1;
            pushUpSubLevel=1;
        }else if(numberOfPushUps>10 && numberOfPushUps<21){
            pushUpLevel=2;
            pushUpSubLevel=1;
        }else if(numberOfPushUps>20){
            pushUpLevel=3;
            pushUpSubLevel=1;
        }
    }

    public void testPushUpLevel(int pushUpLev, int numberOfPushUps){
        if(pushUpLev<10){
            if(numberOfPushUps<11){
                pushUpLevel=10;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>10 && numberOfPushUps<21){
                pushUpLevel=11;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>20){
                pushUpLevel=12;
                pushUpSubLevel=1;
            }
        }else if(pushUpLev<19){
            if(numberOfPushUps<11){
                pushUpLevel=19;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>10 && numberOfPushUps<41){
                pushUpLevel=20;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>40){
                pushUpLevel=21;
                pushUpSubLevel=1;
            }
        }else if(pushUpLev<28){
            if(numberOfPushUps<21){
                pushUpLevel=28;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>20 && numberOfPushUps<31){
                pushUpLevel=29;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>30){
                pushUpLevel=30;
                pushUpSubLevel=1;
            }
        }else if(pushUpLev<37){
            if(numberOfPushUps<41){
                pushUpLevel=37;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>40 && numberOfPushUps<51){
                pushUpLevel=38;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>50){
                pushUpLevel=39;
                pushUpSubLevel=1;
            }
        }else if(pushUpLev<46){
            if(numberOfPushUps<51){
                pushUpLevel=46;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>50 && numberOfPushUps<61){
                pushUpLevel=47;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>60){
                pushUpLevel=48;
                pushUpSubLevel=1;
            }
        }
        else if(pushUpLev<55){
            if(numberOfPushUps<51){
                pushUpLevel=46;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>50 && numberOfPushUps<61){
                pushUpLevel=47;
                pushUpSubLevel=1;
            }else if(numberOfPushUps>60){
                pushUpLevel=48;
                pushUpSubLevel=1;
            }
        }
    }

    public void levelUp(int level, int subLevel){
        pushUpLevel= level;
        pushUpSubLevel= subLevel;
        if((pushUpLevel+3)<54){
            pushUpSubLevel++;
            if(pushUpSubLevel>3){
                pushUpSubLevel=0;
            }else{
                pushUpLevel+=3;
            }
        }
    }

    public int getPushUpLevel(){
        return pushUpLevel;
    }

    public int getSubPushUpLevel(){
        return pushUpSubLevel;
    }
}
