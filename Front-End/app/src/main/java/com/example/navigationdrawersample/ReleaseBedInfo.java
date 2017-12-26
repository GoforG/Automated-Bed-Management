package com.example.navigationdrawersample;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;


/**
 * A simple {@link Fragment} subclass.
 */
public class ReleaseBedInfo extends AppCompatActivity {

    private Button submitButton;
    private TextView releaseBed;
    String releaseIntent, setPatientText, employeeId, deviceId, bedId;

    public ReleaseBedInfo() {
        // Required empty public constructor
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.fragment_release_bed_info_);
        SharedPreferences mypref =  getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        bedId = getIntent().getStringExtra("bedId");

        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
        releaseBed = (TextView)findViewById(R.id.releaseBed_textview);
        submitButton = (Button)findViewById(R.id.releaseButton);
      //  cancelButton = (Button)findViewById(R.id.cancel_button);

        String releaseResponse = getIntent().getStringExtra("releaseBedIntent");

        try {
            JSONObject obj = new JSONObject(releaseResponse);
            setPatientText = "This bed is allocated to patient " + obj.getString("name") + "(" + obj.getString("id") + ")";
            bedId = obj.getString("bedId");
        }
        catch(Exception c){
            System.out.println("exception in release bed info");
        }

        releaseBed.setText(setPatientText);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    JSONObject params = new JSONObject();
                    params.put("empId", employeeId);
                    params.put("deviceId", deviceId);
                    params.put("bedId", bedId);
                    params.put("choice", "free");

                    invokeWS(params);
                }
                catch(Exception e){

                }
            }
        });


    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
     //   prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip + "freeretainbeds/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog

                //  byte[] bytes = {...}
                String response = new String(responseBody);
          //      prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("success")) {
                        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);

                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "Bed is released successfully", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    // Else display error message
                    else  {
                        Toast.makeText(getApplicationContext(), "Bed is not released", Toast.LENGTH_LONG).show();
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
     //           prgDialog.hide();
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
