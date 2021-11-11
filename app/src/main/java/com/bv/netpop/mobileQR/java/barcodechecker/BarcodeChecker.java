package com.bv.netpop.mobileQR.java.barcodechecker;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import com.bv.netpop.mobileQR.java.barcodes.BarcodeBase;
import com.bv.netpop.mobileQR.java.barcodes.POPQRBarcode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

@RequiresApi(api = Build.VERSION_CODES.O)
public class BarcodeChecker  {

    private static final String WSDL_TARGET_NAMESPACE = "http://tempuri.org/";
    private static final String SOAP_ADDRESS = "http://104.214.148.221/urinaviSysWS/UriNaviWS.asmx";

    public void checkBarcode(POPQRBarcode bc) {
        String sSQL = "select * from TB_STDMAST where F1 = '%s'";
        String janCode = bc.getParsedValue().get("JAN");
        sSQL = String.format(sSQL, janCode);

        SoapObject response = this.SQL_ExecuteSQLAdapterRequest("Demo","TB_STDMAST",sSQL);
        UrinaviMasterTable master = new UrinaviMasterTable(response);

        if (master.ContainsItem(janCode)) {
            bc.setItemName(master.GetItemName(janCode));
            String correctPrice = "";
            Boolean isCorrect = false;
            switch (bc.getParsedValue().get("POP種類")) {
                case "7": //POP
                    correctPrice = master.GetPrice(janCode);
                    isCorrect = correctPrice.equals(bc.getParsedValue().get("定番売価"));
                    break;
                case "8": //特売
                    correctPrice = master.GetSalePrice(janCode);
                    isCorrect = correctPrice.equals(bc.getParsedValue().get("POP売価"));
            }
            if (isCorrect) {
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
            bc.errorComment = "JAN違い";
        }
    }

    public void GetFolderName(POPQRBarcode bc) {
        String sSQL = "select * from TB_FOLDER_TEMPLATE where TemplateFolderID = '%s'";
        String templateFolderID = bc.getParsedValue().get("POP種類");
        if(templateFolderID.equals("")) templateFolderID = "1";
        sSQL = String.format(sSQL, templateFolderID);

        SoapObject response = this.SQL_ExecuteSQLAdapterRequest("Demo","TB_FOLDER_TEMPLATE",sSQL);
        UrinaviTemplateFolderTable folderTable = new UrinaviTemplateFolderTable(response);

        if (folderTable.ContainsItem(templateFolderID)) {
            bc.setPOPType(folderTable.GetName(templateFolderID));
            return;
        }
        bc.barcodeStatus = BarcodeBase.status.Incorrect;
        bc.errorComment = "POP違い";
    }

    public void CheckPOPType(POPQRBarcode bc) {
        String sSQL = "select * from TB_STDMAST as m ";
        sSQL += "join TB_POPTEMPLATE as t on m.T1 = 'demo.' + t.POPTemplateID ";
        sSQL += "where F1 = '%s'";

        String janCode = bc.getParsedValue().get("JAN");
        sSQL = String.format(sSQL, janCode);

        SoapObject response = this.SQL_ExecuteSQLAdapterRequest("Demo","TB_STDMAST",sSQL);
        UrinaviMasterDisplayTable MasterDisplayTable = new UrinaviMasterDisplayTable(response);

        String templateFolderID = bc.getParsedValue().get("POP種類");

        if (MasterDisplayTable.ContainsItem(janCode)) {
            String correctType = MasterDisplayTable.GetFolderID(templateFolderID);
            if (correctType.equals(templateFolderID)) {
                bc.barcodeStatus = BarcodeBase.status.Correct;
                return;
            }
        }

        bc.barcodeStatus = BarcodeBase.status.Incorrect;
        bc.errorComment = "POP違い";

    }

    public void SendReprintItems(ArrayList<POPQRBarcode> bc) {

        String folderPath = "E:\\inetpub\\ftproot\\demo";
        String fileName = this.getFileName();
        String data = this.encodeItemCodes(bc);
        Boolean overWrite = true;

        SoapObject response = this.FTP_UpLoadFileRequest("Demo",folderPath,fileName,data,overWrite);

    }

    private String encodeItemCodes(ArrayList<POPQRBarcode> bc) {
        String JanList = bc.stream()
                .map(x -> x.getParsedValue().get("JAN"))
                .collect(Collectors.joining("\r\n"));

        String encodedString = Base64.getEncoder().encodeToString(JanList.getBytes());

        return encodedString;
    }

    private String getFileName() {
        String filename;
        String template = "'%s'_'%s'_'%s'.csv ";

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        filename = String.format(template, "9011",formatter.format(date), "00000");

        return filename;
    }

    private SoapObject SQL_ExecuteSQLAdapterRequest(String clientID, String tableName, String sSQL) {

        final String SOAP_ACTION = "http://tempuri.org/SQL_ExecuteSQLAdapter";
        final String OPERATION_NAME = "SQL_ExecuteSQLAdapter";

        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        request.addProperty("ClientID",clientID);
        request.addProperty("sSQL", sSQL);
        request.addProperty("TableName", tableName);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

        SoapObject response = null;

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            response = (SoapObject) envelope.getResponse();

        } catch (Exception exception) {
            String log = exception.getMessage();
        }

        return response;
    }

    private SoapObject FTP_UpLoadFileRequest(String clientID, String folderPath,String fileName, String data, Boolean Overwrite) {

        final String SOAP_ACTION = "http://tempuri.org/FTP_UpLoadFile";
        final String OPERATION_NAME = "FTP_UpLoadFile";

        SoapObject request = new SoapObject(WSDL_TARGET_NAMESPACE, OPERATION_NAME);
        request.addProperty("ClientID",clientID);
        request.addProperty("FolderPath", folderPath);
        request.addProperty("FileName", fileName);
        request.addProperty("Base64", data);
        request.addProperty("OverWrite", Overwrite);

        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransport = new HttpTransportSE(SOAP_ADDRESS);

        SoapObject response = null;

        try {
            httpTransport.call(SOAP_ACTION, envelope);
            response = (SoapObject) envelope.getResponse();

        } catch (Exception exception) {
            String log = exception.getMessage();
        }

        return response;
    }

}
