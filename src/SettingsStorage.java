import java.io.IOException;
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

public class SettingsStorage extends Storage {

	private static String encryptAlgo = "DES/ECB/PKCS5Padding";
	private static final String ENCRYPTION_FAIL = "fail to encrypt password";
	private static final String USERNAME = "username";
	private static final String PASSWORD = "password";
	private static final String DISPLAY_REMAINING = "display_remaining";
	private static final String THEMEMODE = "themeMode";
	private static final String COLOR_SCHEME="colorScheme";
	private static final String AUTO_SYNC = "autoSync";
	
	private String dir;
	public SettingsStorage(String fileName, Model model) {

		createDir();
		dir = findUserDocDir() + FOLDERNAME + "/" + fileName;
		xmlFile = new File(dir);
		checkIfFileExists(xmlFile);
		this.model = model;
        //Security.insertProviderAt(new BouncyCastleProvider(), 1);
	}
	
	/************************** store and load account information  **************************/
	
	@Override
	/**
	 * Store account information to XML file of setting storage
	 */
	public void storeToFile() throws IOException {
		Element root = new Element("root");
		Document doc = new Document(root);
		Element account = new Element("account");
		doc.getRootElement().getChildren().add(account);
		String encryptedPassword;
		try{
			encryptedPassword = encryptString(model.getPassword());
			System.out.println("store: "+model.getPassword()+"->"+encryptedPassword);
		}catch(Exception e) {
			System.out.println(ENCRYPTION_FAIL);
			System.out.println("store: "+e.getMessage());
			encryptedPassword = model.getPassword();
		}
		account.addContent(new Element(USERNAME).setText(model.getUsername()));
		account.addContent(new Element(PASSWORD).setText(encryptedPassword));
		account.addContent(new Element("display_remaining").setText(model.getDisplayRemaining()==true? Common.TRUE : Common.FALSE));
		account.addContent(new Element("themeMode").setText(model.getThemeMode()));
		account.addContent(new Element("colourScheme").setText(model.getColourScheme()));
		account.addContent(new Element("autoSync").setText(model.getAutoSync() == true? Common.TRUE : Common.FALSE));
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(dir));
		System.out.println("Setting saved");
	}
	
	@Override
	/**
	 * Update account information to XML file of setting storage after settings changed
	 */
	public void updateToFile() throws IOException{
		 
		  try {	 
			SAXBuilder builder = new SAXBuilder();
			File xmlFile = new File(dir);
	 
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			if (model.getUsername() != null) {
				account.getChild(USERNAME).setText(model.getUsername());				
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
			account.getChild("display_remaining").setText(model.getDisplayRemaining() == true? Common.TRUE : Common.FALSE);
			if (model.getThemeMode()!=null) {
				account.getChild("themeMode").setText(model.getThemeMode());	
			}
			if (model.getColourScheme() != null){
				account.getChild("colourScheme").setText(model.getColourScheme());
			} else {
				account.getChild("colourScheme").setText("Default day mode");
			}
			account.getChild("autoSync").setText(model.getAutoSync() == true? Common.TRUE : Common.FALSE);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(dir));
	 
			// xmlOutput.output(doc, System.out);
	 
			System.out.println("File updated!");
		  } catch (JDOMException e) {
			e.printStackTrace();
		  }
	}
	
	@Override
	/**
	 * Load account information from XML file of setting storage when the program is launched
	 */
	public void loadFromFile() throws IOException {
		 
		SAXBuilder builder = new SAXBuilder();
		  try {
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			Element username = account.getChild(USERNAME);
			Element password = account.getChild(PASSWORD);
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
				model.setDisplayRemaining(displayRemaining.getText().equals(Common.TRUE) ? true : false);
				model.setThemeMode(themeMode.getText());
				model.setColourScheme(colourScheme.getText());
				model.setAutoSync(autoSync.getText().equals(Common.TRUE) ? true : false);
			}			
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}

	/***************** encryption and decryption *************************/
	public String encryptString(String plainText) throws Exception {
		return new Encryptor(encryptAlgo).encrypt(plainText);

	}

	public String decryptString(String cipherString) throws Exception {
		return new Encryptor("encryptAlgo").decrypt(cipherString);
	}

}
