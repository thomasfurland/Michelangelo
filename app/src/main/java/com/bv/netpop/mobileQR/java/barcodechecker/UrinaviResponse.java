package com.bv.netpop.mobileQR.java.barcodechecker;

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
        AtomicBoolean foundMatch = new AtomicBoolean(false);
        for (SoapObject row: DataTable.data) {
            MapCells(row, col -> {
                if (!col.getName().equals(this.PrimaryKey)) return;
                SoapPrimitive value = (SoapPrimitive) col.getValue();
                Boolean match = value.toString().trim().equals(primaryKeyValue);
                foundMatch.set(match);
            });
        }
        return foundMatch.get();
    }

    public String lookupItem(String primaryKeyValue, String colName) {
        AtomicReference<String> lookUpValue = new AtomicReference<>("");
        this.MapFilteredRows(DataTable.data, primaryKeyValue,col -> {
            if (!col.getName().equals(colName)) {
                return;
            }
            SoapPrimitive value = (SoapPrimitive) col.getValue();
            lookUpValue.set((String) value.getValue());
        });
        return lookUpValue.get();
    }

    protected void MapFilteredRows(ArrayList<SoapObject> table, String primaryKeyValue, UrinaviMasterTable.TwoDMapFunction func) {
        for (SoapObject row: table) {
            AtomicBoolean foundMatch = new AtomicBoolean(false);
            MapCells(row, col -> {
                if (!col.getName().equals(this.PrimaryKey)) {
                    return;
                }
                foundMatch.set(true);
            });
            if (foundMatch.get()) MapCells(row, col -> func.map(col));
        }
    }

    private void MapCells(SoapObject row, UrinaviMasterTable.TwoDMapFunction func) {
        for (int i = 0; i < row.getPropertyCount(); i++) {
            PropertyInfo colInfo = (PropertyInfo) row.getPropertyInfo(i);
            func.map(colInfo);
        }
    }

}

