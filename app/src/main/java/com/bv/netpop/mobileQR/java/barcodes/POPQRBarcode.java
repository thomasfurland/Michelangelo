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

        List<String> sections = Arrays.asList(value.split("\\+"));

        newParsedValue.put("JOB",sections.get(0));
        newParsedValue.put("JAN",sections.get(1));
        newParsedValue.put("品名",sections.get(2));
        newParsedValue.put("POP種類",sections.get(3));
        newParsedValue.put("テンプレートCD",sections.get(4));
        newParsedValue.put("定番売価",sections.get(5));
        newParsedValue.put("POP売価",sections.get(6));
        newParsedValue.put("MM個数",sections.get(7));
        newParsedValue.put("MM売価",sections.get(8));

        return newParsedValue;
    }

}
