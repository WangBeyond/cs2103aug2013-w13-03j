import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.String;

public class Setting extends Store {
	
	private final static String TRUE = "true";
	private final static String FALSE = "false";
	
	private static String googleAccount;
	private static String password;
	//12-hour or 24-hour format
	private static Boolean isTwelveHour;
	//Whether date display include year
	private static Boolean isYearDisplayed;
	private static Boolean isPasswordRemembered;
	//Whether task sorted chronologically first or according to importance first
	private static Boolean isChronological;
	private static int fontSize;
	
	private static BufferedReader in;
	private static BufferedWriter out;
	
	private static String fileName;
	
	public static void setAccount(String accountInput, String passwordInput) {
		googleAccount = accountInput;
		password = passwordInput;
	}
	
	public static void initialize(String fileNameInput) {
		fileName = fileNameInput;
		googleAccount = "";
		password = "";
		isTwelveHour = false;
		isYearDisplayed = false;
		isPasswordRemembered = false;
		isChronological = false;
		fontSize = 14;
	}
	
	public static void loadFromFile() throws IOException{
		in = new BufferedReader(new FileReader(fileName));
		googleAccount = in.readLine();
		password = in.readLine();
		isTwelveHour = in. readLine().equals(TRUE)?true:false;
		isYearDisplayed = in. readLine().equals(TRUE)?true:false;
		isPasswordRemembered = in. readLine().equals(TRUE)?true:false;
		isChronological = in. readLine().equals(TRUE)?true:false;
		fontSize = Integer.valueOf(in.readLine());	
		in.close();
	}
	
	public static void storeToFile() throws IOException {
		out = new BufferedWriter(new FileWriter(fileName,false));
		out.write(googleAccount+"\r\n");
		out.write(password+"\r\n");
		out.write(isTwelveHour.toString()+"\r\n");
		out.write(isYearDisplayed.toString()+"\r\n");
		out.write(isPasswordRemembered.toString()+"\r\n");
		out.write(isChronological.toString()+"\r\n");
		out.write(fontSize+"\r\n");
		out.flush();
	}
	
	public static void setTwelveHour(boolean isCheck) {
		isTwelveHour = isCheck;
	}
	
	public static void setYearDisplayed(boolean isDisplayed) {
		isYearDisplayed = isDisplayed;
	}
	
	public static void setPasswordRemember(boolean isRemembered) {
		isPasswordRemembered = isRemembered;
	}
	
	public static void setChronological(boolean isChro) {
		isChronological = isChro;
	}
	
	public static void setFontSize(int fontSizeInput) {
		fontSize = fontSizeInput;
	}
}
