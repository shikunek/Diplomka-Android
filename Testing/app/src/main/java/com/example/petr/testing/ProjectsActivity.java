package com.example.petr.testing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.auth.UserRecord;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import static com.example.petr.testing.R.id.parent;

public class ProjectsActivity extends AppCompatActivity {

    RowAdapter adapter;
    ArrayList<String> mobileArray = new ArrayList<String>();
//    ArrayList<String> users = new ArrayList<String>();
//    ArrayList<String> ids = new ArrayList<String>();
//    ArrayList<Integer> usersOnProjectCount = new ArrayList<Integer>();
    ArrayList<ProjectClass> projects = new ArrayList<>();
    DatabaseReference mData;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);


        adapter = new RowAdapter(this, projects, mobileArray);
        mData = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final ListView listView = (ListView) findViewById(R.id.projects_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object o = listView.getItemAtPosition(position);
                LinearLayout ln = (LinearLayout) view.findViewById(R.id.lin);
                TextView tw = (TextView) view.findViewById(R.id.newProjectName);
                mData.child("Uzivatel").child(currentUser.getUid()).child("Active").setValue((String) ln.getTag());
            }
        });
        listView.setAdapter(adapter);

        final ArrayList<String> usersUID = new ArrayList<>();
        mData.child("Uzivatel").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot projs) {

                        if (!projs.child(currentUser.getUid()).hasChild("Projects"))
                        {
                            return;
                        }
                        for (final DataSnapshot project : projs.child(currentUser.getUid()).child("Projects").getChildren())
                        {
                            final ProjectClass newProject = new ProjectClass();
                            mData.child("Projects").child(project.getKey()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot projectUsers) {

//                                            mobileArray.add(project.child("projectName").getValue().toString());
                                            newProject.setProjectName(project.child("projectName").getValue().toString());
                                            newProject.setId(project.getKey());
//                                            ids.add(project.getKey());
                                            Boolean allRight = false;
                                            for (final DataSnapshot user : projectUsers.getChildren())
                                            {
                                                if (user.getKey().equals("projectName"))
                                                {
                                                    continue;
                                                }

                                                for (DataSnapshot uzivatel1 : projs.getChildren())
                                                {
                                                    if (uzivatel1.getKey().equals(user.getKey()))
                                                    {
                                                        newProject.addProjectUsers(uzivatel1.child("email").getValue().toString());
                                                    }

                                                }
//                                                mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
//                                                    @Override
//                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                                        int usersCount = 0;
//
////                                                        usersOnProjectCount.add(usersCount);
//                                                        projects.add(newProject);
//                                                        mobileArray.add("asd");
//                                                        adapter.notifyDataSetChanged();
//                                                    }
//
//                                                    @Override
//                                                    public void onCancelled(DatabaseError databaseError) {
//
//                                                    }
//                                                });

                                            }
                                            projects.add(newProject);
                                            mobileArray.add("asd");
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
        // Create custom dialog object
        final Dialog dialog = new Dialog(ProjectsActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.create_project_dialog);
        final ProjectClass newProject = new ProjectClass();
        // Set dialog title
        dialog.setTitle("Custom Dialog");
        dialog.getWindow().setLayout(675, 750);
        TextView text = (TextView) dialog.findViewById(R.id.newProjectName);
        text.setText("SIN");

//        final TextView text1 = (TextView) dialog.findViewById(R.id.userNameToProject);
//        text1.setText("x@f.cz");
//        final TextView text2 = (TextView) dialog.findViewById(R.id.user2);
//        text2.setText("g@f.cz");
//        final String[] str = new String[]{text1.getText().toString(), text2.getText().toString()};
//

        dialog.show();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        Button addButton = (Button) dialog.findViewById(R.id.addProjectButton);
        final LinearLayout linearLayout = (LinearLayout) dialog.findViewById(R.id.usersLinLayout);
        Button addUserButton = (Button) dialog.findViewById(R.id.addUsersButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tw = new EditText(v.getContext());
                tw.setText("g@f.cz");
                linearLayout.addView(tw);
            }
        });
        Calendar c = Calendar.getInstance();
        final ArrayList<String> usersUID = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
//        usersOnProjectCount = 0;
        final String formattedDate = df.format(c.getTime());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                mData.child("Uzivatel").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final int editTextsCount = linearLayout.getChildCount();
                                for (int i = 0; i < editTextsCount; i++) {
                                    Boolean allRight = false;
                                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                                        EditText eT = (EditText) linearLayout.getChildAt(i);
                                        if (user.child("email").getValue().toString().equals(eT.getText().toString())) {
                                            usersUID.add(user.getKey());
                                            allRight = true;
                                        }
                                    }
                                    if (!allRight) {
                                        // ERROR
//                                        return;
                                    }
                                }
                                TextView text = (TextView) dialog.findViewById(R.id.newProjectName);
                                String projectName = text.getText().toString();
                                String projectKey = mData.child("Projects").push().getKey();
                                mData.child("Projects").child(projectKey).child("projectName").
                                        setValue(projectName);

                                newProject.setProjectName(projectName);
                                newProject.setId(projectKey);



                                for (int i = 0; i < usersUID.size(); i++) {
                                    Report report = new Report(0, "", 0);

                                    EditText eT = (EditText) linearLayout.getChildAt(i);
                                    newProject.addProjectUsers(eT.getText().toString());

                                    Map updatedUserData = new HashMap();
                                    updatedUserData.put("Projects/" + projectKey + "/" +
                                            usersUID.get(i) + "/" + formattedDate , report);

                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Active" , projectKey);


                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Projects/" + projectKey + "/" + "projectName" , projectName);

                                    mData.updateChildren(updatedUserData);
                                }
//                                newProject.addProjectUsers(text1.getText().toString());
//                                newProject.addProjectUsers(text2.getText().toString());

                                mobileArray.add("asd");
                                projects.add(newProject);
                                adapter.notifyDataSetChanged();
                                dialog.dismiss();

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
            public void onDataChange(DataSnapshot activeProject) {

                if (!activeProject.hasChild("Active"))
                {
                    return;
                }
                Map updatedUserData = new HashMap();
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Projects/" + activeProject.child("Active").getValue().toString() + "/"  , null);

                updatedUserData.put("Projects/" + activeProject.child("Active").getValue().toString() + "/" +
                        currentUser.getUid() + "/" , null);
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Active" , null);

                mData.updateChildren(updatedUserData);

                for (ProjectClass prj : projects)
                {
                    if (prj.getID().equals(activeProject.child("Active").getValue().toString()))
                    {
                        projects.remove(prj);
                        mobileArray.remove(mobileArray.size()-1);

                        adapter.notifyDataSetChanged();
                        break;
                    }
                }

                mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("Projects"))
                        {
                            for (DataSnapshot firstProject : dataSnapshot.child("Projects").getChildren())
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

