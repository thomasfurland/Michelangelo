package com.bv.netpop.mobileQR;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.bv.netpop.mobileQR.java.ChooserActivity;
import com.bv.netpop.mobileQR.java.barcodechecker.BarcodeChecker;
import com.google.mlkit.vision.barcode.Barcode;

public class EntryChoiceActivity extends AppCompatActivity {

    public Boolean ConnectedToWifi = false;
    private AsyncTask<Void, Void, Boolean> CheckWifiTask;
    private Boolean RepeatAttempt = true;

    @RequiresApi(api = VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_entry_choice);
        TextView tv = findViewById(R.id.scanner_entry_point);

        tv.setOnClickListener(
            v -> {

                try {
                    if (RepeatAttempt) {
                        CheckWifiTask = new ServerConnectTask().execute();
                    }
                    ConnectedToWifi = CheckWifiTask.get();
                }catch(Exception ex){
                    ConnectedToWifi = false;
                }

                if(ConnectedToWifi) {
                    startActivity(new Intent(this, ChooserActivity.class));
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "WiFiに接続してません", Toast.LENGTH_LONG);
                    toast.show();
                }
            });

         CheckWifiTask = new ServerConnectTask().execute();
    }

    class ServerConnectTask extends AsyncTask<Void, Void, Boolean> {

        @RequiresApi(api = VERSION_CODES.O)
        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                BarcodeChecker bc = new BarcodeChecker();
                return bc.PingServer();
            }catch (Exception ex) {
                return false;
            }

        }
    }
}
