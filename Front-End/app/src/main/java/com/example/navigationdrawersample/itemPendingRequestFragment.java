package com.example.navigationdrawersample;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;
//import static android.content.Context.MODE_WORLD_READABLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class itemPendingRequestFragment extends Fragment {

    String response,employeeId,deviceId;
    JSONObject inputparams;
    TextView dt, pid, fromward, toward, tounit, status, returnbed, condition, frommsg, tomsg, towardtype, fromWardType, fromResident;
    String fromUnit,toUnit;
    int requesttype;
    RelativeLayout btns_pending;
    Context context;
    ProgressDialog prgDialog;
    int stat;
    String[] app_stat ={"Pending", "Approved", "Rejected", "Waiting For Borrowing Response"};


    public void changeData(String s){
        response = s;
    }


    public itemPendingRequestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_pending_request, container, false);
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        prgDialog = new ProgressDialog(context);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        SharedPreferences mypref = getActivity().getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);

        dt = (TextView) getActivity().findViewById(R.id.date_value_pending);
        pid = (TextView) getActivity().findViewById(R.id.patientId_pending);
        fromResident = (TextView) getActivity().findViewById(R.id.fromResident_value);
        fromward = (TextView)getActivity().findViewById(R.id.fromWard_value);
        toward = (TextView) getActivity().findViewById(R.id.toWard_pending);
        towardtype = (TextView) getActivity().findViewById(R.id.wardtype_value_pending);
        fromWardType = (TextView) getActivity().findViewById(R.id.fromWardType_value);
        tounit = (TextView) getActivity().findViewById(R.id.toUnit_pending);
        status = (TextView) getActivity().findViewById(R.id.approval_status_pending);
        returnbed = (TextView) getActivity().findViewById(R.id.returnbedid_pending);
        tomsg = (TextView) getActivity().findViewById(R.id.to_msg_pending);
        frommsg = (TextView) getActivity().findViewById(R.id.from_msg_pending);
        btns_pending = (RelativeLayout) getActivity().findViewById(R.id.buttons_pending);
        //condition = (TextView)getActivity().findViewById(R.id.condition_pending); should be uncommented when condition is added to ui.

        try {
            inputparams = new JSONObject(response);
            dt.setText(inputparams.getString("timestamp"));
            pid.setText(inputparams.getString("patientId"));
            fromResident.setText(inputparams.getString("issuing_doctor_name"));
            fromward.setText(inputparams.getString("fromWard"));
            toward.setText(inputparams.getString("toWard"));
            tounit.setText(inputparams.getString("toUnit"));
            fromWardType.setText(inputparams.getString("fromWardType"));
            towardtype.setText(inputparams.getString("toWardType"));
            status.setText(app_stat[Integer.parseInt(inputparams.getString("approval_status"))]);
            returnbed.setText(inputparams.getString("returnbedId"));
            tomsg.setText(inputparams.getString("to_msg"));
            frommsg.setText(inputparams.getString("from_msg"));
            stat = inputparams.getInt("approval_status");
            fromUnit = inputparams.getString("fromUnit");
            requesttype = inputparams.getInt("requestType");
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        if (stat == 1 && requesttype==0) {// put confirm and cancel button
            Button confirmButton = new Button(context);
            confirmButton.setText("Confirm");
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.setMargins(60, 0, 0, 0);
            confirmButton.setId(R.id.confirmbutton_pending);
            confirmButton.setLayoutParams(params1);
            btns_pending.addView(confirmButton);
            Button cancelButton = new Button(context);
            cancelButton.setText("Cancel");

            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.RIGHT_OF, R.id.confirmbutton_pending);
            params2.setMargins(80, 0, 0, 0);
            cancelButton.setId(R.id.cancelbutton_pending);
            cancelButton.setLayoutParams(params2);
            btns_pending.addView(cancelButton);

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("empId",employeeId);
                        parameter.put("deviceId",deviceId);
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("replystatus", "accepted");
                        parameter.put("fromUnit",inputparams.getString("fromUnit"));
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
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("replystatus", "rejected");
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

        else if (stat == 2 || (stat==1 && requesttype == 1)) {//put ok button
            Button okButton = new Button(context);
            okButton.setText("OK");
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params1.addRule(RelativeLayout.ALIGN_PARENT_START);
            okButton.setId(R.id.okbutton_pending);
            okButton.setLayoutParams(params1);
            btns_pending.addView(okButton);

            okButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("empId",employeeId);
                        parameter.put("deviceId",deviceId);
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("replystatus", "delete_request_or_borrow_bed");

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
//            Button borrowButton = new Button(context);
//            borrowButton.setText("Borrow");
//            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//            params2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            borrowButton.setId(R.id.cancelbutton_pending);
//            borrowButton.setLayoutParams(params2);
//            btns_pending.addView(borrowButton);
//            borrowButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    confirmDialog();
//                }
//            });

        }
    }


 /*   public void confirmDialog(){
        String[] unitIds = {"1","2","3"};
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);

        builderSingle.setTitle("Select the unit");

        int i;
        int l = 0;

        final String list[] = new String[unitIds.length];
        for (i = 0; i < unitIds.length; i++) {
            if(!unitIds.equals(fromUnit))
                list[l++] = unitIds[i];
        }
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);


        builderSingle.setSingleChoiceItems(list,-1,new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int which) {

                //String strName = arrayAdapter.getItem(which);

                System.out.println("str: " + list[which] + " Which is " + which);
                getUnitID(list[which]);

            }
        });


        builderSingle
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogOuter, int id) {
                        //System.out.println("Bed Id " + bedId);
                        //bhejna hai inwoke ws
                        //sendtoserver();
                        JSONObject parameter = new JSONObject();
                        try {
                            parameter.put("empId",employeeId);
                            parameter.put("deviceId",deviceId);
                            parameter.put("requestid", inputparams.getString("requestid"));
                            parameter.put("replystatus","3");
                            parameter.put("toUnit",toUnit);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            invokeWS(parameter);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialogOuter.dismiss();
                    }
                })

                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        getUnitID(null);
                    }
                });

        builderSingle.setCancelable(false);
        System.out.println("str: ");
        final AlertDialog alert11 = builderSingle.create();
        System.out.println("str2 : ");
        alert11.setCancelable(false);
        alert11.show();
    }

    public void getUnitID(String id){
        toUnit = id;
    } */

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"resident_transfer_verify/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        Toast.makeText(context, "Database Updated", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else{
                            Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                            Intent i = getActivity().getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
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
