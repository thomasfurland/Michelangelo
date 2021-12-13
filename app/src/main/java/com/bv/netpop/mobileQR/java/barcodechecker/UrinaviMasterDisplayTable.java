package com.bv.netpop.mobileQR.java.barcodechecker;

import org.ksoap2.serialization.SoapObject;

public class UrinaviMasterDisplayTable extends UrinaviResponse{

    public UrinaviMasterDisplayTable(SoapObject response) {
        super(response);
        PrimaryKey = "F1";
    }

    public String GetFolderID(String TemplateFolderID) {
        String col = "TemplateFolderID";
        return lookupItem(TemplateFolderID, col);
    }
}
