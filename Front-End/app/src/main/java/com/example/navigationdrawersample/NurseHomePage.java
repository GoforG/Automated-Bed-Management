package com.example.navigationdrawersample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class NurseHomePage extends AppCompatActivity implements Communicator {

    Communicator comm;
    String res;
    Fragment fragment = null;
    ProgressDialog prgDialog;
    String employeeId,deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nurse_home_page);
        comm = this;
        Intent i = getIntent();
        res = i.getStringExtra("nurseIntent");
        //employeeId = i.getStringExtra("empId");
        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);

        fragment = new NurseActivity();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, fragment, "transferfrag");
        ft.commit();
        comm.respond(res, "nurseactivitytag", fragment);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.logout_button) {
            prgDialog = new ProgressDialog(this);
            // Set Progress Dialog Text
            prgDialog.setMessage("Please wait...");
            // Set Cancelable as False
            prgDialog.setCancelable(false);
            JSONObject params = new JSONObject();
            try {
                params.put("empId", employeeId);
                params.put("deviceId",deviceId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try {

                invokeWS(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void respond(String data, String tag, Fragment frag) {
        if(tag.equals("nurseactivitytag")){
            NurseActivity f = (NurseActivity)frag;
            f.changeData(data);
        }
        else if(tag.equals("nurseinfotag")){
            NurseInfo f = (NurseInfo)frag;
            f.changeData(data);
        }
    }
    public void logoutNurse(View v)  {
        prgDialog = new ProgressDialog(this);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        JSONObject params = new JSONObject();
        try {
            params.put("empId", employeeId);
            System.out.println(employeeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {

            invokeWS(params);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"logout/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else {
                            Toast.makeText(getApplicationContext(), "Sorry you are logged out", Toast.LENGTH_LONG).show();
                            SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
                            mypref.edit().clear().commit();
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
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
