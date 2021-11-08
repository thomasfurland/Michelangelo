package com.bv.netpop.mobileQR.java.barcodechecker;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class UrinaviMasterTable extends UrinaviResponse {

    public UrinaviMasterTable(SoapObject response) {
        super(response);
        DataTable = extractTable(rawResponse);
        PrimaryKey = "F1";
    }

    public String GetPrice(String janCode) {
        String priceCol = "F5";
        return lookupItem(janCode, priceCol);
    }

    public String GetItemName(String janCode) {
        String itemNameCol = "F3";
        return lookupItem(janCode,itemNameCol);
    }


}
