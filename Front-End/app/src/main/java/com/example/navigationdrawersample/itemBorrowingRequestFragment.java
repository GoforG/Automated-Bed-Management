package com.example.navigationdrawersample;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import static android.content.Context.MODE_WORLD_READABLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class itemBorrowingRequestFragment extends Fragment{
    String response;
    ProgressDialog prgDialog;
    Context context;
    JSONObject inputparams;
    Button returnBedButton;
    TextView borrowedfromunit,borrowingunit, borrowingtime;

    public void changeData(String s){
        response = s;
    }

    public itemBorrowingRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_borrowing_request, container, false);
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        SharedPreferences mypref = getActivity().getSharedPreferences("user",MODE_PRIVATE);
        final String employeeId = mypref.getString("empId",null);
        final String deviceId = mypref.getString("deviceId",null);
        prgDialog = new ProgressDialog(context);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        //String borrowingId = null;

        borrowedfromunit = (TextView)getActivity().findViewById(R.id.borrowedfromunit);
        borrowingunit = (TextView)getActivity().findViewById(R.id.borrowingUnit);
        borrowingtime = (TextView)getActivity().findViewById(R.id.borrowingTime);
      //  borrowedbedid = (TextView)getActivity().findViewById(R.id.borrowedbedid);
        returnBedButton = (Button)getActivity().findViewById(R.id.returnButton);

        try {
            inputparams = new JSONObject(response);
            borrowingtime.setText("Borrowed on - "+inputparams.getString("timestamp"));
            borrowedfromunit.setText("From Unit - "+inputparams.getString("fromUnit"));
            borrowingunit.setText("To Unit - "+inputparams.getString("toUnit"));
            //borrowingId = inputparams.getString("borrowingid");
         //   borrowedbedid.setText("Bed  Id - "+inputparams.getString("bedId"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        returnBedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject parameter = new JSONObject();
                try {
                    parameter.put("borrowId", inputparams.getString("borrowingid"));
                    parameter.put("empId",employeeId);
                    parameter.put("deviceId",deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    invokeWS(parameter);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"borrowed_bed_return/", entity, "application/json", new AsyncHttpResponseHandler() {
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

                    if (obj.getString("status").equals("LOGOUT")) {
                        Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    // When the JSON response has status boolean value assigned with true
                    else if (obj.getString("status").equals(("Success"))) {
                        Toast.makeText(context, "Database Updated", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else{
                        Toast.makeText(context, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
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
