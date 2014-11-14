package de.mmbbs.tictactoetournament;

import java.util.List;

import org.json.JSONObject;

import de.mmbbs.R;
import de.mmbbs.gameserver.GameServerApplication;
import de.mmbbs.gameserver.GameServerListener;
import de.mmbbs.gameserver.GameStates;
import de.mmbbs.gameserver.GameUserListener;
import de.mmbbs.gameserver.User;
import de.mmbbs.gameserver.UserState;
import de.mmbbs.tictactoetournament.game.Game;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
public class FragmentActivity extends Activity implements  GameUserListener, GameServerListener {
	// Declaring our tabs and the corresponding fragments.
		ActionBar.Tab userTab, chatTab,highscoreTab;
		Fragment userListFragment = new UserListFragment();
		Fragment chatFragment = new ChatFragment();
		Fragment highscoreFragment = new HighscoreFragment();
		private GameServerApplication gc;
		private UserListArrayAdapter adapter;

		protected CustomDialogClass cdd;
		private FragmentActivity instance;
		private Handler handler;
		
		@Override
		protected void onCreate(Bundle savedInstanceState) {
			
			
			super.onCreate(savedInstanceState);
			Log.d(Main.TAG,"FragmentActivity onCreate()");
			requestWindowFeature(Window.FEATURE_ACTION_BAR);
			setContentView(R.layout.activity_main);
			
			// Asking for the default ActionBar element that our platform supports.
			ActionBar actionBar = this.getActionBar();
			Log.d(Main.TAG,"FragmentActivity onCreate() actionBar="+actionBar);
	        // Screen handling while hiding ActionBar icon.
	        actionBar.setDisplayShowHomeEnabled(false);
	 
	        // Screen handling while hiding Actionbar title.
	        actionBar.setDisplayShowTitleEnabled(false);
	 
	        // Creating ActionBar tabs.
	        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
	 
	        // Setting custom tab icons.
	        userTab = actionBar.newTab().setIcon(R.drawable.add_friend);
	        chatTab = actionBar.newTab().setIcon(R.drawable.add_friend);
	        highscoreTab = actionBar.newTab().setIcon(R.drawable.add_friend);

	        
	        // Setting tab listeners.
	        userTab.setTabListener(new TabListener(userListFragment));
	        chatTab.setTabListener(new TabListener(chatFragment));
	        highscoreTab.setTabListener(new TabListener(highscoreFragment));
	        
	        // Adding tabs to the ActionBar.
	        actionBar.addTab(userTab);
	        actionBar.addTab(chatTab);
	        actionBar.addTab(highscoreTab);
	        
			gc=(GameServerApplication) this.getApplication();
			handler = new Handler();
			gc.setUserCallbacks(this, handler);

		}

		@Override
		protected void onResume() {
			// TODO Auto-generated method stub
			super.onResume();
			gc.setActivityVisible(true);
			Log.d(Main.TAG,"onResume() FragmentActivity conected="+gc.isConnected());
			gc.setUserCallbacks(this, handler);
			if (cdd != null && cdd.isShowing()) cdd.dismiss();
			Bundle extras = getIntent().getExtras();
			if(extras == null) {
			        
			} else {
				if (extras.getString("command").compareTo("request")==0) {
					showRequestDialog(extras.getString("from_player"));
					getIntent().removeExtra("command");
					getIntent().removeExtra("from_player");
			    }
			}
			
		}
		
		
		
		@Override
		protected void onStop() {
			gc.setActivityVisible(false);
			super.onStop();
		}

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.user_menu, menu);
			return super.onCreateOptionsMenu(menu);
		}
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			// TODO Auto-generated method stub
			switch (item.getItemId()) {
			case R.id.item_friends_only:
				if (item.isChecked()) {
					Log.d(Main.TAG," Friends olny is checked ");
					item.setChecked(false);
					adapter.setFriendsOnly(false);
					
					adapter.getFilter().filter(((EditText) userListFragment.getView().findViewById(R.id.editText_user_filter)).getText());
				}
				else {
					Log.d(Main.TAG," Friends olny is unchecked ");
					item.setChecked(true);
					adapter.setFriendsOnly(true);
					adapter.getFilter().filter(((EditText) userListFragment.getView().findViewById(R.id.editText_user_filter)).getText());
					
				}
				break;
			}
			return super.onOptionsItemSelected(item);
		}

		public void showRequestDialog(final String from) {
			cdd = new CustomDialogClass(this,CustomDialogType.INFO ,"Request from player '"+from+"'",
					this.getResources().getString(R.string.ok),this.getResources().getString(R.string.reject));
			cdd.setOnCustomDialog(new CustomDialogListener() {


				@Override
				public void onNegativeButton() {
					gc.request(from, "request_rejected");
					Log.d(Main.TAG,"Request rejected!");
					gc.setPendingrequest(null, null);

				}

				@Override
				public void onPositiveButton() {
					if (cdd!=null) cdd.dismiss();
					cdd = new CustomDialogClass(instance,CustomDialogType.INFO ,"Wait for partner ",null,null);
					cdd.setCancelable(false);
					cdd.show();
					gc.request(from, "request_acknowledged");
//			    	startGame(true,obj.optString("from_player"));									
				}
				
			});
			cdd.setCancelable(false);
			gc.setPendingrequest(from, gc.getUser());
			cdd.show();
		}
		


		@Override
		public void updateUsers(List<User> userlist) {
			
			
		}

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

		private void onLogin() {
			// TODO Auto-generated method stub
			
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
		public void updateRequest(JSONObject obj) {
			// TODO Auto-generated method stub
			Log.d(Main.TAG,"!!update request in GameManagement Activity command="+obj.optString("command"));
			if (obj.optString("command").compareTo("request")==0) {
				this.showRequestDialog(obj.optString("from_player"));
				
				
			}
			else if (obj.optString("command").compareTo("request_acknowledged")==0) {
				Log.d(Main.TAG,"---> request acknowladged in GameManagement Activity");
				if (cdd!=null) cdd.dismiss();
				gc.setPendingrequest(null, null);
		    	startGame(false,obj.optString("from_player"));							
				
			}
			else if (obj.optString("command").compareTo("request_finished")==0) {
				Log.d(Main.TAG,"-----> request finished in GameManagement Activity");
				if (cdd!=null) cdd.dismiss();
				gc.setPendingrequest(null, null);
		    	startGame(true,obj.optString("from_player"));										
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
		
}
