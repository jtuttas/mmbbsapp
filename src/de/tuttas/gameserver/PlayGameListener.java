package de.tuttas.gameserver;

import org.json.JSONObject;

public interface PlayGameListener {

	void updateChat(JSONObject obj);

	void updatePlay(JSONObject obj);

}
