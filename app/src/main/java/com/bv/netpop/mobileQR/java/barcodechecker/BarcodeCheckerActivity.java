package com.bv.netpop.mobileQR.java.barcodechecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bv.netpop.mobileQR.R;
import com.bv.netpop.mobileQR.java.CameraXLivePreviewActivity;

import java.util.ArrayList;

public class BarcodeCheckerActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private ArrayList<String> barcodes = new ArrayList<>(0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_entry_choice);
        TextView tv = findViewById(R.id.scanner_entry_point);

        if (savedInstanceState != null) {
            barcodes = savedInstanceState.getStringArrayList("BarcodeArray");
        } else {
            barcodes.add(0, "Read QR Codes:");
        }

        ArrayList bc_list = getIntent().getStringArrayListExtra("BarcodeArray");
        if (bc_list != null) {
            barcodes = bc_list;
        }

        gestureDetector = new GestureDetector(this, new BarcodeCheckerActivity.GestureListener());
        tv.setOnTouchListener(
                (v, event) -> gestureDetector.onTouchEvent(event)
        );

        // add gridview of barcode objects, add verify button, add make corrected genko button
        //overlay correct barcodes with green border and incorrect border with red

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putStringArrayList("BarcodeArray",barcodes);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
                else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffY > 0) {
                        onSwipeBottom();
                    } else {
                        onSwipeTop();
                    }
                    result = true;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
        Intent intent = new Intent(this, CameraXLivePreviewActivity.class);
        intent.putStringArrayListExtra("BarcodeArray",barcodes);
        startActivity(intent);
    }

    public void onSwipeLeft() {

    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}
