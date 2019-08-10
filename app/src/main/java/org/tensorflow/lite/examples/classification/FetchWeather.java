package com.example.visualaid;

import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FetchWeather extends AsyncTask {
    private double latitude, longitude;

    public FetchWeather(double latitude, double longitude) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        Log.i("info", "lat and lon "+latitude+" "+longitude);
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        String API_KEY="bed14f6a8ba4dee61a3cbf811fe3e12c", API_LINK="api.openweathermap.org/data/2.5/weather";

        try{
            URL url = new URL("https://api.openweathermap.org/data/2.5/weather?lat="
                    +latitude+"&lon="+longitude+"&appid=bed14f6a8ba4dee61a3cbf811fe3e12c");

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            if(connection.getResponseCode() == 200){
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder sb= new StringBuilder();
                String line = "";
                while((line=bufferedReader.readLine())!=null){
                    sb.append(line);
                }
                String response = sb.toString();
                Log.i("weather", response);
                connection.disconnect();
                return parseJson(response);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String parseJson(String response){
        JsonObject jsonObject = new Gson().fromJson(response, JsonObject.class);
        JsonArray weather = jsonObject.get("weather").getAsJsonArray();
        JsonObject description = weather.get(0).getAsJsonObject();
        String res=description.get("description").getAsString();
        return res;
    }
}
