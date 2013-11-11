import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import javafx.scene.paint.Color;

//@author A0100927M
/**
 * 
 * This class provides static colours for the GUI in View.
 * 
 */
public class ColourPalette {
	
	/********************************* Colours for javafx **************************************/
	public static final Color fxWHITE = Color.WHITE;
	public static final Color fxNEAR_WHITE = Color.rgb(250, 250, 250);
	public static final Color occurrenceColour = Color.rgb(42, 186, 143);
			
	public static final Color[] defaultScheme = {Color.rgb(130, 255, 121), 
		fxWHITE, Color.rgb(18, 235, 166), Color.rgb(92, 190, 247), 
			Color.RED, Color.ORANGE, Color.ORCHID};
	public static final Color[] defaultNightScheme = {Color.rgb(89, 213, 100), 
		fxWHITE, Color.rgb(18, 235, 166), Color.rgb(92, 190, 247), 
			Color.RED, Color.ORANGE, Color.ORCHID};
	public static final Color[] goldfishScheme = {Color.rgb(250, 105, 0), 
		Color.WHITE, Color.rgb(224, 228, 204), Color.rgb(167, 219, 216), 
			Color.rgb(105, 210, 231),Color.rgb(105, 169, 231), 
				Color.rgb(243, 134, 48)};
	public static final Color[] brightScheme = {Color.rgb(250, 105, 0), fxWHITE, 
		Color.rgb(247, 196, 31), Color.rgb(224, 224, 90), Color.rgb(204, 243, 144), 
			Color.rgb(152, 242, 140), Color.rgb(252, 147, 10)};
	
	/******************************* Colours for javaswing ************************************/
	public static final StyleContext cont = StyleContext.getDefaultStyleContext();
	
	public static final java.awt.Color WHITE = java.awt.Color.white;
	public static final java.awt.Color BLACK = java.awt.Color.black;
	public static final java.awt.Color caretColour = new java.awt.Color(0, 0, 0, 0);
	public static final java.awt.Color cmdBackgroundColour = new java.awt.Color(50, 50, 50);
	
	public static final AttributeSet attrRed = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 41, 41));
	public static final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(84, 173, 225));
	public static final AttributeSet attrDarkCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(44, 62, 80));
	public static final AttributeSet attrDarkBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(5, 82, 199));
	public static final AttributeSet attrOrange = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 165, 0));
	public static final AttributeSet attrGreen = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(39, 174, 96));
	public static final AttributeSet attrCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(16, 217, 153));
	public static final AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(189, 195, 199));
	public static final AttributeSet attrMagenta = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(155, 89, 182));
	public static final AttributeSet attrRedNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(247, 139, 139));
	public static final AttributeSet attrBlueNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(110, 242, 243));
	public static final AttributeSet attrWhiteNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(252, 252, 252));
	public static final AttributeSet attrDarkBlueNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(66, 185, 254));
	public static final AttributeSet attrOrangeNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(254, 186, 63));
	public static final AttributeSet attrGreenNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(108, 248, 134));
	public static final AttributeSet attrCyanNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(63, 248, 189));
	public static final AttributeSet grayNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(220, 220, 220));
	public static final AttributeSet attrMagentaNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(238, 152, 233));
	public static final AttributeSet orangeGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(250, 105, 0));
	public static final AttributeSet lightOrangeGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(243, 134, 48));
	public static final AttributeSet beachGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(194, 202, 154));
	public static final AttributeSet paleBlueGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(167, 219, 216));
	public static final AttributeSet darkBlueGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(105, 169, 231));
	public static final AttributeSet blueGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(105,210,231));
	public static final AttributeSet purpleGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(105, 106, 231));
	public static final AttributeSet greyGF = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(143, 143, 143));
	public static final AttributeSet redBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 0, 61));
	public static final AttributeSet orangeBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(252, 147, 10));
	public static final AttributeSet yellowBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(247, 196, 31));
	public static final AttributeSet paleYellowBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(224, 224, 90));
	public static final AttributeSet paleGreenBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(204, 243, 144));
	public static final AttributeSet greenBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(152, 242, 140));
	public static final AttributeSet blueBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(196, 216, 248));
	public static final AttributeSet pinkBright = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(254, 203, 200));
	
	/******************************* Colour sets for javaswing ********************************/
	public static final AttributeSet[] defaultDaySchemeSwing = {attrGray, attrGreen, attrDarkCyan, 
		attrOrange, attrCyan, attrBlue, attrRed, attrDarkBlue, attrMagenta};
	public static final AttributeSet[] defaultNightSchemeSwing = {grayNight, attrGreenNight, 
		attrWhiteNight, attrOrangeNight, attrCyanNight, attrBlueNight, attrRedNight, attrDarkBlueNight,
		attrMagentaNight};
	public static final AttributeSet[] goldfishSchemeSwing = {attrGray, orangeGF, greyGF,
		darkBlueGF, beachGF, paleBlueGF, blueGF, purpleGF, lightOrangeGF};	
	public static final AttributeSet[] brightSchemeSwing = {grayNight, redBright, pinkBright,
		greenBright, yellowBright, paleYellowBright, paleGreenBright, blueBright, orangeBright};
	
}
