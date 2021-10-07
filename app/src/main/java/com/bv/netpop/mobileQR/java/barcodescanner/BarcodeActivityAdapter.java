package com.bv.netpop.mobileQR.java.barcodescanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bv.netpop.mobileQR.R;
import java.util.List;

public class BarcodeActivityAdapter extends RecyclerView.Adapter<BarcodeActivityAdapter.ViewHolder> {

    List<String> mBarcodes;

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView bcTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bcTextView  = itemView.findViewById(R.id.barcode_data);
        }
    }

    public BarcodeActivityAdapter(List<String> mBarcodes) {
        super();
        this.mBarcodes = mBarcodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View barcodeView = inflater.inflate(R.layout.item_barcode,parent,false);
        return new ViewHolder(barcodeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String barcode = mBarcodes.get(position);
        TextView tv = holder.bcTextView;
        tv.setText(barcode);
    }

    @Override
    public int getItemCount() {
        return mBarcodes.size();
    }
}