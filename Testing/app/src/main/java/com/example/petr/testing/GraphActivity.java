package com.example.petr.testing;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class GraphActivity extends AppCompatActivity {

    LineChart lineChart;
    LineData data;
    int lastDay;
    int numDays;
    int leftDay = 0;
    int numShownDays = 0;
    final String[] xLabels = new String[] { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };

    private DatabaseReference mData;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

//        lineChart = (LineChart) findViewById(R.id.lineChart);

        mData = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();
        // dataset creation from array

//        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mData.child("Uzivatel").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot uzivatel) {

                if (!uzivatel.hasChild("Active"))
                {
                    setContentView(R.layout.activity_graph);
                    return;
                }

                mData.child("Projects").child(uzivatel.child("Active").getValue().toString()).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(user.getUid()))
                                {
                                    setContentView(R.layout.activity_graph);
                                    return;
                                }
                                lineChart = (LineChart) findViewById(R.id.lineChart);
                                final YAxis yAxis = lineChart.getAxisLeft();
                                final XAxis xAxis = lineChart.getXAxis();

                                final ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                                int y = 0;
                                for (DataSnapshot user : dataSnapshot.getChildren())
                                {
                                    if (user.getKey().equals("projectName"))
                                    {
                                        continue;
                                    }
                                    Log.d("B", String.valueOf(user.getKey()));
                                    int i = 1;

                                    final ArrayList<Entry> yAxes = new ArrayList<>();

                                    xAxis.setGranularity(1f); // minimum axis-step (interval) is 1

                                    final int numDataPoints = numDays = numShownDays;
                                    Random randomGenerator = new Random();
                                    float number = 0f;
                                    yAxes.add(new Entry(0, 0)); // first entry is 0

                                    for (DataSnapshot value : user.getChildren())
                                    {
                                        if (!value.exists())
                                        {
                                            return;
                                        }
                                        String key = value.getKey();

                                        Log.d("A", String.valueOf(value.child("Y").getValue()));
                                        long smile = (long) value.child("sendValue").getValue();
                                        number = translateEntry(number, (int)smile);
                                        yAxes.add(new Entry(i, number)); // data entry creation
                                        i++;
                                    }

                                    lastDay = (int) yAxes.get((int)user.getChildrenCount()-1).getY();

                                    LineDataSet lineDataSet = new LineDataSet(yAxes, "data label to be removed");
                                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                                    lineDataSet.setDrawCircles(true);
                                    lineDataSet.setCircleRadius(5f);
                                    lineDataSet.setCircleHoleRadius(2f);
                                    switch (y)
                                    {
                                        case 1:
                                        {
                                            lineDataSet.setColor(Color.argb(255, 79, 195, 247));
                                            lineDataSet.setCircleColor(Color.argb(255, 79, 195, 247));
                                            break;
                                        }
                                        case 2:
                                            lineDataSet.setColor(Color.argb(255, 129, 199, 132));
                                            lineDataSet.setCircleColor(Color.argb(255, 129, 199, 132));
                                            break;

                                        case 3:
                                            lineDataSet.setColor(Color.argb(255, 186, 104, 200));
                                            lineDataSet.setCircleColor(Color.argb(255, 186, 104, 200));
                                            break;
                                        default:
                                            lineDataSet.setColor(Color.BLUE);
                                            break;


                                    }
                                    y++;

                                    lineDataSet.setDrawValues(false);
                                    lineDataSet.setLineWidth(3f);
                                    lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
                                    lineDataSet.setCubicIntensity(0.2f);

                                    lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
                                    lineDataSet.setDrawCircles(true);
                                    lineDataSet.setCircleRadius(3f);
                                    lineDataSet.setCircleHoleRadius(1f);
                                    lineDataSet.setDrawValues(false);
                                    lineDataSet.setLineWidth(3f);
                                    // Modes: LINEAR, STEPPED, CUBIC_BEZIER, HORIZONTAL_BEZIER
                                    lineDataSet.setMode(LineDataSet.Mode.LINEAR);
                                    dataSets.add(lineDataSet);


                                    yAxis.setDrawGridLines(false);
                                    yAxis.setDrawAxisLine(false);
                                    yAxis.setAxisMinimum(-1f);
                                    yAxis.setAxisMaximum(1f);
                                    xAxis.setGranularityEnabled(true);
                                    xAxis.setGranularity(1); // minimum axis-step (interval) is 1
                                    xAxis.setLabelCount(numShownDays, true);
                                    xAxis.setDrawAxisLine(false);
                                    xAxis.setDrawGridLines(false);
                                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                                    xAxis.setTextSize(10);
                                    xAxis.setTypeface(Typeface.DEFAULT_BOLD);
                                    xAxis.setGridColor(Color.BLACK);
                                    IAxisValueFormatter formatter = new IAxisValueFormatter() {

                                        @Override
                                        public String getFormattedValue(float value, AxisBase axis) {
                                            if (Math.round(value) == numDays - 14) return "2w ago";
                                            else if (Math.round(value) == numDays - 9) return "1w ago";
                                            else if (Math.round(value) > numDays - 8) return xLabels[(int) value % 7];
                                            else return "";
                                        }
                                    };
                                    xAxis.setValueFormatter(formatter);

                                }
                                data = new LineData(dataSets);
                                lineChart.setData(data);

                                Description description = new Description();
                                description.setText("");
                                lineChart.getAxisLeft().setEnabled(false);
                                lineChart.getAxisRight().setEnabled(false);
                                lineChart.setDescription(description);
                                lineChart.setBackgroundColor(Color.TRANSPARENT);
                                lineChart.setDrawGridBackground(false);// this is a must
                                lineChart.animateY(1000);
                                lineChart.setDrawBorders(false);
                                Legend legend = lineChart.getLegend();
                                legend.setEnabled(false);

                                lineChart.notifyDataSetChanged(); // let the chart know it's data changed
                                lineChart.setVisibleXRangeMaximum(numShownDays-1);
                                lineChart.moveViewToX(leftDay);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("C", "getUser:onCancelled", databaseError.toException());
