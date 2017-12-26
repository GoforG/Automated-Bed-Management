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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import static android.content.Context.MODE_PRIVATE;
//import static android.content.Context.MODE_WORLD_READABLE;

public class NurseInfo extends Fragment {
    String response;
    TextView patName, patId, unit, fromWard, toWard, fromBedId, toBedId,fromWardType,toWardType;
    Context context;
    JSONObject params, obj;
    ProgressDialog prgDialog;
    Button b3;
    String employeeId,empType,deviceId;
    public void changeData(String s){
        response = s;
    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_nurse_info,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Patient Transfer Info");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        prgDialog = new ProgressDialog(context);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        SharedPreferences mypref = context.getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        empType = mypref.getString("empType",null);
        deviceId = mypref.getString("deviceId",null);
        params = new JSONObject();
        b3 = (Button)getActivity().findViewById(R.id.button3);
        Toast.makeText(context, "Nurse Info", Toast.LENGTH_LONG).show();
        //Intent intent = getIntent();
        //response = intent.getStringExtra("nurseActivity");

        patName = (TextView)getActivity().findViewById(R.id.text2200);
        patId = (TextView)getActivity().findViewById(R.id.text44);
        unit = (TextView)getActivity().findViewById(R.id.text66);
        fromWard = (TextView)getActivity().findViewById(R.id.text8);
        toWard = (TextView)getActivity().findViewById(R.id.text10) ;
        fromBedId = (TextView)getActivity().findViewById(R.id.text12) ;
        toBedId = (TextView)getActivity().findViewById(R.id.text14);
        fromWardType = (TextView)getActivity().findViewById(R.id.text20);
        toWardType = (TextView)getActivity().findViewById(R.id.text22);

        try {
            obj = new JSONObject(response);
            System.out.println("Hooga Booga "+obj.getString("name"));
            patName.setText(obj.getString("name"));
            patId.setText(obj.getString("patientId")+"");
            unit.setText(obj.getString("fromUnit")+"");
            fromWard.setText(obj.getString("fromWard")+"");
            toWard.setText(obj.getString("toWard")+"");
            fromBedId.setText(obj.getString("fromBedId")+"");
            toBedId.setText(obj.getString("toBedId")+"");
            fromWardType.setText(obj.getString("fromWardType"));
            toWardType.setText(obj.getString("toWardType"));
        }
        catch(JSONException e){
            Toast.makeText(context, "Exception in nurse Info Activity", Toast.LENGTH_LONG).show();
        }

        b3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(empType.equals("nurse")) {
                    boolean ret = confirmDialog();

                    if (ret == true) {
                        try {
                            /*if (obj.getString("toWard").equals("out"))
                                params.put("status", "discharge");
                            else
                                params.put("status", "transfer");*/
                            params.put("patientId", obj.getString("patientId"));
                            params.put("empId", employeeId);
                            params.put("deviceId",deviceId);
                            System.out.println("Patient sending id = " + obj.getString("patientId"));
                            Toast.makeText(context, "calling invokeWS", Toast.LENGTH_SHORT).show();
                            invokeWS(params);
                        } catch (Exception e) {
                            Toast.makeText(context, "error in calling invokeWS", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }
                else{
                    Toast.makeText(context, "Patient is still to be transferred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public boolean confirmDialog(){

        final boolean mResult[] = {false};
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };
        String show;
        // make a text input dialog and show it
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Confirm");

        alert.setMessage("Are you sure want to transfer?");

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
        client.post(context, ServerIPAddress.ip+"nurse_transfer/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                prgDialog.hide();
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    if (obj.getString("status").equals("LOGOUT")) {
                        Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    // When the JSON response has status boolean value assigned with true
                    else if (obj.getString("status").equals("Success")) {
                        Toast.makeText(context, "Database Updated", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    } else {
                        Toast.makeText(context, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                }catch (JSONException e) {
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
                    Toast.makeText(context, "Unexpected Error occurred! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
