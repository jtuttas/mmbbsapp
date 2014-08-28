package de.mmbbs.gameserver;

import org.json.JSONObject;


import de.mmbbs.R;
import de.mmbbs.tictactoetournament.CustomDialogClass;
import de.mmbbs.tictactoetournament.CustomDialogListener;
import de.mmbbs.tictactoetournament.CustomDialogType;
import de.mmbbs.tictactoetournament.FontOverride;
import de.mmbbs.tictactoetournament.Main;
import de.mmbbs.tictactoetournament.game.Game;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public abstract class GameManagementActivity extends Activity implements GameServerListener{

	protected Handler handler;
	protected static GameServer	gc;
	protected CustomDialogClass cdd;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		FontOverride.setDefaultFont(this, "DEFAULT", "fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "MONOSPACE", "fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Isserley-Bold.otf");
		handler = new Handler();
		gc = (GameServer) getApplication();

	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		gc.setServerCallbacks(this, handler);
		Log.d(Main.TAG,"onResume GameManagementActivity connected="+gc.isConnected()+" state="+gc.getState());
		if (!gc.isConnected()) {
			gc.connect("http://service.joerg-tuttas.de:8080", this,handler);
		}
		else {
			if (gc.getState()==GameStates.CONNECTED) {
				SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
				String user = pref.getString("user", null);
			    String pw = pref.getString("password", null);
			    gc.login(user, pw, Main.GAME);
			}
			if (gc.getState()==GameStates.LOGGED_IN) {
				onLogin();
			}
		}
		if (cdd != null && cdd.isShowing()) cdd.dismiss();

	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"onStart() GameManagement Activity");
		super.onStart();
	//	gc.disconnectOnStop(true);
	}
	
	
	
	@Override
	protected void onStop() {
		Log.d(Main.TAG,"GameManagementActivity onStop()");
		// TODO Auto-generated method stub
		super.onStop();
		if (cdd!=null && cdd.isShowing()) {
			// Dieser Client wurde angefragt
			if (gc.getPendingRequestToPlayer().compareTo(gc.getUser())==0) {
				gc.request(gc.getPendingRequestFromPlayer(),"request_rejected");
			}
			else {
				gc.request(gc.getPendingRequestToPlayer(), "cancelrequest");
			}
			cdd.dismiss();
			gc.setPendingrequest(null, null);
		}
		//Toast.makeText(getApplicationContext(),"onStop()", Toast.LENGTH_LONG).show();			
		//if (gc.getDisconnectOnStop()) gc.disconnect();

	}
	
	public static GameServer getGameServer() {
		return gc;
	}
	
	/**
	 * GameServer Schnittstelle
	 */
	
	
	@Override
	public void updateLogin(JSONObject obj) {
		Log.d(Main.TAG, "update Login Game Management Activity");
					
		if (obj.optBoolean("success")) {
			String user = obj.optString("user");
			String pw = obj.optString("password");
			 SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
			Editor e = pref.edit();
			e.putString("user", user);
			e.putString("password", pw);
			e.commit();
			onLogin();
			
		}	

	}
	@Override
	public void updateResendLogin(JSONObject obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateRegister(JSONObject obj) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void updateRequest(final JSONObject obj) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"update request in GameManagement Activity");
		if (obj.optString("command").compareTo("request")==0) {
			
			cdd = new CustomDialogClass(this,CustomDialogType.INFO ,"Request from player '"+obj.optString("from_player")+"'",
					this.getResources().getString(R.string.ok),this.getResources().getString(R.string.reject));
			cdd.setOnCustomDialog(new CustomDialogListener() {


				@Override
				public void onNegativeButton() {
					gc.request(obj.optString("from_player"), "request_rejected");
					gc.setPendingrequest(null, null);

				}

				@Override
				public void onPositiveButton() {
					gc.request(obj.optString("from_player"), "request_acknowledged");
			    	startGame(true,obj.optString("from_player"));									
				}
				
			});
			cdd.setCancelable(false);
			gc.setPendingrequest(obj.optString("from_player"), gc.getUser());
			cdd.show();
			
		}
		else if (obj.optString("command").compareTo("request_acknowledged")==0) {
			Log.d(Main.TAG," request acknowladged in GameManagement Activity");
			if (cdd!=null) cdd.dismiss();
			gc.setPendingrequest(null, null);
	    	startGame(false,obj.optString("from_player"));				
		}
		else if (obj.optString("command").compareTo("request_rejected")==0) {
			if (cdd!=null) cdd.dismiss();
			gc.setPendingrequest(null, null);
			Toast.makeText(getApplicationContext(),"Request rejected from player '"+obj.optString("from_player")+"'!", Toast.LENGTH_LONG).show();				
		}
		else if (obj.optString("command").compareTo("cancelrequest")==0) {
			if (cdd!= null) cdd.dismiss();
			gc.setPendingrequest(null, null);

			Toast.makeText(getApplicationContext(),"Request canceled from player '"+obj.optString("from_player")+"'", Toast.LENGTH_LONG).show();				
		}
		
	}


	private void startGame(boolean turn,String gegner) {
		Intent i = new Intent(this,Game.class);
		i.putExtra("start",turn);
		
		i.putExtra("gegner", gegner);
    	startActivity(i);
    	
	}
	
	@Override
	public void updateDisconnect() {
		Toast.makeText(getApplicationContext(),"updateDisconnect()", Toast.LENGTH_LONG).show();	
		this.finish();//try activityname.finish instead of this
		setResult(100);

		
	}
	@Override
	public void connected() {
		if (gc.getState()!=GameStates.LOGGED_IN) {
			SharedPreferences pref= PreferenceManager.getDefaultSharedPreferences(this);
			String user = pref.getString("user", null);
		    String pw = pref.getString("password", null);
			gc.login(user, pw, Main.GAME);
		}
		
	}
	@Override
	public void connectionError() {
		// TODO Auto-generated method stub
		Toast.makeText(getApplicationContext(),"ConnectionError()", Toast.LENGTH_LONG).show();	
		// TODO Auto-generated method stub
		cdd = new CustomDialogClass(this,CustomDialogType.ERROR , this.getResources().getString(R.string.failed_connect),
				null,this.getResources().getString(R.string.retry));
		cdd.setOnCustomDialog(new CustomDialogListener() {


			@Override
			public void onNegativeButton() {
		    	reconnect();
				
			}

			@Override
			public void onPositiveButton() {
				
			}
			
		});
		cdd.setCancelable(false);
		cdd.show();
	}
	public void reconnect() {
		Log.d(Main.TAG,"**** GameManegementActivity reconnect!");
		startActivity(new Intent(this, Main.class));
		this.finish();
	}
	
	public abstract void onLogin();
		
	
}
