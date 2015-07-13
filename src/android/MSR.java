/*
       Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
*/
package ar.com.nigdy.msr;


import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import hdx.msr.*;


import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.util.Log;
import android.util.TimeUtils;

import java.io.BufferedReader;

import android.os.Handler;
import android.os.Messenger;
import java.lang.*;
import java.util.concurrent.locks.*;



public class MSR extends CordovaPlugin {



    private MagneticStripeReader msr;
    protected String result;
    private boolean read_flag;
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("read")) {



	    result = "";
	    msr = new MagneticStripeReader(new MyHandler());
	    try {
		
		msr.Open();
	    } catch ( IOException e ) {
		JSONObject r = new JSONObject();
		r.put("result","ERROR, no se puede conectar al lector");
		callbackContext.error(r);
	    } 
	    msr.StartReading();

        }
        else {
            return false;
        }
        return true;
    }




	
    private class MyHandler extends Handler {

	private CallbackContext callbackContext;

	MyHandler(CallbackContext callbackContext) {
	    this.callbackContext = callbackContext;
	}

    	String ParseOneTrack(int startPos,byte[] data)
    	{
    		int len;
    		
    		len = data[startPos];
    		if(len == 0)
    		{
    		    return "Error";
    		}
    		else
    		{
		    String tmp="";

		    try {
			tmp = new String(data,startPos+1,len,"GBK");
		    } catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		    }
		    return tmp;
    		}
    	}
    	void ParseData(int size,byte[] data)
    	{
    		int pos;


		JSONObject r = new JSONObject();

		r.put("line1", ParseOneTrack(0,data));
    		pos = data[0]+1;
    		r.put("line2",  ParseOneTrack(pos,data));
    		pos += data[pos]+1;
		r.put("line3", ParseOneTrack(pos,data));

		this.callbackContext.success(r);


       	}
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MagneticStripeReader.ON_READ_DATA:
            	ParseData(msg.arg1,(byte [])msg.obj);
            	break;
               default:
                  	break;
            }
        }		
	}

}
