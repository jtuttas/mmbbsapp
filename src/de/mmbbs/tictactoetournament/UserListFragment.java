package de.mmbbs.tictactoetournament;

import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.mmbbs.R;
import de.mmbbs.gameserver.GameManagementActivity;
import de.mmbbs.gameserver.GameServerApplication;
import de.mmbbs.gameserver.GameServerListener;
import de.mmbbs.gameserver.GameStates;
import de.mmbbs.gameserver.GameUserListener;
import de.mmbbs.gameserver.User;
import de.mmbbs.gameserver.UserState;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class UserListFragment extends Fragment implements OnItemClickListener, TextWatcher,GameUserListener{

	private GameServerApplication gc;
	private UserListArrayAdapter adapter;
	private DBManager dbm;
	private Handler handler;
	private View rootView;
	private CustomDialogClass customDialog;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.user_list_layout, container, false);
		ListView lv = (ListView) rootView.findViewById(R.id.listView_users);
		gc = (GameServerApplication) getActivity().getApplication();
		adapter = new UserListArrayAdapter(getActivity().getApplicationContext(), R.layout.user_line,gc.getUserList());
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setTextFilterEnabled(true);

		this.registerForContextMenu(lv);
		dbm = new DBManager(getActivity(), "friends.db", null, 1);
		Log.d(Main.TAG," onCreate() userlist size="+gc.getUserList().size());
		TextView tv = (TextView) rootView.findViewById(R.id.textView_number_of_users);
		tv.setText(Integer.toString(gc.getUserList().size()));
		EditText et = (EditText) rootView.findViewById(R.id.editText_user_filter);
		et.addTextChangedListener(this);
		
		handler = new Handler();
		gc.setUserCallbacks(this, handler);
		this.rootView=rootView;
        return rootView;
    }

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.d(Main.TAG," Context Selected userlist size="+gc.getUserList().size());
		ListView lv = (ListView) rootView.findViewById(R.id.listView_users);
		User user = (User) adapter.getItem((int) info.id);
		TextView playerfriend; 
		 switch (item.getItemId()) {
	        case R.id.item_add_friend:
	        	dbm.addFriend(user.getName());
	        	user.setFriend(true);
	        	gc.getUserList().set((int) info.id, user);
	        	adapter.notifyDataSetChanged();
	        	return true;
	        case R.id.item_remove_friend:
	        	dbm.removeFriend(user.getName());
	        	user.setFriend(false);
	        	gc.getUserList().set((int) info.id, user);
				adapter.getFilter().filter(((EditText) rootView.findViewById(R.id.editText_user_filter)).getText());
	        	break;
	        case R.id.item_play_with:
	        	requestPlayer(info.id);
	        return true;
	    }
		return super.onContextItemSelected(item);
	}

	
	private void requestPlayer(long index) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG," Request Position="+index);
		requestPlayer(adapter.getItem((int) index));
		
	}
	private void requestPlayer(final User u) {
		// TODO Auto-generated method stub
		if (u.getState()==UserState.FREE) {
			customDialog = new CustomDialogClass(getActivity(),CustomDialogType.INFO ,getResources().getString(R.string.request_to_player)+"'"+u.getName()+"'",
					null,this.getResources().getString(R.string.cancel));
			customDialog.setOnCustomDialog(new CustomDialogListener() {


				@Override
				public void onNegativeButton() {
			    	gc.request(u.getName(), "cancelrequest");
					gc.setPendingrequest(null, null);

				}

				@Override
				public void onPositiveButton() {
					
				}
				
			});
			customDialog.setCancelable(false);
			customDialog.show();
			gc.setPendingrequest(gc.getUser(), u.getName());
	    	gc.request(u.getName(), "request");
		}
		else {
			Toast.makeText(getActivity(),getResources().getString(R.string.not_a_free_player), Toast.LENGTH_LONG).show();				
		}

	}
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		final User u = adapter.getItem(pos);
		Log.d(Main.TAG,"Geklick auf "+u.getName());
		requestPlayer(u);
		
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		Log.d(Main.TAG," Text Changed cras="+arg0);
		adapter.getFilter().filter(arg0);		

		
	}

	@Override
	public void updateUsers(List<User> userlist) {
		Log.d(Main.TAG,"updateUser in UserList Fragment");
		for (int i=0;i<userlist.size();i++) {
			User user = userlist.get(i);
			if (dbm.isFriend(user.getName())) {
				user.setFriend(true);
			}
			else {
				user.setFriend(false);
			}
		}
		adapter.clear();
		adapter.setUserList(gc.getUserList());
		adapter.getFilter().filter(((EditText) rootView.findViewById(R.id.editText_user_filter)).getText());
		TextView tv = (TextView) rootView.findViewById(R.id.textView_number_of_users);
		tv.setText(Integer.toString(gc.getUserList().size()));
		Log.d(Main.TAG,"updateUser in UserList Activity userlist size="+gc.getUserList().size());
	}

	
	
	
	/*
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(Main.TAG,"onCreate() UserList Activity");

		
		//Toast.makeText(getApplicationContext(),"onCretae()", Toast.LENGTH_LONG).show();
		setContentView(R.layout.user_list_layout);
		
		ListView lv = (ListView) findViewById(R.id.listView_users);
		adapter = new UserListArrayAdapter(getApplicationContext(), R.layout.user_line,gc.getUserList());
		lv.setAdapter(adapter);
		lv.setOnItemClickListener(this);
		lv.setTextFilterEnabled(true);
		this.registerForContextMenu(lv);
		dbm = new DBManager(this, "friends.db", null, 1);
		Log.d(Main.TAG," onCreate() userlist size="+gc.getUserList().size());
		TextView tv = (TextView) findViewById(R.id.textView_number_of_users);
		tv.setText(Integer.toString(gc.getUserList().size()));
		EditText et = (EditText) findViewById(R.id.editText_user_filter);
		et.addTextChangedListener(this);
	}
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		Log.d(Main.TAG,"onResume() UserList Activity conected="+gc.isConnected());
		gc.setUserCallbacks(this, handler);
		if (cdd != null && cdd.isShowing()) cdd.dismiss();
		Bundle extras = getIntent().getExtras();
		if(extras == null) {
		        
		} else {
			if (extras.getString("command").compareTo("request")==0) {
				super.showRequestDialog(extras.getString("from_player"));
				getIntent().removeExtra("command");
				getIntent().removeExtra("from_player");
		    }
		}
		
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
				
				adapter.getFilter().filter(((EditText) findViewById(R.id.editText_user_filter)).getText());
			}
			else {
				Log.d(Main.TAG," Friends olny is unchecked ");
				item.setChecked(true);
				adapter.setFriendsOnly(true);
				adapter.getFilter().filter(((EditText) findViewById(R.id.editText_user_filter)).getText());
				
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		Log.d(Main.TAG," Context Selected userlist size="+gc.getUserList().size());
		ListView lv = (ListView) findViewById(R.id.listView_users);
		User user = (User) adapter.getItem((int) info.id);
		TextView playerfriend; 
		 switch (item.getItemId()) {
	        case R.id.item_add_friend:
	        	dbm.addFriend(user.getName());
	        	user.setFriend(true);
	        	gc.getUserList().set((int) info.id, user);
	        	adapter.notifyDataSetChanged();
	        	return true;
	        case R.id.item_remove_friend:
	        	dbm.removeFriend(user.getName());
	        	user.setFriend(false);
	        	gc.getUserList().set((int) info.id, user);
				adapter.getFilter().filter(((EditText) findViewById(R.id.editText_user_filter)).getText());
	        	break;
	        case R.id.item_play_with:
	        	requestPlayer(info.id);
	        return true;
	    }
		return super.onContextItemSelected(item);
	}
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		getMenuInflater().inflate(R.menu.player_context, menu);
		AdapterView.AdapterContextMenuInfo acmi = (AdapterContextMenuInfo) menuInfo;
		ListView lv = (ListView) findViewById(R.id.listView_users);
		User user = (User) adapter.getItem(acmi.position);
		Log.d(Main.TAG,"Create Context Menu for user "+user.getName()+ "user list size="+gc.getUserList().size());
		if (user.isFriend()) {
			menu.findItem(R.id.item_add_friend).setVisible(false);
		}
		else {
			menu.findItem(R.id.item_remove_friend).setVisible(false);
		}
		
		super.onCreateContextMenu(menu, v, menuInfo);
	}

	
	private void requestPlayer(long index) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG," Request Position="+index);
		requestPlayer(adapter.getItem((int) index));
		
	}
	
	@Override
	public void updateUsers(List<User> userlist) {
		Log.d(Main.TAG,"updateUser in UserList Activity");
		for (int i=0;i<userlist.size();i++) {
			User user = userlist.get(i);
			if (dbm.isFriend(user.getName())) {
				user.setFriend(true);
			}
			else {
				user.setFriend(false);
			}
		}
		adapter.clear();
		adapter.setUserList(gc.getUserList());
		adapter.getFilter().filter(((EditText) findViewById(R.id.editText_user_filter)).getText());
		TextView tv = (TextView) findViewById(R.id.textView_number_of_users);
		tv.setText(Integer.toString(gc.getUserList().size()));
		Log.d(Main.TAG,"updateUser in UserList Activity userlist size="+gc.getUserList().size());
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		// TODO Auto-generated method stub
		final User u = adapter.getItem(pos);
		Log.d(Main.TAG,"Geklick auf "+u.getName());
		requestPlayer(u);
	}

	
	private void requestPlayer(final User u) {
		// TODO Auto-generated method stub
		if (u.getState()==UserState.FREE) {
			cdd = new CustomDialogClass(this,CustomDialogType.INFO ,getResources().getString(R.string.request_to_player)+"'"+u.getName()+"'",
					null,this.getResources().getString(R.string.cancel));
			cdd.setOnCustomDialog(new CustomDialogListener() {


				@Override
				public void onNegativeButton() {
			    	gc.request(u.getName(), "cancelrequest");
					gc.setPendingrequest(null, null);

				}

				@Override
				public void onPositiveButton() {
					
				}
				
			});
			cdd.setCancelable(false);
			cdd.show();
			gc.setPendingrequest(gc.getUser(), u.getName());
	    	gc.request(u.getName(), "request");
		}
		else {
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.not_a_free_player), Toast.LENGTH_LONG).show();				
		}

	}
	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG," Text Changed cras="+arg0);
		adapter.getFilter().filter(arg0);		
	}
	
	@Override
	public void onLogin() {
		// TODO Auto-generated method stub
		
	}
	*/
}
