package com.example.emotionalsupportapp.Member.Registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Credentials;
import android.net.wifi.hotspot2.pps.Credential;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.emotionalsupportapp.MainActivity;
import com.example.emotionalsupportapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    EditText eMail, password;

    String result = "failed";
    String signUpURL = "https://www-student.cse.buffalo.edu/CSE442-542/2020-spring/cse-442e/login.php";
    SharedPreferences sp;
    RequestQueue requestQueue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        sp = getSharedPreferences("Login",MODE_PRIVATE);
        if(sp.getBoolean("Login",false)){
            Intent intent = new Intent(this, MainActivity.class);
            String userID =  sp.getString("UserID","");
            Log.e("userID",userID);
            intent.putExtra("EXTRA_USER_ID",userID);
            startActivity(intent);
        }

    }

    public void openSignUp(View view) {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
    }

    public void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    public void openMain(View view){
        eMail = (EditText) findViewById(R.id.E_MailTB);
        password = (EditText) findViewById(R.id.passwordTB);
        String sendLogIn = signUpURL + "?eMail=" + eMail.getText().toString() + "&password=" + password.getText().toString();
        final Intent intent = new Intent(this, MainActivity.class);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, sendLogIn, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Login Error",response + "");

                try {
                    String returner = response.getString("response");
                    result = returner;

                    if(!result.equals("failed")) {
                        SharedPreferences.Editor ed = sp.edit();
                        ed.putString("userID",result);
                        ed.putBoolean("Login",true);
                        ed.commit();
                        intent.putExtra("EXTRA_USER_ID", result);
                        startActivity(intent);
                    }
                    else{
                        displayToast("Password or E-Mail is Wrong...");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Login Error",error + "");
            }
        });

        requestQueue.add(jsonObjectRequest);

        }

}
