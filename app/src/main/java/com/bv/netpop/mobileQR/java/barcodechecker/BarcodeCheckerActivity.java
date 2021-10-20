package com.bv.netpop.mobileQR.java.barcodechecker;

import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.bv.netpop.mobileQR.R;
import com.bv.netpop.mobileQR.java.barcodes.BarcodeBase;
import com.bv.netpop.mobileQR.java.CameraXLivePreviewActivity;
import com.bv.netpop.mobileQR.java.barcodes.POPQRBarcode;
import com.bv.netpop.mobileQR.java.barcodescanner.BarcodeActivityAdapter;

import java.util.ArrayList;

public class BarcodeCheckerActivity extends AppCompatActivity {

    private GestureDetector gestureDetector;
    private ArrayList<POPQRBarcode> barcodes = new ArrayList<>(0);
    private BarcodeCheckerAdapter checkerAdapter;

    private Boolean activeFlag;

    private static final String SOAP_ACTION = "http://tempuri.org/SQL_ExecuteSQLAdapter";
    private static final String OPERATION_NAME = "SQL_ExecuteSQLAdapter";
    private static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private static final String SOAP_ADDRESS = "http://104.214.148.221/urinaviSysWS/UriNaviWS.asmx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.barcode_check_menu);
        View tv = findViewById(R.id.priceCheckActivity);
        View cv = findViewById(R.id.barcodeCheckerRecyclerView);

        if (savedInstanceState != null) {
            barcodes = savedInstanceState.getParcelableArrayList("BarcodeArray");
            activeFlag = savedInstanceState.getBoolean("ActiveFlag");
        } else {
            barcodes.add(0, new POPQRBarcode("Read QR Codes:"));
        }

        ArrayList<BarcodeBase> bc_list = getIntent().getParcelableArrayListExtra("BarcodeArray");
        activeFlag = getIntent().getBooleanExtra("ActiveFlag",false);
        if (bc_list != null) {
            barcodes = new ArrayList<>(0);
            for (BarcodeBase bc : bc_list) {
                POPQRBarcode qrBC = new POPQRBarcode(bc.rawValue);
                qrBC.barcodeStatus = bc.barcodeStatus;
                barcodes.add(qrBC);
            }
        }

        gestureDetector = new GestureDetector(this, new BarcodeCheckerActivity.GestureListener());
        tv.setOnTouchListener(
                (v, event) -> gestureDetector.onTouchEvent(event)
        );
        cv.setOnTouchListener(
                (v, event) -> gestureDetector.onTouchEvent(event)
        );

        RecyclerView rvBarcodeChecks = findViewById(R.id.barcodeCheckerRecyclerView);
        checkerAdapter = new BarcodeCheckerAdapter(barcodes);
        rvBarcodeChecks.setAdapter(checkerAdapter);
        rvBarcodeChecks.setLayoutManager(new LinearLayoutManager(this));

        // add gridview of barcode objects, add verify button, add make corrected genko button
        //overlay correct barcodes with green border and incorrect border with red

        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        request.addProperty("ClientID","demo");
        request.addProperty("sSQL", "select top 5 * from TB_STDMAST");
        request.addProperty("TableName", "TB_STDMAST");


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

            POPQRBarcode bc = barcodes.get(1);
            bc.barcodeStatus = BarcodeBase.status.Correct;
            barcodes.set(1,bc);

            POPQRBarcode bc2 = barcodes.get(2);
            bc2.barcodeStatus = BarcodeBase.status.Incorrect;
            bc2.errorType = "123";
            bc2.errorComment = "売価ミス";
            barcodes.set(2,bc2);

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
        intent.putExtra("ActiveFlag",activeFlag);
        startActivity(intent);
    }

    public void onSwipeLeft() {

    }

    public void onSwipeTop() {
    }

    public void onSwipeBottom() {
    }

}
