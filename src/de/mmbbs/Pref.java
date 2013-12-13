package de.mmbbs;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

// Kommentar auf T400 mit Eclipse

public class Pref extends PreferenceActivity  {

	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		PreferenceManager.setDefaultValues(this, R.xml.einstellungen, false);
		addPreferencesFromResource(R.xml.einstellungen);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		
		SharedPreferences.OnSharedPreferenceChangeListener prefListener = 
		        new SharedPreferences.OnSharedPreferenceChangeListener() {
		    public void onSharedPreferenceChanged(SharedPreferences prefs,
		            String key) {
		        if (key.equals("klasse")) {
		        	Log.d(Main.TAG,"SharePrefrences Klasse changed");
		        	String klasse = prefs.getString("klasse", null);
		        	klasse=klasse.toUpperCase();
		        	klasse=klasse.trim();
		        	Editor e = prefs.edit();
		        	e.putString("klasse", klasse);
		        	e.commit();
		        	registerInBackground();
		        }
		    }
		};
		
		prefs.registerOnSharedPreferenceChangeListener(prefListener);
	}

	
	private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                
                 String klasse = prefs.getString("klasse", "");
                Log.d(Main.TAG,"registration id="+Main.regid);
            	Log.d(Main.TAG, "Trage Registration ID ein für ("+klasse+")");
        		try {
        		    // Create a URL for the desired page
        		    URL url = new URL(Main.DB_URL+"gcm.php?KLASSE="+klasse+"&GCMid="+Main.regid);

        		    // Read all the text returned by the server
        		    BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        		    String str;
        		    String s="";
        		    while ((str = in.readLine()) != null) {
        		        // str is one line of text; readLine() strips the newline character(s)
        		    	s=s+str+"\r\n";
        		    }
        		    in.close();
        		    Log.d(Main.TAG,"Empfangen:"+s);
        		} catch (MalformedURLException e) {
        			Log.d(Main.TAG, "Malformed URL Exception bei Lade DBInfo");
        		} catch (IOException e) {
        			Log.d(Main.TAG, "IO-Exception bei Lade DBInfo:");
        		}
                return "ok";
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            	Log.d(Main.TAG,msg);
            }
        }.execute(null, null, null);
    }
   


}
