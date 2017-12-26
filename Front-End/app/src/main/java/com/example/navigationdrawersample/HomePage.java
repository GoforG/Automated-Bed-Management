package com.example.navigationdrawersample;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Stack;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

public class HomePage extends AppCompatActivity
        implements Communicator,NavigationView.OnNavigationItemSelectedListener {

    ProgressDialog prgDialog;
    Communicator comm;
    String response;
    Fragment fragment = null;
    TextView empName,empId;
    String employeeId;
    String deviceId;
    Stack<Fragment> fragmentStack;
    Spinner spinner;
    ArrayAdapter<String> spinnerArrayAdapter;
    String[] spinnerOptions = {"Recovery", "ICU", "Male Surgery", "Female Surgery", "Ped. Neurosurgery", "B-Block", "Head Injury", "Step Down"};
    String responsible = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fragmentStack = new Stack<>();
        comm = (Communicator)this;
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //empName = (TextView)findViewById(R.id.empName);
        empId = (TextView)findViewById(R.id.empId);

        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
        employeeId = mypref.getString("empId",null);
        deviceId = mypref.getString("deviceId",null);
        String employeeName = mypref.getString("empName",null);
        responsible = mypref.getString("responsible", null);
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View hView =  navigationView.getHeaderView(0);
        empId = (TextView)hView.findViewById(R.id.empId);
        empId.setText(employeeId);


        empName = (TextView)hView.findViewById(R.id.empName);
        empName.setText(employeeName);

        navigationView.setNavigationItemSelectedListener(this);

        //responsible = getIntent().getStringExtra("responsible");

        String menuFragment = getIntent().getStringExtra("openfrag"); // for notification
        spinnerArrayAdapter = new ArrayAdapter<String>(getApplicationContext(),   android.R.layout.simple_spinner_item, spinnerOptions);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view

        // If menuFragment is defined, then this activity was launched with a fragment selection
        if (menuFragment != null) {
            // Here we can decide what do to -- perhaps load other parameters from the intent extras such as IDs, etc
            if (menuFragment.equals("openrequests")) {
                displaySelectedScreen(R.id.nav_menu3);
            }
        }
        else {
            displaySelectedScreen(R.id.nav_menu2);
        }
    }



    @Override
    public void onBackPressed() {

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }
        else {
            fragmentStack.pop();

            if (fragmentStack.size() == 0) {
                super.onBackPressed();
            }
            else {
                showFragment(fragmentStack.lastElement(), false);
            }
        }
    }

    public void showFragment(Fragment fragment, boolean addToStack) {
        if (addToStack){
             fragmentStack.push(fragment);
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        JSONObject params = new JSONObject();

        try {
            params.put("empId", employeeId);
            params.put("deviceId", deviceId);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        //noinspection SimplifiableIfStatement
        if (id == R.id.logout_button) {
            prgDialog = new ProgressDialog(this);
            // Set Progress Dialog Text
            prgDialog.setMessage("Please wait...");
            // Set Cancelable as False
            prgDialog.setCancelable(false);

            try {

                invokeWS(params);
            }
            catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return true;
        }
        else if(id == R.id.responsible_button){
            prgDialog = new ProgressDialog(this);
            // Set Progress Dialog Text
            prgDialog.setMessage("Please wait...");
            // Set Cancelable as False
            prgDialog.setCancelable(false);

           try{
               inwokeWSResponsible(params);
           }
           catch (UnsupportedEncodingException e) {
               e.printStackTrace();
           }
            return true;
        }
        else if(id == R.id.change_Ward){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder
                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialogOuter, int id) {
                            String changeToWard = String.valueOf(spinner.getSelectedItem());
                            prgDialog = new ProgressDialog(getApplicationContext());
                            // Set Progress Dialog Text
                            prgDialog.setMessage("Please wait...");
                            // Set Cancelable as False
                            prgDialog.setCancelable(false);
                            JSONObject params1 = new JSONObject();
                            try {
                                params1.put("empId", employeeId);
                                params1.put("deviceId", deviceId);
                                params1.put("changeWardTo",changeToWard);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try{
                                inwokeWSChangeWard(params1);
                            }
                            catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }
                    });

            AlertDialog dialog = builder.create();
            dialog.setTitle("Change your ward");
            dialog.setView(getLayoutInflater().inflate(R.layout.spinner1,null));
            dialog.show();

            spinner = (Spinner) dialog.findViewById(R.id.spinnerForWard);
            spinner.setAdapter(spinnerArrayAdapter);
        }

        return super.onOptionsItemSelected(item);
    }


    private void displaySelectedScreen(int itemId) {

        String str="";
        //creating fragment object
        //initializing the fragment object which is selected
        switch (itemId) {

            case R.id.nav_menu1:
                if(responsible.equals("NO"))
                {
                    Toast.makeText(getApplicationContext(), "Sorry, only responsible resident can do admissions ", Toast.LENGTH_LONG).show();
                    return;
                }
                fragment = new ResidentEntry();
                str = "residententryfrag";
                break;

            case R.id.nav_menu2:
                fragment = new ConsultantHomePage();
                str = "consultanthomefrag";
                break;

            // For requests display
            case R.id.nav_menu3:
                prgDialog = new ProgressDialog(this);
                // Set Progress Dialog Text
                prgDialog.setMessage("Please wait...");
                // Set Cancelable as False
                prgDialog.setCancelable(false);

                JSONObject paramsRequests = new JSONObject();

                try {
                    paramsRequests.put("deviceId", deviceId);
                    paramsRequests.put("empId", employeeId);

                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
                try {

                    inwokeWSRequests(paramsRequests);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                fragment = new RequestFragment();

              //  Toast.makeText(getApplicationContext(), "yahan aa gye hum", Toast.LENGTH_LONG).show();
              //  System.out.println("hello hum yahan tum kahan?");
                str = "requestfrag";
                break;

            case R.id.bedstatus_menu:
                fragment = new BedStatus();
                str = "bedstatusfrag";
                break;

            //For displaying all transfer requests
            case R.id.nav_menu4:
                prgDialog = new ProgressDialog(this);
                // Set Progress Dialog Text
                prgDialog.setMessage("Please wait...");
                // Set Cancelable as False
                prgDialog.setCancelable(false);

                JSONObject paramsNurse = new JSONObject();

                try {
                    paramsNurse.put("empId", employeeId);
                    paramsNurse.put("deviceId", deviceId);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                try {

                    inwokeWSNurse(paramsNurse);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                //if(fragment instanceof NurseActivity)
                fragment = new NurseActivity();
                str = "transferfrag";
                break;

            //for logout
            case R.id.nav_menu5:
                prgDialog = new ProgressDialog(this);
                // Set Progress Dialog Text
                prgDialog.setMessage("Please wait...");
                // Set Cancelable as False
                prgDialog.setCancelable(false);

                JSONObject params = new JSONObject();

                try {
                    params.put("empId", employeeId);
                    params.put("deviceId", deviceId);
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
                str = "logoutfrag";

        }

        //replacing the fragment
        if (fragment != null && !str.equals("transferfrag") && !str.equals("logoutfrag") && !str.equals("requestfrag")) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment,str);
            ft.commit();
            showFragment(fragment,true);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        Fragment f = getSupportFragmentManager().findFragmentByTag("qractivity");
        f.onActivityResult(requestCode,resultCode,data);
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        displaySelectedScreen(id);

        return true;
    }


    public void respond(String data, String tag, Fragment frag){

        if(tag.equals("consultantformtag")) {
            ConsultantForm f = (ConsultantForm)frag;
            f.changeData(data);
        }

        else if(tag.equals("patient_transfer")){
            PatientTransferByConsultant f = (PatientTransferByConsultant)frag;
            f.changeData(data);
        }

        else if(tag.equals("residentformtag")){
            ResidentForm f = (ResidentForm)frag;
            f.changeData(data);
        }

        else if(tag.equals("nurseinfotag")){
            NurseInfo f = (NurseInfo)frag;
            f.changeData(data);
        }

        else if(tag.equals("calendartag")){
            Calendarmain f = (Calendarmain)frag;
            f.changeData(data);
        }

        else if(tag.equals("nurseactivitytag")){
            NurseActivity f = (NurseActivity)frag;
            f.changeData(data);
            //System.out.println(data);
        }

        else if(tag.equals("requestfragtag")){
            RequestFragment f = (RequestFragment) frag;
            f.changeData(data);
            //System.out.println(data);
        }

        else if(tag.equals("itemincomingrequestfrag")){
            itemIncomingRequestFragment f = (itemIncomingRequestFragment) frag;
            f.changeData(data);
            //System.out.println(data);
        }

        else if(tag.equals("itempendingrequestfrag")){
            itemPendingRequestFragment f = (itemPendingRequestFragment) frag;
            f.changeData(data);
            //System.out.println(data);
        }

        else if(tag.equals("itemborrowingrequestfrag")){
            itemBorrowingRequestFragment f = (itemBorrowingRequestFragment) frag;
            f.changeData(data);
            //System.out.println(data);
        }

        else if(tag.equals("incomingrequestfrag")) {
            IncomingRequestFragment f = (IncomingRequestFragment) frag;
            f.changeData(data);
        }

        else if(tag.equals("pendingrequestfrag")){
            PendingRequestFragment f = (PendingRequestFragment) frag;
            f.changeData(data);
        }

        else if(tag.equals("borrowingrequestfrag")){
            BorrowingRequestFragment f = (BorrowingRequestFragment) frag;
            f.changeData(data);
        }

    }


    public void open_qr_reader(View v) {
        Fragment f = new Read_qr_code();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.content_main, f,"qractivity");
        //ft.addToBackStack(null);
        showFragment(fragment,true);
        ft.commit();
    }


    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"logout/", entity, "application/json", new AsyncHttpResponseHandler() {
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
                        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    // Else display error message
                    else  {
                        Toast.makeText(getApplicationContext(), "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        SharedPreferences mypref = getSharedPreferences("user",MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void inwokeWSNurse(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        //prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"nurselogin/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                prgDialog.hide();
                // Hide Progress Dialog
                //  byte[] bytes = {...}
                response = new String(responseBody);
                //Log.e("hello fcyhjb ",response);
                Fragment testFragment =  getSupportFragmentManager().findFragmentByTag("transferfrag");
                if (testFragment != null && testFragment.isVisible()) { //if fragment is same then don't replace #willHavetoChange
                    return;
                }
                else {
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment, "transferfrag");
                    //System.out.println("Arre yaar");
                    ft.commit();
                    comm.respond(response, "nurseactivitytag", fragment);
                    showFragment(fragment,true);
                }

                //prgDialog.hide();
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void inwokeWSResponsible(JSONObject params) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip + "make_responsible/", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                prgDialog.hide();
                // Hide Progress Dialog
                //  byte[] bytes = {...}
                response = new String(responseBody);
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("LOGOUT")) {
                        SharedPreferences mypref = getSharedPreferences("user", MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "You are successfully logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "You are successfully made responsible", Toast.LENGTH_LONG).show();
                    }
                }
                    catch (JSONException e) {
                        // TODO Auto-generated catch block
                        Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                        e.printStackTrace();

                    }

                //prgDialog.hide();
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void inwokeWSChangeWard(JSONObject params) throws UnsupportedEncodingException {
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"change_ward/", entity, "application/json", new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                prgDialog.hide();
                // Hide Progress Dialog
                //  byte[] bytes = {...}
                response = new String(responseBody);
                try {
                    // JSON Object
                    JSONObject obj = new JSONObject(response);
                    // When the JSON response has status boolean value assigned with true
                    if (obj.getString("status").equals("LOGOUT")) {
                        SharedPreferences mypref = getSharedPreferences("user", MODE_PRIVATE);
                        mypref.edit().clear().commit();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        //Toast.makeText(getApplicationContext(), "Logged out", Toast.LENGTH_LONG).show();
                        startActivity(i);
                        Toast.makeText(getApplicationContext(), "Sorry you are logged out!", Toast.LENGTH_LONG).show();
                        // Navigate to Home screen
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Your ward has been successfully changed", Toast.LENGTH_LONG).show();
                    }
                }
                catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void inwokeWSRequests(JSONObject params) throws UnsupportedEncodingException {
        // Show Progress Dialog
        prgDialog.show();
        StringEntity entity = new StringEntity(params.toString());
        // Make RESTful webservice call using AsyncHttpClient object
        AsyncHttpClient client = new AsyncHttpClient();
        client.post(getApplicationContext(), ServerIPAddress.ip+"request_display/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                prgDialog.hide();
                // Hide Progress Dialog
                //  byte[] bytes = {...}
                response = new String(responseBody);
                try {
                    JSONObject obj = new JSONObject(response);

                    if (obj.getBoolean("status")==false) {
                        Toast.makeText(getApplicationContext(), "Sorry you are logged out", Toast.LENGTH_LONG).show();
                        Intent i = getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                }catch (JSONException e) {
                    // TODO Auto-generated catch block
                    Toast.makeText(getApplicationContext(), "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();

                }
                //Log.e("hello fcyhjb ",response);
                Fragment testFragment =  getSupportFragmentManager().findFragmentByTag("requestfrag");
                if (testFragment != null && testFragment.isVisible()) { //if fragment is same then don't replace #willHavetoChange
                    return;
                }
                else {
                    Toast.makeText(getApplicationContext(), "arre arre yaar", Toast.LENGTH_LONG).show();
                    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                    ft.replace(R.id.content_main, fragment, "requestfragtag");
                    Toast.makeText(getApplicationContext(), "arre yaar", Toast.LENGTH_LONG).show();
                    ft.commit();
                    comm.respond(response, "requestfragtag", fragment);
                    showFragment(fragment,true);
                }
                //prgDialog.hide();
            }


            // When the response returned by REST has Http response code other than '200'
            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] errorResponse, Throwable e) {
                // Hide Progress Dialog
                prgDialog.hide();
                // When Http response code is '404'
                if (statusCode == 404) {
                    Toast.makeText(getApplicationContext(), "Requested resource not found", Toast.LENGTH_LONG).show();
                }
                // When Http response code is '500'
                else if (statusCode == 500) {
                    Toast.makeText(getApplicationContext(), "Something went wrong at server end", Toast.LENGTH_LONG).show();
                }
                // When Http response code other than 404, 500
                else {
                    Toast.makeText(getApplicationContext(), "Unexpected Error occcured! [Most common Error: Device might not be connected to Internet or remote server is not up and running]", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
