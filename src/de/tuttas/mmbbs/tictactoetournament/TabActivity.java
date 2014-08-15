package de.tuttas.mmbbs.tictactoetournament;

import de.mmbbs.R;
import android.content.Intent;
import android.os.Bundle;
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
        setContentView(R.layout.tab_layout);
        FontOverride.setDefaultFont(this, "DEFAULT", "fonts/Glametrix.otf");
		FontOverride.setDefaultFont(this, "MONOSPACE", "fonts/Glametrix.otf");
		FontOverride.setDefaultFont(this, "SANS_SERIF", "fonts/Glametrix.otf");
        TabHost tabHost = getTabHost();
         
        // Tab for Users
        TabSpec users = tabHost.newTabSpec(getResources().getString(R.string.users));
        // setting Title and Icon for the Tab
        users.setIndicator(getResources().getString(R.string.users), getResources().getDrawable(R.drawable.icon_users_tab));
        Intent usersIntent = new Intent(this, UserListActivity.class);
        users.setContent(usersIntent);
         
        // Tab for Chat
        TabSpec chat = tabHost.newTabSpec(getResources().getString(R.string.chat));
        // setting Title and Icon for the Tab
        chat.setIndicator(getResources().getString(R.string.chat), getResources().getDrawable(R.drawable.icon_chat_tab));
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chat.setContent(chatIntent);
         
        // Tab for Highscore
        TabSpec highscore = tabHost.newTabSpec(getResources().getString(R.string.highscore));
        // setting Title and Icon for the Tab
        highscore.setIndicator(getResources().getString(R.string.highscore), getResources().getDrawable(R.drawable.icon_highscore_tab));
        Intent highscoreIntent = new Intent(this, HighscoreActivity.class);
        highscore.setContent(highscoreIntent);
         
         
        // Adding all TabSpec to TabHost
        tabHost.addTab(users);
        tabHost.addTab(chat); 
        tabHost.addTab(highscore); 
    }
}
