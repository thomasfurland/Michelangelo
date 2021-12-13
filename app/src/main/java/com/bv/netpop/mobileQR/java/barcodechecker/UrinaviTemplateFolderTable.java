package com.bv.netpop.mobileQR.java.barcodechecker;

import org.ksoap2.serialization.SoapObject;

public class UrinaviTemplateFolderTable extends UrinaviResponse {
    public UrinaviTemplateFolderTable(SoapObject response) {
        super(response);
        PrimaryKey = "TemplateFolderID";
    }

    public String GetName(String TemplateFolderID) {
        String col = "TemplateFolderName";
        return lookupItem(TemplateFolderID, col);
    }
}
