package com.example.navigationdrawersample;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class Rollback_Info_Page extends AppCompatActivity {

    String response;
    TextView patName, patId, unit, fromWard, toWard, fromBedId, toBedId,fromWardType,toWardType;
 //   Context context;
    JSONObject params, obj;
    ProgressDialog prgDialog;
    Button b3;
    String employeeId,empType,deviceId, patientId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rollback_info_page);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        this.setTitle("Rollback");
        prgDialog = new ProgressDialog(this);

        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        empType = mypref.getString("empType",null);
        deviceId = mypref.getString("deviceId",null);
        params = new JSONObject();
        b3 = (Button)findViewById(R.id.button_rollback);

        Intent i = getIntent();
        response = i.getStringExtra("adminIntent");

        patName = (TextView)findViewById(R.id.text2200);
        patId = (TextView)findViewById(R.id.text44);
        unit = (TextView)findViewById(R.id.text66);
        fromWard = (TextView)findViewById(R.id.text8);
        toWard = (TextView)findViewById(R.id.text10) ;
        fromBedId = (TextView)findViewById(R.id.text12) ;
        toBedId = (TextView)findViewById(R.id.text14);
        fromWardType = (TextView)findViewById(R.id.text20);
        toWardType = (TextView)findViewById(R.id.text22);

        try {
            obj = new JSONObject(response);
//            String data = obj1.getString("data");
//            obj = new JSONObject(data);
            System.out.println("Hooga Booga "+obj.getString("name"));
            patName.setText(obj.getString("name"));
            patId.setText(obj.getString("patientId"));
            unit.setText(obj.getString("fromUnit"));
            fromWard.setText(obj.getString("fromWard"));
            toWard.setText(obj.getString("toWard"));
            fromBedId.setText(obj.getString("fromBedId"));
            toBedId.setText(obj.getString("toBedId"));
            fromWardType.setText(obj.getString("fromWardType"));
            toWardType.setText(obj.getString("toWardType"));
            patientId = obj.getString("patientId");
        }
        catch(JSONException e){
            Toast.makeText(getApplicationContext(), "Exception in nurse Info Activity", Toast.LENGTH_LONG).show();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(empType.equals("admin")) {
                    boolean ret = confirmDialog();

                    if (ret == true) {
                        try {
                            /*if (obj.getString("toWard").equals("out"))
                                params.put("status", "discharge");
                            else
                                params.put("status", "transfer");*/

                            params.put("patientId", patientId);
                            System.out.println("sdcsd c sdkndsc");
                            params.put("empId", employeeId);
                            params.put("deviceId",deviceId);

                     //       System.out.println(obj.getString("patientId"));
                       //     System.out.println("Patient sending id = " + obj.getString("patientId"));
                       //     Toast.makeText(context, "calling invokeWS", Toast.LENGTH_SHORT).show();
                           invokeWS(params);
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "error in calling invokeWS", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(), "Patient is still to be transferred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
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

    public boolean confirmDialog(){

        final boolean mResult[] = {false};
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };
   //     String show;
        // make a text input dialog and show it
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Confirm");

        alert.setMessage("Are you sure want to rollback?");

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult[0] = true;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                mResult[0] = false;
                handler.sendMessage(handler.obtainMessage());
            }
        });
        alert.show();

        // loop till a runtime exception is triggered.
        try { Looper.loop(); }
        catch(RuntimeException e2) {}

        //     Toast.makeText(getApplicationContext(),"ALert Dialog", Toast.LENGTH_SHORT).show();
        return mResult[0];
    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(this, ServerIPAddress.ip + "dorollback/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    if (obj.getString("status").equals("LOGOUT")) {
                        Toast.makeText(getApplicationContext(), "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    // When the JSON response has status boolean value assigned with true
                    else if (obj.getString("status").equals("rollback_successfull")) {
                        Toast.makeText(getApplicationContext(), "Database Updated", Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)  {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if(statusCode == 404){
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if(statusCode == 500){
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else{
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
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
