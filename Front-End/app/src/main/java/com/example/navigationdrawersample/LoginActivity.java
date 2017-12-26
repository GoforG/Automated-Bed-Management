package com.example.navigationdrawersample;

/**
 * Created by Tanish on 13-02-2017.
 */



import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;
import com.google.firebase.iid.FirebaseInstanceId;
/**
 *
 * Login Activity Class
 *
 */
public class LoginActivity extends AppCompatActivity {
    // Progress Dialog Object
    public final static String EXTRA_MESSAGE = "nurseIntent";
    final Context context = this;
    String email;
    String password;
    ProgressDialog prgDialog;
    // Error Msg TextView Object
    EditText emailET;
    // Passwprd Edit View Object
    EditText pwdET;
    JSONObject params;
    String token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mylogin);
        // Find Error Msg Text View control by ID
        // Find Email Edit View control by ID
        emailET = (EditText) findViewById(R.id.loginId);
        emailET.setText("r1");
        // Find Password Edit View control by ID
        pwdET = (EditText) findViewById(R.id.loginPassword);
        pwdET.setText("r1");
        // Instantiate Progress Dialog object
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        SharedPreferences mypref = this.getSharedPreferences("user", MODE_PRIVATE);
        String empId = mypref.getString("empId",null);
        String deviceId = mypref.getString("deviceId",null);
        String pass = mypref.getString("password",null);

        if(empId!=null && deviceId!=null) {
            email = empId;
            password = pass;
            automaticLoginUser(empId, deviceId, pass);
        }
    }
    public void automaticLoginUser(String empId,String deviceId,String pass) {
        //RequestParams params = new RequestParams();
        params = new JSONObject();
        // When Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(empId) && Utility.isNotNull(pass)) {
            // When Email entered is Valid
            if (Utility.validate(empId)) {
                // Put Http parameter username with value of Email Edit View control
                try {
                    params.put("userId",empId);
                    token = FirebaseInstanceId.getInstance().getToken();
                    params.put("deviceId",deviceId);
                    // Put Http parameter password with value of Password Edit Value control
                    params.put("password", pass);
                    // Invoke RESTful Web Service with Http parameters
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    invokeWS(params);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            // When Email is invalid
            else {
                Toast.makeText(getApplicationContext(), "Please enter valid user id", Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }

    }

    public void forgotPassword(View view){
        Intent i = new Intent(this, ForgetPassword.class);
        startActivity(i);
    }

    public void loginUser(View view) {
        // Get Email Edit View Value
        email = emailET.getText().toString();
        // Get Password Edit View Value
        password = pwdET.getText().toString();
        // Instantiate Http Request Param Object
        //RequestParams params = new RequestParams();
        params = new JSONObject();
        // When Email Edit View and Password Edit View have values other than Null
        if (Utility.isNotNull(email) && Utility.isNotNull(password)) {
            // When Email entered is Valid
            if (Utility.validate(email)) {
                // Put Http parameter username with value of Email Edit View control
                try {
                    params.put("userId", email);
                    token = FirebaseInstanceId.getInstance().getToken();
                    params.put("deviceId",token);
                    System.out.println(FirebaseInstanceId.getInstance().getToken());
                    // Put Http parameter password with value of Password Edit Value control
                    params.put("password", password);
                    // Invoke RESTful Web Service with Http parameters
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    invokeWS(params);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
            // When Email is invalid
            else {
                Toast.makeText(getApplicationContext(), "Please enter valid id", Toast.LENGTH_LONG).show();
            }

        }
        else {
            Toast.makeText(getApplicationContext(), "Please fill the form, don't leave any field blank", Toast.LENGTH_LONG).show();
        }

    }

    public void showFragment(Fragment fragment, boolean addToStack) {

        getSupportFragmentManager().beginTransaction().replace(R.id.mylogin, fragment).commit();
    }

    /**
     * Method that performs RESTful webservice invocations
     *
     * @param params
     */
    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
  //      System.out.println("hello"+entity);
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(getApplicationContext(), ServerIPAddress.ip + "login/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog

                //  byte[] bytes = {...}
                String response = new String(responseBody);
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);

                    // When the JSON response has status boolean value assigned with true
                    if (obj.getBoolean("status")) {

                        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
                        mypref.edit().putString("empId",email).commit();
                        mypref.edit().putString("password",password).commit();
                        mypref.edit().putString("deviceId",token).commit();
                        mypref.edit().putString("empType",obj.getString("empType")).commit();
                        mypref.edit().putString("empName",obj.getString("empName")).commit();
                        mypref.edit().putString("responsible",obj.getString("responsible")).commit();
                        Toast.makeText(getApplicationContext(), "You are successfully logged in!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        if(obj.getString("empType").equals("nurse")){
                            Intent i = new Intent(getApplicationContext(), NurseHomePage.class);
                            i.putExtra(EXTRA_MESSAGE, response);
                            //i.putExtra("empId", email);
                            startActivity(i);
                        }
                        else if(obj.getString("empType").equals("admin")){
                            Intent i = new Intent(getApplicationContext(), Admin.class);

                            startActivity(i);
                        }
                        else{
                            Intent i = new Intent(getApplicationContext(), HomePage.class);
                            i.putExtra(EXTRA_MESSAGE, response);
                            //i.putExtra("empId", email);
                            startActivity(i);
                        }
                    }
                    // Else display error message
                    else {
                        //errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}

