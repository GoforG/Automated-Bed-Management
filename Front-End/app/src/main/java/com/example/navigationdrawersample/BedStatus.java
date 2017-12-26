package com.example.navigationdrawersample;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;


public class BedStatus extends Fragment {
    Context context;
    JSONObject params;
    JSONArray table;
  //  String units[] = {"1", "2", "3"};
  //  String[] wardList = {"Recovery", "ICU", "Male Surgery", "Female Surgery", "Pediatric Neurosurgery", "B-Block", "Head Injury", "Step Down"};

    String units[] = {"1","2","3", "4", "5", "6"};
    String[] wardList = {"a", "bd", "bs", "dw", "fsr", "fsw", "hiw", "icu", "msr", "msw", "pw", "rw", "sw", "sdw"};

    String wardTypeList[] = {"GENERAL", "SPECIAL"};
    int nob,noeb;
    String ward, unit, wardType;

    public BedStatus() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.bed_status_fragment, container, false);
    }
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Bed Availibility");

    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
     //   params = new JSONObject();

        final Spinner wardPicker = (Spinner)getActivity().findViewById(R.id.wardPickSpinner);
        final Spinner unitPicker = (Spinner)getActivity().findViewById(R.id.unitPickSpinner);
        final Spinner wardTypePicker = (Spinner)getActivity().findViewById(R.id.wardtype_spinner);

        ArrayAdapter<String> unitSpinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, units);
        ArrayAdapter<String> wardSpinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wardList);
        ArrayAdapter<String> wardTypeSpinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wardTypeList);

        wardSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        wardPicker.setAdapter(wardSpinnerAdapter);

        unitSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        unitPicker.setAdapter(unitSpinnerAdapter);

        wardTypeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wardTypePicker.setAdapter(wardTypeSpinnerAdapter);

        SharedPreferences mypref = getActivity().getSharedPreferences("user",MODE_PRIVATE);
        final String empid = mypref.getString("empId",null);
        final String deviceId = mypref.getString("deviceId",null);

        Button searchButton = (Button) getActivity().findViewById(R.id.bedStatusSearchButton);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ward = wardPicker.getSelectedItem().toString();
                unit = unitPicker.getSelectedItem().toString();
                wardType = wardTypePicker.getSelectedItem().toString();

                params = new JSONObject();
                try {
                    params.put("location", ward);
                    params.put("unit", unit);
                    params.put("empId",empid);
                    params.put("deviceId",deviceId);
                    params.put("wardType", wardType);

                    //System.out.println(deviceId);
                } catch (JSONException e) {
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

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {


        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
      //  Toast.makeText(context, "Yahan pe aa gye", Toast.LENGTH_LONG).show();
        client.post(this.getActivity(), ServerIPAddress.ip + "BedList/", entity ,"application/json", new AsyncHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);

                    if (obj.getString("status").equals("LOGOUT")) {
                        Toast.makeText(context, "Logged out", Toast.LENGTH_LONG).show();
                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    else if(obj.getString("status").equals("True"))
                        bedstatusDialog(obj);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }

            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e)  {

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
    public void bedstatusDialog(JSONObject res) {
        try {


            LayoutInflater inflator = (LayoutInflater) getActivity().getLayoutInflater();

            View dialogLayout = null;

            String resp_resident = res.getString("empname");
            int unit_threshold = res.getInt("unit-threshold");
            int unit_filled = res.getInt("unitfilled");

            if(wardType.equals("GENERAL")){
                dialogLayout = inflator.inflate(R.layout.general_bedstatus, null);
                TextView unitStatus = (TextView)dialogLayout.findViewById(R.id.unitStatus_general_value);
                TextView wardStatus = (TextView)dialogLayout.findViewById(R.id.wardStatus_general_value);
                TextView responsible = (TextView)dialogLayout.findViewById(R.id.responsible_general_value);

                int ward_total = res.getInt("wardTotal");
                int ward_filled = res.getInt("wardfilled");

                String unit = unit_threshold + "(Threshold), " + unit_filled + "(filled)";
                unitStatus.setText(unit);

                String ward = ward_total + "(Total), " + ward_filled + "(filled)";
                wardStatus.setText(ward);

                responsible.setText(resp_resident);

            }
            else{
                dialogLayout = inflator.inflate(R.layout.special_bedstatus, null);
                TextView unitStatus = (TextView)dialogLayout.findViewById(R.id.unitStatus_special_value);
                TextView responsible = (TextView)dialogLayout.findViewById(R.id.responsible_special_value);

                String unit = unit_threshold + "(Threshold), " + unit_filled + "(filled)";
                unitStatus.setText(unit);

                responsible.setText(resp_resident);
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Bed Availability");
            builder.setView(dialogLayout);


//
//            Resources r = getResources();
//
//            int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
//
//            RelativeLayout.LayoutParams params, params2, params3;
//            params = params2 = params3 = null;
//
//            params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//            //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//            params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//            params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
//
//            params2 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//            params3 = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
//            //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//
//
//            String resp_resident = res.getString("empname");
//            int unit_threshold = res.getInt("unit-threshold");
//            int unit_filled = res.getInt("unitfilled");
//
//            RelativeLayout rel, rel2;
//
//            rel = rel2 = null;
//
//
//            if(wardType.equals("GENERAL")) {
//
//
//                int ward_total = res.getInt("wardTotal");
//                int ward_filled = res.getInt("wardfilled");
//
//                rel = new RelativeLayout(context);
//
//                String unitStatus = "Unit" + unit + ": " + unit_threshold + "(Threshold), " + unit_filled + "(filled)";
//
//                TextView unitTextView = new TextView(context);
//                unitTextView.setText(unitStatus);
//                unitTextView.setId(R.id.unitStatus);
//                unitTextView.setPadding(5, 5, 5, 5);
//                unitTextView.setTextSize(20);
//                unitTextView.setGravity(Gravity.CENTER);
//                unitTextView.setBackgroundColor(Color.parseColor("#7FB3D5"));
//                unitTextView.setTextColor(Color.parseColor("#FFFFFF"));
//
//                //Adding Text View To Relative Layout
//                rel.addView(unitTextView);
//                unitTextView.setLayoutParams(params);
//
//                builder.setView(rel);
//
//                rel2 = new RelativeLayout(context);
//
//                params2.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//                params2.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//                params2.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
//                params2.addRule(RelativeLayout.BELOW, rel.getId());
//
//                String wardStatus = "Ward" + ward + ": " + ward_total + "(Total), " + ward_filled + "(filled)";
//
//                TextView wardTextView = new TextView(context);
//                wardTextView.setText(wardStatus);
//                wardTextView.setId(R.id.unitStatus);
//                wardTextView.setPadding(5, 5, 5, 5);
//                wardTextView.setTextSize(20);
//                wardTextView.setGravity(Gravity.CENTER);
//           //     wardTextView.setBackgroundColor(Color.parseColor("#7FB3D5"));
//                wardTextView.setTextColor(Color.parseColor("#FFFFFF"));
//
//                rel2.addView(wardTextView);
//                wardTextView.setLayoutParams(params);
//
//              //  rel2.setlayo
//                builder.setView(rel2);
//
//            }
//
//            else if(wardType.equals("SPECIAL")){
//
//                int totalBeds = res.getInt("total-beds");
//                int freebeds = res.getInt("freebeds");
//
//                rel = new RelativeLayout(context);
//
//                String unitStatus = "Unit" + unit + ": " + unit_threshold + "(Threshold), " + unit_filled + "(filled)";
//                TextView unitTextView = new TextView(context);
//                unitTextView.setText(unitStatus);
//                unitTextView.setId(R.id.unitStatus);
//                unitTextView.setPadding(5, 5, 5, 5);
//                unitTextView.setTextSize(20);
//                unitTextView.setGravity(Gravity.CENTER);
//         //       unitTextView.setBackgroundColor(Color.parseColor("#7FB3D5"));
//                unitTextView.setTextColor(Color.parseColor("#FFFFFF"));
//
//                rel.addView(unitTextView);
//                unitTextView.setLayoutParams(params);
//
//                builder.setView(rel);
//            }
//
//            RelativeLayout rel3 = new RelativeLayout(context);
//
//            String respResident = resp_resident;
//            TextView respResidentTextView = new TextView(context);
//            respResidentTextView.setText(respResident);
//            respResidentTextView.setId(R.id.respRes);
//            respResidentTextView.setPadding(5, 5, 5, 5);
//            respResidentTextView.setTextSize(20);
//            respResidentTextView.setGravity(Gravity.CENTER);
//      //      respResidentTextView.setBackgroundColor(Color.parseColor("#7FB3D5"));
//            respResidentTextView.setTextColor(Color.parseColor("#FFFFFF"));
//
//            rel3.addView(respResidentTextView);
//
//            params3.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//            params3.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//            params3.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
//
//            if(wardType.equals("GENERAL")){
//                params3.addRule(RelativeLayout.BELOW, rel2.getId());
//                respResidentTextView.setLayoutParams(params3);
//            }
//            else{
//                params3.addRule(RelativeLayout.BELOW, rel.getId());
//                respResidentTextView.setLayoutParams(params3);
//            }
//          //  respResidentTextView.setLayoutParams(params);
//
//            builder.setView(rel3);


            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialogOuter, int id) {
                            dialogOuter.dismiss();
                        }
                    });

            builder.setCancelable(false);

            final AlertDialog alert11 = builder.create();

            alert11.setCancelable(false);
            alert11.show();

        }
        catch(JSONException e){
            Toast.makeText(context, "Error in Alert Dialog", Toast.LENGTH_LONG).show();
        }
    }

}
