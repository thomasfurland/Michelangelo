package com.bv.netpop.mobileQR.java.barcodescanner

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bv.netpop.mobileQR.R

class BarcodeActivityAdapter (private val mBarcodes: List<String>) : RecyclerView.Adapter<BarcodeActivityAdapter.ViewHolder>()
{
    // Provide a direct reference to each of the views within a data item
    // Used to cache the views within the item layout for fast access
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Your holder should contain and initialize a member variable
        // for any view that will be set as you render a row
        val bcTextView = itemView.findViewById<TextView>(R.id.barcode_data)
    }

    // ... constructor and member variables
    // Usually involves inflating a layout from XML and returning the holder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        // Inflate the custom layout
        val contactView = inflater.inflate(R.layout.item_barcode, parent, false)
        // Return a new holder instance
        return ViewHolder(contactView)
    }

    // Involves populating data into the item through holder
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        // Get the data model based on position
        val contact: String = mBarcodes.get(position)
        // Set item views based on your views and data model
        val textView = viewHolder.bcTextView
        textView.setText(contact)
    }

    // Returns the total count of items in the list
    override fun getItemCount(): Int {
        return mBarcodes.size
    }
}