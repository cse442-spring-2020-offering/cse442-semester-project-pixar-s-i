package com.example.emotionalsupportapp.Member.Registration;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.emotionalsupportapp.Member.Registration.LoginActivity;
import com.example.emotionalsupportapp.R;

import org.json.JSONException;
import org.json.JSONObject;

public class SignUpActivity extends AppCompatActivity {

    EditText emailText, passwordText, passwordConText, fNameText, lNameText;
    Button nowCreated;
    Switch genderSwitch, choiceSwitch;
    String result;
    RequestQueue requestQueue;
    String signUpURL = "https://www-student.cse.buffalo.edu/CSE442-542/2020-spring/cse-442e/signup.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        emailText = (EditText) findViewById(R.id.emailText);
        passwordText = (EditText) findViewById(R.id.passwordText);
        passwordConText = (EditText) findViewById(R.id.passConText);
        fNameText = (EditText) findViewById(R.id.fNameText);
        lNameText = (EditText) findViewById(R.id.lNameText);
        result = "";

        genderSwitch = findViewById(R.id.genderSwitch);
        choiceSwitch = findViewById(R.id.ChoiceSwitch);

        requestQueue = Volley.newRequestQueue(getApplicationContext());

        nowCreated = (Button) findViewById(R.id.signInButton);
        nowCreated.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMainer();
            }
        });

    }

    public void displayToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    public void openMainer(){
        final Intent intent = new Intent(this, LoginActivity.class);
        String signUpSendURL = signUpURL + "?eMail=" + emailText.getText().toString() + "&FirstName=" +
                fNameText.getText().toString() + "&LastName=" + lNameText.getText().toString() +
                "&password=" + passwordText.getText().toString() + "&male_female=" + genderSwitch.isChecked() + "&sameSex=" + choiceSwitch.isChecked();

        String pass1 = passwordText.getText().toString();
        String pass2 = passwordConText.getText().toString();

        if(pass1.equals(pass2)){

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET,
                    signUpSendURL, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        String responder = response.getString("response");
                        result = responder;

                        if (result.equals("ok")){
                            startActivity(intent);
                        }
                        else{
                            if(result.equals("exists")){
                                displayToast("User Exists");
                            }
                            else{
                                displayToast("Something Went Wrong :(");
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            requestQueue.add(jsonObjectRequest);
        }
        else{
            displayToast("Passwords do not match...");
        }
    }
}
