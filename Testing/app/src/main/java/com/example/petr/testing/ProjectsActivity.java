package com.example.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class ProjectsActivity extends AppCompatActivity {

    RowAdapter adapter;
//    ArrayList<String> arrayListJustForTriggerAdapter = new ArrayList<>();
//    ArrayList<ProjectClass> projectListToShow = new ArrayList<>();
    DatabaseReference mData;
//    ArrayList<String> listOfRegisteredUsers = new ArrayList<>();
    FirebaseUser currentUser;
    ArrayList<ProjectClass> myProjectset;

    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        getSupportActionBar().setTitle("Projects list");


        mRecyclerView = (RecyclerView) findViewById(R.id.projects_list);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Button addProjectButton = (Button) findViewById(R.id.addProject);
//        addProjectButton.setImageResource(R.drawable.checked);
//
//        addProjectButton.setBackgroundResource(R.drawable.round_button_blue);
//        Drawable drw = addProjectButton.getBackground();
//        drw.setColorFilter(Color.argb(255, 79, 195, 247), PorterDuff.Mode.LIGHTEN);

        mData = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        mData.child("Uzivatel").addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot userProjects) {

                        myProjectset = new ArrayList<>();
                        mAdapter = new RowAdapter(myProjectset);
//                        for (DataSnapshot user : userProjects.getChildren())
//                        {
//                            listOfRegisteredUsers.add(user.child("email").getValue().toString());
//                        }

                        if (!userProjects.child(currentUser.getUid()).hasChild("Projects"))
                        {
                            return;
                        }
                        for (final DataSnapshot userProject : userProjects.child(currentUser.getUid()).child("Projects").getChildren())
                        {
                            final ProjectClass newProject = new ProjectClass();
                            mData.child("Projects").child(userProject.getKey()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot usersOnProject) {

                                            newProject.setProjectName(userProject.child("projectName").getValue().toString());
                                            newProject.setId(userProject.getKey());

                                            for (final DataSnapshot userOnCurrentProject : usersOnProject.getChildren())
                                            {
                                                if (userOnCurrentProject.getKey().equals("projectName"))
                                                {
                                                    continue;
                                                }

                                                for (DataSnapshot actualUserProject : userProjects.getChildren())
                                                {
                                                    if (actualUserProject.getKey().equals(userOnCurrentProject.getKey()))
                                                    {
                                                        newProject.addProjectUsers(actualUserProject.child("email").getValue().toString());
                                                    }

                                                }
                                            }
                                            myProjectset.add(newProject);
//                                            arrayListJustForTriggerAdapter.add("asd");
//                                            adapter.notifyDataSetChanged();
                                            mRecyclerView.setAdapter(mAdapter);
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
                }
        );

    }


    public void addProject(final View view) {

        Intent intent = new Intent(ProjectsActivity.this, AddProjectActivity.class);
        startActivity(intent);
    }

}

