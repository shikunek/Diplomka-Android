package com.example.petr.testing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Petr on 02.10.2017.
 */

public class RowAdapter extends ArrayAdapter<String>
{
    private final Context context;
    private final ArrayList<ProjectClass> project;
    private ArrayList<String> mobileArray = new ArrayList<>();

    public RowAdapter(Context context, ArrayList<ProjectClass> project , ArrayList<String> mobileArray) {
        super(context, R.layout.row_item, mobileArray);
        this.context = context;
        this.project = project;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(R.layout.row_item, parent, false);
        LinearLayout rowLinearLayout = (LinearLayout) rowView.findViewById(R.id.lin);

        TextView projectNameView = new TextView(context);
        projectNameView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        projectNameView.setText(project.get(position).getProjectName());
        projectNameView.setTextColor(Color.BLACK);

        projectNameView.setPadding(20, 20, 20, 20);
        rowLinearLayout.setTag(project.get(position).getID());
        rowLinearLayout.addView(projectNameView);
        for (int i = 0; i < project.get(position).getProjectUsers().size(); i++)
        {
            TextView userOnProjectView = new TextView(context);
            userOnProjectView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            userOnProjectView.setText(project.get(position).getProjectUsers().get(i));
            userOnProjectView.setTextColor(Color.RED);
            userOnProjectView.setPadding(20, 20, 20, 20);
            rowLinearLayout.addView(userOnProjectView);
        }



        return rowView;
    }

}
