package de.mmbbs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

/**
 * Klasse zu Stundenplan / Vertretungsplan.
 * @author Fritz, Lammers, Schwanda
 *
 */
public class Stundenplan extends Activity   {
	/* Hier wird die aktuell ausgewaehlte Woche abgespeichert. */
	GregorianCalendar gc;
	int week;
	boolean errorOccured=false;
	/* Standardwert fuer den Vertretungsplan ist false */
	private boolean vertretungsplan=false;
	
	
	/**
	 * Erstellt ein neues Objekt "Stundenplan".
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stundenplan);
		
		/* Kalenderwoche abrufen */
		gc = new GregorianCalendar();
		gc.setTime(new Date());
		gc.setFirstDayOfWeek(GregorianCalendar.MONDAY);
		week=gc.get(GregorianCalendar.WEEK_OF_YEAR);
		
		/* MMBBS-WebViewClient laden */
		final WebView webV = (WebView) findViewById(R.id.webviewPage);
		webV.setWebViewClient(new WebViewClientMmbbs(false,"F�r diese Woche sind leider keine Daten hinterlegt.",this));

		/* Aktuelle Seite aufrufen */
		openPage();
	}
	
	/**
	 * Gibt die in der Datenbank gespeicherte Klassenhinterlegung zu der jeweiligen Klasse aus.
	 * @return
	 */
	private final String getKlassenhinterlegung() {
		/* Sucht den in den Einstellungen hinterlegten Klassennamen. */
		String klasse;
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		klasse = prefs.getString("klasse", "");
		/* wandelt alle Kleinbuchstaben in Grossbuchstaben um. */
		klasse=klasse.toUpperCase();
		
		/* Prüfen ob ein Klassenname hinterlegt ist. */
		if(klasse.length()==0) {
			/* Klasse nicht hinterlegt */
			Toast.makeText(this, "Bitte hinterlege in den Einstellungen zuerst deinen Klassennamen.", Toast.LENGTH_LONG).show();
			errorOccured=true;
			return "";
		}
		else {
			/* Sucht zu dem Klassennamen den zugehoerigen Link. */
			
			if (Main.dbm==null) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				Main.dbm=new DBManager(this,pref.getInt("dbvers", 10));
			}
			DBManager dbm = Main.dbm;
			
			if (vertretungsplan) {
				/* SQL-Output Vertretungsplan ausgeben. */
				final String sqlErgebnis=dbm.getVertretungsplanLink(klasse);
				
				if (sqlErgebnis.length()==0) {
					/* Zu dem Klassennamen wurden keine brauchbaren Informationen gefunden. */
					Toast.makeText(this, "Leider sind zu der Klasse \""+klasse+"\" keine Informationen zu dem Vertretungsplan hinterlegt.", Toast.LENGTH_LONG).show();
					errorOccured=true;
				}
				
				return sqlErgebnis;
				
			}
			else {
				/* SQL-Output Stundenplan ausgeben. */
				final String sqlErgebnis=dbm.getStundenplanLink(klasse);
				
				if (sqlErgebnis.length()==0) {
					/* Zu dem Klassennamen wurden keine brauchbaren Informationen gefunden. */
					Toast.makeText(this, "Leider sind zu der Klasse \""+klasse+"\" keine Informationen zu dem Stundenplan hinterlegt.", Toast.LENGTH_LONG).show();
					errorOccured=true;
				}
				
				return sqlErgebnis;
			}
		}
	}
	
	/**
	 * Ändert das Datum auf die in <i>week</i> hinterlegte Kalenderwoche und setzt das Datum des
	 * Montags in der Woche als Titel.
	 */
	private final void changeDate() {
		final SimpleDateFormat dateFormatter = new SimpleDateFormat ("dd.MM.yyyy");

		gc.set(GregorianCalendar.DAY_OF_WEEK, GregorianCalendar.MONDAY);
		gc.set(GregorianCalendar.WEEK_OF_YEAR, week);
		gc.set(GregorianCalendar.YEAR, GregorianCalendar.getInstance().get(GregorianCalendar.YEAR));
	    
		final String dateOutput = dateFormatter.format(gc.getTime());
		
		final TextView lblDate = (TextView) findViewById(R.id.lblDate);
		lblDate.setText(dateOutput);
	}
	
	/**
	 * Öffnet die geforderte Internetseite im WebView.
	 */
	private final void openPage() {
		errorOccured=false;
		/* Ändert das Datum */
		changeDate();
		
		/* Die Wochenzahl muss zweistellig sein. */
		String wochenzahl=""+week;
		if (wochenzahl.length()==1) wochenzahl="0"+wochenzahl;
		
		String url;
		
		if (vertretungsplan) {
			/* Vertretungsplan-URL */
			String vertretungsplanURL ="http://stundenplan.mmbbs.de/plan1011/ver_kla/";
			//Anhängen der Kalenderwoche
			vertretungsplanURL += wochenzahl +"/c/";
			vertretungsplanURL += getKlassenhinterlegung();
			vertretungsplanURL += ".htm";
			Log.d(Main.TAG,"rufe URL:"+vertretungsplanURL);
			url=vertretungsplanURL;
		}
		else {
			/* Stundenplan-URL */
			String stundenplanURL="http://stundenplan.mmbbs.de/plan1011/klassen/";
			//Anhängen der Kalenderwoche
			stundenplanURL += wochenzahl +"/c/";
			stundenplanURL += getKlassenhinterlegung();
			stundenplanURL += ".htm";
			Log.d(Main.TAG,"rufe URL:"+stundenplanURL);
			url=stundenplanURL;
		}
		
		if (!errorOccured) {
			/* Es sind keine Fehler bei der URL-Generierung aufgetreten. Die Seite darf angezeigt werden. */
			final WebView webV = (WebView) findViewById(R.id.webviewPage);
			/*
			 * Anfangszoom,Plus-Minus-Zoom-Anzeige,Touchfunktion "Doppeltipp auf WebView -> Zoom"
			 */
			webV.setInitialScale(1); // 1 = 1% Zoom, 100 = 100% Zoom, 150 = 150% Zoom
			webV.getSettings().setLoadWithOverviewMode(true);
			webV.getSettings().setUseWideViewPort(true);
			webV.getSettings().setBuiltInZoomControls(true);
			
			/* Da ein Redirect nicht erkannt wird, wenn das Ziel des Redirects gerade geladen ist, wird dem WebView
			 * einmal ein leerer Content uebermittelt. */
			//webV.loadUrl("");
			/* URL laden */
			webV.loadUrl(url);
			
		}
		else {
			/* Es sind Fehler aufgetreten, wie bspw. keine Hinterlegung des Klassennamens. Die Activity wird verlassen. */
			this.finish();
		}
	}
    

	/**
	 * Zeigt die vorherige Woche im WebView an.
	 * @param v
	 */
    public void klick_btnWeekBefore(View v) {
    	if (week>1) week--;
    	openPage();
    }
    

	/**
	 * Zeigt die nachfolgende Woche im WebView an.
	 * @param v
	 */
    public void klick_btnWeekBehind(View v) {
    	if (week<=52) week++;
    	openPage();
    }
    
    /**
     * Setter des Attributs <i>vertretungsplan</i>
     * @param vertretungsplan <br/> --> <b><i>false</i></b>: Die Stundenplanseite wird angezeigt.<br/> --> <b><i>true</i></b>: Die Vertretungsplanseite wird angezeigt.
     */
    protected void setVertretungsplan(boolean vertretungsplan) {
    	this.vertretungsplan=vertretungsplan;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if (keyCode == KeyEvent.KEYCODE_BACK) {
    		final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
    		alertDialog.setTitle("Werbung");
    		alertDialog.setMessage("Heute schon auf die Werbung geklickt?");
    		alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
    		public void onClick(DialogInterface dialog, int which) {
    			alertDialog.cancel();
    			finish();
    		}
    		});
    		alertDialog.setIcon(R.drawable.joerg);
    		alertDialog.show();
               
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }

	

}
