package com.NudgeMe.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

public class AddUsersActivity extends AppCompatActivity {

    DatabaseReference mData;
    MultiAutoCompleteTextView multiAutoCompleteText;
    ArrayList<String> listOfRegisteredUsers;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);
        listOfRegisteredUsers = new ArrayList<String>();
        mData = FirebaseDatabase.getInstance().getReference();

        // Adding whole users from database to helping dialog
        mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userProjects) {

                for (DataSnapshot user : userProjects.getChildren())
                {
                    listOfRegisteredUsers.add(user.child("email").getValue().toString());
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(AddUsersActivity.this,
                        android.R.layout.simple_dropdown_item_1line, listOfRegisteredUsers);

                multiAutoCompleteText.setAdapter(adapter);
                multiAutoCompleteText.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
                multiAutoCompleteText.setThreshold(1);
                ArrayList<String> usersOnProject =  getIntent().getStringArrayListExtra("usersOnProject");
                StringBuilder stringBuilder = new StringBuilder();
                for (String user : usersOnProject)
                {
                    stringBuilder.append(user).append(",").append(" ");
                }

                multiAutoCompleteText.setText(stringBuilder);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        multiAutoCompleteText = (MultiAutoCompleteTextView)findViewById(R.id.multiUsersView);
        Button toolbarButton = (Button) findViewById(R.id.toolbarButton);
        toolbarButton.setVisibility(View.VISIBLE);
        toolbarButton.setText("DONE");

        // Submit added users
        toolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usersWithoutWhitespace = multiAutoCompleteText.getText().toString().replaceAll("\\s+","");
                ArrayList<String> invitedUsers = new ArrayList<>();
                invitedUsers.addAll(Arrays.asList(usersWithoutWhitespace.split(",")));
                Intent intent;
                if (getIntent().hasExtra("activity"))
                {
                    for (String actualUser : invitedUsers)
                    {
                        if (!listOfRegisteredUsers.contains(actualUser))
                        {
                            Context context = getApplicationContext();
                            CharSequence text = "User: " + '"' + actualUser + '"' + " doesn't exist.";
                            int duration = Toast.LENGTH_LONG;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                            toast.show();
                            return;
                        }

                    }
                    intent = new Intent(AddUsersActivity.this, AddProjectActivity.class);
                    if (getIntent().hasExtra("actualProjectName"))
                    {
                        intent.putExtra("actualProjectName", getIntent().getStringExtra("actualProjectName"));
                    }

                }
                else
                {
                    intent = new Intent(AddUsersActivity.this, ProjectInfoActivity.class);
                    intent.putExtra("projectName", getIntent().getStringExtra("projectName"));
                    if (getIntent().hasExtra("actualProjectName"))
                    {
                        intent.putExtra("actualProjectName", getIntent().getStringExtra("actualProjectName"));
                    }
                    ArrayList<String> previousUsersOnProject =  getIntent().getStringArrayListExtra("usersOnProject");
                    ArrayList<String> usersToDelete = new ArrayList<>();

                    for (String previousUser : previousUsersOnProject)
                    {
                        Boolean exists = false;

                        for (String actualUser : invitedUsers)
                        {
                            if (!listOfRegisteredUsers.contains(actualUser))
                            {
                                Context context = getApplicationContext();
                                CharSequence text = "User: " + '"' + actualUser + '"' + " doesn't exist.";
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                                toast.show();
                                return;
                            }

                            if (previousUser.equals(actualUser))
                            {
                                exists = true;
                            }
                        }

                        if (!exists)
                        {
                            usersToDelete.add(previousUser);
                        }
                    }

                    intent.putExtra("UsersToDelete",usersToDelete);
                }

                intent.putExtra("InvitedUsers",invitedUsers);

                /* delete history for not going back to AddUsers */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();

                /* new intent for sending information about added, deleted users */
                startActivity(intent);

            }
        });

    }
}
