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
    private final ArrayList<String> values;
    private final ArrayList<String> users;


    public RowAdapter(Context context, ArrayList<String> values, ArrayList<String> users) {
        super(context, R.layout.row_item, values);
        this.context = context;
        this.values = values;
        this.users = users;
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
        textView1.setText(values.get(position));
        textView1.setTextColor(Color.BLACK);

//        textView1.setBackgroundColor(0xff66ff66); // hex color 0xAARRGGBB
        textView1.setPadding(20, 20, 20, 20);// in pixels (left, top, right, bottom)
        linearLayout.addView(textView1);
        String[] s = new String[]{"Petr","Zdenko"};
        for (int i = 0; i< 2; i++)
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
