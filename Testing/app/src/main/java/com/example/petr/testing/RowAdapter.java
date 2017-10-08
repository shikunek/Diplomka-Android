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
    private final ArrayList<String> projectNames;
    private final ArrayList<String> users;
    private final ArrayList<String> IDs;


    public RowAdapter(Context context, ArrayList<String> projectNames, ArrayList<String> users,
                      ArrayList<String> IDs) {
        super(context, R.layout.row_item, projectNames);
        this.context = context;
        this.projectNames = projectNames;
        this.users = users;
        this.IDs = IDs;
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
        textView1.setText(projectNames.get(position));
        textView1.setTextColor(Color.BLACK);

//        textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
        textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.setTag(IDs.get(position));
        linearLayout.addView(textView1);
        for (int i = position*2; i< (position*2 + 2); i++)
        {
            // Add textview 1
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setText(users.get(i));
            textView.setTextColor(Color.RED);
            textView.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
            linearLayout.addView(textView);
        }



        return rowView;
    }

}
