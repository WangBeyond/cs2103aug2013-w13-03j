import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.lang.String;
import java.io.File;
import sun.misc.*;
//import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Setting extends Store {
	private BufferedReader in;
	private BufferedWriter out;

	private String fileName;

	/*public static void main(String args[]) {
		try {
			System.out.println(System.getProperty("user.home"));
	        Security.insertProviderAt(new BouncyCastleProvider(), 1);

	        String passphrase = "The quick brown fox jumped over the lazy brown dog";
	        String plaintext = "hello world";
	        byte [] ciphertext = encrypt(passphrase, plaintext);
	        String recoveredPlaintext = decrypt(passphrase, ciphertext);

	        System.out.println(recoveredPlaintext);
			// settingStore.setFontSize(20);
			// settingStore.encryptFile();
			// System.out.println(settingStore.getFontSize());
			// settingStore.decryptFile();
			// settingStore.loadFromFile();
			// System.out.println(settingStore.getFontSize());
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	*/
	String dir;
	public Setting(String fileName, Model model) {

		createDir();
		dir = findUserDocDir() + FOLDERNAME + "/" + fileName;
		xmlFile = new File(dir);
		checkIfFileExists(xmlFile);
		this.model = model;
        //Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	public void storeAccount() {
		try {
			Element root = new Element("root");
			Document doc = new Document(root);
			Element account = new Element("account");
			doc.getRootElement().getChildren().add(account);
			String encryptedPassword;
			try{
				encryptedPassword = encryptString(model.getPassword());
				System.out.println("store: "+model.getPassword()+"->"+encryptedPassword);
			}catch(Exception e) {
				System.out.println("fail to encrypt password");
				System.out.println("store: "+e.getMessage());
				encryptedPassword = model.getPassword();
			}
			account.addContent(new Element("username").setText(model.getUsername()));
			account.addContent(new Element("password").setText(encryptedPassword));
			account.addContent(new Element("display_remaining").setText(model.getDisplayRemaining()==true? TRUE : FALSE));
			account.addContent(new Element("themeMode").setText(model.getThemeMode()));
			account.addContent(new Element("colourScheme").setText(model.getColourScheme()));
			account.addContent(new Element("autoSync").setText(model.getAutoSync() == true? TRUE : FALSE));
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(dir));
			System.out.println("Setting saved");
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
	}
	
	public void updateAccount() {
		 
		  try {	 
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(dir);
	 
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			if (model.getUsername() != null) {
				account.getChild("username").setText(model.getUsername());				
			}
			String encryptedPassword;
			try{
				encryptedPassword = encryptString( model.getPassword());
				System.out.println("update: "+model.getPassword()+"->"+encryptedPassword);
			}catch(Exception e) {
				System.out.println("fail to encrypt password");
				System.out.println("update: "+e.getMessage());
				encryptedPassword = model.getPassword();
			}
			if (model.getPassword() != null) {
				account.getChild("password").setText(encryptedPassword);			
			}
			account.getChild("display_remaining").setText(model.getDisplayRemaining() == true? TRUE : FALSE);
			if (model.getThemeMode()!=null) {
				account.getChild("themeMode").setText(model.getThemeMode());	
			}
			if (model.getColourScheme() != null){
				account.getChild("colourScheme").setText(model.getColourScheme());
			} else {
				account.getChild("colourScheme").setText("Default day mode");
			}
			account.getChild("autoSync").setText(model.getAutoSync() == true? TRUE : FALSE);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(dir));
	 
			// xmlOutput.output(doc, System.out);
	 
			System.out.println("File updated!");
		  } catch (IOException io) {
			io.printStackTrace();
		  } catch (JDOMException e) {
			e.printStackTrace();
		  }
	}
	
	public void retrieveAccount() {
		 
		SAXBuilder builder = new SAXBuilder();
		  try {
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			Element username = account.getChild("username");
			Element password = account.getChild("password");
			Element displayRemaining  = account.getChild("display_remaining");
			Element themeMode = account.getChild("themeMode");
			Element colourScheme = account.getChild("colourScheme");
			Element autoSync = account.getChild("autoSync");
			
			if (username == null)
				System.out.println("no account info");
			else {
				String decryptedPassword = password.getText();
				if(password.getText() != null) {
					try{
						decryptedPassword = decryptString(password.getText());
						System.out.println("retrieve: "+password.getText()+"->"+decryptedPassword);
					}catch(Exception e) {
						System.out.println("fail to decrypt password");
						decryptedPassword = password.getText();
					}
				}
				model.setUsername(username.getText());
				model.setPassword(decryptedPassword);
				model.setDisplayRemaining(displayRemaining.getText().equals(TRUE) ? true : false);
				model.setThemeMode(themeMode.getText());
				model.setColourScheme(colourScheme.getText());
				model.setAutoSync(autoSync.getText().equals(TRUE) ? true : false);
			}			
		  } catch (IOException io) {
			System.out.println(io.getMessage());
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}
	
	/*
	public void storeCalendarID () {
		  try {
				 
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(dir);
	 
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			account.addContent(new Element("CalendarID").setText(model.getCalendarID()));
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(dir));
	 
			// xmlOutput.output(doc, System.out);
	 
			System.out.println("calID saved!");
		  } catch (IOException io) {
			io.printStackTrace();
		  } catch (JDOMException e) {
			e.printStackTrace();
		  }
	}
	

	
	public void retrieveCalID() {
		 
		SAXBuilder builder = new SAXBuilder();
		String calendarID = null;
		  try {
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			Element calID = account.getChild("CalendarID");

			if (calID != null)
				model.setCalendarID(calID.getText());
		
		  } catch (IOException io) {
			System.out.println(io.getMessage());
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}
*/	


	protected void createDir() {
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

	protected String findUserDocDir() {
		return System.getProperty("user.home") + "\\Documents\\" ;
	}

	public String encryptString(String plainText) throws Exception {
		return new Encryptor("DES/ECB/PKCS5Padding").encrypt(plainText);

	}

	public String decryptString(String cipherString) throws Exception {
		return new Encryptor("DES/ECB/PKCS5Padding").decrypt(cipherString);
	}

}
