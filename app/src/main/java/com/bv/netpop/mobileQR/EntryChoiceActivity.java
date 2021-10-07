package com.bv.netpop.mobileQR;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import com.bv.netpop.mobileQR.java.ChooserActivity;

public class EntryChoiceActivity extends AppCompatActivity
        implements View.OnClickListener {

    private static final Class<?>[] CLASSES = new Class<?>[] {
            ChooserActivity.class
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_entry_choice);

        TextView tv = findViewById(R.id.scanner_entry_point);
        tv.setOnClickListener((View.OnClickListener) this);
    }

    @Override
    public void onClick(View v) {
        Class<?> clicked = CLASSES[0];
        startActivity(new Intent(this, clicked));
    }
}
