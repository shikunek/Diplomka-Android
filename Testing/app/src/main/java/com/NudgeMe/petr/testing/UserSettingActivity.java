package com.NudgeMe.petr.testing;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserSettingActivity extends AppCompatActivity {

    DatabaseReference mData;
    private final int PICK_IMAGE_REQUEST = 71;
    CircleImageView profilePic;
    private Uri filePath;
    StorageReference storageReference;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_setting);

        mData = FirebaseDatabase.getInstance().getReference();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference().child("images").child(currentUser.getUid());

        profilePic = (CircleImageView) findViewById(R.id.profilePicView);

        Glide.with(UserSettingActivity.this /* context */)
                .using(new FirebaseImageLoader())
                .load(storageReference)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .error(R.drawable.animal_ant_eater)
                .into(profilePic);

//        Glide.with(UserSettingActivity.this)
//                .using(new FirebaseImageLoader())
//                .load(storageReference)
//                .asBitmap()
//                .diskCacheStrategy(DiskCacheStrategy.NONE)
//                .skipMemoryCache(true)
//                .centerCrop()
//                .into(new SimpleTarget< Bitmap >() {
//                    @Override
//                    public void onResourceReady(Bitmap resource, GlideAnimation < ? super Bitmap > glideAnimation) {
//                        profilePic.setImageBitmap(resource);
//                    }
//                });

//        storageReference.child("images").child(currentUser.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//            @Override
//            public void onSuccess(Uri uri) {
//                try {
//                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
//                    profilePic.setImageBitmap(bitmap);
//                }
//                catch (IOException e)
//                {
//                    e.printStackTrace();
//                }
//
//                // Got the download URL for 'users/me/profile.png'
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//            }
//        });

//        storageReference.child("images").child(currentUser.getUid()).getStream(
//                new StreamDownloadTask.StreamProcessor() {
//                    @Override
//                    public void doInBackground(StreamDownloadTask.TaskSnapshot taskSnapshot,
//                                               InputStream inputStream) throws IOException {
//                        long totalBytes = taskSnapshot.getTotalByteCount();
//                        long bytesDownloaded = 0;
//
//                        byte[] buffer = new byte[1024];
//                        int size;
//
////                        while ((size = inputStream.read(buffer)) != -1) {
////                            bytesDownloaded += size;
////                            showProgressNotification(getString(R.string.progress_downloading),
////                                    bytesDownloaded, totalBytes);
////                        }
//
//                        // Close the stream at the end of the Task
//                        inputStream.close();
//                    }
//                })
//                .addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
//                    @Override
//                    public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
////                        Log.d(TAG, "download:SUCCESS");
//
//                        Bitmap bitmap = BitmapFactory.decodeStream(taskSnapshot.getStream());;
//                        profilePic.setImageBitmap(bitmap);
//
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception exception) {
////                        Log.w(TAG, "download:FAILURE", exception);
//
//                    }
//                });
        Button changeProfilePic = (Button) findViewById(R.id.changeImageButton);
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        Button signOutButton = (Button) findViewById(R.id.signOutButton);
        signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                FirebaseMessaging.getInstance().unsubscribeFromTopic(currentUser.getUid());
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserSettingActivity.this, FirstActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profilePic.setImageBitmap(bitmap);
                if(filePath != null)
                {
                    final ProgressDialog progressDialog = new ProgressDialog(UserSettingActivity.this);
                    progressDialog.setTitle("Uploading...");
                    progressDialog.show();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                    byte[] byteArray = baos.toByteArray();
                    storageReference.putBytes(byteArray)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UserSettingActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    progressDialog.dismiss();
                                    Toast.makeText(UserSettingActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                            .getTotalByteCount());
                                    progressDialog.setMessage("Uploaded "+(int)progress+"%");
                                }
                            });
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
