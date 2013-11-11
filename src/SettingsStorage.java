import java.io.IOException;
import java.io.FileWriter;
import java.lang.String;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

//@author A0105667B
public class SettingsStorage extends Storage {

	private static String encryptAlgo = "DES/ECB/PKCS5Padding";
	private static final String ENCRYPTION_FAIL = "fail to encrypt password";
	
	private static final String ROOT = "root";
	private static final String ACCOUNT = "account";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String DISPLAY_REMAINING = "display_remaining";
	private static final String THEMEMODE = "themeMode";
	private static final String COLOR_SCHEME="colourScheme";
	private static final String AUTO_SYNC = "autoSync";
	private static final String SYNC_PERIOD = "syncPeriod";
	
	private static Logger log = Logger.getLogger("SettingStorage");
	private String dir;
	public SettingsStorage(String fileName, Model model) {
		createDir();
		dir = findUserDocDir() + FOLDERNAME + "/" + fileName;
		xmlFile = new File(dir);
		checkIfFileExists(xmlFile);
		this.model = model;
        //Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	
	/************************** store account information  **************************/
	
	@Override
	/**
	 * Store account information to XML file of setting storage
	 */
	public void storeToFile() throws IOException {
		//Initialize the elements
		Element root = new Element(ROOT);
		Document doc = new Document(root);
		Element account = new Element(ACCOUNT);
		doc.getRootElement().getChildren().add(account);
		String encryptedPassword = encryptPassword(model.getPassword());
		account = recordSettings(account, encryptedPassword);
		//Output to XML file
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(dir));
		log.log(Level.INFO, "Setting saved");
	}
	
	private Element recordSettings(Element account, String encryptedPassword) {
		account.addContent(new Element(USERNAME).setText(model.getUsername()));
		account.addContent(new Element(PASSWORD).setText(encryptedPassword));
		account.addContent(new Element(DISPLAY_REMAINING).setText(model.getDisplayRemaining()==true? Common.TRUE : Common.FALSE));
		account.addContent(new Element(THEMEMODE).setText(model.getThemeMode()));
		account.addContent(new Element(COLOR_SCHEME).setText(model.getColourScheme()));
		account.addContent(new Element(AUTO_SYNC).setText(model.hasAutoSync() == true? Common.TRUE : Common.FALSE));
		account.addContent(new Element(SYNC_PERIOD).setText(String.valueOf(model.getSyncPeriod())));
		return account;
	}
	
	
	/*****************************update account information **************************/
	
