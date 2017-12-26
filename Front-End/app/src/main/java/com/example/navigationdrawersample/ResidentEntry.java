package com.example.navigationdrawersample;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

/**
 * Created by Tanish on 11-02-2017.
 */

public class ResidentEntry extends Fragment {
    EditText patId;
    //ProgressDialog prgDialog;
    Context context;
    Communicator comm;
    String employeeId,deviceId;
    Button b;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.resident_entry,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Admission");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        SharedPreferences mypref =  context.getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
        comm = (Communicator)getActivity();
        patId = (EditText)getActivity().findViewById(R.id.patientId_EditText);
        b = (Button)getActivity().findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = patId.getText().toString();
                JSONObject params = new JSONObject();
                try {
                    params.put("patientId", id);
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


    public void invokeWS(JSONObject params )  throws UnsupportedEncodingException{

        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        //Toast.makeText(getApplicationContext(), "hello", Toast.LENGTH_LONG).show();
        client.post(context, ServerIPAddress.ip+"resident_form/",entity ,"application/json",new AsyncHttpResponseHandler() {
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
                    if (obj.getString("status").equals("logout")) {
                        Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    else if(obj.getString("status").equals("admitting")){
                        Toast.makeText(context, "Admitting patient" , Toast.LENGTH_LONG).show();
                        Fragment f = new ResidentForm();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_main, f,"residentformtag");
                        ft.addToBackStack(null);
                        ft.commit();
                        comm.respond(response,"residentformtag",f);
                        ((HomePage)getActivity()).showFragment(f,true);
                    }
                    else{
                        Toast.makeText(context, obj.getString("msg") , Toast.LENGTH_LONG).show();
                    }
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
}
