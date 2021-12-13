package com.bv.netpop.mobileQR.java.barcodechecker;

import android.os.Build;

import androidx.annotation.RequiresApi;

import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class UrinaviResponse {

    private final String RETURNINFO = "RETURNINFO1";
    protected String PrimaryKey = null;
    protected SoapObject rawResponse;
    protected Table DataTable;

    public UrinaviResponse(SoapObject response) {
        rawResponse = response;
        DataTable = extractTable(rawResponse);
    }

    public static class Table {
        ArrayList<SoapObject> data;
        SoapObject info;
        public Table() {
            data = new ArrayList<SoapObject>(0);
        }
    }

    interface TwoDMapFunction {
        void map(PropertyInfo col);
    }

    protected Table extractTable(SoapObject rawResponse) {

        Table table = new Table();

        SoapObject props = (SoapObject) rawResponse.getProperty(1);
        SoapObject props2 = (SoapObject) props.getProperty(0);

        for (int i = 0; i < props2.getPropertyCount(); i++) {
            SoapObject row = (SoapObject) props2.getProperty(i);
            String tableName = (String) row.getAttribute("id");

            switch(tableName) {
                case RETURNINFO:
                    table.info = row;
                    break;
                default:
                    table.data.add(row);
            }
        }
        return table;
    }
    public Boolean ContainsItem(String primaryKeyValue) {
        for (SoapObject row: DataTable.data) {
            if(this.rowContainsItem(row,this.PrimaryKey,primaryKeyValue)) return true;
        }
        return false;
    }

    public String lookupItem(String primaryKeyValue, String colName) {
        AtomicReference<String> lookUpValue = new AtomicReference<>("");
        this.AccessMatchedRows(DataTable.data, this.PrimaryKey,primaryKeyValue,col -> {
            if (!col.getName().equals(colName)) {
                return;
            }
            String value = "";

            //Null及び、空白値以外の場合はSoapPrimitiveのタイプ。
            if (col.getValue() instanceof SoapPrimitive){
                SoapPrimitive soapValue = (SoapPrimitive) col.getValue();
                value = (String) soapValue.getValue();
            }
            lookUpValue.set(value);
        });
        return lookUpValue.get();
    }

    protected void AccessMatchedRows(ArrayList<SoapObject> table, String itemKey, String itemValue, UrinaviMasterTable.TwoDMapFunction func) {
        for (SoapObject row: table) {
            if (this.rowContainsItem(row,itemKey,itemValue)) {
                MapCells(row, col -> func.map(col));
            }
        }
    }

    private boolean rowContainsItem(SoapObject row, String itemKey,String itemValue) {
        AtomicBoolean foundMatch = new AtomicBoolean(false);
        MapCells(row, col -> {
            if (!col.getName().equals(itemKey)) return;
            SoapPrimitive value = (SoapPrimitive) col.getValue();
            Boolean match = value.toString().trim().equals(itemValue.trim());
            foundMatch.set(match);
        });
        return foundMatch.get();
    }

    private void MapCells(SoapObject row, UrinaviMasterTable.TwoDMapFunction func) {
        for (int i = 0; i < row.getPropertyCount(); i++) {
            PropertyInfo colInfo = (PropertyInfo) row.getPropertyInfo(i);
            func.map(colInfo);
        }
    }

    public String ResponseStatus() {
        return GetInfo("STSFLG");
    }

    public String GetInfo(String propertyName) {
        AtomicReference<String> propertyValue = new AtomicReference<>("");
        MapCells(this.DataTable.info, info -> {
            if(info.getName().equals(propertyName)) {
                SoapPrimitive value = (SoapPrimitive) info.getValue();
                propertyValue.set((String)value.getValue());
            }
        });
        return propertyValue.get();
    }

}

