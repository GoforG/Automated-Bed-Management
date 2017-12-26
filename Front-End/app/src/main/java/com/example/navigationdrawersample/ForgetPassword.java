package com.example.navigationdrawersample;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class ForgetPassword extends Activity {

    public final static String EXTRA_MESSAGE = "forgetPasswordIntent";
    EditText userName, dob;
    String username, dateOfBirth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forget_password);

        userName = (EditText)findViewById(R.id.username_edittext);
        dob = (EditText)findViewById(R.id.dob_edittext);

    }
    public void onCancel(View view){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
        startActivity(i);
    }
    public void forgetPassword(View view) {
        username = userName.getText().toString();
        dateOfBirth = dob.getText().toString();

        JSONObject params = new JSONObject();

        if(isNotNull(username) && isNotNull(dateOfBirth)){
            try{
                params.put("empId", username);
                params.put("dob", dateOfBirth);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                invokeWS(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "Please don't leave any field blank", Toast.LENGTH_LONG).show();
        }
    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
       // prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        System.out.println("hello"+entity);
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"forget_password/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog

                //  byte[] bytes = {...}
                String response = new String(responseBody);
             //   prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);

                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("success")) {
                            Intent i = new Intent(getApplicationContext(), NewPassword.class);
                            i.putExtra(EXTRA_MESSAGE, username);

                            startActivity(i);

                    }
                    // Else display error message
                    else {
                       // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), "Please enter valid username and DOB", Toast.LENGTH_LONG).show();
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
             //   prgDialog.hide();
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
    public static boolean isNotNull(String txt){
        return txt!=null && txt.trim().length()>0 ? true: false;
    }
}
