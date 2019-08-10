package com.example.visualaid;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class GoogleAPIService extends Service {
    public static final String BROADCAST_ACTION = "com.example.visualaid";
    Intent intent;

    @Override
    public void onCreate() {
        super.onCreate();
        intent = new Intent(BROADCAST_ACTION);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendMessage("google service start");
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }

    protected void sendMessage(final String msg) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                intent.putExtra("consoleMsg", msg);
                Log.i("info", "service: "+intent.getStringExtra("consoleMsg"));
                sendBroadcast(intent);
            }
        };
        Thread msgThread = new Thread(runnable);
        msgThread.start();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
