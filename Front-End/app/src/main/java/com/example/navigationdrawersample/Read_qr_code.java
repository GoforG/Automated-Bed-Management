package com.example.navigationdrawersample;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

import static android.content.Context.MODE_PRIVATE;
//import static android.content.Context.MODE_WORLD_READABLE;

public class Read_qr_code extends Fragment {
    //ProgressDialog prgDialog;
    public final static String EXTRA_MESSAGE = "com.project.samplelogin.MESSAGE";
    private Button buttonScan,searchButton;
    private EditText editSearch;
    private TextView textViewName, textViewAddress;
    private Context context;
    private IntentIntegrator qrScan;
    Communicator comm;
    String employeeId,deviceId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator)getActivity();
        context = this.getActivity();
        SharedPreferences mypref = context.getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
        //searchButton = (Button)getActivity().findViewById(R.id.searchButton);
        //editSearch = (EditText)getActivity().findViewById(R.id.editSearch);
        /*searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bid = editSearch.getText().toString();
                JSONObject params = new JSONObject();
                try {
                    params.put("bed-id", bid);
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
        });*/
        qrScan = new IntentIntegrator(this.getActivity());
        qrScan.initiateScan();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this.getActivity(), "Result Not Found", Toast.LENGTH_LONG).show();
            }
            else {
                String bedId = "";
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    bedId = obj.getString("bed-id");
                    Toast.makeText(this.getActivity(), bedId, Toast.LENGTH_LONG).show();
                }
                catch(JSONException e){
                    Toast.makeText(this.getActivity(), "Please Scan correctly", Toast.LENGTH_LONG).show();
                }
                JSONObject params = new JSONObject();
                try {
                    params.put("bed-id", bedId);
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
        } else {
            Toast.makeText(this.getActivity(), "Please Scan correctly", Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {


        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();
        Toast.makeText(context, "Yahan pe aa gye", Toast.LENGTH_LONG).show();
        client.post(this.getActivity(), ServerIPAddress.ip+"qr_code/",entity ,"application/json",new AsyncHttpResponseHandler() {

            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String response = new String(responseBody);
                //prgDialog.hide();
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
                    else if (obj.getString("status").equals("A")) {
                        Fragment f = new ConsultantForm();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        ft.replace(R.id.content_main, f,"consultantformtag");
                        ft.addToBackStack(null);
                        ft.commit();
                        comm.respond(response,"consultantformtag",f);
                        ((HomePage)getActivity()).showFragment(f,true);
                    }
                    else {
                        Toast.makeText(context, "Sorry bed is not yet allocated", Toast.LENGTH_LONG).show();
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

