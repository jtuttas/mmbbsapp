package de.mmbbs;
/**
 * Klasse zum Verwalten der Datenbank test
 */
import java.util.Vector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DBManager extends SQLiteOpenHelper {

	public static String DBNAME = "lehrer";
	public static int VERSION = 10;
	public static String CREATE = "CREATE TABLE lehrer(ID INTEGER PRIMARY KEY NOT NULL, SHORT TEXT, NNAME TEXT, VNAME TEXT, GENDER TEXT, EMAIL TEXT);";
	public static String CREATE_KLASSEN = "CREATE TABLE klassen(ID INTEGER PRIMARY KEY NOT NULL, KLASSE TEXT, ID_LEHRER TEXT, STUNDENPLAN TEXT, VERTRETUNGSPLAN TEXT);";
	//public static String CREATE_KLASSENLEHRER = "CREATE TABLE klassenlehrer(ID INTEGER PRIMARY KEY NOT NULL, KLASSE_ID INTEGER, LEHRER_ID INTEGER);";
	Context context;
	

	public static String[] ADD = {
	};

	public static String[] ADD_KLASSEN = {
	};
	
	//forest testwerte eingetragen
	//public static  String[] ADD_KLASSENLEHRER = {
		
	//};
	
	public DBManager(Context context, int VERSION) {
		super(context,DBNAME,null,VERSION);
		this.context=context;
		this.VERSION=VERSION;
		Log.d(Main.TAG, "DBM-Manager initialisiert Version="+VERSION);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG, "DBM-Manager create Database");
		db.execSQL(CREATE);
		for (int i=0;i<ADD.length;i++) {
			db.execSQL(ADD[i]);
		}
		
		//Creating DB
		db.execSQL(CREATE_KLASSEN);
		for (int i=0;i<ADD_KLASSEN.length;i++) {
			db.execSQL(ADD_KLASSEN[i]);
		}
		
		//Create table
		//db.execSQL(CREATE_KLASSENLEHRER);
		//for (int i=0; i < ADD_KLASSENLEHRER.length; i++) {
			//db.execSQL(ADD_KLASSENLEHRER[i]);
		//}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
		
		Log.d(Main.TAG, "DBM-Manager update Database");
		try {
		db.execSQL("DROP TABLE 'lehrer'");
		}
		catch (SQLiteException e) {
			
		}
		try {
			db.execSQL("DROP TABLE 'klassen'");
		}
		catch (SQLiteException e) {
			
		}
		/*
		try {
			db.execSQL("DROP TABLE 'klassenlehrer'");
		}
		catch (SQLiteException e) {
			
		}
		*/

		this.onCreate(db);
	}
	

	/**
	 * get Teacher from shortName
	 * @param s ShotName(ex. TU for Tuttas)
	 * @return the Teacher
	 */
	public Teacher getTeacher(String s) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM lehrer WHERE SHORT='"+s+"'" , null);
		Teacher teacher = null;
		while (c.moveToNext()) {
			String name = c.getString(2);
			String vname = c.getString(3);
			String shortName = c.getString(1);
			String gender = c.getString(4);
			Log.d(Main.TAG, "Gender aus DB:("+c.getString(4)+")");
			teacher = new Teacher(context,name,vname,shortName,gender);
		}
		c.close();
		return teacher;
		
	}
	

	public String[] getShortNames() {
		// TODO Auto-generated method stub
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM lehrer ORDER BY SHORT ASC" , null);
		Log.d(Main.TAG, "Datenbank Lehrer erntält "+c.getCount()+" einträge");
		if (c.getCount()==0) {
			c.close();
			db.close();
			return null;
		}
		String[] s = new String[c.getCount()];		
		Log.d(Main.TAG, "Cursor size="+c.getCount());
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(1);
		}
		c.close();
		return s;
		
	}
	
	public String getVertretungsplanLink(final String klasse) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT VERTRETUNGSPLAN FROM klassen WHERE KLASSE='"+klasse+"' ORDER BY KLASSE ASC LIMIT 1" , null);
		String[] s = new String[c.getCount()];		
		Log.d(Main.TAG, "Cursor size="+c.getCount());
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		c.close();
		if (s.length>0) {
			Log.d(Main.TAG," getVertretungsplanLink von "+klasse+" return:"+s[0]);
			return s[0];
		}
		else {
			Log.d(Main.TAG," getVertretungsplanLink von "+klasse+" return: nix");
			return "";
		}
	}
	
	public String getStundenplanLink(final String klasse) {
		// TODO Auto-generated method stub
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT STUNDENPLAN FROM klassen WHERE KLASSE='"+klasse+"' ORDER BY KLASSE ASC LIMIT 1" , null);
		String[] s = new String[c.getCount()];		
		Log.d(Main.TAG, "Cursor size="+c.getCount());
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		c.close();
		if (s.length>0) {
			return s[0];
		}
		else return "";
	}
	
	/**
	 * herausfinden der Klassen ID
	 * @param String klasse
	 * @return int
	 * @author Herden, Peguschin
	 */
	/*
	public int getKlassenId(String klasse) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT ID FROM klassen WHERE KLASSE='"+klasse+"' LIMIT 1" , null);
		String[] s = new String[c.getCount()];	
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		c.close();
		if (s.length>0) {
			return Integer.parseInt(s[0]);
		} else {
			return 0;
		}
	}
	*/
	
	/**
	 * herausfinden der Klassenlehrer ID
	 * @param int klassenId
	 * @return int
	 * @author Herden, Peguschin
	 *
	 */
	/*
	public int getKlassenlehrerId(int klassenId) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT LEHRER_ID FROM klassenlehrer WHERE KLASSE_ID='" + klassenId + "' LIMIT 1" , null);
		String[] s = new String[c.getCount()];	
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		c.close();
		if (s.length>0) {
			return Integer.parseInt(s[0]);
		} else {
			return 0;
		}
	}
	*/
	
	/**
	 * herausfinden der Klassenlehrer Email Addresse
	 * @param String klasse
	 * @return string (EMail Adresse)
	 * @author Tuttas
	 */
	
	public String getLehrer(String klasse) {
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor c = db.rawQuery("SELECT ID_LEHRER FROM klassen WHERE KLASSE='" + klasse + "' LIMIT 1" , null);
		if (c.getCount()==0) {
			c.close();
			return null;
		}
		String[] s = new String[c.getCount()];	
		int i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		
		Log.d(Main.TAG, "Klassenlehrer ist :"+s[0]);
		
		/*
		c = db.rawQuery("SELECT EMAIL FROM lehrer WHERE SHORT='" + s[0] + "' LIMIT 1" , null);
		if (c.getCount()==0) {
			c.close();
			return null;
		}
		s = new String[c.getCount()];	
		i=0;
		while (c.moveToNext()) {
			s[i++]=c.getString(0);
		}
		Log.d(Main.TAG, "Email Klassenlehrer ist :"+s[0]);
		c.close();
		*/
		return s[0];
		
	}
	
}
