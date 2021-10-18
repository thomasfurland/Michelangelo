package com.bv.netpop.mobileQR.java;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POPQRBarcode extends BarcodeBase {
    public POPQRBarcode(String value) {
        super(value);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected Map<String, String> setParsedValue(String value) {
        Map<String,String> newParsedValue = new HashMap<String,String>();

        List<String> sections = Arrays.asList(value.split("~+"));
        sections.stream().map(s -> Integer.valueOf(s));

        newParsedValue.put("default",value);

        return newParsedValue;
    }

    @Override
    protected Boolean validateValue(String value) {
        return true;
    }

}
