package com.NudgeMe.petr.testing;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.concurrent.TimeUnit;

import static android.view.Menu.CATEGORY_SYSTEM;


public class GraphActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    LineChart lineChart;
    LineData data;
    int numDays = 0;
    int leftDay = 0;
    int numShownDays = 10;
    int firstDayOfWeek = 0;
    Date firstDayShown = null;
    LinearLayout usersLinearLayout;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList<String> mPlanetTitles;
    private ListView mDrawerList;
    final String[] xLabels = new String[] { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };

    private DatabaseReference mData;
    private FirebaseUser user;

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
            obj.put("to", "/topics/" + userID);
            obj.put("priority", 10);
            obj.put("notification", objData);
            obj.put("data", dataobjData);

//            Log.e("!_@rj@_@@_PASS:>", obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send", obj,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
//                        Log.e("!_@@_SUCESS", response + "");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
//                        Log.e("!_@@_Errors--", error + "");
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

        int socketTimeout = 1000 * 60;// 60 seconds
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        jsObjRequest.setRetryPolicy(policy);
        requestQueue.add(jsObjRequest);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(final MenuItem item) {
        // Handle navigation view item clicks here.

        // You choose to manage projects or choose one of your project to be active
        switch (item.getItemId())
        {
            case 1:
            {
                Intent intent = new Intent(this, ProjectsActivity.class);
                startActivity(intent);
                break;
            }
            case 2:
            {
                Intent intent = new Intent(this, UserSettingActivity.class);
                startActivity(intent);
                break;
            }

            default:
            {
                mData.child("Uzivatel").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot uzivatel) {

                        int id = item.getItemId();
                        for (DataSnapshot project : uzivatel.child("Projects").getChildren())
                        {
                            if (project.getKey().hashCode() == id)
                            {
                                Map<String, Object> updatedUserData = new HashMap<>();

                                updatedUserData.put("Uzivatel/" + uzivatel.getKey() + "/" +
                                        "Active" , project.getKey());

                                mData.updateChildren(updatedUserData);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
                break;

            }

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    @Override
    protected void onStart()
    {
        super.onStart();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);


        user = FirebaseAuth.getInstance().getCurrentUser();
        mData = FirebaseDatabase.getInstance().getReference();
        mData.keepSynced(true);

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

        if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
        {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            mData.child("Uzivatel").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.child("Active").exists())
                    {
                        if (dataSnapshot.child("Projects").child(dataSnapshot.child("Active").getValue().toString()).child("projectName").exists())
                        {
                            getSupportActionBar().setTitle(dataSnapshot.child("Projects").child(dataSnapshot.child("Active").getValue().toString()).child("projectName").getValue().toString());
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();

            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

            final Menu menu = navigationView.getMenu();
            menu.clear();
            menu.add(1, 1, Menu.NONE, "Manage projects").setIcon(R.drawable.ic_settings_black_24px);
            menu.add(1, 2, Menu.NONE, "User").setIcon(R.drawable.ic_person_black_24dp);

            navigationView.setNavigationItemSelectedListener(this);
            final SubMenu subMenu = menu.addSubMenu("My projects");
            mData.child("Uzivatel").child(user.getUid()).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot uzivatel) {
                    final ArrayList<String> projectsList = new ArrayList<>();
                    final ArrayList<String> projectsIDs = new ArrayList<>();
                    subMenu.clear(); // je treba vymazat existujici nabidku projektů
                    for (DataSnapshot project : uzivatel.child("Projects").getChildren())
                    {
                        subMenu.add(1, project.getKey().hashCode(), CATEGORY_SYSTEM, project.child("projectName").getValue().toString()).setIcon(R.drawable.file);
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


            ImageButton nudgeMeButton = (ImageButton) findViewById(R.id.nudgeMyTeam);
            nudgeMeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Calendar myCalendar = Calendar.getInstance();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                    String id = user.getUid();
                    final String formattedDate = dateFormat.format(myCalendar.getTime());
                    mData.child("Uzivatel").child(id)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot currentUser) {

                                    if (!currentUser.hasChild("Active"))
                                    {
                                        Toast.makeText(getApplicationContext(), "Current user doesn't have any project!", Toast.LENGTH_LONG).show();
                                        return;
                                    }
                                    mData.child("Projects").child(currentUser.child("Active").getValue().toString())
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


            ImageButton goToReport = (ImageButton) findViewById(R.id.goToReportButton);
            goToReport.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(GraphActivity.this, DisplayMessageActivity.class);
                    startActivity(intent);
                }
            });
        }


//        lineChart = (LineChart) findViewById(R.id.lineChart);
//
//        List<Entry> entries = new ArrayList<>();
//        List<Entry> entries1 = new ArrayList<>();
//        for (int i = 0 ; i <= 10 ; i++)
//        {
//            entries.add(new Entry(i,i));
//            entries1.add((new Entry(i,i*i)));
//        }
//
//
//
//        LineDataSet dataSet = new LineDataSet(entries, "Label");
//        dataSet.setHighLightColor(Color.GREEN);
//        dataSet.setDrawHighlightIndicators(false);
//        dataSet.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        LineDataSet dataSet1 = new LineDataSet(entries1, "Label1");
//        dataSet1.setAxisDependency(YAxis.AxisDependency.RIGHT);
//        List<ILineDataSet> dataSets = new ArrayList<>();
//        dataSets.add(dataSet);
//        dataSets.add(dataSet1);
//        LineData lineData = new LineData(dataSets);
//        lineChart.setData(lineData);
//        lineChart.setScaleEnabled(true);
//        lineChart.setHighlightPerDragEnabled(false);
////        lineChart.setHighlightPerTapEnabled(false);
//
////        lineChart.invalidate();
//
//        // the labels that should be drawn on the XAxis
//        final String[] quarters = new String[] { "Q1", "Q2", "Q3", "Q4","Q5", "Q6", "Q7", "Q8","Q9", "Q10", "Q11" };
//
//        IAxisValueFormatter formatter = new IAxisValueFormatter() {
//
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return quarters[(int) value];
//            }
//
//        };
//
//        XAxis xAxis = lineChart.getXAxis();
//        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
//        xAxis.setValueFormatter(formatter);
//        xAxis.setDrawGridLines(false);
//
//        YAxis yAxis = lineChart.getAxisLeft();
//        yAxis.setAxisMinimum(10);
//        yAxis.setAxisMinValue(0);
//        yAxis.setGranularity(50);
//        yAxis.setTextSize(20);

        mData.child("Uzivatel").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot uzivatel) {

                if (!uzivatel.hasChild("Active"))
                {
                    lineChart = (LineChart) findViewById(R.id.lineChart);
                    lineChart.clear();
                    usersLinearLayout = (LinearLayout) findViewById(R.id.projectUsersLayout);
                    usersLinearLayout.removeAllViews();
                    getSupportActionBar().setTitle("");
                    return;
                }


                mData.child("Projects").child(uzivatel.child("Active").getValue().toString()).addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(final DataSnapshot dataSnapshot) {

                                if (!dataSnapshot.hasChild(user.getUid()))
                                {
                                    lineChart = (LineChart) findViewById(R.id.lineChart);
                                    lineChart.clear();
                                    usersLinearLayout = (LinearLayout) findViewById(R.id.projectUsersLayout);
                                    usersLinearLayout.removeAllViews();
                                    getSupportActionBar().setTitle("");
                                    return;
                                }
                                numDays = leftDay = 0;
                                lineChart = (LineChart) findViewById(R.id.lineChart);
                                ArrayList<ILineDataSet> dataSets = new ArrayList<>();

                                if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                                {
                                    usersLinearLayout = (LinearLayout) findViewById(R.id.projectUsersLayout);
                                    usersLinearLayout.removeAllViews();
                                }

                                int iUser = 0;
                                for (final DataSnapshot user : dataSnapshot.getChildren())
                                {
                                    if (user.getKey().equals("projectName") || user.getKey().equals("Ending"))
                                    {
                                        continue;
                                    }

                                    if (getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
                                    {
                                        mData.child("Uzivatel").child(user.getKey()).addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot userForIconSetting) {
                                                if (userForIconSetting.hasChild("Icon"))
                                                {
                                                    setAvatars(userForIconSetting.child("Icon").getValue().toString(), uzivatel.child("email").getValue().toString(), user.getKey(), dataSnapshot.getKey());
                                                }
                                                else
                                                {
                                                    setAvatars("", uzivatel.child("email").getValue().toString(), user.getKey(), dataSnapshot.getKey());
                                                }
                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                                    }

                                    dataSets = someGraphSetting(iUser, user, dataSets);
                                    if (dataSets.isEmpty())
                                    {
                                        return;
                                    }

                                    iUser++;
                                }

                                ArrayList<Entry> filler = fillEmptyDays();
                                if (filler.size() > 0) {
                                    LineDataSet lineDataSetFill = new LineDataSet(filler, "fill");
                                    setUpDataset(lineDataSetFill, true);
                                    lineDataSetFill.setColor(Color.TRANSPARENT);
                                    lineDataSetFill.setCircleColor(Color.TRANSPARENT);
                                    dataSets.add(0, lineDataSetFill);
                                }

                                data = new LineData(dataSets);
                                lineChart.setData(data);

                                setUpChart(lineChart);

                                lineChart.notifyDataSetChanged(); // let the chart know it's data changed
                                lineChart.setVisibleXRangeMaximum(numShownDays-1);
////                                lineChart.setVisibleXRangeMaximum(numDays);
                                lineChart.setVisibleXRangeMinimum(numShownDays-1);
                                lineChart.setExtraOffsets(0, 0, 0, 5);
//
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

    // NEKTERA NASTAVENI GRAFU, KTERA JESTE NECHAPU
    public ArrayList<ILineDataSet> someGraphSetting(int iUser, DataSnapshot user, ArrayList<ILineDataSet> dataSets)
    {
        ArrayList<Entry> yValues = new ArrayList<>();
        ArrayList<Entry> yValuesMiss = new ArrayList<>();

        float y = 0f;
        yValues.add(new Entry(0, y)); // first entry is 0
        Entry lastEntry = new Entry(0, y);
        Entry lastMissEntry = null;
        int i = 1;
        boolean preReport = true; // solution for late addition of user and his missed reports
        boolean lastMiss = false;
        boolean thisMiss;
        Date date;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        Calendar actDate = Calendar.getInstance();
        for (DataSnapshot value : user.getChildren())
        {
            // zeptat se Michala na lepsi moznost podminky
            if (!value.exists()) {
                return dataSets;
            }
            // get the smile
            long smile = (long) value.child("sendValue").getValue();
            //Log.d("SMILE_TEST", "smile: " + (int)smile);
            // get the date
            String key = value.getKey();
            try {
                date = dateFormatter.parse(key);
                actDate.setTime(date);
            } catch (ParseException e) {
                e.printStackTrace();
            }

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
            else
            { // !thisMiss && !lastMiss => just add new value to dataset
                if (lastMiss)
                {
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
//            yValues.add(new Entry(i, (int)smile));
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


        if (yValues.size() > 0)
        {
            LineDataSet lineDataSet = new LineDataSet(yValues, i + ": data");
            setUpDataset(lineDataSet, false);
            setDatasetColor(lineDataSet, iUser, false);
            dataSets.add(0, lineDataSet);
        }
        if (yValuesMiss.size() > 0)
        {
            LineDataSet lineDataSetMiss = new LineDataSet(yValuesMiss, i + "m: data");
            setUpDataset(lineDataSetMiss, true);
            setDatasetColor(lineDataSetMiss, iUser, true);
            dataSets.add(0, lineDataSetMiss);
        }
        return dataSets;
    }

    public void setAvatars(String iconName, final String email, final String userKey, final String projectKey)
    {
        final ImageButton userOnProjectButton = new ImageButton(GraphActivity.this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(usersLinearLayout.getHeight(), usersLinearLayout.getHeight());
        int margin = 10;
        params.setMargins(margin, 0, margin, 0);
        params.gravity = Gravity.CENTER;
        userOnProjectButton.setLayoutParams(params);

        if (!iconName.isEmpty())
        {
            int iconID = userOnProjectButton.getContext().getResources().getIdentifier(iconName, "drawable", userOnProjectButton.getContext().getPackageName());
            userOnProjectButton.setBackgroundResource(iconID);
        }
        else
        {
            userOnProjectButton.setBackgroundResource(R.drawable.animal_ant_eater);
        }

        userOnProjectButton.setPadding(20, 20, 20, 20);
        userOnProjectButton.setTag(userKey);
        usersLinearLayout.addView(userOnProjectButton);
        userOnProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GraphActivity.this, UserReportsActivity.class);
                intent.putExtra("projectName", projectKey);
                intent.putExtra("userName", userKey);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });
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
                    lineDataSet.setColor(Color.argb(255, 229, 115, 115));
                    lineDataSet.setCircleColor(Color.argb(255, 229, 115, 115));
                break;
            case 1: // Base_Light-Blue: #4fc3f7, Miss_Light-Blue: #b3e5fc
                    lineDataSet.setColor(Color.argb(255, 79, 195, 247));
                    lineDataSet.setCircleColor(Color.argb(255, 79, 195, 247));
                break;
            case 2: // Base_Green: #81c784, Miss_Green: #c8e6c9
                    lineDataSet.setColor(Color.argb(255, 129, 199, 132));
                    lineDataSet.setCircleColor(Color.argb(255, 129, 199, 132));
                break;
            case 3: // Base_Purple: #ba68c8, Miss_Purple: #e1bee7
                    lineDataSet.setColor(Color.argb(255, 186, 104, 200));
                    lineDataSet.setCircleColor(Color.argb(255, 186, 104, 200));
                break;
            default:
                    lineDataSet.setColor(Color.BLUE);
                    lineDataSet.setCircleColor(Color.BLUE);
                break;
        }
    }

    public void setUpChart(final LineChart lineChart) {
        final YAxis yAxis = lineChart.getAxisLeft();
        final XAxis xAxis = lineChart.getXAxis();

        yAxis.setDrawGridLines(false);
        yAxis.setDrawAxisLine(false);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(1); // minimum axis-step (interval) is 1
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(11);
        xAxis.setTypeface(Typeface.DEFAULT_BOLD);
        xAxis.setGridColor(Color.BLACK);

        if (numDays < numShownDays)
            lineChart.getXAxis().setLabelCount(numDays, true);
        else
            lineChart.getXAxis().setLabelCount(numShownDays, true);


        IAxisValueFormatter labelFormatter = new IAxisValueFormatter() {
            final String[] dayLabels = new String[] { "Mo", "Tu", "We", "Th", "Fr", "Sa", "Su" };

            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                String label = "";
                int day = Math.round(value);

                int actDaysShown = (int)Math.ceil(lineChart.getVisibleXRange());
                float actXShown = lineChart.getVisibleXRange();
                int actLabelsShown = lineChart.getXAxis().getLabelCount();
                float labelInterval = actXShown/actLabelsShown;

                if (day == numDays) {
                    label = "Today";
                }
                else if (actDaysShown > numShownDays) {
                    if (value % 7 > (value+labelInterval) % 7 && value < numDays - 8) {
                        label = (int)((numDays+1) / 7 - value / 7) + "w ago";
                    }
                } else {
                    if (day > numDays - 7)
                        label = dayLabels[(day + firstDayOfWeek) % 7];
                    else if ((day % 7 + 6) % 7 == numDays % 7 && day < numDays - 8)
                        label = (numDays + 1) / 7 - day / 7 + "w ago";
                }

                return label;
            }
        };
        xAxis.setValueFormatter(labelFormatter);

        OnChartGestureListener gestureListener = new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {
                lineChart.setVisibleXRangeMaximum(numDays);
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
                lineChart.getXAxis().setLabelCount((int)Math.ceil(lineChart.getVisibleXRange()), true);
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
        lineChart.setScaleYEnabled(false);

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

    public ArrayList<Entry> fillEmptyDays() {
        Calendar firstDay = Calendar.getInstance();
        firstDay.setTime(firstDayShown);
        //Log.d("TODAY", "first day: " + firstDayShown);
        firstDay.set(Calendar.HOUR_OF_DAY, 0);
        firstDay.set(Calendar.MINUTE, 0);
        firstDay.set(Calendar.SECOND, 0);
        firstDay.set(Calendar.MILLISECOND, 0);
        Calendar now = Calendar.getInstance();
        long daysBetween = TimeUnit.MILLISECONDS.toDays(
                Math.abs(now.getTimeInMillis() - firstDay.getTimeInMillis()))+1;
        //Log.d("TODAY", "days between: " + daysBetween + ", numdays: " + numDays);

        ArrayList<Entry> filler = new ArrayList<>();

        for (int i = numDays+1; i <= daysBetween; i++) {
            //Log.d("TODAY", "adding transparent day at: " + i);
            filler.add(new Entry(i, 0));
            numDays++;
        }

        return filler;
    }

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

}