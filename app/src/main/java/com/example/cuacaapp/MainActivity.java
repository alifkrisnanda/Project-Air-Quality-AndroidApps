package com.example.cuacaapp;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String THINGSPEAK_API_URL = "https://api.thingspeak.com/channels/2358270/feeds.json?api_key=A8B4KGYDBHSNQYSO&results=2";

    private EditText editTextField1;
    private EditText getEditTextField2;
    private EditText getEditTextField3;
    private EditText getEditTextField4;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private  Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextField1 = findViewById(R.id.editTextText3);
        getEditTextField2 = findViewById(R.id.editTextText4);
        getEditTextField3 = findViewById(R.id.editTextText5);
        getEditTextField4 = findViewById(R.id.editTextText6);

        // Inisialisasi dan jalankan tugas asinkron untuk mendapatkan data dari Thingspeak API
        new FetchThingspeakDataTask().execute();

        int[] editTextIds = {R.id.editTextText3, R.id.editTextText4, R.id.editTextText5, R.id.editTextText6};

        for (int id : editTextIds) {
            EditText editText = findViewById(id);
            editText.setEnabled(false);
        }
//        setup runable
        runnable = new Runnable() {
            @Override
            public void run() {
                new FetchThingspeakDataTask().execute();
                handler.postDelayed(this, 100);
            }
        };
        handler.postDelayed(runnable, 0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Hentikan polling ketika aplikasi dihancurkan
        handler.removeCallbacks(runnable);
    }

    private class FetchThingspeakDataTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... voids) {
            try {
                URL url = new URL(THINGSPEAK_API_URL);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                try {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }
                    bufferedReader.close();
                    return stringBuilder.toString();
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching data from Thingspeak API", e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray feeds = jsonObject.getJSONArray("feeds");

                    // Ambil data dari entri terbaru
                    if (feeds.length() > 0) {
                        JSONObject latestFeed = feeds.getJSONObject(feeds.length() - 1);

                        // Tampilkan data Field 1 di editTextField1
                        double field1Value = latestFeed.getDouble("field1");
                        long roundedValue1 = Math.round(field1Value);
                        editTextField1.setText(String.valueOf(roundedValue1)+ " " + "°C");

                        // Tampilkan data Field 2 di editTextField4
                        double field2Value = latestFeed.getDouble("field2");
                        long roundedValue2 = Math.round(field2Value);
                        getEditTextField2.setText(String.valueOf(roundedValue2)+ " "+ "%");

                        //datafield 3
                        double field3Value = latestFeed.getDouble("field3");
                        long roundedValue3 = Math.round(field3Value);
                        getEditTextField3.setText(String.valueOf(roundedValue3) + " "+ "PPB");

                        //datafield4
                        double field4Value = latestFeed.getDouble("field4");
                        long roundedValue4 = Math.round(field4Value);
                        getEditTextField4.setText(String.valueOf(roundedValue4) + " "+ "PPM");
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Error parsing Thingspeak API response", e);
                }
            }
        }
    }
}