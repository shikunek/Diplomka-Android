package com.NudgeMe.petr.testing;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersOnProjectAdapter extends RecyclerView.Adapter<UsersOnProjectAdapter.ViewHolder>
{
    private ArrayList<String> mEmailset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mUserEmailTextView;
        public CircleImageView mUserImage;
        public DatabaseReference mData;

        public ViewHolder(View v) {
            super(v);
            mData = FirebaseDatabase.getInstance().getReference();
            mUserEmailTextView = (TextView) v.findViewById(R.id.userEmailView);
            mUserImage = (CircleImageView) v.findViewById(R.id.userImageView);
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
                        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + user.getKey());
                        Glide.with(holder.mUserEmailTextView.getContext())
                                .using(new FirebaseImageLoader())
                                .load(storageReference)
                                .asBitmap()
                                .signature(new StringSignature(String.valueOf(System.currentTimeMillis() / (48 * 60 * 60 * 1000))))
                                .error(R.drawable.animal_ant_eater)
                                .into(holder.mUserImage);
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
