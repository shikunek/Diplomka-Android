package com.example.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

//import com.google.firebase.auth.FirebaseCredentials;

public class FirstActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mData;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        getSupportActionBar().setTitle("Welcome");

        mAuth = FirebaseAuth.getInstance();
        mEmailField = (EditText) findViewById(R.id.loginTB);
        mPasswordField = (EditText) findViewById(R.id.passwordTB);
        mEmailField.setText("g@f.cz", TextView.BufferType.EDITABLE);
        mPasswordField.setText("set123", TextView.BufferType.EDITABLE);
        mData = FirebaseDatabase.getInstance().getReference();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();


        if (currentUser != null)
        {
            Log.d("myTag", "ANO");
        }
        else
        {
            Log.d("myTag", "Ne");

        }
//        updateUI(currentUser);
    }

    public void singInUsers(final View view)
    {
        mAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("NECO", "signInWithEmail:success");

                            mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot allUsersToUnsubscribe) {
                                    for (DataSnapshot user : allUsersToUnsubscribe.getChildren())
                                    {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(user.getKey());
                                    }
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                    FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            goToGraph(view);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("NECO", "signInWithEmail:failure", task.getException());
                            Context context = getApplicationContext();
                            CharSequence text = "Your email or password is incorrect!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                        if (!task.isSuccessful()) {
                            Log.d("NECO", "signInWithEmail:failure", task.getException());
                        }

                    }

                });
    }


    public void signUpUsers(final View view)
    {
//        Intent intent = new Intent(this, GraphActivity.class);
//        startActivity(intent);

        mAuth.createUserWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("myID", "createUserWithEmail:success");
                            final FirebaseUser user = mAuth.getCurrentUser();
                            mData.child("Uzivatel").child(user.getUid()).child("email").
                                    setValue(mEmailField.getText().toString());

                            FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());
                            goToGraph(view);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("myID", "createUserWithEmail:failure", task.getException());
                            Toast toast = Toast.makeText(FirstActivity.this,
                                    "Your email or password is incorrect", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    }
                });

    }

    public void sendMessage(View view)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }
    public void goToReport(View view)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        startActivity(intent);
    }

    public void goToGraph(View view)
    {
        Intent intent = new Intent(this, GraphActivity.class);
        startActivity(intent);
    }

    public void goToProjects(View view)
    {
        Intent intent = new Intent(this, ProjectsActivity.class);
        startActivity(intent);
    }
}
