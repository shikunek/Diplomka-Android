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

import static com.example.petr.testing.R.id.parent;

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
        mData.child("Uzivatel").child(currentUser.getUid()).child("Projects").addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot projects) {
                        for (DataSnapshot project : projects.getChildren())
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


    public void addUser(View view)
    {
        setContentView(R.layout.create_project_dialog);
        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.usersLinLayout);
        TextView tw = new TextView(this);
        tw.setText("ADAM");
        linearLayout.addView(tw);
    }

    public void addProject(View view) {
        // Create custom dialog object
        final Dialog dialog = new Dialog(ProjectsActivity.this);
        // Include dialog.xml file
        dialog.setContentView(R.layout.create_project_dialog);
        // Set dialog title
        dialog.setTitle("Custom Dialog");
//        dialog.getWindow().setLayout(575, 550);
        TextView text = (TextView) dialog.findViewById(R.id.newProjectName);
        text.setText("TAM");
        final TextView text1 = (TextView) dialog.findViewById(R.id.userNameToProject);
        text1.setText(FirebaseAuth.getInstance().getCurrentUser().getEmail().toString());

        final TextView text2 = (TextView) dialog.findViewById(R.id.user2);
        text2.setText("zbrandejs@gmail.com");
        final String[] str = new String[]{text1.getText().toString(), text2.getText().toString()};
        dialog.show();
        final DatabaseReference mData = FirebaseDatabase.getInstance().getReference();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Button addButton = (Button) dialog.findViewById(R.id.addProjectButton);

        Button addUserButton = (Button) dialog.findViewById(R.id.addUsersButton);
        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LayoutInflater inflater = (LayoutInflater) ProjectsActivity.this
//                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                View rowView = inflater.inflate(R.layout.row_item, v, false);
//                LinearLayout linearLayout = (LinearLayout) rowView.findViewById(R.id.lin);
            }
        });
        Calendar c = Calendar.getInstance();
        final ArrayList<String> usersUID = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        final String formattedDate = df.format(c.getTime());
        // if decline button is clicked, close the custom dialog
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
                                    if (allRight == false) {
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

                                    mData.child("Projects").child(projectKey).child(usersUID.get(i)).
                                            child(formattedDate).setValue(report);

                                    mData.child("Uzivatel").child(usersUID.get(i)).
                                            child("Projects").child(projectKey).child("projectName").
                                            setValue(projectName);
                                    mData.child("Uzivatel").child(usersUID.get(i)).child("Active").
                                            setValue(projectKey);


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
        mData.child("Projects").child("Active").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot activeProject) {
//                mData.child("Projects").child("Active").setValue("");
//                mData.child("Projects").child(activeProject.getKey()).child(currentUser.getUid()).removeValue();
//                mData.child("Uzivatel").child(currentUser.getUid()).child("Projects").
//                        child(activeProject.getKey()).removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

