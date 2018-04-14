package com.NudgeMe.petr.testing;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
    ArrayList<String> userEmailTextset;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    int userId = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project_info);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        userEmailTextset = new ArrayList<>();

        mRecyclerView = (RecyclerView) findViewById(R.id.projectUsersRecyclerView);
        mAdapter = new UsersOnProjectAdapter(userEmailTextset);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

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

        final Button applyChangesButton = (Button) findViewById(R.id.toolbarButton);
        applyChangesButton.setVisibility(View.VISIBLE);
        applyChangesButton.setText("Edit");
        applyChangesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("projectName"))
                {
                    saveChanges(intent.getExtras().getString("projectName"));
                }

            }
        });
        final Button deleteProjectButton = (Button) findViewById(R.id.deleteProjectButton);
        deleteProjectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (intent.hasExtra("projectName"))
                {
                    deleteProject(intent.getExtras().getString("projectName"));
                }

            }
        });



        mData.child("Projects").child(intent.getExtras().getString("projectName")).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot choosenProject) {

                        if (intent.hasExtra("actualProjectName"))
                        {
                            projectName.setText(intent.getExtras().getString("actualProjectName"));
                        }
                        else
                        {
                            projectName.setText(choosenProject.child("projectName").getValue().toString());
                        }

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
                                String myFormat = "dd.MM.yyyy";
                                String myFormat1 = "yyyy-MM-dd";
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.GERMANY);
                                SimpleDateFormat sdf1 = new SimpleDateFormat(myFormat1, Locale.GERMANY);
                                try {
                                    Date date = sdf1.parse(firstDate.next().getKey());
                                    startDate.setText(sdf.format(date));
                                }
                                catch (ParseException e)
                                {
                                    startDate.setText("");
                                    return;
                                }

                                break;
                            }

                            break;
                        }


                        if(choosenProject.hasChild("Ending"))
                        {
                            endDateEditText.setText(choosenProject.child("Ending").getValue().toString());
                        }


                        if (intent.hasExtra("InvitedUsers"))
                        {
                            ArrayList<String> invitedUsers = intent.getStringArrayListExtra("InvitedUsers");
                            mRecyclerView.removeAllViews();
                            userEmailTextset.clear();
                            for (int i = 0; i < invitedUsers.size() ; i++ )
                            {
                                userEmailTextset.add(invitedUsers.get(i));

                            }
                            mRecyclerView.setAdapter(mAdapter);


                        }

                        else
                        {
                            mRecyclerView.removeAllViews();
                            userEmailTextset.clear();
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
                                        userEmailTextset.add(userEmail.getValue().toString());
                                        mRecyclerView.setAdapter(mAdapter);
                                    }

                                    @Override
                                    public void onCancelled(DatabaseError databaseError) {

                                    }
                                });
                            }
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

                Intent intent = new Intent(ProjectInfoActivity.this, AddUsersActivity.class);
                intent.putExtra("projectName", getIntent().getStringExtra("projectName"));
                intent.putExtra("actualProjectName", projectName.getText().toString());
                intent.putExtra("usersOnProject", userEmailTextset);
                startActivity(intent);

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

                for (final DataSnapshot currentProjectUser : currentProject.getChildren())
                {
                    if (currentProjectUser.getKey().equals("projectName") || currentProjectUser.getKey().equals("Ending"))
                    {
                        continue;
                    }

                    updatedUserData.put("Uzivatel/" + currentProjectUser.getKey() + "/" +
                            "Projects" + "/" + projectID + "/" + "projectName", projectName
                            .getText().toString());

                }

