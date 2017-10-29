package com.driving.self.sueno.sueno;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.StringDef;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by jerem_000 on 13/10/2017.
 */

public class DriveActivity extends AppCompatActivity {

    DriveActivity driveActivity = new DriveActivity();

    public int currentKmph = 0;
    public int maxSpeed = 50;
    public boolean isDriving = false;

    public int normal_delay = 500; //ToChange
    public int emergency_delay = 100; //ToChange


    private DriveActivity driveActivity(){
        if (driveActivity == null){
            driveActivity = new DriveActivity();
        }
        return driveActivity;
    }

    private void startJourney(){
        isDriving = true;
        startDriving();
    }

    private void stopJourney(){
        isDriving = false;
        resetSpeed();
    }

    private void pauseJourney(){
        isDriving = false;
        pauseDriving();
    }

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_drive);
//
//        Button startJourney = (Button) findViewById(R.id.btn_startJourney);
//        Button stopJourney = (Button) findViewById(R.id.btn_stopJourney);
//        Button resetJourney = (Button) findViewById(R.id.btn_resetJourney);
//        Button setMaxSpeed = (Button) findViewById(R.id.btn_changeMaxSpeed);
//        final EditText etMaxSpeed = (EditText) findViewById(R.id.et_journey);
//        final TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
//
//        //OnClick methods for buttons
//        startJourney.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isDriving = true;
//                startDriving();
//            }
//        });
//
//        stopJourney.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isDriving = false;
//                resetSpeed();
//            }
//        });
//
//        resetJourney.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isDriving = false;
//                resetSpeed();
//            }
//        });
//
//        setMaxSpeed.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                int maxSpd = Integer.valueOf(etMaxSpeed.getText().toString());
//                if (maxSpd > 0){
//                    setMaxSpeed(maxSpd);
//                }
//            }
//        });
//
//    }


    public void resetSpeed(){
        currentKmph = 0;
        //TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
        //tvCurrentSpd.setText("STOPPED Current Speed: " + currentKmph);
    }

    public void setMaxSpeed(int max){
        maxSpeed = max;
    }

    public void isSpeedMax(){
        if (currentKmph >= maxSpeed){
            currentKmph--;
        }
    }

    public void pauseDriving(){
        gradualStop();
    }

    public void gradualStop(){
        if (isDriving == false && currentKmph > 0){
            new stopAsync().execute(normal_delay);
        }
    }

    public void quickStop(){
        if (isDriving == false && currentKmph > 0){
            new emergencyAsync().execute(emergency_delay);
        }
    }

    public void brake(){
        //ToDo: ?
    }

    public void startDriving(){
        if (isDriving == true && currentKmph < maxSpeed){
            new driveAsync().execute(normal_delay);
        }
    }

    //AsyncTask for startDriving() method
    public class driveAsync extends AsyncTask<Integer, Integer, Integer>{

        //TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);

        @Override
        protected void onPreExecute(){
            //tvCurrentSpd.setText("onPre-> Current Speed: " + currentKmph);
        }

        @Override
        protected Integer doInBackground(Integer... params){
            while (isDriving == true && currentKmph < maxSpeed) {

                try {
                    currentKmph++;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvCurrentSpd.setText("doInBg-> Current Spd: " + currentKmph);
                        }
                    });
                    Thread.currentThread();
                    Thread.sleep(normal_delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
                return null;
        }

        @Override
        protected void onProgressUpdate(Integer... update){
            //tvCurrentSpd.setText("onProgUpdate -> Current Speed: " + currentKmph);
        }

//        @Override
//        protected void onPostExecute(String s){
//            TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
//            tvCurrentSpd.setText("Current Speed: " + currentKmph);
//        }
    }

    //AsyncTask for gradualStop() or stopDriving() methods
    public class stopAsync extends AsyncTask<Integer, Integer, Integer>{

        //TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);

        @Override
        protected void onPreExecute(){
            //tvCurrentSpd.setText("onPre-> Current Speed: " + currentKmph);
        }

        @Override
        protected Integer doInBackground(Integer... params){
            while (currentKmph > 0) {

                try {
                    currentKmph--;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvCurrentSpd.setText("doInBg-> Current Spd: " + currentKmph);
                        }
                    });
                    Thread.currentThread();
                    Thread.sleep(normal_delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... update){
            //tvCurrentSpd.setText("onProgUpdate -> Current Speed: " + currentKmph);
        }

//        @Override
//        protected void onPostExecute(String s){
//            TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
//            tvCurrentSpd.setText("Current Speed: " + currentKmph);
//        }
    }

    //AsyncTask for quick stops: Emergency brake / sudden stop, etc
    public class emergencyAsync extends AsyncTask<Integer, Integer, Integer>{

        //TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);

        @Override
        protected void onPreExecute(){
            //tvCurrentSpd.setText("onPre-> Current Speed: " + currentKmph);
        }

        @Override
        protected Integer doInBackground(Integer... params){
            while (currentKmph > 0) {

                try {
                    currentKmph--;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //tvCurrentSpd.setText("doInBg-> Current Spd: " + currentKmph);
                        }
                    });
                    Thread.currentThread();
                    Thread.sleep(emergency_delay);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... update){
            //tvCurrentSpd.setText("onProgUpdate -> Current Speed: " + currentKmph);
        }

//        @Override
//        protected void onPostExecute(String s){
//            TextView tvCurrentSpd = (TextView) findViewById(R.id.tv_currentSpd);
//            tvCurrentSpd.setText("Current Speed: " + currentKmph);
//        }
    }
}
