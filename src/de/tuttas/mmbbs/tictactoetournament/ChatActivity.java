package de.tuttas.mmbbs.tictactoetournament;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import de.mmbbs.R;
import de.tuttas.mmbbs.gameserver.GameChatListener;
import de.tuttas.mmbbs.gameserver.GameServer;
import de.tuttas.mmbbs.gameserver.GameServerListener;
import de.tuttas.mmbbs.gameserver.GameStates;
import de.tuttas.mmbbs.gameserver.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class ChatActivity extends GameManagementActivity implements OnEditorActionListener,GameChatListener{
	

	private DiscussArrayAdapter adapter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Main.TAG,"onCreate() Chat Activity");
		setContentView(R.layout.chat_layout);
		
		EditText et = (EditText) this.findViewById(R.id.editText_chat);
		et.setOnEditorActionListener(this);		
		ListView lv = (ListView) findViewById(R.id.listView1);
		adapter = new DiscussArrayAdapter(getApplicationContext(), R.layout.chatline);
		lv.setAdapter(adapter);

	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(Main.TAG,"onResume() Chat Activity");
		gc.setChatCallbacks(this, handler);
	}

	public void send(View v) {
		EditText et = (EditText) this.findViewById(R.id.editText_chat);
		gc.sendGameChat(et.getText().toString());
		et.setText("");
	}
	

	@Override
	public void updateGameChat(JSONObject obj) {
		Log.d(Main.TAG,"updateGameChat in ChatActivity");
		// TODO Auto-generated method stub
		adapter.add(new OneComment(gc.getUser(),obj.optString("from_player"), obj.optString("content")));
		ListView lv = (ListView) findViewById(R.id.listView1);
		lv.setSelection(adapter.getCount() - 1);
	}

	@Override
	public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"on Editor Action:"+arg0);
		if (arg2.getAction()==KeyEvent.ACTION_DOWN) {
			this.send(arg0);
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(arg0.getWindowToken(), 0);
			return true;
		}
		return false;
	}

}