//                        // [START_EXCLUDE]
//                        setEditingEnabled(true);
//                        // [END_EXCLUDE]
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mData.child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Zmena", "onChildAdded:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d("Zmena", "onChildChanged:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d("Zmena", "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                Log.d("Zmena", "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void goToReport(View view)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }

    public void goToProjects(View view)
    {
        Intent intent = new Intent(this, ProjectsActivity.class);
        startActivity(intent);
    }

    public void createGraph(ArrayList<Entry> yAxes)
    {



    }




    // creates y-value from old y-value and new smile
    private float translateEntry(float old_y, int smile) {
        float new_y = 0f;
        float x_delta;
        float shiftPar = 0.25f; // the smaller the number, the slower the change
        if (old_y >= 1 || old_y <= -1) { // old value is outside of bounds
            throw new IllegalArgumentException("Y-value not within bounds (-1,1)");
        }
        else if (old_y > 0) {
            switch(smile) {
                case  1: // function (1)
                    x_delta = (float)Math.tan(old_y * (Math.PI / 2f));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(x_delta)/(Math.PI / 2f));
                    //Log.d("F1", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                case  0: // function (4)
                    x_delta = (float)(-1f * (Math.tan(old_y * (Math.PI / 2f) - (Math.PI / 2f))));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(-1f * x_delta) / (Math.PI / 2f) + 1f);
                    //Log.d("F4", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                case -1: // function (6)
                    x_delta = (float)(-1f * (Math.tan(old_y * (Math.PI / 4f) - (Math.PI / 4f))));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(2f * (Math.atan(-1f * x_delta) / (Math.PI / 2f)) + 1f);
                    //Log.d("F6", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                default:
                    throw new IllegalArgumentException("Smile not within bounds [-1,0,1]");
            }
        }
        else if (old_y <0) {
            switch(smile) {
                case  1: // function (5)
                    x_delta = (float)Math.tan(old_y * (Math.PI / 4f) + (Math.PI / 4f));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(2f * (Math.atan(x_delta)/(Math.PI / 2f)) - 1f);
                    //Log.d("F5", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                case  0: // function (3)
                    x_delta = (float)Math.tan(old_y * (Math.PI / 2f) + (Math.PI / 2f));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(x_delta)/(Math.PI / 2f) - 1f);
                    //Log.d("F3", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                case -1: // function (2)
                    x_delta = (float)(-1f * (Math.tan(old_y * (Math.PI / 2f))));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(-1f * x_delta) / (Math.PI / 2f));
                    //Log.d("F2", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                default:
                    throw new IllegalArgumentException("Smile not within bounds [-1,0,1]");
            }
        }
        else { // old_y == 0
            switch(smile) {
                case  1: // function (1)
                    x_delta = (float)Math.tan(old_y * (Math.PI / 2f));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(x_delta) / (Math.PI / 2f));
                    //Log.d("F1", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                case  0:
                    break;
                case -1: // function (2)
                    x_delta = (float)(-1f * (Math.tan(old_y * (Math.PI / 2f))));
                    x_delta = shiftPar + x_delta;
                    new_y = (float)(Math.atan(-1f * x_delta) / (Math.PI / 2f));
                    //Log.d("F2", "y_old: " + old_y + ", smile: " + smile + ",\nx_d: " + x_delta + ", new_y: " + new_y + "\n");
                    break;
                default:
                    throw new IllegalArgumentException("Smile not within bounds [-1,0,1]");
            }
        }

        return new_y;
    }

    public void entryMinus(View view) {
        int newDay = lastDay - 1;

        data.addEntry(new Entry(numDays, newDay), 0);
        lineChart.notifyDataSetChanged(); // let the chart know it's data changed
        //lineChart.invalidate(); // refresh
        lineChart.setVisibleXRangeMaximum(numShownDays-1);
        lineChart.moveViewToX(++leftDay);

        lastDay = newDay;
        numDays++;
    }

    public void entryZero(View view) {
        data.addEntry(new Entry(numDays, lastDay), 0);
        lineChart.notifyDataSetChanged(); // let the chart know it's data changed
        //lineChart.invalidate(); // refresh
        lineChart.setVisibleXRangeMaximum(numShownDays-1);
        lineChart.moveViewToX(++leftDay);

        numDays++;
    }

    public void entryPlus(View view) {
        int newDay = lastDay + 1;

        data.addEntry(new Entry(numDays, newDay), 0);
        lineChart.notifyDataSetChanged(); // let the chart know it's data changed
        //lineChart.invalidate(); // refresh
        lineChart.setVisibleXRangeMaximum(numShownDays-1);
        lineChart.moveViewToX(++leftDay);

        lastDay = newDay;
        numDays++;
    }
}
