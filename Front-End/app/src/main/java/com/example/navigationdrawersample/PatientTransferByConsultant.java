package com.example.navigationdrawersample;

/**
 * Created by Tanish on 14-02-2017.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * Created by Tanish on 31-01-2017.
 */

public class PatientTransferByConsultant extends Fragment {

    int newDate;
 //   String unitlist[] = {"1","2","3", "4", "5", "6"};
    String[] wardList = {"a", "bd", "bs", "dw", "fsr", "fsw", "hiw", "icu", "msr", "msw", "pw", "rw", "sw", "sdw"};
   // String[] wardList = {"Recovery", "ICU", "Male Surgery", "Female Surgery", "Pediatric Neurosurgery", "B-Block", "Head Injury", "Step Down"};
    String[] wardTypeList = {"GENERAL", "SPECIAL"};
    String patientWard, patientSex;
    String response;
    EditText frommsg;
    CheckBox retainCheckBox;
    String retainResp = "NO";

    Context context;
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.transfer_prompt,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Transfer");
    }

    public void changeData(String s){
        response = s;
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();

        retainCheckBox = (CheckBox)getActivity().findViewById(R.id.retain_checkbox);

        Button b1 = (Button)getActivity().findViewById(R.id.confirm);
        Button b2 = (Button)getActivity().findViewById(R.id.cancel);
        final NumberPicker np = (NumberPicker)getActivity().findViewById(R.id.numberPicker1);
        final Spinner spinner = (Spinner)getActivity().findViewById(R.id.spinner1);
        frommsg = (EditText)getActivity().findViewById(R.id.frommsg);
        final Spinner wardTypeSpin = (Spinner)getActivity().findViewById(R.id.checkup_wardTypeSpinner);

        final ArrayAdapter<String> spinnerArrayAdapter, wardTypeAdapter;
        try {
            JSONObject obj = new JSONObject(response);
            patientWard = obj.getString("loc");
            patientSex = obj.getString("sex");
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Error Occured [Server's JSON response might be invalid]!", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

        ArrayList<String> updatedWardList = new ArrayList<String>();

        int i;

        for(i = 0; i < wardList.length; i++){
   //         if(!wardList[i].equals(patientWard)){
                String split[] = wardList[i].split(" ");

                if(split.length == 1) {
                    updatedWardList.add(wardList[i]);
                }
                else{
                    if(split[0].equals("Male") && patientSex.equals("M")){
                        updatedWardList.add(wardList[i]);
                    }
                    else if(split[0].equals("Female") && patientSex.equals("F")){
                        updatedWardList.add(wardList[i]);
                    }
                    else if(!split[0].equals("Male") && !split[0].equals("Female")){
                        updatedWardList.add(wardList[i]);
                    }
                }
     //       }
         //   if(patientSex.equals("M") && )
        }

        retainCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(retainCheckBox.isChecked()){
                    retainResp = "YES";
                }
                else{
                    retainResp = "NO";
                    return;
                }
            }
        });

        String newWardList[] = updatedWardList.toArray(new String[updatedWardList.size()]);
        spinnerArrayAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, newWardList );
        wardTypeAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, wardTypeList);


        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        wardTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerArrayAdapter);
        wardTypeSpin.setAdapter(wardTypeAdapter);

        np.setMaxValue(50);
        np.setMinValue(0);
        np.setValue(5);

        //np.setWrapSelectorWheel(false);
        np.setOnValueChangedListener(( new NumberPicker.
                OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                getValue(newVal);
            }
        }));
        b1.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                //             Toast.makeText(getApplicationContext(), "OK BTN", Toast.LENGTH_LONG).show();
                String abc = String.valueOf(spinner.getSelectedItem());
                String wardType = String.valueOf(wardTypeSpin.getSelectedItem());

                Intent it = new Intent();
                it.putExtra("TRANSFER?","YES");
                it.putExtra("TRANSFER TO",abc);
                it.putExtra("DAYS IN TRANSFER",newDate);
                it.putExtra("from_msg",frommsg.getText().toString());
                it.putExtra("Ward Type", wardType);
                it.putExtra("retainstatus", retainResp);
                //System.out.println("jdhbkjc "+frommsg.getText());
                getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,it);
                getFragmentManager().popBackStack();
            }
        });

        b2.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                Intent it = new Intent();
                it.putExtra("TRANSFER?","NO");
                getTargetFragment().onActivityResult(getTargetRequestCode(),Activity.RESULT_OK,it);
                getFragmentManager().popBackStack();
            }
        });
    }
    public void getValue(int val){
        newDate = val;
    }
}
