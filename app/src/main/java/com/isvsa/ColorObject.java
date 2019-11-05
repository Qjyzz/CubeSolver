package com.isvsa;

import android.graphics.Color;
//获取颜色RGB
public class ColorObject {
	private int R;
	private int G;
	private int B;
	public ColorObject(int color){
		this.R = Color.red(color);
		this.G = Color.green(color);
		this.B = Color.blue(color);
	}
	public int getColor(){ return Color.rgb(R, G, B); }//返回color类型的RGB
	public int getR() { return R; }
	public void setR(int r) {
		R = r;
	}
	public int getG() {
		return G;
	}
	public void setG(int g) { G = g; }
	public int getB() {
		return B;
	}
	public void setB(int b) {
		B = b;
	}
	
}
