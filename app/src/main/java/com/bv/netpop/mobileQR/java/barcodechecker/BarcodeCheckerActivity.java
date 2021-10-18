package com.bv.netpop.mobileQR.java.barcodechecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.bv.netpop.mobileQR.R;
import com.bv.netpop.mobileQR.java.BarcodeBase;
import com.bv.netpop.mobileQR.java.CameraXLivePreviewActivity;

import java.util.ArrayList;

public class BarcodeCheckerActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private ArrayList<BarcodeBase> barcodes = new ArrayList<>(0);

    private static final String SOAP_ACTION = "http://tempuri.org/CFG_GetClientConfig";
    private static final String OPERATION_NAME = "CFG_GetClientConfig";
    private static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private static final String SOAP_ADDRESS = "http://104.214.148.221/urinaviSysWS/UriNaviWS.asmx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_vision_entry_choice);
        TextView tv = findViewById(R.id.scanner_entry_point);

        if (savedInstanceState != null) {
            barcodes = savedInstanceState.getParcelableArrayList("BarcodeArray");
        } else {
            barcodes.add(0, new BarcodeBase("Read QR Codes:"));
        }

        ArrayList bc_list = getIntent().getParcelableArrayListExtra("BarcodeArray");
        if (bc_list != null) {
            barcodes = bc_list;
        }

        gestureDetector = new GestureDetector(this, new BarcodeCheckerActivity.GestureListener());
        tv.setOnTouchListener(
                (v, event) -> gestureDetector.onTouchEvent(event)
        );

        // add gridview of barcode objects, add verify button, add make corrected genko button
        //overlay correct barcodes with green border and incorrect border with red

        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        request.addProperty("ClientID","manabe");

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

        try
        {
            httpTransport.call(SOAP_ACTION, envelope);
            Object response = envelope.getResponse();
            String res = response.toString();
            String stop = res;
            //do stuff.

        }
        catch (Exception exception) {
            String log = exception.getMessage();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle bundle) {
        super.onSaveInstanceState(bundle);
        bundle.putParcelableArrayList("BarcodeArray",barcodes);
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
        intent.putParcelableArrayListExtra("BarcodeArray",barcodes);
        startActivity(intent);
    }

    public void onSwipeLeft() {

    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}