//                final LinearLayout usersToSave = (LinearLayout) findViewById(R.id.usersLayout);

                    mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot users) {
                            for (final DataSnapshot user : users.getChildren())
                            {
                                for (int i = 0; i < userEmailTextset.size(); i++)
                                {
                                    if (user.child("email").getValue().toString().equals(userEmailTextset.get(i)) &&
                                            !currentProject.hasChild(user.getKey()))
                                    {
                                        Calendar myCalendar = Calendar.getInstance();
                                        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMANY);
                                        final String today = dateFormat.format(myCalendar.getTime());
                                        Date startDate = myCalendar.getTime();
                                        Iterator<DataSnapshot> firstUser =  currentProject.getChildren().iterator();

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
                                                try
                                                {
                                                    startDate = dateFormat.parse(firstDate.next().getKey());
                                                }

                                                catch (ParseException e)
                                                {
                                                }

                                                break;
                                            }

                                            break;
                                        }

                                        Calendar calendar = Calendar.getInstance();
                                        calendar.setTime(startDate);
                                        while(calendar.getTime().before(myCalendar.getTime()))
                                        {
                                            String newDay = dateFormat.format(calendar.getTime());
                                            Report defaultReport = new Report(0, "", -2);
                                            updatedUserData.put("Projects/" + projectID + "/" + user.getKey() +
                                                    "/" + newDay, defaultReport);
                                            calendar.add(Calendar.DATE, 1);
                                        }

//                                        Report report = new Report(0, "", 0);
//
//                                        updatedUserData.put("Projects/" + projectID + "/" + user.getKey() +
//                                                "/" + today, report);
                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                "Projects" + "/" + projectID + "/" + "projectName", projectName
                                                .getText().toString());
                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                "Active", projectID);

                                    }


                                }

                                if (getIntent().hasExtra("UsersToDelete"))
                                {
                                    Iterator<String> it = getIntent().getStringArrayListExtra("UsersToDelete").iterator();
                                    while(it.hasNext())
                                    {
                                        if (user.child("email").getValue().toString().equals(it.next()))
                                        {
                                            updatedUserData.put("Projects/" + projectID + "/" + user.getKey(), null);
                                            updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                    "Projects/" + projectID + "/" + "projectName" , null);
                                            updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                    "Active", null);
                                            // Pokud se uzivatel rozhodl smazat aktualne vybrany projekt
                                            if (user.child("Active").getValue().toString().equals(projectID) && user.hasChild("Projects"))
                                            {
                                                for (DataSnapshot firstProject : user.child("Projects").getChildren())
                                                {
                                                    if (!firstProject.getKey().equals(projectID))
                                                    {
                                                        updatedUserData.put("Uzivatel/" + user.getKey() + "/" +
                                                                "Active" , firstProject.getKey());
                                                        break;
                                                    }

                                                }

                                            }
                                        }
                                    }
                                }

                            }
                            mData.updateChildren(updatedUserData);
                            Intent intent = new Intent(ProjectInfoActivity.this, ProjectsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            finish();
                            startActivity(intent);
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

    public void deleteProject(final String projectID){

        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        mData.child("Uzivatel").child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot currentUsersData) {

                if (!currentUsersData.hasChild("Active"))
                {
                    return;
                }
                final Map<String, Object> updatedUserData = new HashMap<>();
                updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                        "Projects/" + projectID + "/"  , null);

                updatedUserData.put("Projects/" + projectID + "/" +
                        currentUser.getUid() + "/" , null);


                if (projectID.equals(currentUsersData.child("Active").getValue().toString()))
                {
                    updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/Active" , null);
                }


                // pokud je na projektu jediny uzivatel tak je smazan cely
                mData.child("Projects").child(projectID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot remainingUsers) {
                        Iterator<DataSnapshot> project =  remainingUsers.getChildren().iterator();
                        int i = 0;
                        while (project.hasNext())
                        {
                            DataSnapshot firstChild = project.next();
                            if ( firstChild.getKey().equals("projectName") || firstChild.getKey().equals("Ending"))
                            {
                                continue;
                            }
                            i++;
                        }
                        // <= je kvuli tomu, ze pokud existuje jeden uzivatel, tak je i == 1
                        if (i <= 1 && remainingUsers.hasChild(currentUser.getUid()))
                        {
                            updatedUserData.put("Projects/" + projectID + "/" + "projectName" , null);
                        }


                        // Pokud se uzivatel rozhodl smazat aktualne vybrany projekt
                        if (projectID.equals(currentUsersData.child("Active").getValue().toString()) && currentUsersData.hasChild("Projects"))
                        {
                            for (DataSnapshot firstProject : currentUsersData.child("Projects").getChildren())
                            {
                                if (!firstProject.getKey().equals(projectID))
                                {
                                    updatedUserData.put("Uzivatel/" + currentUser.getUid() + "/" +
                                            "Active" , firstProject.getKey());
                                    break;
                                }

                            }

                        }

                        mData.updateChildren(updatedUserData);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });



                finish(); // aby uzivatel po stisknuti Back, nesel na jiz neexistujici projekt

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
