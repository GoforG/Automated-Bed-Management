package com.example.navigationdrawersample;



import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.SeekBar;
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
//import static android.content.Context.MODE_WORLD_READABLE;

public class ConsultantForm extends Fragment {

    public final static String EXTRA_MESSAGE = "com.project.samplelogin.MESSAGE";
    private TextView text2;
    Communicator comm;
    private TextView daystodischargefromhosp,daystodischargefromward,textdaystodischargefromward;
    private String response = "";
    private String patientWard, destination_wardType;
    Context context;
    private NumberPicker numberPicker,numberPicker1;
    private CheckBox checkBox1;
    private CheckBox checkBox2;
    private SeekBar seekBar;
    private JSONObject params;
    private Button submitbutton;

    String  transfer_to, patient_no,frommsg;
    int condition;
    boolean transfer_needed = false;
    int transfer_days , daysinhospital,daysinward,status;
    int isTransferChecked, isDischargeChecked;
    View rootview;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return  inflater.inflate(R.layout.consultant_form,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Checkup");
    }

    public void changeData(String s){
        response = s;
    }

    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context =this.getActivity();
        comm = (Communicator)getActivity();
        //Intent intent = getIntent();
        //response = intent.getStringExtra("HELLO");
        text2 = (TextView)getActivity().findViewById(R.id.bedId_value);
        checkBox1 = (CheckBox)getActivity().findViewById(R.id.checkBox1);
        checkBox2 = (CheckBox)getActivity().findViewById(R.id.checkBox2);
        seekBar = (SeekBar)getActivity().findViewById(R.id.seekBar);
        submitbutton=(Button)getActivity().findViewById(R.id.submitbutton);
        params = new JSONObject();

        SharedPreferences mypref = getActivity().getSharedPreferences("user",MODE_PRIVATE);

