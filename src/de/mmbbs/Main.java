package de.mmbbs;

import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
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
	public Main() {
		Log.d(Main.TAG, "Main");

	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		Log.d(Main.TAG, "onCreate");
        setContentView(R.layout.main);
    	pref = PreferenceManager.getDefaultSharedPreferences(this);
    	dbm = new DBManager(this,pref.getInt("dbvers", 10));
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