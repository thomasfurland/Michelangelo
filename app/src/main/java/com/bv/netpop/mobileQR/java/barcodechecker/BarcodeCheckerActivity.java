package com.bv.netpop.mobileQR.java.barcodechecker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
        Button btn = findViewById(R.id.send_button);

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
                qrBC.errorComment = bc.errorComment;
                qrBC.errorType = bc.errorType;
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
        btn.setOnClickListener(
                v -> {
                    AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                    dlgAlert.setMessage("POP要求印刷リストを売技ナビへ出力します。");
                    dlgAlert.setTitle("POP要求印刷リスト出力");
                    dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast toast = Toast.makeText(getApplicationContext(), "出力完了", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    });
                    dlgAlert.setCancelable(true);
                    dlgAlert.create().show();
                }
        );

        RecyclerView rvBarcodeChecks = findViewById(R.id.barcodeCheckerRecyclerView);
        checkerAdapter = new BarcodeCheckerAdapter(barcodes);
        rvBarcodeChecks.setAdapter(checkerAdapter);
        rvBarcodeChecks.setLayoutManager(new LinearLayoutManager(this));

        String sSQL = "select * from TB_STDMAST where F1 in (%s)";
        String jan_codes = "";
        for (POPQRBarcode bc: barcodes) {
            if (bc.rawValue.equals("Read QR Codes:")) continue;
            if (bc.barcodeStatus == BarcodeBase.status.Correct || bc.barcodeStatus == BarcodeBase.status.Incorrect) continue;
                jan_codes = jan_codes.concat("'");
                jan_codes = jan_codes.concat(bc.getParsedValue().get("JAN"));
                jan_codes = jan_codes.concat("', ");
        }
        if (jan_codes.equals("")) return;
        jan_codes = (String) jan_codes.subSequence(0, jan_codes.length() - 2);
        sSQL = String.format(sSQL, jan_codes);

        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        request.addProperty("ClientID","demo");
        request.addProperty("sSQL", sSQL);
        request.addProperty("TableName", "TB_STDMAST");


        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

        try
        {
            httpTransport.call(SOAP_ACTION, envelope);
            SoapObject response = (SoapObject) envelope.getResponse();
            UrinaviMasterTable master = new UrinaviMasterTable(response);

            for (POPQRBarcode bc: barcodes) {
                if (bc.rawValue.equals("Read QR Codes:")) continue;
                if (bc.barcodeStatus == BarcodeBase.status.Correct || bc.barcodeStatus == BarcodeBase.status.Incorrect) continue;
                String janCode = bc.getParsedValue().get("JAN");
                if (master.Match(janCode)) {
                    String correctPrice = master.GetPrice(janCode);
                    if (correctPrice.equals(bc.getParsedValue().get("POP売価"))) {
                        bc.barcodeStatus = BarcodeBase.status.Correct;
                    } else {
                        bc.barcodeStatus = BarcodeBase.status.Incorrect;
                        bc.errorType = correctPrice;
                        bc.errorComment = "売価ミス";
                    }
                }
                else {
                    bc.barcodeStatus = BarcodeBase.status.Incorrect;
                    bc.errorType = "N/A";
                    bc.errorComment = "POP違い";
                }
            }

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
