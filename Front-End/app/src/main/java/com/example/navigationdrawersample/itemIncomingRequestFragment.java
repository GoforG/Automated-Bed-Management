package com.example.navigationdrawersample;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;
//import static android.content.Context.MODE_WORLD_READABLE;


/**
 * A simple {@link Fragment} subclass.
 */
public class itemIncomingRequestFragment extends Fragment {

    String response,bedId, patientId, unit, wardtypechoice="none",selectedborrowunit,yourborrowmsg;
    String unitlist[] = {"1", "2", "3"};
    boolean bedavailableflag = true;
    String employeeId,deviceId;
    JSONObject inputparams;
    TextView dt, pid, fromward, docname, status, condition, frommsg, daysexpected, returnbed,wardtypeview, toWard, toWardType, fromWardType;
    EditText  tomsg;
    RelativeLayout btns_incoming;
    Context context;
    ProgressDialog prgDialog;
    //    Button giveBedButton, rejectButton ;
    int stat,requesttype;
    JSONArray table;
    String[] app_stat ={"Pending","Approved","Rejected", "Waiting for borrowing response"};


    public itemIncomingRequestFragment() {
        // Required empty public constructor
    }

    public void changeData(String s) {
        response = s;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.item_incoming_request, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        SharedPreferences mypref = context.getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",deviceId);
        prgDialog = new ProgressDialog(context);
        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
        // Set Cancelable as False
        prgDialog.setCancelable(false);

        dt = (TextView) getActivity().findViewById(R.id.date_incoming);
        pid = (TextView) getActivity().findViewById(R.id.patientId_incoming);
        fromward = (TextView) getActivity().findViewById(R.id.fromward_incoming);
        toWard = (TextView) getActivity().findViewById(R.id.toWard_incoming_value);
        docname = (TextView) getActivity().findViewById(R.id.doctor_name_incoming);
        fromWardType = (TextView) getActivity().findViewById(R.id.fromWardType_incoming_value);
        toWardType = (TextView) getActivity().findViewById(R.id.toWardType_incoming_value);
        //toresident = (TextView)getActivity().findViewById(R.id.);
        status = (TextView) getActivity().findViewById(R.id.approval_status_incoming);
        wardtypeview = (TextView) getActivity().findViewById(R.id.giveWardType);
        tomsg = (EditText) getActivity().findViewById(R.id.to_msg_incoming);
        frommsg = (TextView) getActivity().findViewById(R.id.from_msg_incoming);
        condition = (TextView) getActivity().findViewById(R.id.condition_incoming);
        btns_incoming = (RelativeLayout) getActivity().findViewById(R.id.buttons_incoming);
        //      giveBedButton = (Button)getActivity().findViewById(R.id.incoming_reject_button);
        //     rejectButton = (Button)getActivity().findViewById(R.id.incoming_reject_button);

        //daysexpected = (TextView)getActivity().findViewById(R.id.daysexpected_incoming); to be added late
        wardtypeview.setPaintFlags(wardtypeview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        try {
            inputparams = new JSONObject(response);
            dt.setText(inputparams.getString("timestamp"));
            patientId = inputparams.getString("patientId");
            pid.setText(inputparams.getString("patientId"));
            fromward.setText(inputparams.getString("fromWard"));
            toWard.setText(inputparams.getString("toWard"));
            fromWardType.setText(inputparams.getString("fromWardType"));
            toWardType.setText(inputparams.getString("toWardType"));
            docname.setText(inputparams.getString("issuing_doctor_name"));
            status.setText(app_stat[Integer.parseInt(inputparams.getString("approval_status"))]);

            unit = inputparams.getString("toUnit");
            //returnbed.setText(inputparams.getString("returnbedId"));
            //   inputparams.
            //    System.out.println("he;;p " + inputparams.getString("from_msg"));
            if(inputparams.getString("from_msg").isEmpty()){
                frommsg.setText("No Receieved Message");
            }
            else{
                frommsg.setText(inputparams.getString("from_msg"));
            }
//            if(!inputparams.getString("to_msg").isEmpty()){
//                tomsg.setText(inputparams.getString("to_msg"));
//            }

            condition.setText(inputparams.getString("condition"));
            stat = inputparams.getInt("approval_status");
            requesttype = inputparams.getInt("requestType");
            //daysexpected.setText(inputparams.getString("daysexpected"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //System.out.println("approval status: "+status);
        wardtypeview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                giveWardType();
            }
        });
        /*returnbed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject params = new JSONObject();
                try {
                    params.put("choice",1);
                    params.put("empId",employeeId);
                    params.put("deviceId",deviceId);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    invokeWSBedList(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });*/
        if(stat != 3 && stat != 1){
        if (requesttype == 0) {// put givebed, borrowbed and cancel button
            Button giveBedButton = new Button(context);
            giveBedButton.setText("Give Bed");
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //       params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params1.setMargins(20, 0, 0, 0);
            giveBedButton.setId(R.id.givebedbutton_incoming);
            giveBedButton.setLayoutParams(params1);
            btns_incoming.addView(giveBedButton);

            Button borrowBedButton = new Button(context);
            borrowBedButton.setText("Borrow");
            RelativeLayout.LayoutParams params4 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params4.addRule(RelativeLayout.RIGHT_OF, R.id.givebedbutton_incoming);
            params4.setMargins(40,0,0,0);
            borrowBedButton.setId(R.id.borrowbedbutton_incoming);
            borrowBedButton.setLayoutParams(params4);
            btns_incoming.addView(borrowBedButton);


            Button rejectButton = new Button(context);
            rejectButton.setText("Reject");
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.RIGHT_OF, R.id.borrowbedbutton_incoming);
            params2.setMargins(40, 0, 0, 0);
            rejectButton.setId(R.id.rejectbutton_incoming);
            rejectButton.setLayoutParams(params2);
            btns_incoming.addView(rejectButton);

            giveBedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(wardtypechoice.equals("none")){ //when no ward type selected
                        Toast.makeText(context, "Please select ward type first", Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONObject param = new JSONObject();
                    try {
                        param.put("empId",employeeId);
                        param.put("deviceId",deviceId);
                        param.put("patientId", patientId);
                        param.put("location",inputparams.getString("toWard"));
                        param.put("unit",inputparams.getString("toUnit"));
                        param.put("wardType",inputparams.getString("toWardType"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        invokeWSBedList(param);
                    } catch (UnsupportedEncodingException e) {
                  //      bedavailableflag = false;
                        e.printStackTrace();
                    }

                }
            });
            borrowBedButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(stat!=0){
                        Toast.makeText(context, "You have already one borrowing request pending for this patient", Toast.LENGTH_LONG).show();
                        return;
                    }
                    onBorrowButtonPressed();
                }
            });
            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("replystatus", 2);
                        parameter.put("returnmsg", tomsg.getText());
                        parameter.put("empId",employeeId);
                        parameter.put("deviceId",deviceId);
                        //parameter.put("returnbed", null);
                        //parameter.put("returnmsg", null);
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
        else if(requesttype==1) {//borrowing request from responsible to responsible
            Button approveBorrowingButton = new Button(context);
            approveBorrowingButton.setText("Approve");
            RelativeLayout.LayoutParams params1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //       params1.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            params1.setMargins(20, 0, 0, 0);
            approveBorrowingButton.setId(R.id.givebedbutton_incoming);
            approveBorrowingButton.setLayoutParams(params1);
            btns_incoming.addView(approveBorrowingButton);


            Button rejectButton = new Button(context);
            rejectButton.setText("Reject");
            RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params2.addRule(RelativeLayout.RIGHT_OF, R.id.givebedbutton_incoming);
            params2.setMargins(80, 0, 0, 0);
            rejectButton.setId(R.id.rejectbutton_incoming);
            rejectButton.setLayoutParams(params2);
            btns_incoming.addView(rejectButton);

            approveBorrowingButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (wardtypechoice.equals("none")) { //when no ward type selected
                        Toast.makeText(context, "Please select ward type first", Toast.LENGTH_LONG).show();
                        return;
                    }
                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("wardtype", wardtypechoice);
                        //parameter.put("returnbed", bedId);
                        parameter.put("replystatus", 1);
                        parameter.put("returnmsg", tomsg.getText());
                        parameter.put("empId", employeeId);
                        parameter.put("deviceId", deviceId);
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
            rejectButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject parameter = new JSONObject();
                    try {
                        parameter.put("requestid", inputparams.getString("requestid"));
                        parameter.put("replystatus", 2);
                        parameter.put("returnmsg", tomsg.getText());
                        parameter.put("empId", employeeId);
                        parameter.put("deviceId", deviceId);
                        //parameter.put("returnbed", null);
                        //parameter.put("returnmsg", null);
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
        }
    }
    public void onBorrowButtonPressed(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.borrowbedlayout, null);
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Select unit");
        builderSingle.setView(dialoglayout);

        ArrayList<String> updatedUnitList = new ArrayList<String>();

        int i;

        for(i = 0; i < unitlist.length; i++){
            if(!unitlist[i].equals(unit)){
                updatedUnitList.add(unitlist[i]);
            }
        }

        String newUnitList[] = updatedUnitList.toArray(new String[updatedUnitList.size()]);

        final Spinner unitspinner = (Spinner)dialoglayout.findViewById(R.id.borrow_unit_spinner);
        final ArrayAdapter<String> unitspinneradapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, newUnitList );
        unitspinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitspinner.setAdapter(unitspinneradapter);
        final EditText msg = (EditText)dialoglayout.findViewById(R.id.borrow_msg);
        builderSingle
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogOuter, int id) {
                        JSONObject params = new JSONObject();
                        try{
                            params.put("empId",employeeId);
                            params.put("deviceId",deviceId);
                            params.put("borrowtype","transfer_borrowing");
                            params.put("requestid", inputparams.getString("requestid"));
                            params.put("unit",(String)unitspinner.getSelectedItem());
                            params.put("returnmsg",msg.getText().toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                        try {
                            invokeWSBorrow(params);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        dialogOuter.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setCancelable(false);

        final AlertDialog alert11 = builderSingle.create();

        alert11.setCancelable(false);
        alert11.show();
    }
    public void giveWardType(){
        final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
        builderSingle.setTitle("Select a ward type");
        final String list[] = new String[2];
        list[0] = "GENERAL";
        list[1] = "SPECIAL";
        builderSingle.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
            public void onClick(final DialogInterface dialog, int which) {
                System.out.println("str: " + list[which] + " Which is " + which);
                getWardTypeChoice(list[which]);
            }
        });

        builderSingle
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogOuter, int id) {
                        wardtypeview.setText(wardtypechoice);
                        dialogOuter.dismiss();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        getBedID(null);
                    }
                });

        builderSingle.setCancelable(false);

        final AlertDialog alert11 = builderSingle.create();

        alert11.setCancelable(false);
        alert11.show();

    }
    public void getWardTypeChoice(String id){ wardtypechoice = id; };
    public void confirmDialog(JSONArray bedIds) {
        try {

            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);

            builderSingle.setTitle("Select a bed");

            JSONObject item;
            final String list[] = new String[bedIds.length()];
            for (int i = 0; i < bedIds.length(); i++) {
                item = bedIds.getJSONObject(i);
                list[i] = item.getString("bedId");
            }
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);


            builderSingle.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int which) {
                    System.out.println("str: " + list[which] + " Which is " + which);
                    getBedID(list[which]);

                }
            });



            builderSingle
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialogOuter, int id) {
                       //     returnbed.setText(bedId);
                            dialogOuter.dismiss();

                            callInvokeWs();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            getBedID(null);
                        }
                    });

            builderSingle.setCancelable(false);

            final AlertDialog alert11 = builderSingle.create();

            alert11.setCancelable(false);
            alert11.show();

        }
        catch(JSONException e){
            Toast.makeText(context, "Error in Alert Dialog", Toast.LENGTH_LONG).show();
        }
    }
    public void getBedID(String id){
        bedId = id;
    }


    public void callInvokeWs(){
        JSONObject parameter = new JSONObject();
        try {
            parameter.put("requestid", inputparams.getString("requestid"));
            parameter.put("wardtype", wardtypechoice);
            parameter.put("returnbed", bedId);
            parameter.put("replystatus", 1);
            parameter.put("returnmsg", tomsg.getText());
            parameter.put("empId", employeeId);
            parameter.put("deviceId", deviceId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            invokeWS(parameter);
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
        client.post(context, ServerIPAddress.ip+"resident_reply/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                    if (obj.getString("status").equals("TRUE")) {
                        Toast.makeText(context, "Database Updated", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else if(obj.getString("status").equals("FALSE")){
                        Toast.makeText(context, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
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
    public void invokeWSBedList(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        //   prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip + "admit/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Hide Progress Dialog

                //  byte[] bytes = {...}
                String response = new String(responseBody);
                //        prgDialog.hide();
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
                    if (obj.getString("status").equals("Y")) {
                        bedavailableflag = true;
                        table = new JSONArray(obj.getString("emptybedlist"));
                        confirmDialog(table);


                    }
                    // Else display error message
                    else{
            //            bedavailableflag = false;
                        //  errorMsg.setText(obj.getString("error_msg"));
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
                //   prgDialog.hide();
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
    public void invokeWSBorrow(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"borrowrequest/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                    if (obj.getString("status").equals("TRUE")) {
                        Toast.makeText(context, "Borrowing request made", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //Toast.makeText(context, "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else if (obj.getString("status").equals("FALSE")){
                        //kuch ni karo
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
