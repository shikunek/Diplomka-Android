package com.example.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        public ViewHolder(View v) {
            super(v);

            mProjectNameTextView = (TextView) v.findViewById(R.id.projectName);
            mProjectUsersLayout = (LinearLayout) v.findViewById(R.id.lin);

            v.setOnClickListener(new View.OnClickListener() {
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
    public void onBindViewHolder(RowAdapter.ViewHolder holder, int position) {

        holder.mProjectNameTextView.setText(projectList.get(position).getProjectName());
        holder.mProjectNameTextView.setPadding(20,0,0,0);
        holder.mProjectNameTextView.setTextSize(26);
        holder.mProjectUsersLayout.setTag(projectList.get(position).getID());
//        holder.mProjectUsersLayout.setMinimumHeight(60);
        int sed = holder.mProjectUsersLayout.getHeight();
        for (int i = 0; i < projectList.get(position).getProjectUsers().size(); i++)
        {
            ImageView userOnProjectView = new ImageView(holder.mProjectNameTextView.getContext());

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(110,110);
            int margin = 10;
            int size = 130;
            params.setMargins(margin, 0, margin, 0);
            params.gravity = Gravity.CENTER;
            userOnProjectView.setLayoutParams(params);


//            userOnProjectView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
//                    LinearLayout.LayoutParams.WRAP_CONTENT));

            userOnProjectView.setBackgroundResource(R.drawable.round_button_blue);
            Drawable drw = userOnProjectView.getBackground();
            switch (i)
            {
                case 0:
                    drw.setColorFilter(Color.argb(255, 229, 115, 115), PorterDuff.Mode.LIGHTEN);
                    break;

                case 1:
                    drw.setColorFilter(Color.argb(255, 79, 195, 247), PorterDuff.Mode.LIGHTEN);
                    break;

                case 2:
                    drw.setColorFilter(Color.argb(255, 129, 199, 132), PorterDuff.Mode.LIGHTEN);
                    break;

                default:
                    drw.setColorFilter(Color.argb(255, 79, 195, 247), PorterDuff.Mode.LIGHTEN);
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
