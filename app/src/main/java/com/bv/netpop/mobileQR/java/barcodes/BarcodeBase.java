package com.bv.netpop.mobileQR.java.barcodes;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarcodeBase implements Parcelable {
    public String rawValue;
    protected Map<String,String> _parsedValue;

    public Map<String, String> getParsedValue() {
        if (_parsedValue == null) {
            _parsedValue = this.setParsedValue(rawValue);
        }
        return _parsedValue;
    }
    public LocalDateTime scannedTime;
    public String errorType = "";
    public String errorComment = "";
    public status barcodeStatus = status.Pending;
    public Boolean checked = true;

    public BarcodeBase(String value) {
        this.rawValue = value;
    }

    public static Boolean QRMatch(String value) {

        if(value == null) {
            return false;
        }

        List<String> sections = Arrays.asList(value.split("\\+",-1));

        if (!(sections.size() == 5)) {
            return false;
        }

        if (!sections.get(0).equals("PCHK")) {
            return false;
        }

        return true;
    }

    protected BarcodeBase(Parcel in) {
        rawValue = in.readString();
        errorType = in.readString();
        errorComment = in.readString();
        barcodeStatus = status.valueOf(in.readString());
        checked = Boolean.valueOf(in.readString());
    }

    public static final Creator<BarcodeBase> CREATOR = new Creator<BarcodeBase>() {
        @Override
        public BarcodeBase createFromParcel(Parcel in) {
            return new BarcodeBase(in);
        }

        @Override
        public BarcodeBase[] newArray(int size) {
            return new BarcodeBase[size];
        }
    };

    protected Map<String,String> setParsedValue(String value) {
        Map<String,String> newParsedValue = new HashMap<>();

        newParsedValue.put("default",value);

        return newParsedValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(rawValue);
        dest.writeString(errorType);
        dest.writeString(errorComment);
        dest.writeString(barcodeStatus.toString());
        dest.writeString(checked.toString());
    }

    public enum status {
        Pending,
        Correct,
        Incorrect
    }

}

