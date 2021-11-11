package com.bv.netpop.mobileQR.java.barcodes;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POPQRBarcode extends BarcodeBase implements Parcelable {

    public POPQRBarcode(String value) {
        super(value);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Map<String, String> setParsedValue(String value) {
        Map<String,String> newParsedValue = new HashMap<>();

        List<String> sections = Arrays.asList(value.split("\\+",-1));

        if (sections.size() == 5) {
            newParsedValue.put("JOB",sections.get(0));
            newParsedValue.put("JAN",sections.get(1));
            newParsedValue.put("POP種類",sections.get(2));
            newParsedValue.put("定番売価",sections.get(3));
            newParsedValue.put("POP売価",sections.get(4));
            //updated later after successful read.
            newParsedValue.put("品名","");
            newParsedValue.put("POP種類名","");


            //newParsedValue.put("MM個数","");
            //newParsedValue.put("MM売価","");
        }
        return newParsedValue;
    }

    public void setItemName(String value) {
        this._parsedValue.put("品名",value);
    }

    public void setPOPType(String value) {
        this._parsedValue.put("POP種類名",value);
    }

}
