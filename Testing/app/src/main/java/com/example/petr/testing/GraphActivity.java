package com.example.petr.testing;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
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
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GraphActivity extends AppCompatActivity {

    LineChart lineChart;
    LineData data;
    int numDays = 0;
    int leftDay = 0;
    int numShownDays = 14;
    int firstDayOfWeek = 0;
    Date firstDayShown = null;
    LinearLayout usersLinearLayout;
    final String[] xLabels = new String[] { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };

    private DatabaseReference mData;
    private FirebaseUser user;


    public String completeCondition(ArrayList<String> usersToNudge)
    {
        StringBuilder completeCondition = new StringBuilder();
        completeCondition.append("(");
        for (int i = 0; i < usersToNudge.size(); i++)
        {
            if (i != 0)
            {
                completeCondition.append("&&");
            }

            completeCondition.append("'");
            completeCondition.append(usersToNudge.get(i));
            completeCondition.append("'");
            completeCondition.append(" in topics");


        }
        completeCondition.append(")");
        return completeCondition.toString();
    }

    public void sendFCMPush(String userID) {

        final String Legacy_SERVER_KEY = "AIzaSyCB88Oy7989Wj319s4Q4PCDy1oGZo7SMAI";
        String msg = "PLEASE SEND YOUR REPORT";
        String title = "DEAR CO-WORKER";
        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;

        try {
            obj = new JSONObject();
            objData = new JSONObject();

            objData.put("body", msg);
            objData.put("title", title);
            objData.put("sound", "default");
            objData.put("icon", "icon_name"); //   icon_name image must be there in drawable
            objData.put("priority", "high");

            dataobjData = new JSONObject();
            dataobjData.put("text", msg);
            dataobjData.put("title", title);


            obj.put("content_available", true);
//            obj.put("condition", condition2);
            obj.put("to", "/topics/" + userID);
            obj.put("priority", 10);
//            obj.put("topic","news");
            obj.put("notification", objData);
            obj.put("data", dataobjData);

            Log.e("!_@rj@_@@_PASS:>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.e("!_@@_SUCESS", response + "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("!_@@_Errors--", error + "");
                    }
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + Legacy_SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
//        RequestQueue requestQueue = SingletonRequestForNudgeMe.getInstance(this.getApplicationContext()).getRequestQueue();

        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);
        getSupportActionBar().setTitle("Project chart");

        mData = FirebaseDatabase.getInstance().getReference();
        user = FirebaseAuth.getInstance().getCurrentUser();

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.checked)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!");

        Intent resultIntent = new Intent(this, DisplayMessageActivity.class);

    // The stack builder object will contain an artificial back stack for the
    // started Activity.
    // This ensures that navigating backward from the Activity leads out of
    // your app to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
    // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(GraphActivity.class);
    // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

        mBuilder.setAutoCancel(true);
        ImageButton nudgeMeButton = (ImageButton) findViewById(R.id.nudgeMyTeam);
        nudgeMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Calendar myCalendar = Calendar.getInstance();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                String id = user.getUid();
                final String formattedDate = dateFormat.format(myCalendar.getTime());
                mData.child("Uzivatel").child(id).child("Active")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot activeProject) {

                            mData.child("Projects").child(activeProject.getValue().toString())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot currentProject) {
                                            for (DataSnapshot user1 : currentProject.getChildren())
                                            {
                                                if (user1.getKey().equals("projectName") || user1.getKey().equals("Ending"))
                                                {
                                                    continue;
                                                }
                                                if (!user1.hasChild(formattedDate))
                                                {
                                                    sendFCMPush(user1.getKey());
                                                }
                                            }

                                        }

                                        @Override
                                        public void onCancelled(DatabaseError databaseError) {

                                        }
                                    });

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

        final Spinner activeProjectNameSpinner = (Spinner) findViewById(R.id.projectNameSpinner);

        ImageButton goToReport = (ImageButton) findViewById(R.id.goToReportButton);
        goToReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphActivity.this, DisplayMessageActivity.class);
                startActivity(intent);
            }
        });

        mData.child("Uzivatel").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot uzivatel) {
                ArrayList<String> projectsList = new ArrayList<>();
                final ArrayList<String> projectsIDs = new ArrayList<>();
                for (DataSnapshot project : uzivatel.child("Projects").getChildren())
                {
                    projectsList.add(project.child("projectName").getValue().toString());
                    projectsIDs.add(project.getKey());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(GraphActivity.this, android.R.layout.simple_spinner_dropdown_item, projectsList);

                activeProjectNameSpinner.setAdapter(adapter);
                adapter.notifyDataSetChanged();

                activeProjectNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id)
                    {
                        Map<String, Object> updatedUserData = new HashMap<>();

                        updatedUserData.put("Uzivatel/" + uzivatel.getKey() + "/" +
                                "Active" , projectsIDs.get(position));

                        mData.updateChildren(updatedUserData);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parentView) {
                        // your code here
                    }

                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
                            public void onDataChange(final DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(user.getUid()))
                                {
                                    setContentView(R.layout.activity_graph);
                                    return;
                                }
                                numDays = leftDay = 0;
                                lineChart = (LineChart) findViewById(R.id.lineChart);
                                final ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                usersLinearLayout = (LinearLayout) findViewById(R.id.projectUsersLayout);
                                usersLinearLayout.removeAllViews();
                                int iUser = 0;
                                for (final DataSnapshot user : dataSnapshot.getChildren())
                                {
                                    if (user.getKey().equals("projectName") || user.getKey().equals("Ending"))
                                    {
                                        continue;
                                    }
                                    Log.d("SMILE_TEST", "NEW USER");

                                    Button userOnProjectButton = new Button(GraphActivity.this);
                                    userOnProjectButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT));
                                    userOnProjectButton.setText("USER" + String.valueOf(iUser+1));

                                    userOnProjectButton.setTextColor(Color.RED);
                                    userOnProjectButton.setPadding(20, 20, 20, 20);
                                    userOnProjectButton.setTag(user.getKey());
                                    usersLinearLayout.addView(userOnProjectButton);
                                    userOnProjectButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent intent = new Intent(GraphActivity.this, UserReportsActivity.class);
                                            intent.putExtra("projectName", dataSnapshot.getKey());
                                            intent.putExtra("userName", user.getKey());
                                            intent.putExtra("email", uzivatel.child("email").getValue().toString());
                                            startActivity(intent);
                                        }
                                    });

                                    ArrayList<Entry> yValues = new ArrayList<>();
                                    ArrayList<Entry> yValuesMiss = new ArrayList<>();

                                    float y = 0f;
                                    yValues.add(new Entry(0, y)); // first entry is 0
                                    Entry lastEntry = new Entry(0, y);
                                    Entry lastMissEntry = null;
                                    int i = 1;
                                    //boolean preReport = true; // solution for late addition of user and his missed reports
                                    boolean lastMiss = false;
                                    boolean thisMiss;
                                    Date date;
                                    SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                                    Calendar actDate = Calendar.getInstance();
                                    for (DataSnapshot value : user.getChildren())
                                    {
                                        if (!value.exists()) {
                                            return;
                                        }
                                        // get the smile
                                        long smile = (long) value.child("sendValue").getValue();
                                        Log.d("SMILE_TEST", "smile: " + (int)smile);
                                        // get the date
                                        String key = value.getKey();
                                        try {
                                            date = dateFormatter.parse(key);
                                            actDate.setTime(date);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                        Log.d("SMILE_TEST", "actDate: " + dateFormatter.format(actDate.getTime()));

                                        thisMiss = smile < -1f;
                                        /*if (preReport) { // let all missed days before first report go
                                            if (thisMiss) {
                                                i++;
                                                continue;
                                            }
                                            else preReport = false;
                                        }*/

                                        if (i == 1 && (firstDayShown == null || actDate.before(firstDayShown))) {
                                            firstDayShown = actDate.getTime();
                                            // DAY_OF_WEEK start 1 = Su
                                            firstDayOfWeek = (actDate.get(Calendar.DAY_OF_WEEK)+ 4) % 7;
                                        }

                                        if (thisMiss) { // just add new value to miss dataset
                                            if (!lastMiss)
                                                yValuesMiss.add(lastEntry);
                                            y = translateEntry(y, -1);
                                            yValuesMiss.add(new Entry(i, y));
                                            lastMissEntry = new Entry(i, y);
                                        }
                                        else { // !thisMiss && !lastMiss => just add new value to dataset
                                            if (lastMiss) {
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
                                            }
                                            y = translateEntry(y, (int)smile);
                                            yValues.add(new Entry(i, y));
                                            lastEntry = new Entry(i, y);
                                        }

                                        lastMiss = thisMiss;
                                        i++;
                                    }

                                    if (i-1 > numDays)
                                    {
                                        numDays = i-1;
                                    }


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
        lineDataSet.setLineWidth(3f);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        lineDataSet.setCubicIntensity(0.1f);
        if (miss) {
            float lineLength = 20f;
            float spaceLength = 6f;
            float phase = 0.5f;
            lineDataSet.enableDashedLine(lineLength, spaceLength, phase);
        } else {
            lineDataSet.setDrawCircles(true);
            lineDataSet.setCircleRadius(3f);
            lineDataSet.setCircleHoleRadius(1f);
        }
    }

    public void setDatasetColor(LineDataSet lineDataSet, int user, boolean miss) {
        /*if (miss) {
            lineDataSet.setColor(Color.DKGRAY);
            lineDataSet.setCircleColor(Color.DKGRAY);
            return;
        }*/

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

    public void setUpChart(final LineChart lineChart) {
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
        xAxis.setTextSize(11);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setGridColor(Color.BLACK);

        if (numDays <= 14)
        {
            xAxis.setLabelCount(numDays+1, true);
        }

        else
        {
            xAxis.setLabelCount(numShownDays, true);
        }


        IAxisValueFormatter labelFormatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                int day = Math.round(value);
                if (day < 0)
                    return "";

                else if (day == numDays)
                    return "Today";

                else if (day > numDays - 7)
                    return xLabels[(day+firstDayOfWeek) % 7];

                else if ((day % 7 + 6) % 7 == numDays % 7 && day < numDays - 8)
                    return (numDays+1) / 7 - day / 7 + "w ago";

                else return "";
            }
        };
        xAxis.setValueFormatter(labelFormatter);

        OnChartGestureListener gestureListener = new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

            }

            @Override
            public void onChartLongPressed(MotionEvent me) {

            }

            @Override
            public void onChartDoubleTapped(MotionEvent me) {

            }

            @Override
            public void onChartSingleTapped(MotionEvent me) {

            }

            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

            }

            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
//                lineChart.zoomIn();
            }

            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {

            }
        };

        lineChart.setTouchEnabled(true);
        lineChart.setOnChartGestureListener(gestureListener);

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