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

public class PendingRequestFragment extends Fragment {

    String response1;
    Context context;
    JSONObject param;
    Communicator comm;

    public PendingRequestFragment() {
        // Required empty public constructor
    }
    public void changeData(String s){
        response1 = s;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_pending_request, container, false);
        //   View rootView = inflater.inflate(R.layout.fragment_pending_request, container, false);
        // Toast.makeText(getContext(), "Top Rated Fragment", Toast.LENGTH_LONG).show();
        return rootView;
    }

    public  void onResume(){
        super.onResume();
        System.out.println("fragment resumed............");
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator)getActivity();
        context = this.getActivity();
     //   Toast.makeText(getContext(), "aur bhai", Toast.LENGTH_LONG).show();
        param = new JSONObject();

//        TextView unitTextView = (TextView)getActivity().findViewById(R.id.unitPendingRequest);
//        TextView wardTextView = (TextView)getActivity().findViewById(R.id.wardPendingRequest);
//        TextView patTextView = (TextView)getActivity().findViewById(R.id.patPendingRequest);
//
//        unitTextView.setPaintFlags(unitTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        wardTextView.setPaintFlags(wardTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        patTextView.setPaintFlags(patTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

     //   System.out.println("debskz"+response1);
        try {
            JSONObject jObj = new JSONObject(response1);
            JSONArray table = new JSONArray(jObj.getString("pending_table"));
            String res_resident = jObj.getString("ResEmp");

            LinearLayout base = (LinearLayout)getActivity().findViewById(R.id.pending_request_parent);

            RelativeLayout headerRel = new RelativeLayout(context);
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            headerRel.setBackgroundColor(Color.DKGRAY);

//            RelativeLayout.LayoutParams unitparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            unitparams.addRule(RelativeLayout.ALIGN_LEFT, RelativeLayout.TRUE);
//            unitparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//            unitparams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);


            TextView unit_tv = new TextView(context);
            unit_tv.setText("Unit");
            unit_tv.setTextSize(20);
            unit_tv.setTextColor(Color.WHITE);
          //  unit_tv.setLayoutParams(unitparams);
         //   headerRel.addView(unit_tv);

            RelativeLayout.LayoutParams doneLayoutParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            unit_tv.setLayoutParams(doneLayoutParams);
            doneLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            headerRel.addView(unit_tv, doneLayoutParams);

//            RelativeLayout.LayoutParams wardparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//            //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//            wardparams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//            wardparams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
//            wardparams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);

            TextView ward_tv = new TextView(context);
            ward_tv.setText("Ward");
            ward_tv.setTextSize(20);
            ward_tv.setTextColor(Color.WHITE);
           // ward_tv.setLayoutParams(wardparams);
         //   headerRel.addView(ward_tv);

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


            for (int i = 0; i < table.length(); i++){
                //System.out.println("hum yahan hain");
                final JSONObject obj = table.getJSONObject(i);

                RelativeLayout rel = new RelativeLayout(context);

                Resources r = getResources();
                //int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, , r.getDisplayMetrics());
          //      int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());

                RelativeLayout.LayoutParams lpa = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            //    headerRel.setBackgroundColor(Color.DKGRAY);
                lpa.setMargins(0, 20, 0, 0);
                rel.setLayoutParams(lpa);
//
//                LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(new LinearLayout.LayoutParams(
//                                LinearLayout.LayoutParams.MATCH_PARENT,
//                                LinearLayout.LayoutParams.WRAP_CONTENT));
//                linearParams.setMargins(0, 80, 0, 0);
//                rel.setLayoutParams(linearParams);

                String tounit = obj.getString("toUnit");
                String ward = obj.getString("toWard");
                String patientId = obj.getString("patientId");
                String resident = obj.getString("from_resident");

            //    String info = tounit +"           "+ward+"           "+patientId;
                //System.out.println("patientId: "+ patientId);

                TextView unit_msg = new TextView(context);
                unit_msg.setText(tounit);
                unit_msg.setTextSize(20);
                unit_msg.setTextColor(Color.WHITE);
                //  unit_tv.setLayoutParams(unitparams);
                //   headerRel.addView(unit_tv);

                RelativeLayout.LayoutParams unit_msg_params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                unit_msg.setLayoutParams(doneLayoutParams);
                unit_msg_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rel.addView(unit_msg, unit_msg_params);

//                TextView pId = new TextView(context);
//                pId.setText(info);
//                pId.setId(R.id.transferlist3);
//                pId.setPadding(5, 5, 5, 5);
//                pId.setTextSize(20);
//                pId.setGravity(Gravity.CENTER);

                if(res_resident.equals(resident)) {
                    rel.setBackgroundColor(Color.parseColor("#7FB3D5"));
                }
                else{
                    rel.setBackgroundColor(Color.parseColor("#85929E"));
                }
            //   unit_msg.setTextColor(Color.parseColor("#FFFFFF"));
            //    pId.setBackgroundColor(Color.parseColor("#7FB3D5"));
           //     pId.setTextColor(Color.parseColor("#FFFFFF"));

//                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT);
//                //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
//                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
//                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
//                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);


                //Adding Text View To Relative Layout

                TextView ward_msg = new TextView(context);
                ward_msg.setText(ward);
                ward_msg.setTextSize(20);
                ward_msg.setTextColor(Color.WHITE);
                // ward_tv.setLayoutParams(wardparams);
                //   headerRel.addView(ward_tv);

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

             //   rel.addView(pId);
          //      pId.setLayoutParams(params);
                final String response = obj.toString();

                rel.setOnClickListener(new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {
                        Toast.makeText(context, "On Click Listener 1", Toast.LENGTH_LONG).show();
                        try {
                            Fragment f = new itemPendingRequestFragment();
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main,f,"itempendingrequestfrag");
                            ft.addToBackStack(null);
                            ft.commit();
                            comm.respond(response,"itempendingrequestfrag",f);
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
            Toast.makeText(context, "Nurse Table Pending Exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }
}
