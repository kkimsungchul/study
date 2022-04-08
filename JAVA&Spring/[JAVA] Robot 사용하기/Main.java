package com.sungchul.main;

import java.util.Timer;
import java.util.TimerTask;

import com.sungchul.macro.MacroUtil;

public class Main {
	
	
	public static void main(String[]args) {

		String macroWord = "windows,c,m,d,enter,i,p,shift,c,o,n,shift,f,i,g,enter";
		
		MacroUtil macroUtil = new MacroUtil();
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				macroUtil.customWordPress(macroWord);
			}
		};
		timer.schedule(timerTask, 0,5000);
	}
}
