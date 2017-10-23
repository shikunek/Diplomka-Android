package com.example.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.IMarker;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Calendar;

public class GraphActivity extends AppCompatActivity {

    LineChart lineChart;
    LineData data;
    int numDays = 0;
    int leftDay = 0;
    int numShownDays = 14;
    int firstDayOfWeek = 0;
    Date firstDayShown = null;
    final String[] xLabels = new String[] { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };

    private DatabaseReference mData;
    private FirebaseUser user;
    Random randomGenerator = new Random();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        getSupportActionBar().setTitle("Project chart");

        mData = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

//        String user = FirebaseAuth.getInstance().getCurrentUser().getUid();

        mData.child("Uzivatel").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot uzivatel) {

                if (!uzivatel.hasChild("Active"))
                {
                    setContentView(R.layout.activity_graph);
                    return;
                }

                mData.child("Projects").child(uzivatel.child("Active").getValue().toString()).addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(user.getUid()))
                                {
                                    setContentView(R.layout.activity_graph);
                                    return;
                                }
                                numDays = leftDay = 0;
                                lineChart = (LineChart) findViewById(R.id.lineChart);

                                final ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

                                int iUser = 0;
                                for (DataSnapshot user : dataSnapshot.getChildren())
                                {
                                    if (user.getKey().equals("projectName"))
                                    {
                                        continue;
                                    }

                                    ArrayList<Entry> yValues = new ArrayList<>();
                                    ArrayList<Entry> yValuesMiss = new ArrayList<>();

                                    float y = 0f;
                                    yValues.add(new Entry(0, y)); // first entry is 0
                                    Entry lastEntry = new Entry(0, y);
                                    int i = 1;
                                    boolean split = false;
                                    Date date;
                                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                    Calendar actDate = Calendar.getInstance();
                                    Calendar targetDate = Calendar.getInstance();
                                    for (DataSnapshot value : user.getChildren())
                                    {
                                        if (!value.exists())
                                        {
                                            return;
                                        }
                                        // get the smile
                                        long smile = (long) value.child("sendValue").getValue();
                                        // get the date
                                        String key = value.getKey();
                                        try {
                                            date = dateFormatter.parse(key);
                                            targetDate.setTime(date);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        if (i == 1) {
                                            if (firstDayShown == null || targetDate.before(firstDayShown)) {
                                                firstDayShown = targetDate.getTime();
                                                // DAY_OF_WEEK start 1 = Su
                                                firstDayOfWeek = (targetDate.get(Calendar.DAY_OF_WEEK)+ 4) % 7;
                                            }
                                            actDate.setTime(targetDate.getTime());
                                        }
                                        Log.d("DATE_TEST", "actDate: " + dateFormatter.format(actDate.getTime()) +
                                                ", targetDate: " + dateFormatter.format(targetDate.getTime()));
                                        Entry lastMissEntry = null;
                                        while (!actDate.equals(targetDate)) {
                                            if (lastMissEntry == null) {
                                                yValuesMiss.add(lastEntry);
                                            }

                                            y = translateEntry(y, -1);
                                            yValuesMiss.add(new Entry(i, y));
                                            lastMissEntry = new Entry(i, y);
                                            i++;
                                            actDate.add(Calendar.DATE, 1);
                                            split = true;
                                        }

                                        if (split) { // missing values between last and this date
                                            LineDataSet lineDataSet = new LineDataSet(yValues, i + ": data");
                                            setUpDataset(lineDataSet, false);
                                            setDatasetColor(lineDataSet, iUser, false);
                                            dataSets.add(0, lineDataSet);

                                            LineDataSet lineDataSetMiss = new LineDataSet(yValuesMiss, i + "m: data");
                                            setUpDataset(lineDataSetMiss, true);
                                            setDatasetColor(lineDataSetMiss, iUser, true);
                                            dataSets.add(0, lineDataSetMiss);

                                            yValues = new ArrayList<>();
                                            // entry to connect last miss and first base entry in base color
                                            yValues.add(lastMissEntry);

                                            yValuesMiss = new ArrayList<>();

                                            split = false;
                                        }
                                        //Log.d("DATE_TEST", "value for " + dateFormatter.format(date) + " added");
                                        y = translateEntry(y, (int)smile);
                                        yValues.add(new Entry(i, y)); // data entry creation
                                        lastEntry = new Entry(i, y);
                                        i++;
                                        actDate.add(Calendar.DATE, 1);
                                    }

                                    if (i-1 > numDays) numDays = i-1;

                                    if (yValues.size() > 0) {
                                        LineDataSet lineDataSet = new LineDataSet(yValues, i + ": data");
                                        setUpDataset(lineDataSet, false);
                                        setDatasetColor(lineDataSet, iUser, false);
                                        dataSets.add(0, lineDataSet);
                                    }
                                    if (yValuesMiss.size() > 0) {
                                        LineDataSet lineDataSetMiss = new LineDataSet(yValuesMiss, i + "m: data");
                                        setUpDataset(lineDataSetMiss, true);
                                        setDatasetColor(lineDataSetMiss, iUser, true);
                                        dataSets.add(0, lineDataSetMiss);
                                    }

                                    iUser++;
                                }

                                data = new LineData(dataSets);
                                lineChart.setData(data);

                                setUpChart(lineChart);

                                lineChart.notifyDataSetChanged(); // let the chart know it's data changed
                                lineChart.setVisibleXRangeMaximum(numShownDays-1);
                                lineChart.setVisibleXRangeMinimum(numShownDays-1);

                                leftDay = numDays-numShownDays+1 >= 0 ? numDays-numShownDays+1 : 0;
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

    // creates y-value from old y-value and new smile
    private float translateEntry(float old_y, int smile) {
        float new_y = 0f;
        float x_delta;
        float shiftPar = 0.4f; // the smaller the number, the slower the change
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

    public void setUpDataset(LineDataSet lineDataSet, boolean miss) {
        lineDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        lineDataSet.setHighlightEnabled(true);
        lineDataSet.setDrawHighlightIndicators(false);
        lineDataSet.setDrawValues(false);
        lineDataSet.setLineWidth(4f);
        if (miss) {
            lineDataSet.setDrawCircles(false);
            lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
            lineDataSet.setCubicIntensity(0.2f);
            /*float lineLength = 20f;
            float spaceLength = 6f;
            float phase = 0.5f;
            lineDataSet.enableDashedLine(lineLength, spaceLength, phase);*/
        } else {
            lineDataSet.setDrawCircles(true);
            lineDataSet.setCircleRadius(4f);
            lineDataSet.setCircleHoleRadius(1.5f);
        }

        //lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        //lineDataSet.setCubicIntensity(0.2f);
    }

    public void setDatasetColor(LineDataSet lineDataSet, int user, boolean miss) {
        if (miss) {
            lineDataSet.setColor(Color.DKGRAY);
            lineDataSet.setCircleColor(Color.DKGRAY);
            return;
        }

        switch (user)
        {
            case 0: // Base_Red: #e57373, Miss_Red: #ffcdd2
                //if (!miss) {
                    lineDataSet.setColor(Color.argb(255, 229, 115, 115));
                    lineDataSet.setCircleColor(Color.argb(255, 229, 115, 115));
                //} else {
                //    lineDataSet.setColor(Color.argb(255, 255, 205, 210));
                //    lineDataSet.setCircleColor(Color.argb(255, 255, 205, 210));
                //}
                break;
            case 1: // Base_Light-Blue: #4fc3f7, Miss_Light-Blue: #b3e5fc
                //if (!miss) {
                    lineDataSet.setColor(Color.argb(255, 79, 195, 247));
                    lineDataSet.setCircleColor(Color.argb(255, 79, 195, 247));
                //} else {
                //    lineDataSet.setColor(Color.argb(255, 179, 229, 252));
                //    lineDataSet.setCircleColor(Color.argb(255, 179, 229, 252));
                //}
                break;
            case 2: // Base_Green: #81c784, Miss_Green: #c8e6c9
                //if (!miss) {
                    lineDataSet.setColor(Color.argb(255, 129, 199, 132));
                    lineDataSet.setCircleColor(Color.argb(255, 129, 199, 132));
                //} else {
                //    lineDataSet.setColor(Color.argb(255, 200, 230, 201));
                //    lineDataSet.setCircleColor(Color.argb(255, 200, 230, 201));
                //}
                break;
            case 3: // Base_Purple: #ba68c8, Miss_Purple: #e1bee7
                //if (!miss) {
                    lineDataSet.setColor(Color.argb(255, 186, 104, 200));
                    lineDataSet.setCircleColor(Color.argb(255, 186, 104, 200));
                //} else {
                //    lineDataSet.setColor(Color.argb(255, 225, 190, 231));
                //    lineDataSet.setCircleColor(Color.argb(255, 225, 190, 231));
                //}
                break;
            default:
                //if (!miss) {
                    lineDataSet.setColor(Color.BLUE);
                    lineDataSet.setCircleColor(Color.BLUE);
                //} else {
                //lineDataSet.setColor(Color.GRAY);
                //    lineDataSet.setCircleColor(Color.GRAY);
                //}
                break;
        }
    }

    public void setUpChart(LineChart lineChart) {
        final YAxis yAxis = lineChart.getAxisLeft();
        final XAxis xAxis = lineChart.getXAxis();

        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        yAxis.setAxisMinimum(-1f);
        yAxis.setAxisMaximum(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1); // minimum axis-step (interval) is 1
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setGridColor(Color.BLACK);

        if (numDays <= 14) xAxis.setLabelCount(numDays+1, true);
        else xAxis.setLabelCount(numShownDays, true);

        IAxisValueFormatter labelFormatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int day = Math.round(value);
                if (day < 0) return "";
                else if (day == numDays) return "Today";
                else if (day > numDays - 7) return xLabels[(day+firstDayOfWeek) % 7];
                else if ((day % 7 + 6) % 7 == numDays % 7 && day < numDays - 8)
                    return (numDays+1) / 7 - day / 7 + "w ago";
                else return "";
            }
        };
        xAxis.setValueFormatter(labelFormatter);

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

        /*IMarker marker = new MarkerView(getApplicationContext(), R.layout.activity_graph) {
            @Override
            public void refreshContent(Entry e, Highlight highlight) {
                tvContent.setText("" + e.getY());
                // this will perform necessary layouting
                super.refreshContent(e, highlight);
            }
        };
        lineChart.setMarker(marker);*/
    }
}