package de.mmbbs.tictactoetournament.game;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Object2D {

	protected int x=0;
	protected int y=0;
	protected Bitmap bitmap;
	protected Rect rect;
	private float getX;
	private float getY;
	private Acceleration xa,ya;
	
	public Object2D (int id,Context context) {
		bitmap = BitmapFactory.decodeResource(context.getResources(),id);
		rect = new Rect(x,y,x+bitmap.getWidth(),y+bitmap.getHeight());
	}
	
	public void resize(int width,int height) {
		bitmap = bitmap.createScaledBitmap(bitmap, width, height, false);
	}
	
	
	public void setAcceleration(Acceleration xa,Acceleration ya) {
		this.xa=xa;
		this.ya=ya;
	}
	
	public void tick() {
		if (xa!=null) xa.tick();
		if (ya!=null) ya.tick();
		if (xa!=null && ya!=null) this.setPosition(xa.getS(), ya.getS());
	}
	
	public void setPosition(int xPos, int yPos) {
		x=xPos;
		y=yPos;
		rect.set(x, y, x+bitmap.getWidth(), y+bitmap.getHeight());
	}
	
	public void paint(Canvas c,Paint p) {
		c.drawBitmap(bitmap, x, y, p);
	}
	
	
	public boolean dotInObject(int x,int y) {
		return rect.intersects(x, y, x, y);
	}
	
	public boolean collide(Object2D o) {
		return rect.intersects(rect, o.getRect());
	}

	public Rect getRect() {
		return rect;
	}

	public int getWidth() {
		return bitmap.getWidth();
	}
	
	public int getHeight() {
		return bitmap.getHeight();
	}
	
	public void left(int i) {
		this.setPosition(x-i, y);
	}

	public void right(int i) {
		this.setPosition(x+i, y);
	}

	public void up(int i) {
		this.setPosition(x, y-i);
	}
	public void down(int i) {
		this.setPosition(x, y+i);
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}
}
