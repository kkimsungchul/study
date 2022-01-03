package com.sungchul.macro;

import java.awt.Robot;
import java.util.HashMap;

public class MacroUtil {

	final HashMap<String, String> hashmap = new HashMap<String, String>();
	boolean shift = false;
	
	public MacroUtil() {
		hashmap.put("cancel","3");
		hashmap.put("backspace","8");
		hashmap.put("tab","9");
		hashmap.put("enter","10");
		hashmap.put("clear","12");
		hashmap.put("shift","16");
		hashmap.put("ctrl","17");
		hashmap.put("alt","18");
		hashmap.put("pause","19");
		hashmap.put("caps lock","20");
		hashmap.put("kana","21");
		hashmap.put("final","24");
		hashmap.put("kanji","25");
		hashmap.put("escape","27");
		hashmap.put("convert","28");
		hashmap.put("no convert","29");
		hashmap.put("accept","30");
		hashmap.put("mode change","31");
		hashmap.put("space","32");
		hashmap.put("page up","33");
		hashmap.put("page down","34");
		hashmap.put("end","35");
		hashmap.put("home","36");
		hashmap.put("left","37");
		hashmap.put("up","38");
		hashmap.put("right","39");
		hashmap.put("down","40");
		hashmap.put("comma","44");
		hashmap.put("minus","45");
		hashmap.put("period","46");
		hashmap.put("slash","47");
		hashmap.put("0","48");
		hashmap.put("1","49");
		hashmap.put("2","50");
		hashmap.put("3","51");
		hashmap.put("4","52");
		hashmap.put("5","53");
		hashmap.put("6","54");
		hashmap.put("7","55");
		hashmap.put("8","56");
		hashmap.put("9","57");
		hashmap.put("semicolon","59");
		hashmap.put("equals","61");
		hashmap.put("a","65");
		hashmap.put("b","66");
		hashmap.put("c","67");
		hashmap.put("d","68");
		hashmap.put("e","69");
		hashmap.put("f","70");
		hashmap.put("g","71");
		hashmap.put("h","72");
		hashmap.put("i","73");
		hashmap.put("j","74");
		hashmap.put("k","75");
		hashmap.put("l","76");
		hashmap.put("m","77");
		hashmap.put("n","78");
		hashmap.put("o","79");
		hashmap.put("p","80");
		hashmap.put("q","81");
		hashmap.put("r","82");
		hashmap.put("s","83");
		hashmap.put("t","84");
		hashmap.put("u","85");
		hashmap.put("v","86");
		hashmap.put("w","87");
		hashmap.put("x","88");
		hashmap.put("y","89");
		hashmap.put("z","90");
		hashmap.put("open bracket","91");
		hashmap.put("back slash","92");
		hashmap.put("close bracket","93");
		hashmap.put("numpad-0","96");
		hashmap.put("numpad-1","97");
		hashmap.put("numpad-2","98");
		hashmap.put("numpad-3","99");
		hashmap.put("numpad-4","100");
		hashmap.put("numpad-5","101");
		hashmap.put("numpad-6","102");
		hashmap.put("numpad-7","103");
		hashmap.put("numpad-8","104");
		hashmap.put("numpad-9","105");
		hashmap.put("numpad *","106");
		hashmap.put("numpad +","107");
		hashmap.put("numpad ","108");
		hashmap.put("numpad -","109");
		hashmap.put("numpad .","110");
		hashmap.put("numpad /","111");
		hashmap.put("f1","112");
		hashmap.put("f2","113");
		hashmap.put("f3","114");
		hashmap.put("f4","115");
		hashmap.put("f5","116");
		hashmap.put("f6","117");
		hashmap.put("f7","118");
		hashmap.put("f8","119");
		hashmap.put("f9","120");
		hashmap.put("f10","121");
		hashmap.put("f11","122");
		hashmap.put("f12","123");
		hashmap.put("delete","127");
		hashmap.put("dead grave","128");
		hashmap.put("dead acute","129");
		hashmap.put("dead circumflex","130");
		hashmap.put("dead tilde","131");
		hashmap.put("dead macron","132");
		hashmap.put("dead breve","133");
		hashmap.put("dead above dot","134");
		hashmap.put("dead diaeresis","135");
		hashmap.put("dead above ring","136");
		hashmap.put("dead double acute","137");
		hashmap.put("dead caron","138");
		hashmap.put("dead cedilla","139");
		hashmap.put("dead ogonek","140");
		hashmap.put("dead iota","141");
		hashmap.put("dead voiced sound","142");
		hashmap.put("dead semivoiced sound","143");
		hashmap.put("num lock","144");
		hashmap.put("scroll lock","145");
		hashmap.put("ampersand","150");
		hashmap.put("asterisk","151");
		hashmap.put("double quote","152");
		hashmap.put("less","153");
		hashmap.put("print screen","154");
		hashmap.put("insert","155");
		hashmap.put("help","156");
		hashmap.put("meta","157");
		hashmap.put("greater","160");
		hashmap.put("left brace","161");
		hashmap.put("right brace","162");
		hashmap.put("back quote","192");
		hashmap.put("quote","222");
		hashmap.put("up","224");
		hashmap.put("down","225");
		hashmap.put("left","226");
		hashmap.put("right","227");
		hashmap.put("alphanumeric","240");
		hashmap.put("katakana","241");
		hashmap.put("hiragana","242");
		hashmap.put("full-width","243");
		hashmap.put("half-width","244");
		hashmap.put("roman characters","245");
		hashmap.put("all candidates","256");
		hashmap.put("previous candidate","257");
		hashmap.put("code input","258");
		hashmap.put("japanese katakana","259");
		hashmap.put("japanese hiragana","260");
		hashmap.put("japanese roman","261");
		hashmap.put("kana lock","262");
		hashmap.put("input method on/off","263");
		hashmap.put("at","512");
		hashmap.put("colon","513");
		hashmap.put("circumflex","514");
		hashmap.put("dollar","515");
		hashmap.put("euro","516");
		hashmap.put("exclamation mark","517");
		hashmap.put("inverted exclamation mark","518");
		hashmap.put("left parenthesis","519");
		hashmap.put("number sign","520");
		hashmap.put("plus","521");
		hashmap.put("right parenthesis","522");
		hashmap.put("underscore","523");
		hashmap.put("windows","524");
		hashmap.put("context menu","525");
		hashmap.put("f13","61440");
		hashmap.put("f14","61441");
		hashmap.put("f15","61442");
		hashmap.put("f16","61443");
		hashmap.put("f17","61444");
		hashmap.put("f18","61445");
		hashmap.put("f19","61446");
		hashmap.put("f20","61447");
		hashmap.put("f21","61448");
		hashmap.put("f22","61449");
		hashmap.put("f23","61450");
		hashmap.put("f24","61451");
		hashmap.put("compose","65312");
		hashmap.put("begin","65368");
		hashmap.put("alt graph","65406");
		hashmap.put("stop","65480");
		hashmap.put("again","65481");
		hashmap.put("props","65482");
		hashmap.put("undo","65483");
		hashmap.put("copy","65485");
		hashmap.put("paste","65487");
		hashmap.put("find","65488");
		hashmap.put("cut","65489");
	
	}
	
	public String getValue(String key) {
		return hashmap.get(key);
	}
	
	public void customKeyPress(String key) {
		try {
			
			Robot robot = new Robot();
			int macroKey = Integer.parseInt(getValue(key));
			if(key.equalsIgnoreCase("shift")) {
				
				
				if(shift) {
					robot.keyRelease(macroKey);	
					shift = false;
				}else {
					robot.keyPress(macroKey);	
					shift = true;
				}
				
				
				
			}else {
				robot.keyPress(macroKey);
				robot.keyRelease(macroKey);	
			}
			
			
			
			robot.delay(100);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void customWordPress(String word) {
		String words[] = word.split(",");
		for(int i=0;i<words.length;i++) {
			customKeyPress(words[i]);
		}
		
		
	}
	
	
	
	
}
