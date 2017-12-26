package com.example.navigationdrawersample;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.content.Context;


import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

//import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by Nocturnal on 2/15/2017.
 */

public class Calendarmain extends Fragment {

    Context context;
    Communicator comm;
    String response, EXTRA_MESSAGE;
    String patientId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_main,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Calendar ");
    }
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        comm = (Communicator) getActivity();
        try {
            JSONObject obj = new JSONObject(response);
            //patientName = obj.getString("name");
            //diagnosis = obj.getString("diagnosis");
            patientId = obj.getString("patientId");
            System.out.println("Patient id is :" + patientId);
            //unit = obj.getString("unit");
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Error Occured after admitting [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
        CalendarView cv = ((CalendarView)getActivity().findViewById(R.id.calendar_view));
        cv.updateCalendar();
        SharedPreferences mypref = getActivity().getSharedPreferences("user",Context.MODE_PRIVATE);
        final String empid = mypref.getString("empId",null);
        final String deviceId = mypref.getString("deviceId",null);
        cv.setEventHandler(new CalendarView.EventHandler()
        {

            @Override
            public void onDayLongPress(Date date, int m)
            {
                // show returned day
                //DateFormat df = SimpleDateFormat.getDateInstance();
                //Toast.makeText(context, df.format(date), Toast.LENGTH_SHORT).show();
                Date today = new Date();
                //Calendar cal = Calendar.getInstance();
                //cal.setTime(today);
                //cal.add(Calendar.DATE, 120); //add number would increment the days
                //Date maxalloweddate =  cal.getTime();
                int month = date.getMonth();
                if(date.getTime() >= today.getTime() && m==month ){// && date.getTime() <= maxalloweddate.getTime() ) {
                    boolean val = confirmDialog("submit", date);
                    if (val == true) {
                        DateFormat df = SimpleDateFormat.getDateInstance();
                        Toast.makeText(context, df.format(date), Toast.LENGTH_SHORT).show();
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String format = formatter.format(date);
                        JSONObject params = new JSONObject();

                       try {
                                params.put("patientId", patientId);
                                params.put("admitDate",format);
                                params.put("deviceId",deviceId);
                                params.put("empId",empid);
                                //System.out.println("unit " + unit);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try{
                            System.out.println("Hi ");
                            invokeWS(params);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                        //getFragmentManager().popBackStack();
                    }
                }
            }
        });
    }

    public boolean confirmDialog(String str, Date date){

        final boolean mResult[] = {false};
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };
        String show;
        DateFormat df = SimpleDateFormat.getDateInstance();
        // make a text input dialog and show it
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Confirm");
        alert.setMessage("Are you sure want to select \n " +df.format(date)+" " +" date ?");
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



    /*@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/
    public void changeData(String s){
        response = s;
    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        //   prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(context, ServerIPAddress.ip+ "resident_new_date/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        Toast.makeText(context, "Logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        //onBackPressed();
                    }

                    // When the JSON response has status boolean value assigned with true
                    else if (obj.getString("status").equals("OK")) {


/*
                        admitDialog dial = new admitDialog();
                        dial.show(getFragmentManager(), "missiles");*/
                        Toast.makeText(context, "Patient given date successfully" , Toast.LENGTH_LONG).show();
                        //getFragmentManager().popBackStack();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                        startActivity(i);
                        // Navigate to Home screen
                        /*Intent i = new Intent(context, HomePage.class);
                        i.putExtra(EXTRA_MESSAGE,response);
                        startActivity(i);*/
                    }
                    // Else display error message
                    else{
                        //  errorMsg.setText(obj.getString("error_msg"));
                        Toast.makeText(context,"Sorry, date is not given to patient", Toast.LENGTH_LONG).show();
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
