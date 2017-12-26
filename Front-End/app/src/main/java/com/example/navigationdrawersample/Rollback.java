package com.example.navigationdrawersample;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Rollback extends AppCompatActivity {

    String employeeId, deviceId;
    String EXTRA_MESSAGE = "adminIntent";
    Communicator comm;
    EditText patId;
    Button searchButton;
    ProgressDialog prgDialog;
    Context context;

    public Rollback() {
        // Required empty public constructor
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_admin);
        context = this;
//        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        setTitle("Rollback");

        prgDialog = new ProgressDialog(this);

        SharedPreferences mypref =  getApplicationContext().getSharedPreferences("user",Context.MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
   //     comm = (Communicator)getActivity();
        patId = (EditText)findViewById(R.id.bedId_value);
        searchButton = (Button)findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = patId.getText().toString();
                JSONObject params = new JSONObject();
                try {
                    params.put("patientId", patId.getText().toString());
                    params.put("empId",employeeId);
                    params.put("deviceId",deviceId);
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
        });
    }


    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.logout, menu);
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

                invokeLogout(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void invokeWS(JSONObject params )  throws UnsupportedEncodingException{

        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        //Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();
        client.post(context, ServerIPAddress.ip + "checkforrollback/",entity ,"application/json",new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog
                //              Toast.makeText(getApplicationContext(), "yo yo yo", Toast.LENGTH_LONG).show();

                //  byte[] bytes = {...}
                String response = new String(responseBody);
                //prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    if (obj.getString("status").equals("rollbackdata")) {
            //            Toast.makeText(getApplicationContext(), "Sorry you are logged out", Toast.LENGTH_LONG).show();
//                        Intent i = getBaseContext().getPackageManager()
//                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
//                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        Intent i = new Intent(context, Rollback_Info_Page.class);
                        i.putExtra(EXTRA_MESSAGE, response);
                        //i.putExtra("empId", email);
                        startActivity(i);

                    //    startActivity(i);
                    }
                    // When the JSON response has status boolean value assigned with true
                    else {
                        Toast.makeText(context, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                 /*   else if (obj.getString("status").equals("request_exist")) {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                    else if (obj.getString("status").equals("transfer_exist")) {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg") , Toast.LENGTH_LONG).show();
                    }
                    else if (obj.getString("status").equals("checkup_exist")) {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg") , Toast.LENGTH_LONG).show();
                    }
                    else if (obj.getString("status").equals("bedfilled")) {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }*/
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)  {
                // Hide Progress Dialog
                //prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }


    public void invokeLogout(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"logout/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        SharedPreferences mypref = getSharedPreferences("user",Context.MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else {
                        Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        SharedPreferences mypref = getSharedPreferences("user",Context.MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(context, "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(context, "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(context, "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
