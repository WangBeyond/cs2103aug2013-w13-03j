import javax.swing.text.AttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import javafx.scene.paint.Color;

public class ColourPalette {
	
	/********************************* Colours for javafx **************************************/
	public static final Color[] defaultScheme = {Color.rgb(130, 255, 121), 
		Color.WHITE, Color.rgb(18, 235, 166), Color.rgb(92, 190, 247), 
			Color.RED, Color.ORANGE, Color.ORCHID};
	public static final Color[] defaultNightScheme = {Color.rgb(89, 213, 100), 
		Color.WHITE, Color.rgb(18, 235, 166), Color.rgb(92, 190, 247), 
			Color.RED, Color.ORANGE, Color.ORCHID};
	public static final Color[] gamebookersScheme = {Color.rgb(255, 153, 0), 
		Color.WHITE, Color.rgb(233, 233, 233), Color.rgb(188, 188, 188), 
			Color.rgb(50, 153, 187),Color.rgb(0, 153, 153), 
				Color.rgb(253, 212, 86)};
	
	/******************************* Colours for javaswing ************************************/
	public static final StyleContext cont = StyleContext.getDefaultStyleContext();
	
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
	public static final AttributeSet attrGrayNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(220, 220, 220));
	public static final AttributeSet attrMagentaNight = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(238, 152, 233));
	public static final AttributeSet orangeGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 153, 0));
	public static final AttributeSet yellowGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 204, 51));
	public static final AttributeSet blueGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(50, 153, 187));
	public static final AttributeSet paleGrayGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(97, 97, 97));
	public static final AttributeSet grayGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(58, 58, 58));
	public static final AttributeSet tealGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(0, 153, 153));
	public static final AttributeSet purpleGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(122, 32, 119));
	public static final AttributeSet blackGB = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(9, 9, 9));
	
	public static final AttributeSet[] defaultDaySchemeSwing = {attrGray, attrGreen, attrDarkCyan, 
		attrOrange, attrCyan, attrBlue, attrRed, attrDarkBlue, attrMagenta};

	public static final AttributeSet[] defaultNightSchemeSwing = {attrGrayNight, attrGreenNight, 
		attrWhiteNight, attrOrangeNight, attrCyanNight, attrBlueNight, attrRedNight, attrDarkBlueNight,
		attrMagentaNight};
	
	public static final AttributeSet[] gamebookersSchemeSwing = {attrGray, orangeGB, blackGB,
		tealGB, paleGrayGB, grayGB, blueGB, purpleGB, yellowGB};
	
}
