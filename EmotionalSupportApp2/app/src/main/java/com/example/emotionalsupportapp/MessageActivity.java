package com.example.emotionalsupportapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class MessageActivity extends AppCompatActivity {

    TextView username;

    ImageButton btn_send;
    EditText text_send;

    MessageAdpater messageAdpater;
    ArrayList<Chat> chats;

    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        username = findViewById(R.id.username);
        Bundle data =getIntent().getExtras();
        final Friend friend = data.getParcelable("CURRENT_FRIEND");
        username.setText(friend.getFriendName());

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.text_send);

        chats = new ArrayList<>();

        readMessages(friend.getUserId(),friend.getFriendId());

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = text_send.getText().toString();
                if (!msg.equals("")){
                    sendMessage(friend.getUserId(),friend.getUserName(),friend.getFriendId(),friend.getFriendName(), msg);
                }
                text_send.setText("");
            }
        });

    }

    private void sendMessage(final String senderId, final String senderName, final String receiverId, final String receiverName, String message){
        String phpURLBase =
                "https://www-student.cse.buffalo.edu/CSE442-542/2020-spring/cse-442e/saveMessage.php/"
                        + "?sender_id=" + senderId +
                        "&sender_name=" + senderName +
                        "&receiver_id=" + receiverId+
                        "&receiver_name=" + receiverName +
                        "&message=" + message;
        RequestQueue reqQueue;
        reqQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, phpURLBase, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                readMessages(senderId, receiverId);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR:", "Error on Volley: " + error.toString());
            }
        });
        reqQueue.add(jsonObjectRequest);
    }

    private void readMessages(final String userId, final  String friendId){
        chats.clear();
        String phpURLBase = "https://www-student.cse.buffalo.edu/CSE442-542/2020-spring/cse-442e/getMessages.php/?user_id=" + userId;
        RequestQueue reqQueue;
        reqQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, phpURLBase, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray re = response.getJSONArray("response");
                    for (int i = 0; i < re.length(); i++) {
                        JSONObject jsonobject = re.getJSONObject(i);
                        String senderId = jsonobject.getString("senderId");
                        String senderName = jsonobject.getString("senderName");
                        String receiverId = jsonobject.getString("receiverId");
                        String receiverName = jsonobject.getString("receiverName");
                        String message = jsonobject.getString("message");
                        if (friendId.equals(senderId) || friendId.equals(receiverId)){
                            chats.add(new Chat(senderId,senderName,receiverId,receiverName,message));
                        }
                    }
                     messageAdpater = new MessageAdpater(userId, chats, MessageActivity.this);
                     recyclerView.setAdapter(messageAdpater);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("ERROR:", "Error on Volley: " + error.toString());
            }
        });
        reqQueue.add(jsonObjectRequest);
    }
}
