package com.example.petr.testing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Petr on 01.11.2017.
 */

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ViewHolder> {
    private ArrayList<String> mTextset;
    private ArrayList<String> mImageset;
    private ArrayList<String> mDateset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mReportedTextView;
        public ImageView mReportedSmile;
        public TextView mReportedDate;
        public ViewHolder(View v) {
            super(v);

            mReportedTextView = (TextView) v.findViewById(R.id.reportText);
            mReportedSmile = (ImageView) v.findViewById(R.id.reportedSmile);
            mReportedDate = (TextView) v.findViewById(R.id.dateView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ReportAdapter(ArrayList<String> myDataset, ArrayList<String> mImageset, ArrayList<String> mDateset) {
        mTextset = myDataset;
        this.mImageset = mImageset;
        this.mDateset = mDateset;

    }

    // Create new views (invoked by the layout manager)
    @Override
    public ReportAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
//        TextView v = new TextView(parent.getContext());
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.report_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new ReportAdapter.ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.mReportedTextView.setText(mTextset.get(position));
        holder.mReportedDate.setText(mDateset.get(position));
        switch (mImageset.get(position))
        {
            case "1":
                holder.mReportedSmile.setImageResource(R.drawable.happy);
                break;

            case "0":
                holder.mReportedSmile.setImageResource(R.drawable.confused);
                break;

            case "-1":
                holder.mReportedSmile.setImageResource(R.drawable.mad);
                break;
            case "-2":
                holder.mReportedSmile.setImageResource(R.drawable.mad);
                holder.mReportedTextView.setText("User didn't send report!");
//                holder.mReportedTextView.setTextColor(Color.RED);
                break;

        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mTextset.size();
    }
}