package com.example.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;

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
        public SwipeRevealLayout swipeLayout;
        public LinearLayout mProjectLayout;
        public FrameLayout mDeleteProjectLayout;

        public ViewHolder(View v) {
            super(v);

            mProjectNameTextView = (TextView) v.findViewById(R.id.projectName);
            mProjectUsersLayout = (LinearLayout) v.findViewById(R.id.lin);
            swipeLayout = (SwipeRevealLayout) v.findViewById(R.id.swipe_layout);
            mProjectLayout = (LinearLayout) v.findViewById(R.id.recyclerItem);
            mDeleteProjectLayout = (FrameLayout) v.findViewById(R.id.delete_layout);

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
            mDeleteProjectLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout currentProjectLayout = (LinearLayout) v.findViewById(R.id.lin);
                    currentProjectLayout.getTag();
//                    mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot currentUsersData) {
//
//                            if (!currentUsersData.hasChild("Active"))
//                            {
//                                return;
//                            }
//                            Map<String, Object> updatedUserData = new HashMap<>();
//                            updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
//                                    "Projects/" + currentUsersData.child("Active").getValue().toString() + "/"  , null);
//
//                            updatedUserData.put("Projects/" + currentUsersData.child("Active").getValue().toString() + "/" +
//                                    currentUser.getUid() + "/" , null);
//                            updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
//                                    "Active" , null);
//
//                            mData.updateChildren(updatedUserData);
//
//                            for (ProjectClass project : myProjectset)
//                            {
//                                if (project.getID().equals(currentUsersData.child("Active").getValue().toString()))
//                                {
//                                    myProjectset.remove(project);
//                                    mAdapter.notifyItemRemoved(myProjectset.indexOf(project));
//                                    mAdapter.notifyItemRangeChanged(myProjectset.indexOf(project), myProjectset.size());
//                                    break;
//                                }
//                            }
//
//                            mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
//                                @Override
//                                public void onDataChange(DataSnapshot actualUsersData) {
//                                    if (actualUsersData.hasChild("Projects"))
//                                    {
//                                        for (DataSnapshot firstProject : actualUsersData.child("Projects").getChildren())
//                                        {
//                                            mData.child("Uzivatel").child(currentUser.getUid()).child("Active").setValue(firstProject.getKey());
//                                            break;
//                                        }
//
//                                    }
//                                }
//
//                                @Override
//                                public void onCancelled(DatabaseError databaseError) {
//
//                                }
//                            });
//
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//                    });
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
//        DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
//        mData.child("Uzivatel").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
//                .addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot currentUsersData) {
//                if (!currentUsersData.hasChild("Active"))
//                {
//                    return;
//                }
//
//                if (currentUsersData.child("Active").getValue().toString().equals(projectList.get(position).getID()))
//                {
//                    GradientDrawable gradientDrawable = new GradientDrawable();
//                    gradientDrawable.setStroke(10, Color.BLUE);
//                    holder.mProjectLayout.setBackgroundDrawable(gradientDrawable);
////                    holder.mProjectUsersLayout.setMinimumHeight(60);
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        int sed = holder.mProjectUsersLayout.getHeight();
        for (int i = 0; i < projectList.get(position).getProjectUsers().size(); i++)
        {
            ImageView userOnProjectView = new ImageView(holder.mProjectNameTextView.getContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150,150);
            int margin = 10;
            int size = 130;
            params.setMargins(margin, 0, margin, 0);
            params.gravity = Gravity.CENTER;
            userOnProjectView.setLayoutParams(params);


//            userOnProjectView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT));

//            userOnProjectView.setBackgroundResource(R.drawable.round_button_blue);
            Drawable drw = userOnProjectView.getBackground();
            switch (i)
            {
                case 0:
                    userOnProjectView.setBackgroundResource(R.drawable.animal_rhinoceros);
//                    drw.setColorFilter(Color.argb(255, 229, 115, 115), PorterDuff.Mode.LIGHTEN);
                    break;

                case 1:
                    userOnProjectView.setBackgroundResource(R.drawable.animal_cat);
//                    drw.setColorFilter(Color.argb(255, 79, 195, 247), PorterDuff.Mode.LIGHTEN);
                    break;

                case 2:
                    userOnProjectView.setBackgroundResource(R.drawable.animal_koala);
//                    drw.setColorFilter(Color.argb(255, 129, 199, 132), PorterDuff.Mode.LIGHTEN);
                    break;

                default:
//                    drw.setColorFilter(Color.argb(255, 79, 195, 247), PorterDuff.Mode.LIGHTEN);
                    break;
            }



//            userOnProjectView.setText(projectList.get(position).getProjectUsers().get(i));
//            userOnProjectView.setTextColor(Color.RED);
            int padding = 20;
            userOnProjectView.setPadding(padding, padding, padding, padding);
            holder.mProjectUsersLayout.setPadding(padding, padding, padding, padding);
            holder.mProjectUsersLayout.addView(userOnProjectView);
        }
    }

    @Override
    public int getItemCount() {
        return projectList.size();
    }
}
