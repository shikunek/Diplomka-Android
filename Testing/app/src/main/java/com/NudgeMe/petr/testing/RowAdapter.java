package com.NudgeMe.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * Created by Petr on 02.10.2017.
 */

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.ViewHolder>
{
    private ArrayList<ProjectClass> projectList;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mProjectNameTextView;
        public LinearLayout mProjectUsersLayout;
        public LinearLayout mProjectLayout;
        public LinearLayout mSwipe;
        public DatabaseReference mData;

        public ViewHolder(View v) {
            super(v);

            mData = FirebaseDatabase.getInstance().getReference();
            mProjectNameTextView = (TextView) v.findViewById(R.id.projectName);
            mProjectUsersLayout = (LinearLayout) v.findViewById(R.id.lin);

            mSwipe = (LinearLayout) v.findViewById(R.id.swipe_layout);
            mProjectLayout = (LinearLayout) v.findViewById(R.id.recyclerItem);

            // Go to project info
            mProjectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout currentProjectLayout = (LinearLayout) v.findViewById(R.id.lin);
                    Context context = mProjectNameTextView.getContext();
                    Intent intent = new Intent(context, ProjectInfoActivity.class);
                    intent.putExtra("projectName",(String) currentProjectLayout.getTag());
                    context.startActivity(intent);
                }
            });
        }
    }


    public RowAdapter(ArrayList<ProjectClass> projectList) {
        this.projectList = projectList;
    }

    @Override
    public RowAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View rowView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_item, parent, false);

        return new RowAdapter.ViewHolder(rowView);
    }

    @Override
    public void onBindViewHolder(final RowAdapter.ViewHolder holder, final int position) {


        holder.mProjectNameTextView.setText(projectList.get(position).getProjectName());
        holder.mProjectNameTextView.setPadding(20,0,0,0);
        holder.mProjectNameTextView.setTextSize(26);

        holder.mProjectUsersLayout.setTag(projectList.get(position).getID());


        holder.mData.child("Projects").child(projectList.get(position).getID()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot projectUsers) {
                for (DataSnapshot projectUser : projectUsers.getChildren())
                {
                    if (projectUser.getKey().equals("projectName") || projectUser.getKey().equals("Ending"))
                    {
                        continue;
                    }
                    holder.mData.child("Uzivatel").child(projectUser.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot specificUser) {
                            ImageView userOnProjectView = new ImageView(holder.mProjectNameTextView.getContext());

                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
                            int margin = 10;
                            params.setMargins(margin, 0, margin, 0);
                            params.gravity = Gravity.CENTER;
                            userOnProjectView.setLayoutParams(params);

                            if (specificUser.hasChild("Icon"))
                            {
                                int iconID = holder.mProjectNameTextView.getContext().
                                        getResources().getIdentifier(specificUser.child("Icon").getValue().toString(),
                                        "drawable", holder.mProjectNameTextView.getContext().getPackageName());
                                userOnProjectView.setBackgroundResource(iconID);
                            }
                            else
                            {
                                userOnProjectView.setBackgroundResource(R.drawable.animal_ant_eater);
                            }


                            int padding = 20;
                            userOnProjectView.setPadding(padding, padding, padding, padding);
                            holder.mProjectUsersLayout.setPadding(padding, padding, padding, padding);
                            holder.mProjectUsersLayout.addView(userOnProjectView);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }
}
