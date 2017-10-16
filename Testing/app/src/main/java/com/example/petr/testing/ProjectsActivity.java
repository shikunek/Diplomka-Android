package com.example.petr.testing;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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


public class ProjectsActivity extends AppCompatActivity {

    RowAdapter adapter;
    ArrayList<String> arrayListJustForTriggerAdapter = new ArrayList<>();
    ArrayList<ProjectClass> projectListToShow = new ArrayList<>();
    DatabaseReference mData;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);


        adapter = new RowAdapter(this, projectListToShow, arrayListJustForTriggerAdapter);
        mData = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final ListView projectsListView = (ListView) findViewById(R.id.projects_list);
        projectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                LinearLayout currentProjectLayout = (LinearLayout) view.findViewById(R.id.lin);

                mData.child("Uzivatel").child(currentUser.getUid()).child("Active").setValue((String) currentProjectLayout.getTag());
            }
        });
        projectsListView.setAdapter(adapter);

        mData.child("Uzivatel").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot userProjects) {

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

        final Dialog createNewProjectDialog = new Dialog(ProjectsActivity.this);

        createNewProjectDialog.setContentView(R.layout.create_project_dialog);
        final ProjectClass newProject = new ProjectClass();

        createNewProjectDialog.setTitle("Custom Dialog");
        createNewProjectDialog.getWindow().setLayout(675, 750);
        TextView manualProjectName = (TextView) createNewProjectDialog.findViewById(R.id.newProjectName);
        manualProjectName.setText("SIN");

        createNewProjectDialog.show();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        Button addButton = (Button) createNewProjectDialog.findViewById(R.id.addProjectButton);
        final LinearLayout createProjectLayout = (LinearLayout) createNewProjectDialog.findViewById(R.id.usersLinLayout);
        Button addUserButton = (Button) createNewProjectDialog.findViewById(R.id.addUsersButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText manualUserEditText = new EditText(v.getContext());
                manualUserEditText.setText("g@f.cz");
                createProjectLayout.addView(manualUserEditText);
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
                                TextView projectNameTextView = (TextView) createNewProjectDialog.findViewById(R.id.newProjectName);
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

                                arrayListJustForTriggerAdapter.add("asd");
                                projectListToShow.add(newProject);
                                adapter.notifyDataSetChanged();
                                createNewProjectDialog.dismiss();

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        }
                );

            }
        });

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

