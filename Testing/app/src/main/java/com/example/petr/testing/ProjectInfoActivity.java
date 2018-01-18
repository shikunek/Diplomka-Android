package com.example.petr.testing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;


public class ProjectInfoActivity extends AppCompatActivity {

    DatabaseReference mData;
    Calendar myCalendar = Calendar.getInstance();
    EditText endDateEditText;
    TextView projectName;
    ArrayList<String> usersToDelete;
    int userId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        mData = FirebaseDatabase.getInstance().getReference();
        final Intent intent = getIntent();
        endDateEditText = (EditText) findViewById(R.id.endEditText);
        endDateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEndingDate(endDateEditText);
            }
        });
        usersToDelete = new ArrayList<>();
        projectName = (TextView) findViewById(R.id.projectName);
        final LinearLayout createProjectLayout = (LinearLayout) findViewById(R.id.usersLayout);
        final Button applyChanges = (Button) findViewById(R.id.okButton);
        applyChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges(intent.getExtras().getString("projectName"));
            }
        });
        mData.child("Projects").child(intent.getExtras().getString("projectName")).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot choosenProject) {

                        final TextView projectsListView = (TextView) findViewById(R.id.projectName);
                        projectsListView.setText(choosenProject.child("projectName").getValue().toString());
                        Iterator<DataSnapshot> firstUser =  choosenProject.getChildren().iterator();
                        while (firstUser.hasNext())
                        {
                            DataSnapshot firstChild = firstUser.next();
                            if (firstChild.getKey().equals("Ending"))
                            {
                                continue;
                            }
                            Iterator<DataSnapshot> firstDate = firstChild.getChildren().iterator();
                            while (firstDate.hasNext())
                            {
                                TextView startDate = (TextView) findViewById(R.id.startEditText);
                                startDate.setText(firstDate.next().getKey());
                                break;
                            }

                            break;
                        }


                        if(choosenProject.hasChild("Ending"))
                        {
                            endDateEditText.setText(choosenProject.child("Ending").getValue().toString());
                        }


                        for (DataSnapshot user : choosenProject.getChildren())
                        {
                            if (user.getKey().equals("Ending") || user.getKey().equals("projectName") )
                            {
                                continue;
                            }
                            mData.child("Uzivatel").child(user.getKey()).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(final DataSnapshot userEmail)
                                {
                                    final AutoCompleteTextView manualUserEditText = new AutoCompleteTextView(ProjectInfoActivity.this);
                                    final ImageButton deleteUserButton = new ImageButton(ProjectInfoActivity.this);
                                    deleteUserButton.setId(userEmail.hashCode());

                                    deleteUserButton.setBackgroundColor(Color.WHITE);
                                    deleteUserButton.setImageResource(R.drawable.ic_delete_black_24dp);
                                    deleteUserButton.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            LinearLayout userToDelete = (LinearLayout) createProjectLayout.findViewById(v.getId());
                                            AutoCompleteTextView text = (AutoCompleteTextView) userToDelete.getChildAt(0);
                                            usersToDelete.add(text.getText().toString());
                                            createProjectLayout.removeView(userToDelete);

                                        }
                                    });

                                    mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot userProjects) {
                                            ArrayList<String> listOfRegisteredUsers = new ArrayList<String>();
                                            for (DataSnapshot user : userProjects.getChildren())
                                            {
                                                listOfRegisteredUsers.add(user.child("email").getValue().toString());
                                            }
                                            final LinearLayout item = new LinearLayout(ProjectInfoActivity.this);
                                            item.setOrientation(LinearLayout.HORIZONTAL);
                                            ArrayAdapter<String> adapter = new ArrayAdapter<>(ProjectInfoActivity.this,
                                                    android.R.layout.simple_dropdown_item_1line, listOfRegisteredUsers);

                                            manualUserEditText.setAdapter(adapter);
                                            manualUserEditText.setThreshold(1);
                                            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                                            manualUserEditText.setLayoutParams(params);
                                            manualUserEditText.setText(userEmail.getValue().toString());
                                            manualUserEditText.addTextChangedListener(new TextWatcher() {
                                                @Override
                                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                                }

                                                @Override
                                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                }

                                                @Override
                                                public void afterTextChanged(Editable s) {

                                                }
                                            });
                                            item.addView(manualUserEditText);
                                            item.addView(deleteUserButton);
                                            item.setId(userEmail.hashCode());

                                            createProjectLayout.addView(item);

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

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final AutoCompleteTextView manualUserEditText = new AutoCompleteTextView(v.getContext());
                final ImageButton deleteUserButton = new ImageButton(ProjectInfoActivity.this);
                deleteUserButton.setImageResource(R.drawable.ic_delete_black_24dp);
                deleteUserButton.setBackgroundColor(Color.WHITE);
                deleteUserButton.setId(userId);
                deleteUserButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        LinearLayout userToDelete = (LinearLayout) createProjectLayout.findViewById(v.getId());
                        AutoCompleteTextView text = (AutoCompleteTextView) userToDelete.getChildAt(0);
                        usersToDelete.add(text.getText().toString());
                        createProjectLayout.removeView(userToDelete);

                    }
                });

                mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userProjects) {
                        ArrayList<String> listOfRegisteredUsers = new ArrayList<String>();
                        for (DataSnapshot user : userProjects.getChildren())
                        {
                            listOfRegisteredUsers.add(user.child("email").getValue().toString());
                        }
                        final LinearLayout item = new LinearLayout(ProjectInfoActivity.this);
                        item.setOrientation(LinearLayout.HORIZONTAL);
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(),
                                android.R.layout.simple_dropdown_item_1line, listOfRegisteredUsers);
                        manualUserEditText.setAdapter(adapter);
                        manualUserEditText.setThreshold(1);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
                        manualUserEditText.setLayoutParams(params);
                        manualUserEditText.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                        if(s.length() >= 2) {
//                            if (!manualUserEditText.isPopupShowing()) {
//                                manualUserEditText.setError("Not found");
//                                return;
//                            }
//                        }
                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                        if (s.length() > 1 && (!manualUserEditText.isPopupShowing()) && !manualUserEditText.isPerformingCompletion()) {
//                            manualUserEditText.setError("Not found");
//                            return;
//                        }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

//                            if (!manualUserEditText.isPerformingCompletion()) {
//                                manualUserEditText.setError("Not found");
//                                return;
//                            }

                            }
                        });
                        item.setId(userId);
                        item.addView(manualUserEditText);
                        item.addView(deleteUserButton);
                        userId++;
                        createProjectLayout.addView(item);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });

    }

    private void updateLabel(Calendar myCalendar, EditText edittext) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
        Log.d("NECO","Yesterday's date was "+sdf.format(myCalendar.getTime()));
        edittext.setText(sdf.format(myCalendar.getTime()));
    }

    public void setEndingDate(final EditText editText)
    {
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(myCalendar, editText);
            }

        };
        final DatePickerDialog datePickerDialog = new DatePickerDialog(ProjectInfoActivity.this, date, myCalendar
                .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        datePickerDialog.show();
    }

    public void saveChanges(final String projectID)
    {
        mData.child("Projects").child(projectID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentProject) {

                if (projectName.getText().toString().equals(""))
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Project name must be filled!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                final Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("Projects/" + projectID + "/" +
                        "Ending", endDateEditText.getText().toString());
                updatedUserData.put("Projects/" + projectID + "/" +
                        "projectName", projectName.getText().toString());

                for (DataSnapshot user : currentProject.getChildren())
                {
                    if (user.getKey().equals("projectName") || user.getKey().equals("Ending"))
                    {
                        continue;
                    }

                    updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                            "Projects" + "/" + projectID + "/" + "projectName", projectName
                            .getText().toString());

                }

                final LinearLayout usersToSave = (LinearLayout) findViewById(R.id.usersLayout);

                    mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot users) {
                            for (DataSnapshot user : users.getChildren())
                            {
                                for (int i = 0; i < usersToSave.getChildCount(); i++)
                                {
                                    LinearLayout item = (LinearLayout) usersToSave.getChildAt(i);
                                    for (int j = 0; j < item.getChildCount() ; j++)
                                    {
                                        if (item.getChildAt(j) instanceof AutoCompleteTextView)
                                        {
                                            AutoCompleteTextView userEmail = (AutoCompleteTextView) item.getChildAt(j);
                                            String email = user.child("email").getValue().toString();
                                            String text = userEmail.getText().toString();
                                            Boolean exists = !currentProject.hasChild(user.getKey());
                                            if(user.child("email").getValue().toString().equals(userEmail.getText().toString()) &&
                                                    !currentProject.hasChild(user.getKey()))
                                            {
                                                Calendar myCalendar = Calendar.getInstance();
                                                Report report = new Report(0, "", 0);
                                                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                                                final String formattedDate = dateFormat.format(myCalendar.getTime());
                                                updatedUserData.put("Projects/" + projectID + "/" + user.getKey() +
                                                        "/" + formattedDate, report);
                                                updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                        "Projects" + "/" + projectID + "/" + "projectName", projectName
                                                        .getText().toString());
                                                updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                        "Active", projectID);


                                            }

                                        }
                                    }
                                }

                                Iterator<String> it = usersToDelete.iterator();
                                while(it.hasNext())
                                {
                                    if (user.child("email").getValue().toString().equals(it.next()))
                                    {
                                        updatedUserData.put("Projects/" + projectID + "/" + user.getKey(), null);
                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                "Projects/" + projectID + "/" + "projectName" , null);
                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                "Active", null);
                                    }
                                }
                            }
                            mData.updateChildren(updatedUserData);

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
}
