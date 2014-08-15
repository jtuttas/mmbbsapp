package de.tuttas.tictactoetournament;

import java.lang.reflect.Field;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tuttas.gameserver.GameHighscoreListener;
import de.tuttas.gameserver.GameServer;
import de.tuttas.gameserver.GameServerListener;
import de.tuttas.gameserver.GameStates;
import de.tuttas.gameserver.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

public class HighscoreActivity extends GameManagementActivity implements GameHighscoreListener {

	ProgressDialog pd;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Main.TAG,"onCreate() Highscore Activity");
		//Toast.makeText(getApplicationContext(),"onCretae()", Toast.LENGTH_LONG).show();
		setContentView(R.layout.highscore_layout);
	}
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"onResume() Highscore Activity");
		gc.setHighscoreCallbacks(this, handler);
		super.onResume();
		if (pd!=null && pd.isShowing()) pd.dismiss();
		pd = new ProgressDialog(this,R.style.MyTheme);
		pd.setCancelable(false);
		pd.setProgressStyle(android.R.style.Widget_ProgressBar_Small);
		pd.show();
		gc.highscores(20);
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (pd!=null && pd.isShowing()) pd.dismiss();
		Log.d(Main.TAG,"onStop() Highscore Activity");
	}


	@Override
	public void updateHighscores(JSONObject obj) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"update Highscores");
		if (pd!=null && pd.isShowing()) pd.dismiss();
		JSONArray rows = obj.optJSONArray("rows");
		TableLayout layout = (TableLayout) findViewById(R.id.tablellayout_highscore);
		layout.removeViews(1, layout.getChildCount()-1);
		boolean foundme=false;
		for (int i=0;i<rows.length();i++) {
			Log.d(Main.TAG,"update Highscores:"+i);
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			if (rows.optJSONObject(i).optString("Name").compareTo(gc.getUser())==0) {
				tableRow.setBackgroundColor(getResources().getColor(R.color.lineme));
				foundme=true;
			}
			else {
				if (i%2==0)  tableRow.setBackgroundColor(getResources().getColor(R.color.line0));
				else  tableRow.setBackgroundColor(getResources().getColor(R.color.line1));
			}
			this.addRow(tableRow, 
					Integer.toString(i+1),
					Integer.toString(rows.optJSONObject(i).optInt("score")),
					rows.optJSONObject(i).optString("Name"),
					Integer.toString(rows.optJSONObject(i).optInt("games")),
					Integer.toString(rows.optJSONObject(i).optInt("won")),
					Integer.toString(rows.optJSONObject(i).optInt("lost")),
					rows.optJSONObject(i).optString("location"));
					
             
             layout.addView(tableRow);
		}
		if (!foundme) {
			TableRow tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			this.addRow(tableRow, ":", ":", ":", ":", ":", ":", ":");
            layout.addView(tableRow);
			tableRow = new TableRow(this);
			tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
			this.addRow(tableRow, Integer.toString(obj.optInt("ranking")), 
					Integer.toString(obj.optInt("score")),
					gc.getUser(),
					Integer.toString(obj.optInt("games")),
					Integer.toString(obj.optInt("won")),
					Integer.toString(obj.optInt("lost")),
					obj.optString("location"));
            layout.addView(tableRow);
		}
		
	}

	private void addRow(TableRow tableRow,String rank,String score, String name,String games,String won,String lost, String location) {
        // Rank
		 TextView tv = new TextView(this);
		 //Typeface tf = Typeface.createFromAsset(this.getAssets(), "fonts/TipoPRESS.otf");
        tv.setText(rank);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(10, 0, 0, 0);
        tv.setTextSize(20);	         
        tableRow.addView(tv);

        // Score
		 tv = new TextView(this);
        tv.setText(score);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);

        // Name
		 tv = new TextView(this);
        tv.setText(name);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);

        // Games
		 tv = new TextView(this);
        tv.setText(games);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);
        
        // Games won
		 tv = new TextView(this);
        tv.setText(won);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);
        
        // Games lost
		 tv = new TextView(this);
        tv.setText(lost);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);
        
        // Name
		 tv = new TextView(this);
        tv.setText(location);
        tv.setTextColor(Color.BLACK);
        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
        tv.setPadding(5, 0, 0, 0);
        tv.setTextSize(20);
        tableRow.addView(tv);

	}


	@Override
	public void updateDisconnect() {
		// TODO Auto-generated method stub
		super.updateDisconnect();
		if (pd!=null && pd.isShowing()) pd.dismiss();

	}


	@Override
	public void connectionError() {
		// TODO Auto-generated method stub
		super.connectionError();
		if (pd!=null && pd.isShowing()) pd.dismiss();

	}
	
}
