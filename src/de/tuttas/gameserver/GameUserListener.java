package de.tuttas.gameserver;

import java.util.List;

import org.json.JSONObject;

public interface GameUserListener {
	void updateUsers(List<User> userlist);
	

}