package de.mmbbs;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

/**
 * Klasse - E-Mail an Klassenlehrer
 * @author Herden, Peguschin
 */
public class EmailKlassenlehrer {
	
	/**
	 * Klasse
	 * @var String
	 * @author Herden, Peguschin
	 */
	private String klasse;
	
	/**
	 * E-Mail Adresse
	 * @var String
	 * @author Herden, Peguschin
	 */
	private String mail;
	
	/**
	 * Objekt - Datenbankmanager
	 * @var DBManager
	 * @author Herden, Peguschin
	 */
	private DBManager dbm;

	/**
	 * Hauptmethode
	 * @param Activity
	 * @author Herden, Peguschin
	 */
	public void main(Activity act) {
		
		//Datenbankmanager
		dbm = Main.dbm;
				
		//auslesen der gespeicherten Klasse
		this.setKlasse(act);
		
		//Prüfen ob Schulklasse hinterlegt
		if(this.klasse.equalsIgnoreCase("NONE")) {
			
			//Fehlermeldung
			Toast.makeText(act, "Du musst eine Klasse hinterlegen", Toast.LENGTH_LONG).show();
			
		} else {
			
			//auslesen der zugehörigen E-Mail Adresse
			this.setMail(act);
			
			//Prüfen ob E-Mail Adresse ermittelt
			if (this.mail==null) {
				Toast.makeText(act, "Die hinterlegte Klasse konnte nicht zugeordnet werden", Toast.LENGTH_SHORT).show();
			}
			else if(this.mail.equalsIgnoreCase("none")) {
				
				//Fehlermeldung
				Toast.makeText(act, "Die hinterlegte Klasse konnte nicht zugeordnet werden", Toast.LENGTH_SHORT).show();
				
			} else {
				
				//E-Mail Programm öffnen				
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				String[] recipients = new String[]{this.mail};
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients);
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");
				emailIntent.setType("text/plain");
				act.startActivity(Intent.createChooser(emailIntent, ""));
				
			}
			
		}
		
	}

	/**
	 * Auslesen der gespeicherten Klasse
	 * @author Herden, Peguschin
	 * @param Activity
	 */
	private void setKlasse(Activity act) {
		
		String klasse;
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(act);
		klasse = prefs.getString("klasse", "none");
		klasse = klasse.toUpperCase();
		this.klasse = klasse;
		
	}
	
	/**
	 * Auslesen der zugehörigen E-Mail Adresse
	 * @author Tuttas
	 */
	private void setMail(Activity act) {
		
		String mail = dbm.getLehrerMail(this.klasse);
		if (mail==null) {
			Toast.makeText(act, "Die hinterlegte Klasse konnte nicht zugeordnet werden", Toast.LENGTH_SHORT).show();
		}
		else {
			this.mail=mail;
		}
		/*
		//int klassenID = dbm.getKlassenId(this.klasse);
		if(klassenID == 0) {
			Toast.makeText(act, "Die hinterlegte Klasse konnte nicht zugeordnet werden", Toast.LENGTH_LONG).show();
		} else {
			int klassenLehrerID = dbm.getKlassenlehrerId(klassenID);
			if(klassenLehrerID == 0) {
				
			} else {
			}
		}
		*/
		
	}
	
}
