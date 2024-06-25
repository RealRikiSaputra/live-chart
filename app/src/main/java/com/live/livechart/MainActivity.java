package com.live.livechart;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Handler handler;
    private Runnable runnable;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.lineChart);
        client = new OkHttpClient();
        handler = new Handler();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if(ContextCompat.checkSelfPermission(MainActivity.this,
            android.Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        runnable = new Runnable() {
            @Override
            public void run() {
                fetchData();
                handler.postDelayed(this, 5000);
            }
        };
        handler.post(runnable);
    }

    private void fetchData() {
        String url = "https://json.link/cQd2bE3yJ9.json";

        Request request = new Request.Builder().url(url).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonData = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonData);

                        // Inisialisasi ArrayList untuk menyimpan semua Entry Lists
                        ArrayList<ArrayList<Entry>> allEntries = new ArrayList<>();

                        // Ambil semua kunci dari objek JSON
                        Iterator<String> keys = jsonObject.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            JSONArray jsonArray = jsonObject.getJSONArray(key);

                            // Convert JSON array to ArrayList, passing the key as array name
                            ArrayList<Entry> entries = parseJsonArrayToEntries(jsonArray, key);
                            allEntries.add(entries);
                        }

                        // Create LineData object
                        List<ILineDataSet> dataSets = new ArrayList<>();
                        for (int i = 0; i < allEntries.size(); i++) {
                            LineDataSet dataSet = new LineDataSet(allEntries.get(i), "Data " + (i + 1));
                            dataSet.setColors(ColorTemplate.COLORFUL_COLORS[i % ColorTemplate.COLORFUL_COLORS.length]);
                            dataSet.setValueTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));
                            dataSets.add(dataSet);
                        }

                        LineData lineData = new LineData(dataSets);
                        lineData.setValueTextColor(ContextCompat.getColor(MainActivity.this, android.R.color.white));

                        // Update UI on main thread
                        runOnUiThread(() -> {
                            lineChart.setData(lineData);
                            lineChart.invalidate();
                        });

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private ArrayList<Entry> parseJsonArrayToEntries(JSONArray jsonArray, String arrayName) {
        ArrayList<Entry> entries = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject dataPoint = jsonArray.getJSONObject(i);
                float x = (float) dataPoint.getDouble("x");
                float y = (float) dataPoint.getDouble("y");

                if (y < 10 || y > 80) {
                    runOnUiThread(() -> {
                        notif();
//                        Toast.makeText(MainActivity.this, "Nilai data di luar batas (10-80) = " + arrayName + ": "+ x + y, Toast.LENGTH_SHORT).show();
                    });
                }

                entries.add(new Entry(x, y));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entries;
    }

    public void notif(){
        String chanelID = "CHANNEL_ID_NOTIFICATION";
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), chanelID);
        builder.setSmallIcon(R.drawable.notif)
                .setContentTitle("Notif Live Chart")
                .setContentText("Isi Notif")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent intent = new Intent(getApplicationContext(), NotifActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("data","Ini Activity Notif Cuy");

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, PendingIntent.FLAG_MUTABLE);
        builder.setContentIntent(pendingIntent);
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            NotificationChannel notificationChannel =
                    notificationManager.getNotificationChannel(chanelID);
            if (notificationChannel == null) {
                int importance = NotificationManager.IMPORTANCE_HIGH;
                notificationChannel = new NotificationChannel(chanelID,
                        "description", importance);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(0, builder.build());
    }
}
