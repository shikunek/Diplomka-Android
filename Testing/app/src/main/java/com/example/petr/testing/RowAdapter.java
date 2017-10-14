package com.example.petr.testing;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.petr.testing.R;

import java.util.ArrayList;

/**
 * Created by Petr on 02.10.2017.
 */

public class RowAdapter extends ArrayAdapter<String>
{
    private final Context context;
    private final ArrayList<ProjectClass> project;
    private ArrayList<String> mobileArray = new ArrayList<String>();

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
        LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.lin);

        TextView textView1 = new TextView(context);
        textView1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        textView1.setText(project.get(position).getProjectName());
        textView1.setTextColor(Color.BLACK);

        textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.setTag(project.get(position).getID());
        linearLayout.addView(textView1);
//        for (int i = (users.size() - usersOnProjectCount.get(position)); i < users.size() ; i++)
        for (int i = 0; i < project.get(position).getProjectUsers().size(); i++)
        {
            // Add textview 1
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setText(project.get(position).getProjectUsers().get(i));
            textView.setTextColor(Color.RED);
            textView.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            linearLayout.addView(textView);
        }



        return rowView;
    }

}
