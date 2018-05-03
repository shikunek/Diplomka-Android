package com.NudgeMe.petr.testing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;

public class ReportActivity extends AppCompatActivity {
    Calendar myCalendar = Calendar.getInstance();
    DatabaseReference mData;
    private Toolbar toolbar;
    String projectID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent.hasExtra("projectID"))
        {
            projectID = intent.getExtras().getString("projectID");
        }


        Button sendButton = (Button) findViewById(R.id.toolbarButton);
        sendButton.setVisibility(View.VISIBLE);
        sendButton.setText("Send");
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendReport(v);
            }
        });

        final EditText edittext = (EditText) findViewById(R.id.date);
        String myFormat1 = "dd.MM.yyyy";
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        edittext.setText(sdf1.format(myCalendar.getTime()));
        mData = FirebaseDatabase.getInstance().getReference();

        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar, edittext);
            }

        };

        edittext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                // TODO Auto-generated method stub
                final DatePickerDialog datePickerDialog = new DatePickerDialog(ReportActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(Calendar.getInstance().getTime());
                datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());

                mData.child("Uzivatel").child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot currentUser) {
                        if (!currentUser.hasChild("Active"))
                        {
                            return;
                        }

                        Intent intent = getIntent();
                        if (intent.hasExtra("projectID"))
                        {
                            projectID = intent.getExtras().getString("projectID");
                        }
                        else
                        {
                            projectID = currentUser.child("Active").getValue().toString();
                        }
                        mData.child("Projects").child(projectID)
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot choosenProject) {

                                        Iterator<DataSnapshot> firstUser =  choosenProject.getChildren().iterator();
                                        String myFormat = "yyyy-MM-dd";
                                        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);

                                        while (firstUser.hasNext())
                                        {
//                                            if (firstUser.next().getKey().equals("Ending") || firstUser.next().getKey().equals("projectName"))
//                                            {
//                                                continue;
//                                            }

                                            Iterator<DataSnapshot> firstDate = firstUser.next().getChildren().iterator();

                                            while (firstDate.hasNext())
                                            {
                                                String projectValue = firstDate.next().getKey();
                                                if (projectValue.equals("projectName") || projectValue.equals("Ending"))
                                                {
                                                    continue;
                                                }

                                                Date date;
                                                try {
                                                    date = sdf.parse(projectValue);
                                                    datePickerDialog.getDatePicker().setMinDate(date.getTime());
                                                } catch (ParseException e) {
                                                    e.printStackTrace();
                                                }


                                                break;
                                            }
                                            break;
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

                datePickerDialog.show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar, EditText edittext) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        edittext.setText(sdf1.format(myCalendar.getTime()));
    }



    public void sendReport(View view)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null)
        {
            return;
        }
        final String userID =  user.getUid();
        Calendar calendar = Calendar.getInstance();
        calendar = myCalendar;
        calendar.add(Calendar.DATE, -1);
        EditText message = (EditText) findViewById(R.id.reportText);
        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        if (message.getText().toString().equals(""))
        {
            Toast.makeText(getApplicationContext(), "You have to fill the message!", Toast.LENGTH_LONG).show();
            return;
        }

        if (radioGroup.getCheckedRadioButtonId() == -1)
        {
            Toast.makeText(getApplicationContext(), "You have to select your evaluation!", Toast.LENGTH_LONG).show();
            return;
        }
        String myFormat = "yyyy-MM-dd";
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        final SimpleDateFormat sdf1 = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMANY);
        final String yesterday = sdf.format(calendar.getTime());
        mData = FirebaseDatabase.getInstance().getReference();
        mData.child("Uzivatel").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentUser) {

                if (!currentUser.hasChild("Active"))
                {
                    return;
                }

                Intent intent = getIntent();
                if (intent.hasExtra("projectID"))
                {
                    projectID = intent.getExtras().getString("projectID");
                }
                else
                {
                    projectID = currentUser.child("Active").getValue().toString();
                }

                mData.child("Projects").child(projectID).child(userID).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {
                                EditText message = (EditText) findViewById(R.id.reportText);
                                String str = message.getText().toString();

                                final EditText edittext = (EditText) findViewById(R.id.date);
                                String dateTime = null;
                                try
                                {
                                    Date showDate = sdf1.parse(edittext.getText().toString());
                                    dateTime = sdf.format(showDate);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                int selectedId = radioGroup.getCheckedRadioButtonId();
                                int selectedValue = 0;

                                switch (selectedId)
                                {
                                    case R.id.happySmile:
                                        selectedValue = 1;
                                        break;

                                    case R.id.sadSmile:
                                        selectedValue = -1;
                                        break;

                                }

                                long previousValue = 0;
                                if (dataSnapshot.child(yesterday).exists())
                                {
                                    previousValue  = (long)dataSnapshot.child(yesterday).child("Y").getValue() + (long)dataSnapshot.child(yesterday).child("sendValue").getValue();
                                }
                                else
                                {

                                    Query lastQuery = mData.child("Projects").child(projectID).child(userID).orderByKey().limitToLast(1);
                                    lastQuery.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                            String lastDate = "";
                                            for (DataSnapshot child : dataSnapshot.getChildren())
                                            {
                                                lastDate = child.getKey();
                                            }
                                            if (lastDate.equals(""))
                                            {
                                                return;
                                            }

                                            EditText edittext = (EditText) findViewById(R.id.date);
                                            String newDate = edittext.getText().toString();
                                            try {

                                                Date newAdded = sdf1.parse(newDate);
                                                Date lastAdded = sdf.parse(lastDate);
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(lastAdded);
                                                calendar.add(Calendar.DATE, 1);
                                                while(calendar.getTime().before(newAdded))
                                                {
                                                    String newDay = sdf.format(calendar.getTime());
                                                    Report report = new Report(0, "", -2);
                                                    mData.child("Projects").child(projectID).child(userID).child(newDay).setValue(report);
                                                    calendar.add(Calendar.DATE, 1);
                                                }


                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                            }

                                        }


                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {
                                    //Handle possible errors.
                                    }
                                    });
                                }

                                if (dateTime == null)
                                {
                                    return;
                                }
                                Report report = new Report(previousValue, str, selectedValue);
                                mData.child("Projects").child(projectID).child(userID).child(dateTime).setValue(report);
                                Toast.makeText(getApplicationContext(), "Report has been sent!", Toast.LENGTH_LONG).show();
                                finish();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }

                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