        try {
            params.put("empId",mypref.getString("empId",null));
            params.put("deviceId",mypref.getString("deviceId",null));
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                    int seekBarProgress = 0;
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        condition = progress;
                        if (progress <= 50) {
                            setProgressBarColor(seekBar, Color.rgb(255 - (255 / 100 * (100 - progress * 2)), 255, 0));
                        }
                        else {
                            setProgressBarColor(seekBar, Color.rgb(255, 255 - (255 / 100 * (progress - 50) * 2), 0));
                        }
                    }

                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    public void onStopTrackingTouch(SeekBar seekBar) {
                        Toast.makeText(context, "Condition: " + condition, Toast.LENGTH_SHORT).show();
                    }

        });

        checkBox1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox1.isChecked())
                    return;
                else if(checkBox2.isChecked())
                    checkBox2.setChecked(false);

                onTransfer();
            }
        });

        checkBox2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!checkBox2.isChecked())
                    return;
                else if(checkBox1.isChecked())
                    checkBox1.setChecked(false);
            }
        });

        submitbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    params.put("patientId", patient_no);
                    params.put("currCond",condition);
                    if(checkBox1.isChecked() == false && checkBox2.isChecked() == false){

                        boolean val = confirmDialog("submit");
                        if(val == true){
                            Toast.makeText(context,"Discharge days >> "+ daysinhospital, Toast.LENGTH_SHORT).show();
                            params.put("hospitaldischarge", daysinhospital);
                            params.put("Warddays",daysinward);
                            params.put("transferNeeded", false);
                            //              Toast.makeText(getApplicationContext(),"Clicked Yes", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //               Toast.makeText(getApplicationContext(),"Clicked No", Toast.LENGTH_SHORT).show();
                            return;
                        }

                    }

                    else if(checkBox1.isChecked() == false && checkBox2.isChecked() == true){
                        boolean val = confirmDialog("discharge");
                        if(val == true){
                            params.put("hospitaldischarge",0);
                            params.put("transferNeeded", true);
                            params.put("transferTo", "out");
                            params.put("daysInWardTransfer", 0); //to be thought about
                            params.put("Warddays", 0); //to be thought about, but most probably 0
                            //              Toast.makeText(getApplicationContext(),"Clicked Yes", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //                Toast.makeText(getApplicationContext(),"Clicked No", Toast.LENGTH_SHORT).show();
                            checkBox2.setChecked(false);
                            return;
                        }
                    }
                    else if(checkBox1.isChecked() == true && checkBox2.isChecked() == false) {
                        //            Toast.makeText(getApplicationContext(),"Transfer needed", Toast.LENGTH_SHORT).show();
                        Toast.makeText(context,"Discharge days >> "+ daysinhospital, Toast.LENGTH_SHORT).show();
                        boolean val = confirmDialog("transfer");

                        if(val == true){
                            if (transfer_needed){
                                //                    Toast.makeText(getApplicationContext(),"Transfer needed", Toast.LENGTH_SHORT).show();
                                params.put("transferNeeded", transfer_needed);
                                params.put("daysInWardTransfer", transfer_days);
                                params.put("transferTo", transfer_to);
                                params.put("hospitaldischarge", daysinhospital);
                                params.put("Warddays", daysinward ); //to be made zero in view
                                params.put("from_msg",frommsg);
                                params.put("ward_type", destination_wardType);
                                params.put("retainstatus", retainResp);

          //                      Toast.makeText(context,frommsg, Toast.LENGTH_SHORT).show();
                            }
                            else{
                                //                   Toast.makeText(getApplicationContext(),"No transfer needed", Toast.LENGTH_SHORT).show();
                                checkBox1.setChecked(false);
                                return;
                            }
                            //                  Toast.makeText(getApplicationContext(),"Clicked Yes", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            //                 Toast.makeText(getApplicationContext(),"Clicked No", Toast.LENGTH_SHORT).show();
                            checkBox1.setChecked(false);
                            return;
                        }

                    }

                }
                catch (JSONException e)
                {
                    Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                }
                try {
                    //         Toast.makeText(getApplicationContext(),"calling invokeWS", Toast.LENGTH_SHORT).show();
                    invokeWS(params);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });


        try {
            JSONObject obj = new JSONObject(response);
            String name = obj.getString("name");
            patient_no = obj.getString("patientId");
            text2.setText(name);
            patientWard = obj.getString("loc");
            //daysinhospital = obj.getInt("daysinhospital");

            if(obj.getString("daysinhospital") != null)
                daysinhospital = obj.getInt("daysinhospital");
            else
                daysinhospital = 0;

            String test = obj.getString("daysInWard");

            if(test.equals("null"))
            {
                daysinward = 0;

            }
            else
            {
                System.out.println(test);
                daysinward = obj.getInt("daysInWard");
            }

        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

        daystodischargefromhosp = (TextView) getActivity().findViewById(R.id.daystodichargefromhosp);
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
                                getValue(numberPicker.getValue());
                                System.out.println("Number picker " +numberPicker.getValue());
                                daystodischargefromhosp.setText(""+daysinhospital);
                                dialogOuter.dismiss();
                            }
                        })
                ;

                AlertDialog dialog = builder.create();
                dialog.setTitle("Expected Discharge From Hospital");
                dialog.setView(getLayoutInflater(savedInstanceState).inflate(R.layout.numberpicker,null));
                dialog.show();
                numberPicker = (NumberPicker)dialog.findViewById(R.id.numberPicker);
                numberPicker.setValue(daysinhospital);
                numberPicker.setMinValue(0);
                numberPicker.setMaxValue(50);
                numberPicker.setValue(daysinhospital);
                numberPicker.setOnValueChangedListener(( new NumberPicker.
                        OnValueChangeListener() {
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        getValue(newVal);
                    }
                }));

            }
        });

        daystodischargefromward = (TextView) getActivity().findViewById(R.id.daystodichargefromward);
        textdaystodischargefromward = (TextView)getActivity().findViewById(R.id.textdaystodischargefromward);

