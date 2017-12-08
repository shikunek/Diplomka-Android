package com.example.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class ProjectsActivity extends AppCompatActivity {

    RowAdapter adapter;
    ArrayList<String> arrayListJustForTriggerAdapter = new ArrayList<>();
    ArrayList<ProjectClass> projectListToShow = new ArrayList<>();
    DatabaseReference mData;
    ArrayList<String> listOfRegisteredUsers = new ArrayList<>();
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);
        getSupportActionBar().setTitle("Projects list");


        adapter = new RowAdapter(this, projectListToShow, arrayListJustForTriggerAdapter);
        mData = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final ListView projectsListView = (ListView) findViewById(R.id.projects_list);
        projectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                LinearLayout currentProjectLayout = (LinearLayout) view.findViewById(R.id.lin);
                Intent intent = new Intent(ProjectsActivity.this, ProjectInfoActivity.class);
                intent.putExtra("projectName",(String) currentProjectLayout.getTag() );
                startActivity(intent);

            }
        });
        projectsListView.setAdapter(adapter);

        mData.child("Uzivatel").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot userProjects) {

                        for (DataSnapshot user : userProjects.getChildren())
                        {
                            listOfRegisteredUsers.add(user.child("email").getValue().toString());
                        }

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
                                            projectListToShow.add(newProject);
                                            arrayListJustForTriggerAdapter.add("asd");
                                            adapter.notifyDataSetChanged();

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

    public void deleteProject(final View view){
        mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot currentUsersData) {

                if (!currentUsersData.hasChild("Active"))
                {
                    return;
                }
                Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Projects/" + currentUsersData.child("Active").getValue().toString() + "/"  , null);

                updatedUserData.put("Projects/" + currentUsersData.child("Active").getValue().toString() + "/" +
                        currentUser.getUid() + "/" , null);
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Active" , null);

                mData.updateChildren(updatedUserData);

                for (ProjectClass project : projectListToShow)
                {
                    if (project.getID().equals(currentUsersData.child("Active").getValue().toString()))
                    {
                        projectListToShow.remove(project);
                        arrayListJustForTriggerAdapter.remove(arrayListJustForTriggerAdapter.size()-1);

                        adapter.notifyDataSetChanged();
                        break;
                    }
                }

                mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot actualUsersData) {
                        if (actualUsersData.hasChild("Projects"))
                        {
                            for (DataSnapshot firstProject : actualUsersData.child("Projects").getChildren())
                            {
                                mData.child("Uzivatel").child(currentUser.getUid()).child("Active").setValue(firstProject.getKey());
                                break;
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
}

