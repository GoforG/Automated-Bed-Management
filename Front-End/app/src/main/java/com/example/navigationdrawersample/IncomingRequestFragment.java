package com.example.navigationdrawersample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vivek Sharma on 02-Apr-17.
 */

public class IncomingRequestFragment extends Fragment {
    String response1;
    Context context;
    JSONObject param;
    Communicator comm;

    public IncomingRequestFragment() {

    }
    public void changeData(String s){
        response1 = s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_incoming_request, container, false);
        return rootView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        comm = (Communicator)getActivity();
        param = new JSONObject();

//        TextView resTextView = (TextView)getActivity().findViewById(R.id.unitIncomingRequest);
//        TextView wardTextView = (TextView)getActivity().findViewById(R.id.wardIncomingRequest);
//        TextView patTextView = (TextView)getActivity().findViewById(R.id.patIncomingRequest);
//
//        resTextView.setPaintFlags(resTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        wardTextView.setPaintFlags(wardTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        patTextView.setPaintFlags(patTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);



  //      System.out.println("debskz"+response1);
        try {
            JSONObject jObj = new JSONObject(response1);
            if(jObj.getString("incoming_table") == null){return;}
            JSONArray table = new JSONArray(jObj.getString("incoming_table"));

            LinearLayout base = (LinearLayout)getActivity().findViewById(R.id.incoming_request_parent);

            RelativeLayout headerRel = new RelativeLayout(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            headerRel.setBackgroundColor(Color.DKGRAY);

            TextView unit_tv = new TextView(context);
            unit_tv.setText("Resident");
            unit_tv.setTextSize(20);
            unit_tv.setTextColor(Color.WHITE);

            RelativeLayout.LayoutParams doneLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            unit_tv.setLayoutParams(doneLayoutParams);
            doneLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            headerRel.addView(unit_tv, doneLayoutParams);

            TextView ward_tv = new TextView(context);
            ward_tv.setText("Ward");
            ward_tv.setTextSize(20);
            ward_tv.setTextColor(Color.WHITE);

            RelativeLayout.LayoutParams lpp = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            ward_tv.setLayoutParams(lpp);
            lpp.addRule(RelativeLayout.CENTER_IN_PARENT);
            headerRel.addView(ward_tv, lpp);

            TextView patient_tv = new TextView(context);
            patient_tv.setText("Patient");
            patient_tv.setTextSize(20);
            patient_tv.setTextColor(Color.WHITE);

            RelativeLayout.LayoutParams saveLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            patient_tv.setLayoutParams(saveLayoutParams);
            saveLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            headerRel.addView(patient_tv, saveLayoutParams);

            base.addView(headerRel);

            for (int i = 0; i < table.length(); i++) { // Walk through the Array.
            //    System.out.println("hum yahan hain");
                final JSONObject obj = table.getJSONObject(i);

                RelativeLayout rel = new RelativeLayout(context);
                rel.setBackgroundColor(Color.parseColor("#7FB3D5"));

                Resources r = getResources();
                //int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, , r.getDisplayMetrics());
                //      int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());

                RelativeLayout.LayoutParams lpa = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

                String residentId = obj.getString("from_resident");
                String ward = obj.getString("fromWard");
                String patientId = obj.getString("patientId");
              //  String info = residentId+"         "+ward+"         "+patientId;

                TextView unit_msg = new TextView(context);
                unit_msg.setText(residentId);
                unit_msg.setTextSize(20);
                unit_msg.setTextColor(Color.WHITE);
                //  unit_tv.setLayoutParams(unitparams);
                //   headerRel.addView(unit_tv);

                RelativeLayout.LayoutParams unit_msg_params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                unit_msg.setLayoutParams(unit_msg_params);
                unit_msg_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rel.addView(unit_msg, unit_msg_params);

                TextView ward_msg = new TextView(context);
                ward_msg.setText(ward);
                ward_msg.setTextSize(20);
                ward_msg.setTextColor(Color.WHITE);

                RelativeLayout.LayoutParams lppa = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                ward_msg.setLayoutParams(lppa);
                lppa.addRule(RelativeLayout.CENTER_IN_PARENT);
                rel.addView(ward_msg, lppa);

                TextView patient_msg = new TextView(context);
                patient_msg.setText(patientId);
                patient_msg.setTextSize(20);
                patient_msg.setTextColor(Color.WHITE);

                RelativeLayout.LayoutParams pat_params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                patient_msg.setLayoutParams(pat_params);
                pat_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rel.addView(patient_msg, pat_params);

                final String response = obj.toString();


                rel.setOnClickListener(new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {

                        Toast.makeText(context, "On Click Listener 1", Toast.LENGTH_LONG).show();

                        try {
                            Fragment f = new itemIncomingRequestFragment();
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main, f,"itemincomingrequestfrag");
                            ft.addToBackStack(null);
                            ft.commit();
                            comm.respond(response,"itemincomingrequestfrag",f);
                            ((HomePage)getActivity()).showFragment(f,true);
                            Toast.makeText(context, "doing transfer", Toast.LENGTH_LONG).show();
                        }
                        catch(Exception e){

                        }
                    }
                });
                base.addView(rel);
                //       prev = rel;
            }
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            Toast.makeText(context, "Nurse Table Incoming Exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }

}
