package com.NudgeMe.petr.testing;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;


public class FirstActivity extends AppCompatActivity implements
        View.OnClickListener{

    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private static DatabaseReference mData;
    private EditText mEmailField;
    private EditText mPasswordField;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        if (mData == null) {
//            FirebaseDatabase database = FirebaseDatabase.getInstance();
//            database.setPersistenceEnabled(true);
//            // ...
//        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);
        getSupportActionBar().setTitle("Welcome");


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getResources().getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference().child("images");
        mEmailField = (EditText) findViewById(R.id.loginTB);
        mPasswordField = (EditText) findViewById(R.id.passwordTB);
        mData = FirebaseDatabase.getInstance().getReference();
        mData.keepSynced(true);

        SignInButton signInGoogleButton = (SignInButton) findViewById(R.id.signInGoogleButton);
        signInGoogleButton.setOnClickListener(this);
        Button signInButton = (Button) findViewById(R.id.signInButton);
        signInButton.setOnClickListener(this);



    }

    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        currentUser = mAuth.getCurrentUser();
//
//
//        if (currentUser != null)
//        {
//            goToLogin(new View(getApplicationContext()));
//        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("FAILED", "Google sign in failed", e);
                // [START_EXCLUDE]
                String messageToDisplay = "Authentication failed.";
                switch (e.getStatusCode()) {
                    case CommonStatusCodes.API_NOT_CONNECTED: //17
                        messageToDisplay += "The client attempted to call a method from an API that failed to connect.";
                        break;

                    case CommonStatusCodes.DEVELOPER_ERROR: //10
                        messageToDisplay += "The application is misconfigured.";
                        break;

                    case CommonStatusCodes.ERROR: //13
                        messageToDisplay += "The operation failed with no more detailed information.";
                        break;

                    case CommonStatusCodes.INTERNAL_ERROR: //8
                        messageToDisplay += "An internal error occurred.";
                        break;

                    case CommonStatusCodes.INVALID_ACCOUNT: //8
                        messageToDisplay += "Invalid account name specified.";
                        break;

                    case CommonStatusCodes.SIGN_IN_REQUIRED: //8
                        messageToDisplay += "Please Sign In to continue.";
                        break;
                }
                Log.w("FAILED", messageToDisplay);

                Context context = getApplicationContext();
                CharSequence text = "Log in failed";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                toast.show();
                // [END_EXCLUDE]
            }
        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d("SUCCESS", "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("SUCCESS", "signInWithCredential:success");
                            mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot allUsersToUnsubscribe)
                                {
                                    currentUser = mAuth.getCurrentUser();
                                    for (DataSnapshot user : allUsersToUnsubscribe.getChildren())
                                    {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(user.getKey());
                                    }

                                    FirebaseMessaging.getInstance().subscribeToTopic(currentUser.getUid());

                                    // kdyz uzivatel neexistuje
                                    if (!allUsersToUnsubscribe.hasChild(currentUser.getUid()))
                                    {
                                        mData.child("Uzivatel").child(currentUser.getUid()).child("email").
                                                setValue(acct.getEmail());
                                        setUserAvatar(currentUser.getUid());
                                    }

                                    else
                                    {
                                        // uzivatel existuje, ale nema pridelenu ikonu
                                        if (!allUsersToUnsubscribe.child(currentUser.getUid()).hasChild("Icon"))
                                        {
                                            setUserAvatar(currentUser.getUid());
                                        }
                                        else
                                        {
                                            int iconID = getResources().getIdentifier(allUsersToUnsubscribe.child(currentUser.getUid()).child("Icon").getValue().toString(),
                                                    "drawable", getPackageName());
                                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconID);
                                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                            byte[] data = baos.toByteArray();
                                            storageReference.child(currentUser.getUid()).putBytes(data);
                                        }
                                    }

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            goToGraph(new View(getApplicationContext()));

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("FAILED", "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    public void signInUsers(final View view)
    {
        if(!isEmailAndPasswordCorrect())
        {
            return;
        }
        mAuth.signInWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            currentUser = mAuth.getCurrentUser();
                            mData.child("Uzivatel").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot allUsersToUnsubscribe)
                                {
                                    for (DataSnapshot user : allUsersToUnsubscribe.getChildren())
                                    {
                                        FirebaseMessaging.getInstance().unsubscribeFromTopic(user.getKey());
                                    }

                                    FirebaseMessaging.getInstance().subscribeToTopic(currentUser.getUid());


                                    if (!allUsersToUnsubscribe.child(currentUser.getUid()).hasChild("Icon"))
                                    {
                                        setUserAvatar(currentUser.getUid());
                                    }
                                    else
                                    {
                                        int iconID = getResources().getIdentifier(allUsersToUnsubscribe.child(currentUser.getUid()).child("Icon").getValue().toString(),
                                                "drawable", getPackageName());
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconID);
                                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                        byte[] data = baos.toByteArray();
                                        storageReference.child(currentUser.getUid()).putBytes(data);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            goToGraph(view);

                        } else {
                            // If sign in fails, display a message to the user.
                            Context context = getApplicationContext();
                            CharSequence text = "Your email or password is incorrect!";
                            int duration = Toast.LENGTH_SHORT;

                            Toast toast = Toast.makeText(context, text, duration);
                            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                        if (!task.isSuccessful()) {
                        }

                    }

                });
    }


    public void signUpUsers(final View view)
    {

        if(!isEmailAndPasswordCorrect())
        {
            return;
        }
        mAuth.createUserWithEmailAndPassword(mEmailField.getText().toString(), mPasswordField.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            currentUser = mAuth.getCurrentUser();
                            // Sign in success, update UI with the signed-in user's information
                            mData.child("Uzivatel").child(currentUser.getUid()).child("email").
                                    setValue(mEmailField.getText().toString());

                            setUserAvatar(currentUser.getUid());

                            FirebaseMessaging.getInstance().subscribeToTopic(currentUser.getUid());
                            goToGraph(view);
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast toast = Toast.makeText(FirstActivity.this,
                                    "Your email or password is incorrect", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
                            toast.show();
                        }

                    }
                });

    }

    public void setUserAvatar(String userName)
    {
        Field[] drawablesFields = R.drawable.class.getFields();
        ArrayList<String> animalFields = new ArrayList<>();

        for (Field field : drawablesFields) {
            try {
                if (field.getName().contains("animal"))
                {
                    animalFields.add(field.getName());
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        int indexOfImage = new Random().nextInt(50);

        int iconID = getResources().getIdentifier(animalFields.get(indexOfImage),
                "drawable", getPackageName());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), iconID);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        byte[] data = baos.toByteArray();
        storageReference.child(currentUser.getUid()).putBytes(data);

        mData.child("Uzivatel").child(userName).child("Icon").
                setValue(animalFields.get(indexOfImage));

    }

    public Boolean isEmailAndPasswordCorrect()
    {
        if (mEmailField.getText().toString().equals("") || mPasswordField.getText().toString().equals(""))
        {
            Context context = getApplicationContext();
            CharSequence text = "You have to fill your email or password!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER| Gravity.CENTER, 0, 0);
            toast.show();
            return false;
        }
        return true;
    }


    public void goToGraph(View view)
    {
        Intent intent = new Intent(this, GraphActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        int i = view.getId();
        if (i == R.id.signInGoogleButton)
        {
            signIn();
        }
        else if (i == R.id.signInButton)
        {
            signInUsers(view);
        }
    }
}
