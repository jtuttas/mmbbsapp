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
import android.view.WindowManager;
import android.widget.Toast;

public abstract class GameManagementActivity extends Activity implements
		GameServerListener {

	protected Handler handler;
	protected static GameServerApplication gc;
	private static CustomDialogClass cdd;
	public GameManagementActivity instance;
	private int idDialog;

	public CustomDialogClass getCustomDialog() {
		if (cdd == null) {
			cdd = new CustomDialogClass(this);
		}
		return cdd;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		FontOverride.setDefaultFont(this, "DEFAULT", "fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "MONOSPACE",
				"fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "SANS_SERIF",
				"fonts/Isserley-Bold.otf");
		handler = new Handler();
		gc = (GameServerApplication) getApplication();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		instance = this;

	}

	@Override
	protected void onResume() {
		super.onResume();
		gc.setServerCallbacks(this, handler);
		Log.d(Main.TAG,
				"onResume GameManagementActivity connected=" + gc.isConnected()
						+ " state=" + gc.getState());
		if (!gc.isConnected()) {
			gc.connect("http://service.joerg-tuttas.de:8080", this, handler);
		} else {
			if (gc.getState() == GameStates.CONNECTED) {
				SharedPreferences pref = PreferenceManager
						.getDefaultSharedPreferences(this);
				String user = pref.getString("user", null);
				String pw = pref.getString("password", null);
				gc.login(user, pw, Main.GAME);
			}
			if (gc.getState() == GameStates.LOGGED_IN
					|| gc.getState() == GameStates.REQUEST_PENDING) {
				onLogin();
			}
		}

	}

	@Override
	protected void onStart() {
		Log.d(Main.TAG, "onStart() GameManagement Activity");
		super.onStart();
	}

	@Override
	protected void onStop() {
		Log.d(Main.TAG, "GameManagementActivity onStop()");
		super.onStop();

		if (this.getCustomDialog().isShowing()) {
			// Dieser Client wurde angefragt
			if (gc.getPendingRequestToPlayer().compareTo(gc.getUser()) == 0) {
				gc.request(gc.getPendingRequestFromPlayer(), "request_rejected");
			} else {
				gc.request(gc.getPendingRequestToPlayer(), "cancelrequest");
			}
			this.getCustomDialog().dismiss();
			gc.setPendingrequest(null, null, 0);
		}

	}

	public static GameServerApplication getGameServer() {
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
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);
			Editor e = pref.edit();
			e.putString("user", user);
			e.putString("password", pw);
			e.commit();
			onLogin();

		}

	}

	@Override
	public void updateResendLogin(JSONObject obj) {

	}

	@Override
	public void updateRegister(JSONObject obj) {

	}

	@Override
	public void updateRequest(final JSONObject obj) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG, "!!update request in GameManagement Activity command="
				+ obj.optString("command"));
		if (obj.optString("command").compareTo("request") == 0) {
			this.showRequestDialog(obj.optString("from_player"));

		} else if (obj.optString("command").compareTo("request_acknowledged") == 0) {
			Log.d(Main.TAG,
					"---> request acknowladged in GameManagement Activity");
			this.getCustomDialog().dismiss();
			gc.setPendingrequest(null, null, 0);
			startGame(false, obj.optString("from_player"));

		} else if (obj.optString("command").compareTo("request_finished") == 0) {
			Log.d(Main.TAG,
					"-----> request finished in GameManagement Activity");
			this.getCustomDialog().dismiss();
			gc.setPendingrequest(null, null, 0);
			startGame(true, obj.optString("from_player"));
		} else if (obj.optString("command").compareTo("request_rejected") == 0) {
			this.getCustomDialog().dismiss();
			gc.setPendingrequest(null, null, 0);
			Toast.makeText(
					getApplicationContext(),
					"Request rejected from player '"
							+ obj.optString("from_player") + "'!",
					Toast.LENGTH_LONG).show();
		} else if (obj.optString("command").compareTo("cancelrequest") == 0) {
			this.getCustomDialog().dismiss();
			Log.d(Main.TAG, "!!  Dialog ausgeschaltet!");
			gc.setPendingrequest(null, null, 0);

			Toast.makeText(
					getApplicationContext(),
					"Request canceled from player '"
							+ obj.optString("from_player") + "'",
					Toast.LENGTH_LONG).show();
		}

	}

	public void showRequestDialog(final String from) {

		this.getCustomDialog().setType(CustomDialogType.INFO);
		this.getCustomDialog().setContent("Request from player '" + from + "'");
		this.getCustomDialog().setPositiveMsg(
				this.getResources().getString(R.string.ok));
		this.getCustomDialog().setNegativeMsg(
				this.getResources().getString(R.string.reject));
		this.getCustomDialog().setOnCustomDialog(new CustomDialogListener() {

			@Override
			public void onNegativeButton() {
				gc.request(from, "request_rejected");
				Log.d(Main.TAG, "Request rejected!");
				gc.setPendingrequest(null, null, 0);

			}

			@Override
			public void onPositiveButton() {
				getCustomDialog().dismiss();
				getCustomDialog().setType(CustomDialogType.INFO);
				getCustomDialog().setContent("Wait for partner ");
				getCustomDialog().setPositiveMsg(null);
				getCustomDialog().setNegativeMsg(null);
				getCustomDialog().setCancelable(false);
				getCustomDialog().show();
				gc.request(from, "request_acknowledged");
			}

		});
		getCustomDialog().setCancelable(false);
		gc.setPendingrequest(from, gc.getUser(),
				GameServerApplication.REQUESTED);
		getCustomDialog().show();
	}

	private void startGame(boolean turn, String gegner) {
		Intent i = new Intent(this, Game.class);
		i.putExtra("start", turn);

		i.putExtra("gegner", gegner);
		startActivity(i);

	}

	@Override
	public void updateDisconnect() {
		Toast.makeText(getApplicationContext(), "updateDisconnect()",
				Toast.LENGTH_LONG).show();
		this.finish();// try activityname.finish instead of this
		setResult(100);

	}

	@Override
	public void connected() {
		if (gc.getState() != GameStates.LOGGED_IN) {
			SharedPreferences pref = PreferenceManager
					.getDefaultSharedPreferences(this);
			String user = pref.getString("user", null);
			String pw = pref.getString("password", null);
			gc.login(user, pw, Main.GAME);
		}

	}

	@Override
	public void connectionError() {
		Toast.makeText(getApplicationContext(), "ConnectionError()",
				Toast.LENGTH_LONG).show();
		getCustomDialog().setType(CustomDialogType.ERROR);
		getCustomDialog().setContent(
				this.getResources().getString(R.string.failed_connect));
		getCustomDialog().setPositiveMsg(null);
		getCustomDialog().setNegativeMsg(
				this.getResources().getString(R.string.retry));
		getCustomDialog().setOnCustomDialog(new CustomDialogListener() {

			@Override
			public void onNegativeButton() {
				reconnect();

			}

			@Override
			public void onPositiveButton() {

			}

		});
		getCustomDialog().setCancelable(false);
		getCustomDialog().show();
	}

	public void reconnect() {
		Log.d(Main.TAG, "**** GameManegementActivity reconnect!");
		startActivity(new Intent(this, Main.class));
		this.finish();
	}

	public abstract void onLogin();

}
