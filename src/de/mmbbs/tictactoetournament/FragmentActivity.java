package de.mmbbs.tictactoetournament;

import java.util.List;

import org.json.JSONObject;

import de.mmbbs.R;
import de.mmbbs.gameserver.GameManagementActivity;
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
public class FragmentActivity extends GameManagementActivity {
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
			//Log.d(Main.TAG," Fragment Activity on Create() "+savedInstanceState.getString("command"));
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
			

		}

		@Override
		protected void onStart() {
			// TODO Auto-generated method stub
			super.onResume();
			gc.setActivityVisible(true);
			Log.d(Main.TAG,"onStart() FragmentActivity conected="+gc.isConnected());
			
			if (cdd != null && cdd.isShowing()) cdd.dismiss();
			
			Bundle extras = getIntent().getExtras();
			if(extras == null) {
			        Log.d(Main.TAG,":-( kein Extra ");
			        
			} else {
				if (extras.getString("command").compareTo("request")==0) {
					showRequestDialog(extras.getString("from_player"));
					getIntent().removeExtra("command");
					getIntent().removeExtra("from_player");
			    }
				else {
					Log.d(Main.TAG,"keine richtigen extras ->"+extras.getString("command"));
				}
			}
			
		}
		
		
		
		@Override
		protected void onNewIntent(Intent intent) {
			// TODO Auto-generated method stub
			super.onNewIntent(intent);
			Bundle extras = intent.getExtras();
			if(extras == null) {
			        Log.d(Main.TAG,":-( onNewIntent kein Extra "+intent.getStringExtra("command"));
			        
			} else {
				if (extras.getString("command").compareTo("request")==0) {
					showRequestDialog(extras.getString("from_player"));
					getIntent().removeExtra("command");
					getIntent().removeExtra("from_player");
			    }
				else {
					Log.d(Main.TAG,"keine richtigen extras ->"+extras.getString("command"));
				}
			}
		}

		@Override
		protected void onStop() {
			
			super.onStop();
			gc.setActivityVisible(false);
		}

		
		
		@Override
		protected void onDestroy() {
			// TODO Auto-generated method stub
			super.onDestroy();
			gc.setServerCallbacks(null, null);
			
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
		public void onLogin() {
			// TODO Auto-generated method stub
			
		}
		


		

		
}
