package com.example.visualaid;

/**
 * modified based on jlee375's version.
 */
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class DropNoticeAndGesture extends Service implements SensorEventListener{

    public static final String BROADCAST_ACTION = "com.example.visualaid";

    private SensorManager accelManage;
    private Sensor senseAccel;
    float accelValuesX[] = new float[128];
    float accelValuesY[] = new float[128];
    float accelValuesZ[] = new float[128];
    int index = 0;
    int k=0;
    Bundle b;

    Handler handler;

    Intent sendingIntent;

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            index++;
            accelValuesX[index] = sensorEvent.values[0];
            accelValuesY[index] = sensorEvent.values[1];
            accelValuesZ[index] = sensorEvent.values[2];
            if(index >= 127){
                index = 0;
                accelManage.unregisterListener(this);
                callFallRecognition();
                accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }


    public void callFallRecognition(){
        float prev = 0;
        float curr = 0;
        prev = 10;
        for(int i=11;i<128;i++){
            curr = accelValuesZ[i];
            if(Math.abs(prev - curr) > 10 ){
                Log.i("info", "fall");
                ToneGenerator toneGen1 = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
                toneGen1.startTone(ToneGenerator.TONE_CDMA_PIP,150);
            }
        }
    }
    /*
    public void callGestureRecognition(){
        float avgX = 0;
        float avgY = 0;
        float avgZ = 0;
        //Toast.makeText(this, "I am in gesture recognition", Toast.LENGTH_LONG).show();
        for(int i=0;i<128;i++){
            avgX = avgX + accelValuesX[i];
            avgY = avgY + accelValuesY[i];
            avgZ = avgZ + accelValuesZ[i];

        }
        avgX = avgX/128;
        avgY = avgY/128;
        avgZ = avgZ/128;

        boolean left = true;

        int zeroCrossingX = 0;
        if(accelValuesX[0] >= avgX){
            left = true;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesX[i] < avgX){
                        zeroCrossingX++;
                        left = false;
                    }
                }else{
                    if(accelValuesX[i] >= avgX){
                        zeroCrossingX++;
                        left = true;
                    }
                }
            }
        }else{
            left = false;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesX[i] < avgX){
                        zeroCrossingX++;
                        left = false;
                    }
                }else{
                    if(accelValuesX[i] >= avgX){
                        zeroCrossingX++;
                        left = true;
                    }
                }
            }
        }

        int zeroCrossingY = 0;
        if(accelValuesY[0] >= avgY){
            left = true;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesY[i] < avgY){
                        zeroCrossingY++;
                        left = false;
                    }
                }else{
                    if(accelValuesY[i] >= avgY){
                        zeroCrossingY++;
                        left = true;
                    }
                }
            }
        }else{
            left = false;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesY[i] < avgY){
                        zeroCrossingY++;
                        left = false;
                    }
                }else{
                    if(accelValuesY[i] >= avgY){
                        zeroCrossingY++;
                        left = true;
                    }
                }
            }
        }

        int zeroCrossingZ = 0;
        if(accelValuesZ[0] >= avgZ){
            left = true;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesZ[i] < avgZ){
                        zeroCrossingZ++;
                        left = false;
                    }
                }else{
                    if(accelValuesZ[i] >= avgZ){
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }else{
            left = false;
            for(int i=0;i<128;i+=8){
                if(left){
                    if(accelValuesZ[i] < avgZ){
                        zeroCrossingZ++;
                        left = false;
                    }
                }else{
                    if(accelValuesZ[i] >= avgZ){
                        zeroCrossingZ++;
                        left = true;
                    }
                }
            }
        }

        if(zeroCrossingX > 7 || zeroCrossingY > 7 || zeroCrossingZ > 7){

            Toast.makeText(this, "Hi gesture", Toast.LENGTH_LONG).show();
            if(k == 0){
                sendSMS();
            }
            zeroCrossingX = 0;
            zeroCrossingY = 0;
            zeroCrossingZ = 0;
            k++;

        }


    }
    */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onCreate(){
        Log.i("info", "Drop notice and Gesture service start");
        accelManage = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senseAccel = accelManage.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accelManage.registerListener(this, senseAccel, SensorManager.SENSOR_DELAY_NORMAL);

        sendingIntent = new Intent(BROADCAST_ACTION);

    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        b = intent.getExtras();
        String phoneNumber = b.getString("phone");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
