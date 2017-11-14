package com.example.petr.testing;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
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

public class DisplayMessageActivity extends AppCompatActivity {
    Calendar myCalendar = Calendar.getInstance();
    ImageButton floatButton;
    DatabaseReference mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);
        getSupportActionBar().setTitle("Report your day");

        final EditText edittext = (EditText) findViewById(R.id.date);
        mData = FirebaseDatabase.getInstance().getReference();

        floatButton = (ImageButton) findViewById(R.id.sendReport);
        floatButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                sendReport(v);
                /*
                TODO - udelat lepsi Toast; pokud uz za dany den byl zadan report - ohlasi to
                uzivateli
                */

                Toast.makeText(getApplicationContext(), "Report has been sent!", Toast.LENGTH_LONG).show();
            }
        });

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
                final DatePickerDialog datePickerDialog = new DatePickerDialog(DisplayMessageActivity.this, date, myCalendar
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
                        mData.child("Projects").child(currentUser.child("Active").getValue().toString())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot choosenProject) {

                                        Iterator<DataSnapshot> firstUser =  choosenProject.getChildren().iterator();

                                        while (firstUser.hasNext())
                                        {
                                            if (firstUser.next().getKey().equals("Ending"))
                                            {
                                                continue;
                                            }
                                            Iterator<DataSnapshot> firstDate = firstUser.next().getChildren().iterator();
                                            while (firstDate.hasNext())
                                            {
                                                String myFormat = "yyyy-MM-dd";
                                                final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
                                                Date date;
                                                try {
                                                    date = sdf.parse(firstDate.next().getKey());
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
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(myCalendar.getTime()));
        edittext.setText(sdf.format(myCalendar.getTime()));
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
        String myFormat = "yyyy-MM-dd";
        final SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(calendar.getTime()));
        final String yesterday = sdf.format(calendar.getTime());
        mData = FirebaseDatabase.getInstance().getReference();
        mData.child("Uzivatel").child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentUser) {

                if (!currentUser.hasChild("Active"))
                {
                    return;
                }

                mData.child("Projects").child(currentUser.child("Active").getValue().toString()).child(userID).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {

                                EditText message = (EditText) findViewById(R.id.reportText);
                                String str = message.getText().toString();
                                RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
                                String dateTime = ((EditText) findViewById(R.id.date)).getText().toString();
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

                                    Query lastQuery = mData.child("Projects").child(currentUser.child("Active").getValue().toString()).child(userID).orderByKey().limitToLast(1);
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

                                                Date newAdded = sdf.parse(newDate);
                                                Date lastAdded = sdf.parse(lastDate);
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.setTime(lastAdded);
                                                calendar.add(Calendar.DATE, 1);
                                                while(calendar.getTime().before(newAdded))
                                                {
                                                    String newDay = sdf.format(calendar.getTime());
                                                    Report report = new Report(0, "", -2);
                                                    mData.child("Projects").child(currentUser.child("Active").getValue().toString()).child(userID).child(newDay).setValue(report);
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

                                Report report = new Report(previousValue, str, selectedValue);
                                mData.child("Projects").child(currentUser.child("Active").getValue().toString()).child(userID).child(dateTime).setValue(report);

                                Log.d("myTag", Integer.toString(selectedValue));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {
                                Log.w("NECO", "getUser:onCancelled", databaseError.toException());
                            }

                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });




    }
}
