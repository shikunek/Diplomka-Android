package com.NudgeMe.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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

public class AddProjectActivity extends AppCompatActivity
{
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    ArrayList<String> userEmailTextset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_project);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userEmailTextset = new ArrayList<>();
        mRecyclerView = (RecyclerView) findViewById(R.id.newProjectUsersRecyclerView);
        mAdapter = new UsersOnProjectAdapter(userEmailTextset);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Button createProjectButton = (Button) findViewById(R.id.toolbarButton);
        createProjectButton.setVisibility(View.VISIBLE);
        createProjectButton.setText("Create");

        final TextView manualProjectName = (TextView) findViewById(R.id.newProjectName);
        manualProjectName.setText("");
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        mRecyclerView.removeAllViews();
        userEmailTextset.clear();
        if (getIntent().hasExtra("InvitedUsers"))
        {
            ArrayList<String> invitedUsers = getIntent().getStringArrayListExtra("InvitedUsers");
            for (int i = 0; i < invitedUsers.size() ; i++ )
            {
                userEmailTextset.add(invitedUsers.get(i));
                mRecyclerView.setAdapter(mAdapter);
            }
        }
        else
        {
            userEmailTextset.add(currentUser.getEmail());
            mRecyclerView.setAdapter(mAdapter);
        }

        ImageButton addUserButton = (ImageButton) findViewById(R.id.addUserButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                Intent intent = new Intent(AddProjectActivity.this, AddUsersActivity.class);
                intent.putExtra("activity","NewProject");
                intent.putExtra("usersOnProject", userEmailTextset);
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
                            public void onDataChange(DataSnapshot usersData)
                            {
                                for (int i = 0; i < userEmailTextset.size(); i++)
                                {
                                    for (DataSnapshot user : usersData.getChildren())
                                    {
                                        if (user.child("email").getValue().toString().equals(userEmailTextset.get(i)))
                                        {
                                            usersUID.add(user.getKey());
                                        }
                                    }
                                }
                                TextView projectNameTextView = (TextView) findViewById(R.id.newProjectName);
                                String projectName = projectNameTextView.getText().toString();
                                String projectID = mData.child("Projects").push().getKey();
                                mData.child("Projects").child(projectID).child("projectName").
                                        setValue(projectName);

                                for (int i = 0; i < usersUID.size(); i++) {
                                    Report report = new Report(0, "", 0);

                                    Map<String, Object> updatedUserData = new HashMap<>();
                                    updatedUserData.put("Projects/" + projectID + "/" +
                                            usersUID.get(i) + "/" + formattedDate , report);

                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Active" , projectID);


                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Projects/" + projectID + "/" + "projectName" , projectName);

                                    mData.updateChildren(updatedUserData);
                                }

                                finish();
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
