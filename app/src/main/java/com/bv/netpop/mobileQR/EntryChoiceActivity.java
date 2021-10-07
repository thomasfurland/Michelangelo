package com.bv.netpop.mobileQR;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import com.bv.netpop.mobileQR.java.ChooserActivity;

public class EntryChoiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_entry_choice);
        TextView tv = findViewById(R.id.scanner_entry_point);

        tv.setOnClickListener(
            v -> startActivity(new Intent(this, ChooserActivity.class)));
    }
}
