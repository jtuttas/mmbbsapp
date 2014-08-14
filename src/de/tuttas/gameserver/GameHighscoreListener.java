package de.tuttas.gameserver;

import org.json.JSONObject;

public interface GameHighscoreListener {
	void updateHighscores(JSONObject obj);
}
