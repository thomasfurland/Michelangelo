package com.bv.netpop.mobileQR.java.barcodechecker;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bv.netpop.mobileQR.R;
import com.bv.netpop.mobileQR.java.barcodes.BarcodeBase;
import com.bv.netpop.mobileQR.java.barcodes.POPQRBarcode;

import java.util.ArrayList;
import java.util.List;

public class BarcodeCheckerAdapter extends RecyclerView.Adapter<BarcodeCheckerAdapter.ViewHolder> {

    List<POPQRBarcode> mBarcodes;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ArrayList<TextView> bcTextView = new ArrayList<>(0);
        TextView bcStatusTextView;
        ImageView bcCorrectImage;
        ImageView bcIncorrectImage;
        ProgressBar bcPendingBar;
        CheckBox bcCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            bcTextView.add(itemView.findViewById(R.id.jan_code));
            bcTextView.add(itemView.findViewById(R.id.item_name));
            bcTextView.add(itemView.findViewById(R.id.pop_type));
            bcTextView.add(itemView.findViewById(R.id.display_price));
            bcTextView.add(itemView.findViewById(R.id.correct_price));
            bcTextView.add(itemView.findViewById(R.id.check_comment));

            bcStatusTextView = itemView.findViewById(R.id.status_header);
            bcCorrectImage = itemView.findViewById(R.id.correctImage);
            bcIncorrectImage = itemView.findViewById(R.id.incorrectImage);
            bcPendingBar = itemView.findViewById(R.id.progressBarPending);

            bcCheckBox = itemView.findViewById(R.id.sendCheckBox);

        }
    }

    public BarcodeCheckerAdapter(List<POPQRBarcode> mBarcodes) {
        super();
        this.mBarcodes = mBarcodes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View barcodeView = inflater.inflate(R.layout.item_barcode_status,parent,false);
        return new ViewHolder(barcodeView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        holder.bcCorrectImage.setVisibility(View.GONE);
        holder.bcIncorrectImage.setVisibility(View.GONE);
        holder.bcPendingBar.setVisibility(View.GONE);

        holder.bcCheckBox.setVisibility(View.INVISIBLE);

        clearAllViews(holder);

        POPQRBarcode barcode = mBarcodes.get(position);

        holder.bcTextView.get(0).setText("JAN: " + barcode.getParsedValue().get("JAN"));
        holder.bcTextView.get(1).setText("??????: " + barcode.getParsedValue().get("??????"));
        holder.bcTextView.get(2).setText("POP??????: " + barcode.getParsedValue().get("POP?????????"));

        switch (barcode.getParsedValue().get("POP??????")) {
            case "7": //POP
                holder.bcTextView.get(3).setText("??????: " + barcode.getParsedValue().get("????????????"));
                break;
            case "8": //??????
                holder.bcTextView.get(3).setText("??????: " + barcode.getParsedValue().get("POP??????"));
        }

        switch(barcode.barcodeStatus) {
            case Correct:
                holder.bcCorrectImage.setVisibility(View.VISIBLE);
                break;
            case Incorrect:
                holder.bcIncorrectImage.setVisibility(View.VISIBLE);
                holder.bcCheckBox.setVisibility(View.VISIBLE);

                holder.bcCheckBox.setOnCheckedChangeListener(null);
                holder.bcCheckBox.setChecked(barcode.checked);

                holder.bcTextView.get(5).setText(barcode.errorComment);
                holder.bcTextView.get(5).setTextColor(Color.parseColor("#ffff4444"));

                switch (barcode.errorType) {
                    case "":
                        holder.bcTextView.get(4).setText("");
                        break;
                    default:
                        holder.bcTextView.get(4).setText("????????????: " + barcode.errorType);
                }
                break;
            case Pending:
                holder.bcPendingBar.setVisibility(View.VISIBLE);
                holder.bcPendingBar.animate();
                break;
        }
        holder.bcCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            barcode.checked = isChecked;
        });
    }

    @Override
    public int getItemCount() {
        return mBarcodes.size();
    }

    private void clearAllViews(@NonNull ViewHolder holder) {
        holder.bcTextView.get(0).setText("");
        holder.bcTextView.get(1).setText("");
        holder.bcTextView.get(2).setText("");
        holder.bcTextView.get(3).setText("");
        holder.bcTextView.get(4).setText("");
        holder.bcTextView.get(5).setText("");
    }

}
