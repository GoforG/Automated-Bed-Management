package com.example.navigationdrawersample;

import android.app.Activity;
import android.content.Intent;
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

public class NewPassword extends Activity {

    EditText newPassword, confirm_newPassword;
    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_password);

        Intent i = getIntent();
        username = i.getStringExtra("forgetPasswordIntent");

        newPassword = (EditText) findViewById(R.id.new_password_edittext);
        confirm_newPassword = (EditText)findViewById(R.id.confirm_pass_edittext);
    }
    public void cancelled(View view){
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
        startActivity(i);
    }

    public void setNewPassword(View view){
        String newPass = newPassword.getText().toString();
        String confirm_newPass = confirm_newPassword.getText().toString();

        JSONObject params = new JSONObject();

        if(isNotNull(newPass) && isNotNull(confirm_newPass)){
            if(newPass.equals(confirm_newPass)){
                try{
                    params.put("empId", username);
                    params.put("newpass", newPass );
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    inwokeWS(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Please enter the same password in both fields", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void inwokeWS(JSONObject params) throws UnsupportedEncodingException {

        StringEntity entity = new StringEntity(params.toString());
        System.out.println("hello"+entity);

        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip + "new_password/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        Toast.makeText(getApplicationContext(), "Password is changed", Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                    }
                    // Else display error message
                    else {
                        // errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(getApplicationContext(), "Password is not changed", Toast.LENGTH_LONG).show();
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
