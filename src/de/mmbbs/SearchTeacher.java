package de.mmbbs;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


public class SearchTeacher extends Activity implements OnItemSelectedListener{
	
	private Teacher teacher;
	private Spinner spinner;
	private String [] shortNames;
	private ImageView iv;
	private TextView tv;
	private String anrede;
	//public static SearchTeacher instance;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//instance = this;
		Log.d(Main.TAG, "Search Teacher initialisiert");

		
		setContentView(R.layout.lehrersuche);
		spinner = (Spinner) findViewById(R.id.spinnerLehrer);
		
		if (Main.dbm==null) {
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			Main.dbm=new DBManager(this,pref.getInt("dbvers", 10));
		}
		shortNames = Main.dbm.getShortNames();
		// simple_spinner_item
		ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.select_dialog_item, shortNames);
		spinner.setAdapter(adapter);
		Teacher t = Main.dbm.getTeacher(shortNames[0]);
		
		iv = (ImageView)findViewById(R.id.imageViewTeacher);
		
		iv.setImageBitmap(BitmapFactory.decodeResource(this.getApplicationContext().getResources(), R.drawable.anonym));          		
		tv = (TextView)findViewById(R.id.textViewTeacher);
		tv.setText(t.getVName()+" "+t.getName());
		spinner.setVerticalFadingEdgeEnabled(true);
		
		spinner.setOnItemSelectedListener(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Log.d(Main.TAG, "Back Key");
			if (teacher!=null) {
				if (teacher.isLoading()) {					
					teacher.back();
					return true;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.searchteachermenu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.mailto:
			
			//Toast.makeText(this, "Email an "+teacher.getEmail(), Toast.LENGTH_SHORT).show();
			emailLehrer();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	//@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int i,
			long arg3) {
		// TODO Auto-generated method stub
		teacher = Main.dbm.getTeacher(shortNames[i]);
		if (teacher.getGender().compareTo("male")==0) {
			tv.setText("Herr "+teacher.getVName()+" "+teacher.getName());
		}
		else {
			tv.setText("Frau "+teacher.getVName()+" "+teacher.getName());			
		}

		Log.d(Main.TAG, "Found Teacher:"+teacher.getVName()+" "+teacher.getName());
		teacher.getImage(iv,this);
	}

	//@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	
	public void emailLehrer() {
		anrede = "Hallo ";
		Log.d(Main.TAG, "Gender: "+teacher.getGender());
		if (teacher.getGender().compareTo("male") == 0) {
			this.anrede="Sehr geehrter Herr ";
		} else if (teacher.getGender().compareTo("female") == 0) {
			this.anrede="Sehr geehrte Frau ";
		}
		
		String[] smail = new String[]{teacher.getEmail()};
		Intent emailInt = new Intent(android.content.Intent.ACTION_SEND);
		emailInt.putExtra(android.content.Intent.EXTRA_EMAIL, smail);
		emailInt.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
		emailInt.putExtra(android.content.Intent.EXTRA_TEXT, this.anrede+teacher.getName());
		emailInt.setType("text/plain");
		startActivity(Intent.createChooser(emailInt, "Email versenden..."));
	}
}
