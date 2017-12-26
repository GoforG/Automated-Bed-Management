package com.example.navigationdrawersample;

/**
 * Created by Tanish on 14-02-2017.
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.SeekBar;
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
import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by Tanish on 30-01-2017.
 */



public class ResidentForm extends Fragment{
    Context context;
    Communicator comm;
    JSONArray table;
    String response, EXTRA_MESSAGE;
    ProgressDialog prgDialog ;
    Spinner spinner, spinner2;
    String employeeId,deviceId;
    ArrayAdapter<String> unitspinneradapter;
    //Spinner unitspinner;
    String unitlist[] = {"1","2","3", "4", "5", "6"};
    String[] wardList = {"a", "bd", "bs", "dw", "fsr", "fsw", "hiw", "icu", "msr", "msw", "pw", "rw", "sw", "sdw"};
    String[] wardTypeList = {"GENERAL", "SPECIAL"};
    String patientName,diagnosis, patientId, unit, bedId;
    int daysinhospital;
    private TextView t1,t2;
    private TextView daystodischargefromhosp;
    private NumberPicker numberPicker2;
    private SeekBar sb;
    private String condition = "50", ward;
    private Button admitbtn, giveDate,borrowBedButton;
    public void changeData(String s){
        response = s;
    }

    public static class admitDialog extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Hello")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    })
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.resident_form, container, false);
    }
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Admission Form");
    }


    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        prgDialog = new ProgressDialog(context);
        SharedPreferences mypref = context.getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
        comm = (Communicator) getActivity();
        //Toast.makeText(getApplicationContext(), "Error Occured ", Toast.LENGTH_LONG).show();
        spinner = (Spinner)getActivity().findViewById(R.id.dialogspinner);
        spinner2 = (Spinner)getActivity().findViewById(R.id.wardtypespinner);
        admitbtn = (Button)getActivity().findViewById((R.id.buttonadmit));
        giveDate = (Button)getActivity().findViewById((R.id.button2));
        borrowBedButton = (Button)getActivity().findViewById((R.id.buttonborrow));
        t1 = (TextView)getActivity().findViewById(R.id.text100);
        t2 = (TextView)getActivity().findViewById(R.id.text3);
        sb = (SeekBar)getActivity().findViewById(R.id.sb);

        ArrayAdapter<String> spinnerArrayAdapter,spinnerArrayAdapter2;
        /*Intent intent = getIntent();
        String response = intent.getStringExtra(ResidentEntry.EXTRA_MESSAGE);*/
        //      Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
        try {
            JSONObject obj = new JSONObject(response);
            patientName = obj.getString("name");
            diagnosis = obj.getString("diagnosis");
            patientId = obj.getString("patientId");
            System.out.println("Patient id is :" + patientId);
            unit = obj.getString("unit");
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Error Occured after admitting [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
        String a = "Name: " + patientName;
        String b = "Diagnosis:  "+diagnosis;
        t1.setText(a);
        t2.setText(b);
        daystodischargefromhosp = (TextView) getActivity().findViewById(R.id.daystodichargefromhosp1);
        daystodischargefromhosp.setText(""+daysinhospital);
        daystodischargefromhosp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                builder
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogOuter, int id) {

                                //bhejna hai inwoke ws
                                //sendtoserver();
                                getValue(numberPicker2.getValue());
                                System.out.println("Number picker " +numberPicker2.getValue());
                                daystodischargefromhosp.setText(""+daysinhospital);
                                dialogOuter.dismiss();
                            }
                        })
                        ;
                AlertDialog dialog = builder.create();
                dialog.setTitle("Expected Discharge From Hospital");
                dialog.setView(getLayoutInflater(savedInstanceState).inflate(R.layout.numberpicker2,null));
                dialog.show();
                numberPicker2 = (NumberPicker)dialog.findViewById(R.id.numberPicker2);
                numberPicker2.setValue(daysinhospital);
                numberPicker2.setMinValue(0);
                numberPicker2.setMaxValue(50);
                numberPicker2.setOnValueChangedListener(( new NumberPicker.
                        OnValueChangeListener() {
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        getValue(newVal);
                    }
                }));

            }
        });



        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                condition = String.valueOf(progress);
                // TODO Auto-generated method stub

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Toast.makeText(context,"Condition: "+condition, Toast.LENGTH_SHORT).show();
                // TODO Auto-generated method stub
            }
        });
        spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wardList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner.setAdapter(spinnerArrayAdapter);

        spinnerArrayAdapter2 = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wardTypeList);
        spinnerArrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        spinner2.setAdapter(spinnerArrayAdapter2);

        String abc = String.valueOf(spinner.getSelectedItem());
        giveDate.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Fragment f = new Calendarmain();
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.content_main, f,"calendartag");
                ft.addToBackStack(null);
                ft.commit();
                comm.respond(response,"calendartag",f);
            }



        });

        admitbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                JSONObject params = new JSONObject();
                try {
                    params.put("patientId", patientId);
                    ward = (String)spinner.getSelectedItem();
                    System.out.println("unit " + unit);
                    params.put("wardType",spinner2.getSelectedItem());
                    params.put("unit", unit);
                    params.put("location", ward);
                    params.put("empId",employeeId);
                    params.put("deviceId",deviceId);
                    params.put("choice",0);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                try{
   //                 System.out.println("Hi ");
                    invokeWS(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }



        });
        borrowBedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ward=(String)spinner.getSelectedItem();
                if(((String)spinner2.getSelectedItem()).equals("GENERAL"))
                    onBorrowButtonPressed();
                else
                {
                    Toast.makeText(context, "Borrowing not allowed for SPECIAL beds ", Toast.LENGTH_LONG).show();
                }
            }
        });


    }
    public void getValue(int val){
        //Toast.makeText(context,val, Toast.LENGTH_SHORT).show();

        daysinhospital = val;

    }
    /*
        public void admit(View v){
            JSONObject params = new JSONObject();
            try {
                params.put("patientId", patientId);
                String ward = (String)spinner.getSelectedItem();
                params.put("unit", unit);
                params.put("wardSelected", ward);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                System.out.println("Hi ");
                invokeWS(params);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    */
    public void confirmDialog(JSONArray bedIds){
        try {

            final AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);

            builderSingle.setTitle("Select a bed");


            int i;

            JSONObject item;
            final String list[] = new String[bedIds.length()];
            for (i = 0; i < bedIds.length(); i++) {
                item = bedIds.getJSONObject(i);
                list[i] = item.getString("bedId");
            }
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);
/*
            //builderSingle.setSingleChoiceItems(list, null);
            builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            builderSingle
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialogOuter, int id) {

                        }
                    });
            AlertDialog n = builderSingle.create();
            n.getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //    System.out.println("pos: " + position + " value: "+ list[position]);
                    getBedID(list[position]);

                }
            });
            n.show();*/

            /*builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });*/

            builderSingle.setSingleChoiceItems(list,-1,new DialogInterface.OnClickListener() {
                public void onClick(final DialogInterface dialog, int which) {

                    //String strName = arrayAdapter.getItem(which);

                    System.out.println("str: " + list[which] + " Which is " + which);
                    getBedID(list[which]);

                }
            });

            /*builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, int which) {

                    String strName = arrayAdapter.getItem(which);
                    System.out.println("str: " + strName);
                    getBedID(strName);
                }
            });*/

            builderSingle
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialogOuter, int id) {
                            System.out.println("Bed Id " + bedId);
                            //bhejna hai inwoke ws
                            //sendtoserver();
                            JSONObject params = new JSONObject();
                            try {
                                params.put("patientId", patientId);
                                params.put("bedId",bedId);
                                //System.out.println("unit " + unit);
                                params.put("unit", unit);
                                params.put("location", ward);
                                params.put("wardType",spinner2.getSelectedItem());
                                params.put("daysexpected", daysinhospital);
                                params.put("currCond",condition);
                                params.put("empId",employeeId);
                                params.put("deviceId",deviceId);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try{
                                System.out.println("Hi ");
                                callResidentValidate(params);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
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
            System.out.println("str: ");
            final AlertDialog alert11 = builderSingle.create();
            System.out.println("str2 : ");
            alert11.setCancelable(false);
            alert11.show();
/*
            alert11.setOnShowListener(new DialogInterface.OnShowListener() {

                @Override
                public void onShow(DialogInterface dialog) {

                    Button button = ((AlertDialog) alert11).getButton(AlertDialog.BUTTON_POSITIVE);
                    button.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            // TODO Do something


                            AlertDialog.Builder builderInner = new AlertDialog.Builder(getActivity());
                            builderInner.setTitle("Are you sure want to confirm?");
                            builderInner.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                  //  dialogOuter.dismiss();
                                    Toast.makeText(context, "Selected bedId: " + bedId, Toast.LENGTH_LONG).show();
                                }
                            });

                            builderInner.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builderInner.show();
                        }
                    });
                }
            });
            */
        }
        catch(JSONException e){
            Toast.makeText(context, "Error in Alert Dialog", Toast.LENGTH_LONG).show();
        }


    }

    public void getBedID(String id){
        bedId = id;
    }

    public void onBorrowButtonPressed(){
        LayoutInflater inflater = (LayoutInflater) getActivity().getLayoutInflater();
        View dialoglayout = inflater.inflate(R.layout.borrowbedlayout, null);
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(context);
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

        unitspinneradapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, newUnitList);
        unitspinneradapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitspinner.setAdapter(unitspinneradapter);

        final EditText msg = (EditText)dialoglayout.findViewById(R.id.borrow_msg);
        builderSingle
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialogOuter, int id) {
                        if(unit.equals(unitspinner.getSelectedItem()))
                        {
                            Toast.makeText(context, "Please Select a different Unit", Toast.LENGTH_LONG).show();
                            return;
                        }
                        JSONObject params = new JSONObject();
                        try{
                            params.put("empId",employeeId);
                            params.put("deviceId",deviceId);
                            params.put("borrowtype","admit_borrowing");
                            //params.put("toUnit",(String)unitspinner.getSelectedItem());
                            params.put("toUnit",(String)unitspinner.getSelectedItem());
                            params.put("returnmsg",msg.getText().toString());
                            params.put("patientId", patientId);
                            params.put("fromUnit", unit);
                            params.put("toWard", ward);
                            params.put("toWardType",spinner2.getSelectedItem());
                            params.put("daysexpected", daysinhospital);
                            params.put("condition",condition);
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

        AlertDialog alert11 = builderSingle.create();

        alert11.setCancelable(false);
        alert11.show();
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

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        //   prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"admit/", entity, "application/json", new AsyncHttpResponseHandler() {
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

                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("LOGOUT")) {
                        Toast.makeText(context, "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    else if (obj.getString("status").equals("Y")) {
                        table = new JSONArray(obj.getString("emptybedlist"));
                        confirmDialog(table);
/*
                        admitDialog dial = new admitDialog();
                        dial.show(getFragmentManager(), "missiles");*/
                        //Toast.makeText(context, "Selected bedId: " + bedId, Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                        /*Intent i = new Intent(context, HomePage.class);
                        i.putExtra(EXTRA_MESSAGE,response);
                        startActivity(i);*/
                    }
                    // Else display error message
                    else{
                        //  errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(context,obj.getString("error_msg"), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // TODO Auto-generated catch blocks
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

    public void callResidentValidate(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        //   prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+"residentadmit_validate/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                    else if (obj.getString("status").equals("Y")) {

                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    // Else display error message
                    else{
                        //  errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(context,"Patient may already be admitted or something is wrong ", Toast.LENGTH_LONG).show();
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
}
