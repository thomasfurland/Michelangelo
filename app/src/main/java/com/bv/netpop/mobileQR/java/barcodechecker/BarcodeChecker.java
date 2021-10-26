package com.bv.netpop.mobileQR.java.barcodechecker;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
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

import java.util.ArrayList;

public class BarcodeChecker  {

    private static final String SOAP_ACTION = "http://tempuri.org/SQL_ExecuteSQLAdapter";
    private static final String OPERATION_NAME = "SQL_ExecuteSQLAdapter";
    private static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private static final String SOAP_ADDRESS = "http://104.214.148.221/urinaviSysWS/UriNaviWS.asmx";

    public void checkBarcode(ArrayList<POPQRBarcode> barcodes) {

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

}
