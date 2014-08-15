package de.tuttas.mmbbs.tictactoetournament.game;

import org.json.JSONObject;

import com.google.android.gms.ads.*;


import de.tuttas.mmbbs.gameserver.GameStates;
import de.tuttas.mmbbs.gameserver.PlayGameListener;
import de.tuttas.mmbbs.tictactoetournament.CustomDialogClass;
import de.tuttas.mmbbs.tictactoetournament.CustomDialogListener;
import de.tuttas.mmbbs.tictactoetournament.CustomDialogType;
import de.tuttas.mmbbs.tictactoetournament.GameManagementActivity;
import de.tuttas.mmbbs.tictactoetournament.Main;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
//kommentar 2
public class Game extends GameManagementActivity implements PlayGameListener,GameListener {
	private InterstitialAd interstitial;
	Leinwand l;
	private CustomDialogClass cd;
	private static Handler ghandler; 
	private boolean firstTurn;
	private String gegner;
	
	 @Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(de.mmbbs.R.layout.game);
        ghandler=new Handler();
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            gegner= extras.getString("gegner");
            firstTurn=extras.getBoolean("start");
        } else {
            gegner= (String) savedInstanceState.getSerializable("gegner");
            firstTurn= Boolean.parseBoolean((String) savedInstanceState.getSerializable("start"));
        }
        Log.d(Main.TAG,"game onCreate() start="+firstTurn+" gegner="+gegner);
        interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId("ca-app-pub-5414170988828485/9840893458");
	 // Set the AdListener.
	    interstitial.setAdListener(new AdListener() {
	      @Override
	      public void onAdLoaded() {
    		
	      }

	      @Override
	      public void onAdFailedToLoad(int errorCode) {
	      }
	    });
	    
	    // Create ad request.
	    
	    AdRequest adRequest = new AdRequest.Builder()
        .addTestDevice("ca-app-pub-5414170988828485/9840893458")
        .build();

	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);
	}
	
	 


	@Override
	protected void onResume() {
        Log.d(Main.TAG,"game onResume()");
		// TODO Auto-generated method stub
		gc.setGameCallbacks(this, ghandler);
		super.onResume();
        l=(Leinwand) this.findViewById(de.mmbbs.R.id.gui);
        Display display = getWindowManager().getDefaultDisplay(); 
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        l.setListener(this,ghandler);
        l.init(firstTurn,gegner);
        l.reset(width,height);
	}


	@Override
	protected void onStart() {
        Log.d(Main.TAG,"game onStart()");
		// TODO Auto-generated method stub
		super.onStart();
		if (gegner==null) this.finish();

		
	}


	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.setDisconnectOnStop(false);
		super.onStop();
		l.exit();
		gc.quitPaaring();
		gegner=null;
	}

	 @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.d(Main.TAG,"Game Activity onDestoy()");
	}




	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		 //getMenuInflater().inflate(R.menu.gamemenu, menu);
		 return super.onCreateOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		/*
		switch (item.getItemId()) {
		case R.id.help:
			Log.d(Main.TAG,"Help!!");
			return true;
		case R.id.back:
			finish();
			return true;
		}
		*/
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	 // Invoke displayInterstitial() when you are ready to display an interstitial.
	  public void displayInterstitial() {
		gc.quitPaaring();				
		//onBackPressed();
		super.setDisconnectOnStop(false);
	    if (interstitial.isLoaded()) {
	      interstitial.show();
	    }
	  }

	@Override
	public void showDialog(String msg) {
		// TODO Auto-generated method stub
		cd = new CustomDialogClass(this,CustomDialogType.INFO ,msg,
				this.getResources().getString(de.mmbbs.R.string.ok),null);
		cd.setOnCustomDialog(new CustomDialogListener() {


			@Override
			public void onNegativeButton() {

			}

			@Override
			public void onPositiveButton() {
				displayInterstitial();
			}
			
		});
		cd.setCancelable(false);
		cd.show();

	}


	@Override
	public void updateChat(JSONObject obj) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void updateDisconnect() {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"updateDiscopnnect() Game state="+gc.getState());
		this.showDialog(getResources().getString(de.mmbbs.R.string.player_disconnected));
		gc.stats(1, 1, 0);
		gc.addScore(l.getScore());

	}




	@Override
	public void updatePlay(JSONObject obj) {
		// TODO Auto-generated method stub
		Log.d(Main.TAG,"updatePlay GameActivity");
		if (obj.optString("command").compareTo("timeout")==0) {
			this.showDialog(getResources().getString(de.mmbbs.R.string.player_timedout));			
			gc.stats(1, 1, 0);
			gc.addScore(l.getScore());
			
		}
		else if (obj.optString("command").compareTo("close")==0) {
			this.showDialog(getResources().getString(de.mmbbs.R.string.player_closed));
			gc.stats(1, 1, 0);
			gc.addScore(l.getScore());
		}
		else if (obj.optString("command").compareTo("play")==0 ||
				obj.optString("command").compareTo("won")==0 ||
				obj.optString("command").compareTo("penalty")==0
				) {
			int turnx=obj.optInt("current_turnx");
			int turny=obj.optInt("current_turny");
			l.moveTo(turnx,turny);			
		}
	}




	@Override
	public void setLeftPlayer(Player p) {
		// TODO Auto-generated method stub
		TextView tv = (TextView) this.findViewById(de.mmbbs.R.id.textViewplayerLeft);
		ImageView iv = (ImageView) this.findViewById(de.mmbbs.R.id.imageViewplayerLeft);
		tv.setText(p.getName());
		iv.setImageResource(p.getIcon());
	}




	@Override
	public void setRightPlayer(Player p) {
		TextView tv = (TextView) this.findViewById(de.mmbbs.R.id.textViewplayerRight);
		ImageView iv = (ImageView) this.findViewById(de.mmbbs.R.id.imageViewplayerRight);
		tv.setText(p.getName());
		iv.setImageResource(p.getIcon());
	}




	@Override
	public void setScore(int score) {
		// TODO Auto-generated method stub
		((TextView) this.findViewById(de.mmbbs.R.id.textView_score)).setText(Integer.toString(score));
		
	}


	@Override
	public void setProgessBar(int left, int right) {
		// TODO Auto-generated method stub
		ProgressBar pbl = (ProgressBar) this.findViewById(de.mmbbs.R.id.progressBarleft);
		ProgressBar pbr = (ProgressBar) this.findViewById(de.mmbbs.R.id.ProgressBarright);
		pbl.setProgress(left);
		pbr.setProgress(right);
	}

	public static Handler getHandler() {
		return ghandler;
				
	}
	
}
