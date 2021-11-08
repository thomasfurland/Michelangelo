package com.bv.netpop.mobileQR.java.barcodechecker;

import org.ksoap2.serialization.SoapObject;

public class UrinaviTemplateFolderTable extends UrinaviResponse{

    public UrinaviTemplateFolderTable(SoapObject response) {
        super(response);
        DataTable = extractTable(rawResponse);
        PrimaryKey = "TemplateFolderID";
    }

    public String GetName(String TemplateFolderID) {
        String priceCol = "TemplateFolderName";
        return lookupItem(TemplateFolderID, priceCol);
    }

    public String GetFolderID(String TemplateFolderID) {
        String returnValue = "";
        switch (TemplateFolderID) {
            case "":
                break;
            case "1":
                returnValue = "1";
                break;
            default:
                returnValue = "";
        }
        return returnValue;
    }

}
