package de.mmbbs;
import android.os.Bundle;
import android.preference.PreferenceActivity;

// Kommentar auf T400 mit Eclipse

public class Pref extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.einstellungen);
	}

}
