package com.NudgeMe.petr.testing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UsersOnProjectAdapter extends RecyclerView.Adapter<UsersOnProjectAdapter.ViewHolder>
{
    private ArrayList<String> mEmailset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mUserEmailTextView;
        public ImageView mUserImage;
        public DatabaseReference mData;

        public ViewHolder(View v) {
            super(v);
            mData = FirebaseDatabase.getInstance().getReference();
            mUserEmailTextView = (TextView) v.findViewById(R.id.userEmailView);
            mUserImage = (ImageView) v.findViewById(R.id.userImageView);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public UsersOnProjectAdapter(ArrayList<String> myDataset) {
        mEmailset = myDataset;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public UsersOnProjectAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                       int viewType) {
        // create a new view
//        TextView v = new TextView(parent.getContext());
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        // set the view's size, margins, paddings and layout parameters
        return new UsersOnProjectAdapter.ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final UsersOnProjectAdapter.ViewHolder holder, final int position)
    {
        holder.mUserEmailTextView.setText(mEmailset.get(position));
        holder.mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot users) {
                for (DataSnapshot user : users.getChildren())
                {
                    if (user.child("email").getValue().toString().equals(mEmailset.get(position)))
                    {
                        if (user.hasChild("Icon"))
                        {
                            int iconID = holder.mUserEmailTextView.getContext().
                                    getResources().getIdentifier(user.child("Icon").getValue().toString(),
                                    "drawable", holder.mUserEmailTextView.getContext().getPackageName());
                            holder.mUserImage.setBackgroundResource(iconID);
                        }
                        else
                        {
                            holder.mUserImage.setBackgroundResource(R.drawable.animal_ant_eater);
                        }

                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mEmailset.size();
    }
}
