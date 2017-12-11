package com.example.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddProjectActivity extends AppCompatActivity {

    RowAdapter adapter;
    ArrayList<ProjectClass> projectListToShow = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        final ProjectClass newProject = new ProjectClass();
        final TextView manualProjectName = (TextView) findViewById(R.id.newProjectName);
        manualProjectName.setText("");


        adapter = new RowAdapter(projectListToShow);
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        Button addButton = (Button) findViewById(R.id.addProjectButton);
        final LinearLayout createProjectLayout = (LinearLayout) findViewById(R.id.usersLinLayout);
        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
                final ArrayList<String> listOfRegisteredUsers = new ArrayList<>();
                mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot userProjects) {
                        for (DataSnapshot user : userProjects.getChildren())
                        {
                            listOfRegisteredUsers.add(user.child("email").getValue().toString());
                        }

                        final AutoCompleteTextView manualUserEditText = new AutoCompleteTextView(v.getContext());
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(v.getContext(),
                                android.R.layout.simple_dropdown_item_1line, listOfRegisteredUsers);
                        manualUserEditText.setAdapter(adapter);
                        manualUserEditText.setThreshold(1);
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

                        createProjectLayout.addView(manualUserEditText);

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
        Calendar myCalendar = Calendar.getInstance();
        final ArrayList<String> usersUID = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        final String formattedDate = dateFormat.format(myCalendar.getTime());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.child("Uzivatel").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot usersData) {
                                final int editTextsCount = createProjectLayout.getChildCount();
                                for (int i = 0; i < editTextsCount; i++) {
                                    Boolean allUsersWereFound = false;
                                    for (DataSnapshot user : usersData.getChildren()) {
                                        EditText editText = (EditText) createProjectLayout.getChildAt(i);
                                        if (user.child("email").getValue().toString().equals(editText.getText().toString())) {
                                            usersUID.add(user.getKey());
                                            allUsersWereFound = true;
                                        }
                                    }
                                    if (!allUsersWereFound) {
                                        // ERROR
//                                        return;
                                    }
                                }
                                TextView projectNameTextView = (TextView) findViewById(R.id.newProjectName);
                                String projectName = projectNameTextView.getText().toString();
                                String projectID = mData.child("Projects").push().getKey();
                                mData.child("Projects").child(projectID).child("projectName").
                                        setValue(projectName);

                                newProject.setProjectName(projectName);
                                newProject.setId(projectID);



                                for (int i = 0; i < usersUID.size(); i++) {
                                    Report report = new Report(0, "", 0);

                                    EditText inputUserEditText = (EditText) createProjectLayout.getChildAt(i);
                                    newProject.addProjectUsers(inputUserEditText.getText().toString());

                                    Map<String, Object> updatedUserData = new HashMap<>();
                                    updatedUserData.put("Projects/" + projectID + "/" +
                                            usersUID.get(i) + "/" + formattedDate , report);

                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Active" , projectID);


                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Projects/" + projectID + "/" + "projectName" , projectName);

                                    mData.updateChildren(updatedUserData);
                                }

                                projectListToShow.add(newProject);
                                adapter.notifyDataSetChanged();
                                Intent intent = new Intent(AddProjectActivity.this, ProjectsActivity.class);
                                startActivity(intent);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

            }
        });
    }
}
