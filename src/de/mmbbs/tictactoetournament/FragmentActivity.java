package de.mmbbs.tictactoetournament;

import de.mmbbs.R;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
public class FragmentActivity extends Activity {
	// Declaring our tabs and the corresponding fragments.
		ActionBar.Tab userTab, chatTab;
		Fragment userListFragment = new UserListFragment();
		Fragment chatFragment = new ChatFragment();
		
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

	        
	        // Setting tab listeners.
	        userTab.setTabListener(new TabListener(userListFragment));
	        chatTab.setTabListener(new TabListener(chatFragment));
	        
	        // Adding tabs to the ActionBar.
	        actionBar.addTab(userTab);
	        actionBar.addTab(chatTab);
	        
		}
}
