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

public class BorrowingRequestFragment extends Fragment {
    String response1;
    Context context;
    JSONObject param;
    Communicator comm;

    public BorrowingRequestFragment() {
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
        View rootView = inflater.inflate(R.layout.fragment_borrowing_request, container, false);
        return rootView;
    }
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        comm = (Communicator)getActivity();
        context = this.getActivity();
        Toast.makeText(getContext(), "aur bhai", Toast.LENGTH_LONG).show();
        param = new JSONObject();

//        TextView fromUnitTextView = (TextView)getActivity().findViewById(R.id.fromUnitBorrowingRequest);
//        TextView timeTextView = (TextView)getActivity().findViewById(R.id.timeBorrowingRequest);
//        TextView toUnitTextView = (TextView)getActivity().findViewById(R.id.toUnitBorrowingRequest);
//
//        fromUnitTextView.setPaintFlags(fromUnitTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        timeTextView.setPaintFlags(timeTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
//        toUnitTextView.setPaintFlags(toUnitTextView.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

        //Intent intent = getActivity().getIntent();
        //response = intent.getStringExtra("nurseIntent");
     //   System.out.println("debskz"+response1);

        try {
            JSONObject jObj = new JSONObject(response1);
            if(jObj.getString("borrowing_table") == null){return;}
            JSONArray table = new JSONArray(jObj.getString("borrowing_table"));
            LinearLayout base = (LinearLayout)getActivity().findViewById(R.id.borrowing_request_parent);

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

                final JSONObject obj = table.getJSONObject(i);

                RelativeLayout rel = new RelativeLayout(context);

                String toUnit = obj.getString("toUnit");
                String fromUnit = obj.getString("fromUnit");
                String time = obj.getString("timestamp");
                String info = time + "         " + fromUnit + "         " + toUnit;
                //System.out.println("patientId: "+ patientId);
                TextView pId = new TextView(context);
                pId.setText(info);
                pId.setId(R.id.transferlist3);
                pId.setPadding(5, 5, 5, 5);
                pId.setTextSize(20);
                pId.setGravity(Gravity.CENTER);
                pId.setBackgroundColor(Color.parseColor("#7FB3D5"));
                pId.setTextColor(Color.parseColor("#FFFFFF"));
                Resources r = getResources();
                //int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, , r.getDisplayMetrics());
                int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, r.getDisplayMetrics());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
                //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                //Adding Text View To Relative Layout
                rel.addView(pId);
                pId.setLayoutParams(params);
                final String response = obj.toString();

                pId.setOnClickListener(new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {
                        Toast.makeText(context, "On Click Listener 1", Toast.LENGTH_LONG).show();
                        try {
                            Fragment f = new itemBorrowingRequestFragment();
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.replace(R.id.content_main, f,"itemborrowingrequestfrag");
                            ft.addToBackStack(null);
                            ft.commit();
                            comm.respond(response,"itemborrowingrequestfrag",f);
                            Toast.makeText(context, "doing transfer", Toast.LENGTH_LONG).show();
                            ((HomePage)getActivity()).showFragment(f,true);
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
            Toast.makeText(context, "Nurse Table Borrowing Exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }
}
