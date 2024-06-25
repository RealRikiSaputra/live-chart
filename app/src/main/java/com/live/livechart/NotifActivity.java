package com.live.livechart;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NotifActivity extends AppCompatActivity {

    TextView tvMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notif);

        tvMsg = findViewById(R.id.txtNotif);
        String data = getIntent().getStringExtra("data");
        tvMsg.setText(data);
    }
}