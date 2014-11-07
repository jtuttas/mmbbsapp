package de.mmbbs.tictactoetournament;

import com.google.android.gms.common.internal.safeparcel.SafeParcelable;

import de.mmbbs.R;
import de.mmbbs.gameserver.GameManagementActivity;
import de.mmbbs.gameserver.GameServerApplication;
import de.mmbbs.gameserver.GameStates;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class TabActivity extends android.app.TabActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.tab_layout_game);
        FontOverride.setDefaultFont(this, "DEFAULT", "fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "MONOSPACE", "fonts/Isserley-Bold.otf");
		FontOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Isserley-Bold.otf");
 
        Intent usersIntent = new Intent(this, UserListActivity.class);
        
        Bundle extras = getIntent().getExtras(); 
        TabHost tabHost = getTabHost();
        if(extras == null) {
            Log.d(Main.TAG," keine Extras!");
   
            
            // Tab for Users
            TabSpec users = tabHost.newTabSpec(getResources().getString(R.string.users));
            // setting Title and Icon for the Tab
            users.setIndicator(getResources().getString(R.string.users), getResources().getDrawable(R.drawable.icon_users_tab));
            
            Log.d(Main.TAG,"on Create in TabActivity");

            
            
            // Tab for Chat
            TabSpec chat = tabHost.newTabSpec(getResources().getString(R.string.chat));
            // setting Title and Icon for the Tab
            chat.setIndicator(getResources().getString(R.string.chat), getResources().getDrawable(R.drawable.icon_chat_tab));
            Intent chatIntent = new Intent(this, ChatActivity.class);
             
            // Tab for Highscore
            TabSpec highscore = tabHost.newTabSpec(getResources().getString(R.string.highscore));
            // setting Title and Icon for the Tab
            highscore.setIndicator(getResources().getString(R.string.highscore), getResources().getDrawable(R.drawable.icon_highscore_tab));
            Intent highscoreIntent = new Intent(this, HighscoreActivity.class);
             
                         // Adding all TabSpec to TabHost
            users.setContent(usersIntent);
            chat.setContent(chatIntent);
            highscore.setContent(highscoreIntent);
            tabHost.addTab(users);
            tabHost.addTab(chat); 
            tabHost.addTab(highscore); 
        } else {
            Log.d(Main.TAG," extra command is "+extras.getString("command"));
            usersIntent.putExtra("command", extras.getString("command"));
            usersIntent.putExtra("from_player", extras.getString("from_player"));
            usersIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //users.setContent(usersIntent);
            //GameManagementActivity currentActivity = (GameManagementActivity) getLocalActivityManager().getActivity(tabHost.getCurrentTabTag());
            //currentActivity.showRequestDialog(extras.getString("from_player"));
            // Tab for Users
            TabSpec users = tabHost.newTabSpec(getResources().getString(R.string.users));
            // setting Title and Icon for the Tab
            users.setIndicator(getResources().getString(R.string.users), getResources().getDrawable(R.drawable.icon_users_tab));
            usersIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Log.d(Main.TAG,"on Create in TabActivity");

            
            
            // Tab for Chat
            TabSpec chat = tabHost.newTabSpec(getResources().getString(R.string.chat));
            // setting Title and Icon for the Tab
            chat.setIndicator(getResources().getString(R.string.chat), getResources().getDrawable(R.drawable.icon_chat_tab));
            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
            // Tab for Highscore
            TabSpec highscore = tabHost.newTabSpec(getResources().getString(R.string.highscore));
            // setting Title and Icon for the Tab
            highscore.setIndicator(getResources().getString(R.string.highscore), getResources().getDrawable(R.drawable.icon_highscore_tab));
            Intent highscoreIntent = new Intent(this, HighscoreActivity.class);
            highscoreIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP); 
                         // Adding all TabSpec to TabHost
            users.setContent(usersIntent);
            chat.setContent(chatIntent);
            highscore.setContent(highscoreIntent);
            tabHost.addTab(users);
            tabHost.addTab(chat); 
            tabHost.addTab(highscore); 
        }
 
	
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		//GameServerApplication gs = (GameServerApplication)this.getApplication();
		//if (gs.getState()!=GameStates.PLAY)	gs.disconnect();
		GameServerApplication gs = (GameServerApplication)this.getApplication();
		gs.setActivityVisible(false);
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		GameServerApplication gs = (GameServerApplication)this.getApplication();
		gs.disconnect();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		GameServerApplication gs = (GameServerApplication)this.getApplication();
		gs.setActivityVisible(true);
	}
	
	
	
	
}
