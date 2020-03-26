package com.example.emotionalsupportapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HugActivity extends AppCompatActivity {
    int userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hug);

        String sessionId = getIntent().getStringExtra("EXTRA_USER_ID");
        userID = 1;
    }

    public void returnToMain(View view){
        Intent returnToMainIntent = new Intent(this, MainActivity.class);
        returnToMainIntent.putExtra("EXTRA_USER_ID", userID);
        startActivity(returnToMainIntent);
    }

    public void beginHughVolunteerSearch(View view){
        Intent searchHugVolunteer = new Intent(this, HugRequestActivity.class);
        searchHugVolunteer.putExtra("EXTRA_USER_ID", userID);
        startActivity(searchHugVolunteer);
    }
}