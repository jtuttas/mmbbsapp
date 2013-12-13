package de.mmbbs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class Main extends Activity implements Loadfinished {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    AdView adView;
    private String klasse;
	// Pausen 9:30 , 11:20, 13:10 , 15:00
	public static final int[] PAUSE_MIN = {30,20,10,0};
	public static final int[] PAUSE_STD = {9,11,13,15};
	public static boolean egg=false;
	
	public static final int NEW_VERSION=0;
	public static final int UPDATE_TEACHER_DB=1;
	public static final int UPDATE_CLASSES_DB=2;
	public static final int UPDATE_CLASSTEACHER_DB=3;
	public static final int UPDATE_FINISHED=4;
	public static int UPDATE_STATE=NEW_VERSION;
	
	public static final String TAG = "mmbbsapp";
	public static String IMAGE_URL = "http://www.seminar-mediendidaktik.de/mmbbsapp/";
	public static String DB_URL = "http://midlet.dyndns.org/mmbbsapp/";
	private DBDownloaderTask dbtask;
	private ProgressDialog dialog;
	public static DBManager dbm;
	private SharedPreferences  pref;
	
	// GCM
	GoogleCloudMessaging gcm;
	public static String regid;
	public static final String PROPERTY_REG_ID = "registration_id";  // für shared Preferences
	private static final String PROPERTY_APP_VERSION = "appVersion";
	Context context;
	String SENDER_ID = "182820000538";
	
	public Main() {
		Log.d(Main.TAG, "Main");

	}
	

	@Override
    public void onPause() {
      adView.pause();
      super.onPause();
    }

	

    @Override
    public void onDestroy() {
      adView.destroy();
      super.onDestroy();
    }
   
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(Main.TAG, "onCreate");
        setContentView(R.layout.main);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	dbm = new DBManager(this,pref.getInt("dbvers", 10));
    	context = this.getApplicationContext();
		adView = (AdView)this.findViewById(R.id.adView);
    	klasse = pref.getString("klasse", null);
		
    	if (checkPlayServices()) {
            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this.getApplicationContext());

            if (regid=="") {
                registerInBackground();
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
    
    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    Log.d(TAG,"registration id="+regid);
                    // You should send the registration ID to your server over HTTP, so it
                    // can use GCM/HTTP or CCS to send messages to your app.
                    if (klasse !=null) {
                    	sendRegistrationIdToBackend();
                    }
                    //pref.edit().putString("regid", regid);
                    //Log.d(Main.TAG,"Reg ID in SHare Pref. eingetrage");
                    // For this demo: we don't need to send it because the device will send
                    // upstream messages to a server that echo back the message using the
                    // 'from' address in the message.

                    // Persist the regID - no need to register again.
                    //storeRegistrationId(context, regid);
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                //mDisplay.append(msg + "\n");
            	Log.d(TAG,msg);
            }
        }.execute(null, null, null);
    }
   

    private void sendRegistrationIdToBackend() {
        // Your implementation here.
    	Log.d(Main.TAG, "Trage Registration ID ein für "+klasse);
		try {
		    // Create a URL for the desired page
		    URL url = new URL(DB_URL+"gcm.php?KLASSE="+klasse+"&GCMid="+regid);

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
      }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId == "") {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }
    
    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(Main.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }
    
    @Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	      adView.resume();
		checkPlayServices();
	}

	private boolean checkPlayServices() {
		// TODO Auto-generated method stub
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	            		PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
		
	}

	public void klick_kontakt(View v) {
    	startActivity(new Intent(this,Kontakt.class));
    }
    
    
    public void klick_aboutAdd(View v) {
		AlertDialog.Builder aboutView = new AlertDialog.Builder(this);
        aboutView.setTitle("Werbung");
        ScrollView sv = new ScrollView(this);
            
        LinearLayout credits = new LinearLayout(this);
        credits.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
        credits.setOrientation(LinearLayout.VERTICAL);
        credits.setBackgroundColor(0xffffff);
       
        TextView tv = new TextView(this);
        tv.setText(R.string.about_add);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(10, 0, 10, 0);
        credits.addView(tv);
        sv.addView(credits);
        aboutView.setView(sv);
        aboutView.setPositiveButton(R.string.btn_ok, null);
        aboutView.show();    	
    }
    
    /*
    public void klick_einstellungen(View v) {
    	startActivity(new Intent(this,Pref.class));
    }
    */
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.mainmenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.item_einstellungen:
			startActivity(new Intent(this,Pref.class));
			break;
		case R.id.item_info:
			
			//SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			//String s = prefs.getString("kennwort", "null");
			AlertDialog.Builder aboutView = new AlertDialog.Builder(this);
	        aboutView.setTitle("Info");
	           //aboutView.setMessage(R.string.about_text);
	        ScrollView sv = new ScrollView(this);
	             
	        LinearLayout credits = new LinearLayout(this);
	        credits.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
	        credits.setOrientation(LinearLayout.VERTICAL);
	        credits.setBackgroundColor(0xffffff);
            ImageView img = new ImageView(this);
            img.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 150));
            img.setScaleType(ScaleType.CENTER_INSIDE);
            img.setImageResource(R.drawable.joerg);
            img.setBackgroundColor(0xffffff);
            credits.addView(img);
           
            TextView tv = new TextView(this);
            tv.setText(R.string.about_txt);
            tv.setGravity(Gravity.CENTER);
            credits.addView(tv);
            sv.addView(credits);
            aboutView.setView(sv);
            aboutView.setPositiveButton(R.string.btn_ok, null);
           //aboutView.setIcon(R.drawable.mine);
        
            aboutView.show();
           
            return true;
			//Toast.makeText(this, "(c) 2011/12 by WPK \n'Softwareentwicklung f. mobile Endgeräte'", Toast.LENGTH_LONG).show();
			//return true;

		}
		return super.onOptionsItemSelected(item);
	}

	public void klick_meineklasse(View v) {
    	startActivity(new Intent(this,MeineKlasse.class));
    	
    }

	public void loadFinished(String s) {
		// TODO Auto-generated method stub
		switch (UPDATE_STATE) {
			case NEW_VERSION:
				if (s!=null) {
					s=s.substring(0, s.indexOf("\r"));
					int vers = Integer.parseInt(s);
					if (vers!=DBManager.VERSION) {
						Log.d(Main.TAG, "Versionen unterscheide sich");
						DBManager.VERSION=vers;
						//Toast.makeText(this, "Neue Datenbank verfügbar", Toast.LENGTH_LONG).show();
						dialog = ProgressDialog.show(this, "", "Updating Databases. Please wait...");  
						UPDATE_STATE=UPDATE_TEACHER_DB;
						dbtask = new DBDownloaderTask(this,this);
						dbtask.execute(Main.DB_URL+"index.php?cmd=lehrer");
					}
					else {
						Log.d(Main.TAG, "Installiere Datenbank und remote Datenbank sind identisch");
						UPDATE_STATE=UPDATE_FINISHED;
					}
				}
				else {
					Toast.makeText(this, "Kann Updateserver nicht erreichen!", Toast.LENGTH_LONG).show();
				}
				break;
			case UPDATE_TEACHER_DB:
				//dialog.dismiss();
				UPDATE_STATE=UPDATE_CLASSES_DB;
				DBManager.ADD = s.split("\\n");
				//Log.d(Main.TAG, "DB Lehrer[0]="+DBManager.ADD[0]);
				//Log.d(Main.TAG, "DB Lehrer[1]="+DBManager.ADD[1]);
				//dialog.show(this, "", "Updating Klassen. Please wait...");
				
				dbtask = new DBDownloaderTask(this,this);
				dbtask.execute(Main.DB_URL+"index.php?cmd=klassen");
				
				break;
			/*
			case UPDATE_CLASSES_DB:
				//dialog.dismiss();
				DBManager.ADD_KLASSEN = s.split("\\r?\\n");
				//Log.d(Main.TAG, "DB Klassen[0]="+DBManager.ADD_KLASSEN[0]);
				//Log.d(Main.TAG, "DB Klassen[1]="+DBManager.ADD_KLASSEN[1]);
				//dialog.show(this, "", "Updating Klassenlehrer. Please wait...");
				UPDATE_STATE=UPDATE_CLASSTEACHER_DB;
				dbtask = new DBDownloaderTask(this,this);
				dbtask.execute(Main.IMAGE_URL+"klassenlehrer.db");
				break;
			*/
			case UPDATE_CLASSES_DB:
				DBManager.ADD_KLASSEN = s.split("\\n");
				//Log.d(Main.TAG, "DB Klassenlehrer[0]="+DBManager.ADD_KLASSENLEHRER[0]);
				pref = PreferenceManager.getDefaultSharedPreferences(this);
				
				dialog.dismiss();
				UPDATE_STATE=UPDATE_FINISHED;
				Editor editor = pref.edit();
				editor.putInt("dbvers", DBManager.VERSION);
				editor.commit();
				dbm=new DBManager(this,DBManager.VERSION);
				break;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		// TODO Auto-generated method stub
        Log.d(Main.TAG,"onStart()");
        AdView adView = (AdView)this.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        .addTestDevice("TEST_DEVICE_ID")
        .build();
        adView.loadAd(adRequest);
    
        if (UPDATE_STATE!=UPDATE_FINISHED) {
        	Log.d(Main.TAG, "Aus Pref Version="+DBManager.VERSION);
        	dbtask = new DBDownloaderTask(this,this);
        	dbtask.execute(Main.DB_URL+"index.php");
        }
        if (pref.getString("email", "").compareTo("tuttas")==0 && !egg) {
        	egg=true;
        	Toast.makeText(this, "Easter Egg aktiv", Toast.LENGTH_LONG).show();
            Date pause = getNextPause();
            setAlarm(this,pause);
        }
        
	}

	public static  void setAlarm(Context context,Date pause) {
		// TODO Auto-generated method stub
    	AlarmManager am = (AlarmManager) context.getSystemService(Activity.ALARM_SERVICE);
        Log.d(Main.TAG, "Alarm gestellt auf "+pause.toString());
        Intent intent = new Intent(context, MyAppReciever.class);           
        PendingIntent sender = PendingIntent.getBroadcast(context, 192837, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        
        am.set(AlarmManager.RTC_WAKEUP, pause.getTime(), sender);
        Log.d(Main.TAG, "Alarm Manager initalisiert");

	}

	public static Date getNextPause() {
		// TODO Auto-generated method stub
		Date d = new Date();
		Log.d(Main.TAG,"Heute ist "+d);
		Date pause = (Date)d.clone();
		
		int i=0;
		for (;i<PAUSE_STD.length;i++) {
			pause.setHours(PAUSE_STD[i]);
			pause.setMinutes(PAUSE_MIN[i]);
			pause.setSeconds(0);
			
			if (pause.after(d)) break;
		}
		if (i==PAUSE_STD.length) {
			Log.d(Main.TAG,"Pause erst wieder nächste Tag");			
			d.setHours(PAUSE_STD[0]);
			d.setMinutes(PAUSE_MIN[0]);
			d.setSeconds(0);
			d = new Date(d.getTime()+(1000*60*60*24));
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			if (c.get(Calendar.DAY_OF_WEEK)==7) {
				d = new Date(d.getTime()+(1000*60*60*24*2));				
				Log.d(Main.TAG,"Morgen ist Sa. nächste Pause also erst wieder um "+d);			
			}
		}
		else {
			//i--;
			d.setHours(PAUSE_STD[i]);
			d.setMinutes(PAUSE_MIN[i]);
			d.setSeconds(0);
			Calendar c = Calendar.getInstance();
			c.setTime(d);
			Log.d(Main.TAG,"DAY OF WEEK ist "+c.get(Calendar.DAY_OF_WEEK));			
			
			if (c.get(Calendar.DAY_OF_WEEK)==7) {
				d = new Date(d.getTime()+(1000*60*60*24*2));				
				d.setHours(PAUSE_STD[0]);
				d.setMinutes(PAUSE_MIN[0]);
				d.setSeconds(0);
				Log.d(Main.TAG,"Heute ist Sa. nächste Pause also erst wieder um "+d);			
			}
			if (c.get(Calendar.DAY_OF_WEEK)==1) {
				d = new Date(d.getTime()+(1000*60*60*24*1));				
				d.setHours(PAUSE_STD[0]);
				d.setMinutes(PAUSE_MIN[0]);
				d.setSeconds(0);
				Log.d(Main.TAG,"Heute ist So. nächste Pause also erst wieder um "+d);			
			}
		}
		Log.d(Main.TAG, "nächste Pause ist um "+d.toString());
		return d;
	}

}