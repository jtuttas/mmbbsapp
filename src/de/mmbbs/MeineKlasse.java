package de.mmbbs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

/**
 * Activity zu meine Klasse
 * @author tuttas
 *
 */
public class MeineKlasse extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.meineklasse);
		
	}
	/**
	 * �ffnet den Stundenplan.
	 * @author Fritz, Lammers, Schwanda
	 * @param v
	 */
    public void klick_stundenplan(View v) {
    	startActivity(new Intent(this,Stundenplan.class));
    }
    
	/**
	 * �ffnet den Stundenplan.
	 * @author Fritz, Lammers, Schwanda
	 * @param v
	 */
    public void klick_vertretungsplan(View v) {
    	startActivity(new Intent(this,Vertretungsplan.class));
    }
    
    /**
     * E-Mail an Klassenlehrer
     * @author Herden, Peguschin
     * @param v
     */
    public void klick_emailKlassenlehrer(View v) {
    	EmailKlassenlehrer objEmailKlassenlehrer = new EmailKlassenlehrer();
    	objEmailKlassenlehrer.main(this);
    }
    public void klick_emailKlasse(View v) {
    	String klasse;
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(this);
		klasse = prefs.getString("klasse", "");
    	if (klasse!="") {
	    	Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
			String[] recipients = new String[]{klasse+"@mmbbs.eduplaza.de"}; // hier ist dann die kontaktadresse, sollte ja info@mmbbs sein
			emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
			emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Mail von MMBBS App");
			emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
			emailIntent.setType("text/plain");
			startActivity(Intent.createChooser(emailIntent, "EMail an Klasse"));    
		}   
	    else {
	    	Toast.makeText(this, "Du musst eine Klasse hinterlegen", Toast.LENGTH_SHORT).show();
	    }
    }

}
