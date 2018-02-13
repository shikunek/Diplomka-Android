package com.NudgeMe.petr.testing;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Button createProjectButton = (Button) findViewById(R.id.toolbarButton);
        createProjectButton.setVisibility(View.VISIBLE);
        createProjectButton.setText("Create");
        final ProjectClass newProject = new ProjectClass();
        final TextView manualProjectName = (TextView) findViewById(R.id.newProjectName);
        manualProjectName.setText("");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        adapter = new RowAdapter(projectListToShow);
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        final LinearLayout createProjectLayout = (LinearLayout) findViewById(R.id.usersLinLayout);
        if (getIntent().hasExtra("InvitedUsers"))
        {

            String[] invitedUsers = getIntent().getStringArrayExtra("InvitedUsers");
            createProjectLayout.removeAllViews();
            for (String user : invitedUsers)
            {

                TextView tw = new TextView(AddProjectActivity.this);
                tw.setTextSize(18F);
                tw.setTextColor(Color.BLACK);
                tw.setText(user);
                createProjectLayout.addView(tw);
            }
        }
        else
        {
            TextView actualUserTextView = new TextView(this);
            actualUserTextView.setTextSize(18F);
            actualUserTextView.setTextColor(Color.BLACK);
            actualUserTextView.setText(currentUser.getEmail());
            createProjectLayout.addView(actualUserTextView);
        }

        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent intent = new Intent(AddProjectActivity.this, AddUsersActivity.class);
                ArrayList<String> usersOnProject = new ArrayList<>();
                for (int i = 0; i < createProjectLayout.getChildCount(); i++)
                {
                    TextView tw = (TextView) createProjectLayout.getChildAt(i);
                    usersOnProject.add(tw.getText().toString());

                }
                intent.putExtra("activity","NewProject");
                intent.putExtra("usersOnProject", usersOnProject);
                startActivity(intent);


            }
        });
        Calendar myCalendar = Calendar.getInstance();
        final ArrayList<String> usersUID = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
        final String formattedDate = dateFormat.format(myCalendar.getTime());

        createProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView projectNameTextView = (TextView) findViewById(R.id.newProjectName);

                if (projectNameTextView.getText().toString().equals(""))
                {
                    Toast toast = Toast.makeText(getApplicationContext(), "Project name must be filled!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                    toast.show();
                    return;
                }
                mData.child("Uzivatel").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot usersData) {
                                final int editTextsCount = createProjectLayout.getChildCount();
                                for (int i = 0; i < editTextsCount; i++) {
                                    for (DataSnapshot user : usersData.getChildren()) {
                                        TextView editText = (TextView) createProjectLayout.getChildAt(i);
                                        if (user.child("email").getValue().toString().equals(editText.getText().toString())) {
                                            usersUID.add(user.getKey());
                                        }
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

                                    TextView inputUserEditText = (TextView) createProjectLayout.getChildAt(i);
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
                                finish();
//                                Intent intent = new Intent(AddProjectActivity.this, ProjectsActivity.class);
//                                startActivity(intent);

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
