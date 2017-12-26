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
import static android.content.Context.MODE_WORLD_READABLE;

/**
 * Created by Tanish on 11-02-2017.
 */

public class ConsultantHomePage extends Fragment {

    private Button buttonScan,searchButton;
    private EditText editSearch;
    private Context context;
    Communicator comm;
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.consultant_home_page,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Checkup");
    }
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        comm = (Communicator)getActivity();
        searchButton = (Button) getActivity().findViewById(R.id.searchButton);
        editSearch = (EditText)getActivity().findViewById(R.id.editSearch);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String bid = editSearch.getText().toString();
                JSONObject params = new JSONObject();
                SharedPreferences mypref = getActivity().getSharedPreferences("user",MODE_PRIVATE);

                try {
                    params.put("empId",mypref.getString("empId",null));
                    params.put("deviceId",mypref.getString("deviceId",null));
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    params.put("bed-id", bid);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    invokeWS(params);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {


        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(this.getActivity(), ServerIPAddress.ip + "bedSearch_ByConsultant/", entity ,"application/json",new AsyncHttpResponseHandler() {

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
                    else if(obj.getString("status").equals("NOBEDEXISTS")){
                        Toast.makeText(context, "Invalid Bed ID", Toast.LENGTH_LONG).show();
                    }
                    else if(obj.getString("status").equals("WRONGWARD")){
                        Toast.makeText(context, "This bed is not in your ward", Toast.LENGTH_LONG).show();
                    }
                    else if(obj.getString("status").equals("WRONGUNIT")){
                        Toast.makeText(context, "This bed is not in your unit", Toast.LENGTH_LONG).show();
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
