package com.example.navigationdrawersample;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.SharedPreferences;
import android.widget.Toast;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class RequestFragment extends Fragment {

    private Toolbar toolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    String response;
    Communicator comm;
    Fragment f1 = null, f2 = null, f3 = null;
    String responsible="";
    public RequestFragment() {
        // Required empty public constructor
    }
    public void changeData(String s){
        response = s;
    }




    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.request_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Requests");
    }

    public void onActivityCreated(Bundle savedInstanceState) {

        try {
            JSONObject obj = new JSONObject(response);
            responsible = obj.getString("Responsible");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        super.onActivityCreated(savedInstanceState);
            comm = (Communicator) getActivity();
            viewPager = (ViewPager) getActivity().findViewById(R.id.viewpager);
            setupViewPager(viewPager);

            tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
            tabLayout.setupWithViewPager(viewPager);
    }


    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        if(f1 == null)
            f1 = new PendingRequestFragment();

        if(f2 == null)
            f2 = new IncomingRequestFragment();

        if(f3 == null)
            f3 = new BorrowingRequestFragment();

        adapter.addFragment(f1, "Pending");
        //String responsible = myp

        if(responsible.equals("yes"))
        {
            adapter.addFragment(f2, "Incoming");
            adapter.addFragment(f3, "Borrowing");
        }


    //    System.out.println("he hee he ehh");
        comm.respond(response,"pendingrequestfrag",f1);
        comm.respond(response,"incomingrequestfrag",f2);
        comm.respond(response,"borrowingrequestfrag",f3);

        viewPager.setOffscreenPageLimit(3);
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
       /* @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }*/

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
