package de.tuttas.mmbbs.gameserver;

import org.json.JSONObject;

public interface GameHighscoreListener {
	void updateHighscores(JSONObject obj);
}
