package com.example.navigationdrawersample;

import android.support.v4.app.Fragment;

/**
 * Created by Tanish on 13-02-2017.
 */

public interface Communicator {
    public void respond(String data,String tag,Fragment frag);
}
