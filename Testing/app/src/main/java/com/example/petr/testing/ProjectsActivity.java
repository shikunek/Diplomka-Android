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

public class ProjectsActivity extends AppCompatActivity {

    RowAdapter adapter;
    ArrayList<String> mobileArray = new ArrayList<String>();
    ArrayList<String> users = new ArrayList<String>();
    ArrayList<String> ids = new ArrayList<String>();
    DatabaseReference mData;
    FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        adapter = new RowAdapter(this, mobileArray, users, ids);
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
        mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot projects) {

                        if (!projects.hasChild("Projects"))
                        {
                            return;
                        }
                        for (DataSnapshot project : projects.child("Projects").getChildren())
                        {
                            mobileArray.add(project.child("projectName").getValue().toString());
                            ids.add(project.getKey());
                            mData.child("Projects").child(project.getKey()).addListenerForSingleValueEvent(
                                    new ValueEventListener() {
                                        @Override
                                        public void onDataChange(DataSnapshot projectUsers) {

                                            Boolean allRight = false;
                                            for (final DataSnapshot user : projectUsers.getChildren()) {
                                                if (user.getKey().equals("projectName")) {
                                                    continue;
                                                }
                                                mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
                                                        for (DataSnapshot uzivatel1 : dataSnapshot.getChildren()) {
                                                                if (uzivatel1.getKey().equals(user.getKey())) {
                                                                    users.add(uzivatel1.child("email").getValue().toString());
                                                                }

                                                        }
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
                                    });



                        }


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                }
        );

    }


    public void addProject(View view) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(ProjectsActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.create_project_dialog);
        // Set dialog title
        dialog.setTitle("Custom Dialog");
        dialog.getWindow().setLayout(575, 550);
        TextView text = (TextView) dialog.findViewById(R.id.newProjectName);
        text.setText("TAM");
        final TextView text1 = (TextView) dialog.findViewById(R.id.userNameToProject);
        text1.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());

        final TextView text2 = (TextView) dialog.findViewById(R.id.user2);
        text2.setText("x@f.cz");
        final String[] str = new String[]{text1.getText().toString(), text2.getText().toString()};
        dialog.show();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        Button addButton = (Button) dialog.findViewById(R.id.addProjectButton);

        Calendar c = Calendar.getInstance();
        final ArrayList<String> usersUID = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String formattedDate = df.format(c.getTime());

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Close dialog
                mData.child("Uzivatel").addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (int i = 0; i < 2; i++) {
                                    Boolean allRight = false;
                                    for (DataSnapshot user : dataSnapshot.getChildren()) {
                                        if (user.child("email").getValue().toString().equals(str[i])) {
                                            usersUID.add(user.getKey());
                                            allRight = true;
                                        }
                                    }
                                    if (!allRight) {
                                        // ERROR
                                    }
                                }
                                TextView text = (TextView) dialog.findViewById(R.id.newProjectName);
                                String projectName = text.getText().toString();
                                String projectKey = mData.child("Projects").push().getKey();
                                mData.child("Projects").child(projectKey).child("projectName").
                                        setValue(projectName);


                                for (int i = 0; i < 2; i++) {
                                    Report report = new Report(0, "", 0);


                                    Map updatedUserData = new HashMap();
                                    updatedUserData.put("Projects/" + projectKey + "/" +
                                            usersUID.get(i) + "/" + formattedDate , report);

                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Active" , projectKey);

                                    updatedUserData.put("Uzivatel/" + usersUID.get(i) + "/" +
                                            "Projects/" + projectKey + "/" + "projectName" , projectName);

                                    mData.updateChildren(updatedUserData);
                                }


                                mobileArray.add(projectName);
                                users.add(text1.getText().toString());
                                users.add(text2.getText().toString());
                                ids.add(projectKey);


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

    public void deleteProject(View view){
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
                setContentView(R.layout.activity_projects);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

