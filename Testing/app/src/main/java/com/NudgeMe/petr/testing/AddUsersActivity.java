package com.NudgeMe.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.MultiAutoCompleteTextView;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);

        multiAutoCompleteText = (MultiAutoCompleteTextView)findViewById(R.id.multiUsersView);
        Button toolbarButton = (Button) findViewById(R.id.toolbarButton);
        toolbarButton.setVisibility(View.VISIBLE);
        toolbarButton.setText("DONE");
        toolbarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usersWithoutWhitespace = multiAutoCompleteText.getText().toString().replaceAll("\\s+","");
                ArrayList<String> invitedUsers = new ArrayList<>();
                invitedUsers.addAll(Arrays.asList(usersWithoutWhitespace.split(",")));
                Intent intent;
                if (getIntent().hasExtra("activity"))
                {
                    intent = new Intent(AddUsersActivity.this, AddProjectActivity.class);
                }
                else
                {
                    intent = new Intent(AddUsersActivity.this, ProjectInfoActivity.class);
                    intent.putExtra("projectName", getIntent().getStringExtra("projectName"));
                    ArrayList<String> previousUsersOnProject =  getIntent().getStringArrayListExtra("usersOnProject");
                    ArrayList<String> usersToDelete = new ArrayList<>();

                    for (String previousUser : previousUsersOnProject)
                    {
                        Boolean exists = false;

                        for (String actualUser : invitedUsers)
                        {
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

                /* odstraneni historie, ze uz jsme byli na ProjectInfo a po prijiti z AddUsers a danim
                a stiskem tlacitka zpet znovu nesli na ProjectInfo */
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();

                /* musime vytvorit novy intent, pac potrebujeme dostat do ProjectInfo informace o
                smazanych a pridanych uzivatelich */
                startActivity(intent);

            }
        });

        mData = FirebaseDatabase.getInstance().getReference();

        mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot userProjects) {
                ArrayList<String> listOfRegisteredUsers = new ArrayList<String>();
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
    }
}
