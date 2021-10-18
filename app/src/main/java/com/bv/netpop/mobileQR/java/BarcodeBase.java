package com.bv.netpop.mobileQR.java;

import android.os.Parcel;
import android.os.Parcelable;

import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;

public class BarcodeBase implements Parcelable {
    public String rawValue = "";
    public Map<String,String> parsedValue;

    public Boolean isValid = null;
    public LocalDateTime scannedTime;
    public String errorType = "";
    public String errorComment = "";
    public status barcodeStatus = status.Pending;

    public BarcodeBase(String value) {
        this.rawValue = value;
        this.parsedValue = this.setParsedValue(value);
        this.isValid = this.validateValue(value);
    }


    protected BarcodeBase(Parcel in) {
        rawValue = in.readString();
        byte tmpIsValid = in.readByte();
        isValid = tmpIsValid == 0 ? null : tmpIsValid == 1;
        errorType = in.readString();
        errorComment = in.readString();
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

    protected Boolean validateValue(String value) {
        return true;
    }

    protected Map<String,String> setParsedValue(String value) {
        Map<String,String> newParsedValue =new HashMap<String,String>();

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
        dest.writeByte((byte) (isValid == null ? 0 : isValid ? 1 : 2));
        dest.writeString(errorType);
        dest.writeString(errorComment);
    }

    public enum status {
        Pending,
        Correct,
        Incorrect
    }

}

