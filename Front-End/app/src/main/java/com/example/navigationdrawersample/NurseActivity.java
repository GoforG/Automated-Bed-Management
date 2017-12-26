package com.example.navigationdrawersample;

/**
 * Created by Tanish on 30-01-2017.
 */


import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NurseActivity extends Fragment{
    Context context;
    Communicator comm;
    String response1;
    JSONObject param;
    public void changeData(String s){
        response1 = s;
    }
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_nurse,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Transfers");

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        context = this.getActivity();
        comm = (Communicator)getActivity();
        param = new JSONObject();
        //Intent intent = getActivity().getIntent();
        //response = intent.getStringExtra("nurseIntent");
        System.out.println("debskz"+response1);
        try {
            JSONObject jObj = new JSONObject(response1);
            JSONArray table = new JSONArray(jObj.getString("table"));
            System.out.println("Ehello");
            //      LinearLayout root = (LinearLayout) findViewById(R.id.main);
            LinearLayout base = (LinearLayout)getActivity().findViewById(R.id.main);
            int i;

            System.out.println("len: " + table.length());
            RelativeLayout prev;
            for (i = 0; i < table.length(); i++) { // Walk through the Array.
                System.out.println("hum yahan hain");
                final JSONObject obj = table.getJSONObject(i);

                RelativeLayout rel = new RelativeLayout(context);

                String patientId = obj.getString("patientId");
                System.out.println("patientId: "+ patientId);
                TextView pId = new TextView(context);

                pId.setText(patientId);
                pId.setId(R.id.transferlist1);

                pId.setPadding(5, 5, 5, 5);
                pId.setTextSize(20);
                pId.setGravity(Gravity.CENTER);
                pId.setBackgroundColor(Color.parseColor("#22C778"));
                pId.setTextColor(Color.parseColor("#FFFFFF"));
                Resources r = getResources();
                int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 150, r.getDisplayMetrics());

                int height = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, r.getDisplayMetrics());
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                //           month_name.setTextAppearance(this, android.R.style.TextAppearance_Medium);
                params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                //Adding Text View To Relative Layout
                rel.addView(pId);
                pId.setLayoutParams(params);


                //Setting Parameters For TextViews
                //Date and Month TextView " 12 DEC

                String patientName = obj.getString("name");

                TextView pName = new TextView(context);
                pName.setPaintFlags(pName.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                pName.setText(patientName);
                pName.setId(R.id.transferlist2);
                pName.setPadding(10, 10, 10, 10);
                pName.setGravity(Gravity.CENTER_VERTICAL);
                pName.setBackgroundColor(Color.parseColor("#F1F1F1"));
                pName.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                params = new RelativeLayout.LayoutParams(width, height);
                params.setMargins(25, 5, 0, 0);
                params.addRule(RelativeLayout.RIGHT_OF, R.id.transferlist1);
                rel.addView(pName);
                pName.setLayoutParams(params);
                final String response = obj.toString();
                Toast.makeText(context, "On Click Listener", Toast.LENGTH_LONG).show();
                pName.setOnClickListener(new View.OnClickListener() {

                    @Override

                    public void onClick(View v) {
                        Toast.makeText(context, "On Click Listener 1", Toast.LENGTH_LONG).show();
                        try {
                                Fragment f = new NurseInfo();
                                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                                ft.replace(R.id.content_main, f,"nurseinfotag");
                                ft.addToBackStack(null);
                                ft.commit();
                                comm.respond(response,"nurseinfotag",f);
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
            Toast.makeText(context, "Nurse Table Exception", Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }
    }

}