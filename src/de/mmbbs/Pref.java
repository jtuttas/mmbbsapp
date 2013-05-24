package de.mmbbs;
import android.os.Bundle;
import android.preference.PreferenceActivity;

// Kommentar

public class Pref extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.einstellungen);
	}

}
