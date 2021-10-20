package com.bv.netpop.mobileQR.java.barcodechecker;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;

public class UrinaviMasterTable extends UrinaviResponse {

    private ArrayList<SoapObject> MasterTable;

    public UrinaviMasterTable(SoapObject response) {
        super(response);
        MasterTable = extractMasterTable(rawResponse);

    }

    private ArrayList<SoapObject> extractMasterTable(SoapObject rawResponse) {

        ArrayList<SoapObject> table = new ArrayList<>(0);

        SoapObject props = (SoapObject) rawResponse.getProperty(1);
        SoapObject props2 = (SoapObject) props.getProperty(0);
        for (int i = 0; i < props2.getPropertyCount(); i++) {
            table.add((SoapObject) props2.getProperty(i));
        }

        return table;
    }

    public Boolean Match(String janCode) {

        Boolean foundMatch;

        for (SoapObject row: MasterTable) {
            for (int i = 0; i < row.getPropertyCount(); i++) {
                PropertyInfo colInfo = (PropertyInfo) row.getPropertyInfo(i);
                if (colInfo.getName().equals("F1")) {
                    SoapPrimitive col = (SoapPrimitive) row.getProperty(i);
                    foundMatch = (col.getValue().equals(janCode));
                    if (foundMatch) return true;
                    break;
                }
            }
        }

        return false;
    }

    public String GetPrice(String janCode) {
        String price = "";
        Boolean foundMatch = false;
        for (SoapObject row: MasterTable) {
            for (int i = 0; i < row.getPropertyCount(); i++) {
                PropertyInfo colInfo = (PropertyInfo) row.getPropertyInfo(i);
                if (colInfo.getName().equals("F1")) {
                    SoapPrimitive col = (SoapPrimitive) row.getProperty(i);
                    foundMatch = (col.getValue().equals(janCode));
                    if (!foundMatch) break;
                }
                else if (colInfo.getName().equals("F5")) {
                    SoapPrimitive col = (SoapPrimitive) row.getProperty(i);
                    return (String) col.getValue();
                }
            }
        }
        
        return price;
    }

}
