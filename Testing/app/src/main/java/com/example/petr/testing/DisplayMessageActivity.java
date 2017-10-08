package com.example.petr.testing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DisplayMessageActivity extends AppCompatActivity {
    Calendar myCalendar = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);

        final EditText edittext = (EditText) findViewById(R.id.date);
        myCalendar = Calendar.getInstance();

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
                new DatePickerDialog(DisplayMessageActivity.this, date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    private void updateLabel(Calendar myCalendar, EditText edittext) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(myCalendar.getTime()));
        edittext.setText(sdf.format(myCalendar.getTime()));
    }

    private DatabaseReference mData;

    public void sendReport(View view)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String userID =  user.getUid();
        myCalendar.add(Calendar.DATE, -1);
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(myCalendar.getTime()));
        final String yesterday = sdf.format(myCalendar.getTime());
        mData = FirebaseDatabase.getInstance().getReference();
        mData.child("Projects").child("Active").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot active) {
                mData.child("Projects").child(active.getValue().toString()).child(userID).child(yesterday).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot)
                            {

                                // nactu si text a hodnotu radiobuttonu
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
                                if (dataSnapshot.exists())
                                {
                                    previousValue  = (long)dataSnapshot.child("Y").getValue() + (long)dataSnapshot.child("sendValue").getValue();
                                }

                                Report report = new Report(previousValue, str, selectedValue);
                                mData.child("Projects").child(active.getValue().toString()).child(userID).child(dateTime).setValue(report);
//                        mData.child("Users").child(userID).child(dateTime).child("Y").setValue(previousValue);
//                        mData.child("Users").child(userID).child(dateTime).child("sendValue").setValue(selectedValue);
//                        mData.child("Users").child(userID).child(dateTime).child("repotedText").setValue(str);
//                        mData.child("Users").child(userID).child("LastAdded").child("Y").setValue(previousValue);

                                Log.d("myTag", Integer.toString(selectedValue));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.w("NECO", "getUser:onCancelled", databaseError.toException());
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




    }
}
