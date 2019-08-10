package org.tensorflow.lite.examples.classification;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.tensorflow.lite.examples.classification.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    private static final int MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION = 1;
    private ArrayList<String> arrayList;
    private ArrayAdapter<String> adapter;
    private ListView listView;

    private FusedLocationProviderClient fusedLocationClient;
    private String address;
    protected Location lastLocation;
    private double latitude, longitue;
    public AddressResultReceiver resultReceiver;

    private TextToSpeech textToSpeech;

    private SensorManager sensorManager;
    private Sensor sensor;

    private boolean hasFlash, flashOn;
    static Camera cam;

    private Intent acService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(
                com.example.visualaid.GoogleAPIService.BROADCAST_ACTION));
        sensorManager.registerListener(this, sensor, sensorManager.SENSOR_DELAY_NORMAL);
    }

    private void initialize(){
        arrayList = new ArrayList();
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        listView = (ListView) findViewById(R.id.console);
        listView.setAdapter(adapter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        address="empty";

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status==TextToSpeech.SUCCESS){
                    int res = textToSpeech.setLanguage(Locale.ENGLISH);
                    if(res==TextToSpeech.LANG_MISSING_DATA||res==TextToSpeech.LANG_NOT_SUPPORTED){
                        sendToConsole("language not supported");
                    }
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        flashOn = false;

        acService = new Intent(MainActivity.this, com.example.visualaid.DropNoticeAndGesture.class);
        Bundle b = new Bundle();
        b.putString("phone", "1234");
        acService.putExtras(b);
        startService(acService);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("consoleMsg");
            sendToConsole(msg);
        }
    };

    public void sendToConsole(String msg){
        arrayList.add(0,msg);
        adapter.notifyDataSetChanged();
    }

    public void startService(View view){
        Intent intent = new Intent(this, com.example.visualaid.GoogleAPIService.class);
        startService(intent);
    }

    public void getLocation(View view){
        fetchLocation();
    }

    private void fetchLocation() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION);
            }
        } else {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitue = location.getLongitude();
                                Geocoder geocoder;
                                List<Address> addresses = null;
                                geocoder = new Geocoder(MainActivity.this, Locale.getDefault());

                                try {
                                    addresses = geocoder.getFromLocation(latitude, longitue, 1);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                String address = addresses.get(0).getAddressLine(0);
                                String street = address.substring(0, address.indexOf(','));
                                String city = addresses.get(0).getLocality();
                                String state = addresses.get(0).getAdminArea();
                                String country = addresses.get(0).getCountryName();
                                String postalCode = addresses.get(0).getPostalCode();
                                String knownName = addresses.get(0).getFeatureName();

                                sendToConsole("address: "+street);
                                speakText("you are at "+street);

                                try {
                                    String weather = (String) new com.example.visualaid.FetchWeather(latitude, longitue).execute().get();
                                    sendToConsole("weather: "+weather);
                                    speakText("the weather is "+weather);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }
    }

    private void speakText(String text){
        textToSpeech.speak(text, TextToSpeech.QUEUE_ADD, null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==MY_PERMISSIONS_REQUEST_ACCESS_COARSE_LOCATION){
            if(grantResults.length>0&&grantResults[0] == PackageManager.PERMISSION_GRANTED){

            }
        }

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_LIGHT){
            hasFlash = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
            if(sensorEvent.values[0]<1000){
                if(!flashOn){
                    sendToConsole("false on");
                    flashOn = true;
                    /*
                    cam = Camera.open();
                    Camera.Parameters p = cam.getParameters();
                    p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    cam.setParameters(p);
                    cam.startPreview();
                    */
                }
            }else{
                if(flashOn){
                    sendToConsole("false off");
                    flashOn = false;
                    /*
                    cam.stopPreview();
                    cam.release();
                    cam = null;
                    */
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData == null) {
                return;
            }

            address = resultData.getString(com.example.visualaid.Constants.RESULT_DATA_KEY);
            if (address == null) {
                address = "";
            }

            if (resultCode == com.example.visualaid.Constants.SUCCESS_RESULT) {
                Log.i("address", address);
            }

        }
    }

}
