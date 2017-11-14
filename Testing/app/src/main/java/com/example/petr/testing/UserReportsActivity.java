package com.example.petr.testing;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class UserReportsActivity extends AppCompatActivity {

    DatabaseReference mData;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_reports);

        mData = FirebaseDatabase.getInstance().getReference();
        Intent intent = getIntent();
        final ArrayList<String> myDataset = new ArrayList<>();
        final ArrayList<String> myImageSet = new ArrayList<>();
        mData.child("Uzivatel").child(intent.getExtras().getString("userName"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                getSupportActionBar().setTitle(dataSnapshot.child("email").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRecyclerView = (RecyclerView) findViewById(R.id.reportsList);
        mAdapter = new ReportAdapter(myDataset, myImageSet);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mData.child("Projects").child(intent.getExtras().getString("projectName"))
                .child(intent.getExtras().getString("userName"))
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot user) {
                for (DataSnapshot report : user.getChildren())
                {
                    myDataset.add(report.child("reportedText").getValue().toString());
                    myImageSet.add(report.child("sendValue").getValue().toString());
                }
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