//        if(patientWard == "general") {
//            daystodischargefromward.setVisibility(View.INVISIBLE);
//            textdaystodischargefromward.setEnabled(false);//setVisibility(View.INVISIBLE);
//        }
   //     else {
        daystodischargefromward.setText("" + daysinward);
        daystodischargefromward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder
                        .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialogOuter, int id) {

                                //bhejna hai inwoke ws
                                //sendtoserver();
                                getValue1(numberPicker1.getValue());
          //                      System.out.println("Number picker " +numberPicker1.getValue());
                                daystodischargefromward.setText(""+daysinward);
                                dialogOuter.dismiss();
                            }
                        })
                ;

                AlertDialog dialog = builder.create();
                dialog.setTitle("Expected Discharge From ward");
                dialog.setView(getLayoutInflater(savedInstanceState).inflate(R.layout.numberpicker1, null));
                dialog.show();

                numberPicker1 = (NumberPicker) dialog.findViewById(R.id.numberPicker1);
                numberPicker1.setValue(daysinward);
                numberPicker1.setMinValue(0);
                numberPicker1.setMaxValue(50);
                numberPicker1.setValue(daysinward);
                numberPicker1.setOnValueChangedListener((new NumberPicker.
                        OnValueChangeListener() {
                    public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                        getValue1(newVal);
                    }
                }));

            }
        });
       // }

    }


    public void setProgressBarColor(ProgressBar progressBar, int newColor){

        LayerDrawable ld = (LayerDrawable) progressBar.getProgressDrawable();
        ClipDrawable d1 = (ClipDrawable) ld.findDrawableByLayerId(R.id.progressshape);
        d1.setColorFilter(newColor, PorterDuff.Mode.SRC_IN);

    }

    public boolean confirmDialog(String str){

        final boolean mResult[] = {false};
        final Handler handler = new Handler() {
            @Override
            public void handleMessage(Message mesg) {
                throw new RuntimeException();
            }
        };

        String show;
        // make a text input dialog and show it

        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Confirm");

        if(str.equals("submit")){

            alert.setMessage("Are you sure want to " + str + "?");
        }
        else if(str.equals("transfer")){
            alert.setMessage("Are you sure want to " + str + " the patient?");
        }
        else if(str.equals("discharge")){
            alert.setMessage("Are you sure want to " + str + " the patient?");
        }

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

    public void getValue(int val){
        //Toast.makeText(context,val, Toast.LENGTH_SHORT).show();

        daysinhospital = val;

    }
    public void getValue1(int val){
        //Toast.makeText(context,val, Toast.LENGTH_SHORT).show();

        daysinward = val;

    }

    public void onTransfer(){

        Fragment f = new PatientTransferByConsultant();
        f.setTargetFragment(this,1);
        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        //ft.replace(R.id.content_main, f,"patient_transfer");
        ft.add(R.id.content_main, f, "patient_transfer");
        ft.hide(ConsultantForm.this);
        ft.addToBackStack(ConsultantForm.class.getName());
        ft.commit();
        comm.respond(response,"patient_transfer",f);

    }
    String retainResp = null;
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
       //numberPicker.setValue(daysinhospital);
       if (requestCode == 1) {
            // Make sure the request was successful
            //          Toast.makeText(getApplicationContext(), "result code"+resultCode, Toast.LENGTH_LONG).show();
            if (resultCode == -1) {//resultCode == RESULT_OK

                String transferSelected = data.getStringExtra("TRANSFER?");
                if(transferSelected.equals("YES")){
                    //                 Toast.makeText(getApplicationContext(), "transfer: true ", Toast.LENGTH_LONG).show();
                    transfer_needed = true;
                    transfer_to = data.getStringExtra("TRANSFER TO");
                    transfer_days = data.getIntExtra("DAYS IN TRANSFER", -1);
                    frommsg = data.getStringExtra("from_msg");
                    destination_wardType = data.getStringExtra("Ward Type");
                    retainResp = data.getStringExtra("retainstatus");
                    //System.out.println("khbsdkd"+frommsg+"aur ye kaise "+transfer_to);
             //       Toast.makeText(context,frommsg, Toast.LENGTH_SHORT).show();
                }
                else {
                    //                Toast.makeText(getApplicationContext(), "transfer: false ", Toast.LENGTH_LONG).show();
                    checkBox1.setChecked(false);
                    transfer_needed = false;
                }

            }
        }

    }

    public void invokeWS(JSONObject params) throws UnsupportedEncodingException {

        StringEntity entity = new StringEntity(params.toString());
        AsyncHttpClient client = new AsyncHttpClient();

        client.post(context, ServerIPAddress.ip+"consultant_form/", entity, "application/json", new AsyncHttpResponseHandler() {
            // When the response returned by REST has Http response code '200'
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
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
                    else if(obj.getString("status").equals("ALREADY TRANSFER REQUEST")){
                        Toast.makeText(context, "Please clear previous transfer of the patient", Toast.LENGTH_LONG).show();
                    }
                    else if (obj.getString("status").equals("Success")) {
                 //       Toast.makeText(context, "Database Updated", Toast.LENGTH_LONG).show();

                        Intent i = getActivity().getBaseContext().getPackageManager()
                                .getLaunchIntentForPackage(getActivity().getBaseContext().getPackageName());
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                    else { //koi responsible nhi hai so error...
                        Toast.makeText(context, obj.getString("error_msg"), Toast.LENGTH_LONG).show();
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

   /* @Override
    public void onDestroyView()
    {
        if(rootview.getParent()!=null)
        {
            ((ViewGroup)rootview.getParent()).removeView(rootview);
        }
        super.onDestroyView();
    }
*/
}
