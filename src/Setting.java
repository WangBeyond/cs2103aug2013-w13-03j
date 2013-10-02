import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.String;
import java.io.File;

public class Setting extends Store {


	private final static String TRUE = "true";
	private final static String FALSE = "false";

	private String googleAccount;
	private String password;
	// 12-hour or 24-hour format
	private Boolean isTwelveHour;
	// Whether date display include year
	private Boolean isYearDisplayed;
	private Boolean isPasswordRemembered;
	// Whether task sorted chronologically first or according to importance
	// first
	private Boolean isChronological;
	private int fontSize;

	private BufferedReader in;
	private BufferedWriter out;

	private String fileName;
	private File textFile;

	public static void main(String args[]) {

		Setting settingStore = new Setting("setting.txt");
		// Just set an arbitrary account
		settingStore.setAccount("yy@gmail.com", "12345");
		try {
			System.out.println(System.getProperty("user.home"));
			// settingStore.setFontSize(20);
			 settingStore.storeToFile();
			// settingStore.encryptFile();
			// System.out.println(settingStore.getFontSize());
			// settingStore.decryptFile();
			// settingStore.loadFromFile();
			// System.out.println(settingStore.getFontSize());
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void setAccount(String accountInput, String passwordInput) {
		googleAccount = accountInput;
		password = passwordInput;
	}

	public Setting(String fileNameInput) {
		createDir();
		fileName = findUserDocDir()+FOLDERNAME+"\\"+fileNameInput;
		//fileName = findUserDocDir()+ fileNameInput;
		googleAccount = "";
		password = "";
		isTwelveHour = false;
		isYearDisplayed = false;
		isPasswordRemembered = false;
		isChronological = false;
		fontSize = 14;
	}

	private void createDir() {
		File theDir = new File(findUserDocDir()+FOLDERNAME);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: ");
			boolean result = theDir.mkdir();
			if (result) {
				System.out.println("DIR created");
			}
		}
	}

	private String findUserDocDir() {
		return System.getProperty("user.home") + "\\Documents\\" ;
	}

	public void loadFromFile() throws IOException {
		in = new BufferedReader(new FileReader(fileName));
		googleAccount = in.readLine();
		password = in.readLine();
		isTwelveHour = in.readLine().equals(TRUE) ? true : false;
		isYearDisplayed = in.readLine().equals(TRUE) ? true : false;
		isPasswordRemembered = in.readLine().equals(TRUE) ? true : false;
		isChronological = in.readLine().equals(TRUE) ? true : false;
		fontSize = Integer.valueOf(in.readLine());
		in.close();
		new File(fileName).delete();
	}

	public void storeToFile() throws IOException {
		out = new BufferedWriter(new FileWriter(fileName, false));
		out.write(googleAccount + "\r\n");
		out.write(password + "\r\n");
		out.write(isTwelveHour.toString() + "\r\n");
		out.write(isYearDisplayed.toString() + "\r\n");
		out.write(isPasswordRemembered.toString() + "\r\n");
		out.write(isChronological.toString() + "\r\n");
		out.write(fontSize + "\r\n");
		out.flush();
		out.close();
	}

	private void encryptFile() throws Exception {
		new FileEncryptor("DES/ECB/PKCS5Padding", fileName).encrypt();
		new File(fileName).delete();

	}

	private void decryptFile() throws Exception {
		new FileEncryptor("DES/ECB/PKCS5Padding", fileName).decrypt();
	}

	public void setTwelveHour(boolean isCheck) {
		isTwelveHour = isCheck;
	}

	public void setYearDisplayed(boolean isDisplayed) {
		isYearDisplayed = isDisplayed;
	}

	public void setPasswordRemember(boolean isRemembered) {
		isPasswordRemembered = isRemembered;
	}

	public void setChronological(boolean isChro) {
		isChronological = isChro;
	}

	public void setFontSize(int fontSizeInput) {
		fontSize = fontSizeInput;
	}

	public int getFontSize() {
		return fontSize;
	}
}
