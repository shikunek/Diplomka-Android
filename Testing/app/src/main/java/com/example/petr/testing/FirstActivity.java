package com.example.petr.testing;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseCredentials;
import com.google.firebase.auth.FirebaseUser;

import java.io.FileInputStream;

public class FirstActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = (EditText) findViewById(R.id.loginTB);
        mPasswordField = (EditText) findViewById(R.id.passwordTB);
        mEmailField.setText("x@f.cz", TextView.BufferType.EDITABLE);
        mPasswordField.setText("set123", TextView.BufferType.EDITABLE);
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
        FirebaseUser user;

        mAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("NECO", "signInWithEmail:success");
                            goToReport(view);
                            //goToGraph(view);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("NECO", "signInWithEmail:failure", task.getException());
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
        FirebaseUser user;
        mAuth.createUserWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("myID", "createUserWithEmail:success");

                            goToReport(view);
//                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.d("myID", "createUserWithEmail:failure", task.getException());
                            Toast.makeText(FirstActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
//                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });

        // [END create_user_with_email]
    }

    public void sendMessage(View view)
    {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
//        EditText editText = (EditText) findViewById(R.id.editText2);
//        String message = editText.getText().toString();
//        intent.putExtra(EXTRA_MESSAGE, message);
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