	@Override
	/**
	 * Update account information to XML file of setting storage after settings changed
	 */
	public void updateToFile() throws IOException{
		 
		  try {	 
			 //initialize the XML file builder
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(dir);
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild(ACCOUNT);
			account = updateInfo(account);
			//Output to XML file
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(dir));
			log.log(Level.INFO, "File updated!");
		  } catch (JDOMException e) {
			log.log(Level.WARNING, e.getMessage());
		  }
	}
	
	
	private Element updateInfo(Element account) {
		if (model.getUsername() != null) {
			account.getChild(USERNAME).setText(model.getUsername());				
		}
		String encryptedPassword = encryptPassword(model.getPassword());
		if (model.getPassword() != null) {
			account.getChild(PASSWORD).setText(encryptedPassword);			
		}
		account.getChild(DISPLAY_REMAINING).setText(model.getDisplayRemaining() == true? Common.TRUE : Common.FALSE);
		if (model.getThemeMode()!=null) {
			account.getChild(THEMEMODE).setText(model.getThemeMode());	
		}
		if (model.getColourScheme() != null) {
			account.getChild(COLOR_SCHEME).setText(model.getColourScheme());
		} else {
			account.getChild(COLOR_SCHEME).setText("Default day mode");
		}
		account.getChild(AUTO_SYNC).setText(model.hasAutoSync() == true? Common.TRUE : Common.FALSE);
		account.getChild(SYNC_PERIOD).setText(String.valueOf(model.getSyncPeriod()));
		return account;
	}
	
	
	/*****************************load account information************************************/
	
	@Override
	/**
	 * Load account information from XML file of setting storage when the program is launched
	 */
	public void loadFromFile() throws IOException {
		 
		SAXBuilder builder = new SAXBuilder();
		  try {
				Document doc = (Document) builder.build(xmlFile);
				//Retrieve the elements from XML file
				Element rootNode = doc.getRootElement();
				Element account = rootNode.getChild(ACCOUNT);
				Element username = account.getChild(USERNAME);
				Element password = account.getChild(PASSWORD);
				Element displayRemaining  = account.getChild(DISPLAY_REMAINING);
				Element themeMode = account.getChild(THEMEMODE);
				Element colourScheme = account.getChild(COLOR_SCHEME);
				Element autoSync = account.getChild(AUTO_SYNC);
				Element syncPeriod = account.getChild(SYNC_PERIOD);
				String decryptedPassword= decryptPassword(password.getText());
				//copy the info of elements to the model
				if (username.getText() != null) {
					model.setUsername(username.getText());
				}
				if (decryptedPassword != null) {
					model.setPassword(decryptedPassword);
				}
				if (displayRemaining.getText() != null) {
					model.setDisplayRemaining(displayRemaining.getText().equals(Common.TRUE) ? true : false);
				}
				if (themeMode.getText() != null) {
					model.setThemeMode(themeMode.getText());
				}
				if (colourScheme.getText() != null) {
					model.setColourScheme(colourScheme.getText());
				}
				if (autoSync.getText() != null) {
					model.setAutoSync(autoSync.getText().equals(Common.TRUE) ? true : false);
				}
				if (syncPeriod.getText() != null) {
					model.setSyncPeriod(Integer.valueOf(syncPeriod.getText()));
				}			
		  } catch (JDOMException jdomex) {
			  log.log(Level.WARNING, jdomex.getMessage());
		  }
	}

	
	/***************** encryption and decryption *************************/
	
	private String encryptPassword(String password) {
		String encryptedPassword;
		if(model.getPassword()!=null) {
			try{
				encryptedPassword = encryptString(password);
			}
			catch(Exception e) {
				encryptedPassword = null;
				log.log(Level.WARNING, ENCRYPTION_FAIL);
				log.log(Level.WARNING, "store: "+e.getMessage());
			}
		} else {
			encryptedPassword = null;
		}
		return encryptedPassword;
	}
	
	private String decryptPassword(String encryptedPassword) {
		String decryptedPassword = encryptedPassword;
		if(encryptedPassword != null) {
			try{
				decryptedPassword = decryptString(encryptedPassword);
				log.log(Level.INFO, "retrieve: "+encryptedPassword+"->"+decryptedPassword);
			}catch(Exception e) {
				e.printStackTrace();
				log.log(Level.WARNING, ENCRYPTION_FAIL);
				decryptedPassword = null;
			}
		}
		return decryptedPassword;
	}
	
	public String encryptString(String plainText) throws Exception {
		return new Encryptor(encryptAlgo).encrypt(plainText);

	}

	public String decryptString(String cipherString) throws Exception {
		return new Encryptor(encryptAlgo).decrypt(cipherString);
	}
	
	
	/*******************************Methods for testing**********************************************/
	
	public boolean compareModelAndFileForTest() throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {//retrieve the elements
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild(ACCOUNT);
			Element username = account.getChild(USERNAME);
			Element password = account.getChild(PASSWORD);
			Element displayRemaining  = account.getChild(DISPLAY_REMAINING);
			Element themeMode = account.getChild(THEMEMODE);
			Element colourScheme = account.getChild(COLOR_SCHEME);
			Element autoSync = account.getChild(AUTO_SYNC);
			Element syncPeriod = account.getChild(SYNC_PERIOD);
			String decryptedPassword= decryptPassword(password.getText());
			if (!username.getText().equals(model.getUsername())) {
				return false;
			} else if (!decryptedPassword.equals(model.getPassword())) {
				return false;
			} else if ((displayRemaining.getText().equals(Common.TRUE)? true : false)!=(model.getDisplayRemaining())) {
				return false;
			} else if (!themeMode.getText().equals(model.getThemeMode())) {
				return false;
			} else if (!colourScheme.getText().equals(model.getColourScheme())) {
				return false;
			} else if (!autoSync.getText().equals(model.hasAutoSync() == true? Common.TRUE : Common.FALSE)) {
				return false;
			} else if (Integer.valueOf(syncPeriod.getText())!=(model.getSyncPeriod())) {
				return false;
			} else {
				return true;
			}
			} catch (IOException io) {
			throw io;
		  } catch (JDOMException jdomex) {
			log.log(Level.WARNING, jdomex.getMessage());
			return false;
		  }
	}
	

}
