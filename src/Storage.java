import java.io.File;
import java.io.IOException;

public abstract class Storage {
	
	protected final static String TRUE = "true";
	protected final static String FALSE = "false";
	protected final static boolean DONE_READING = true;
	protected final static boolean UNDONE_READING = false;
	public final static String FOLDERNAME = "iDo Files";
	protected Model model;
	protected File xmlFile;
	
	public void loadFromFile() throws IOException {
		
	};
	
	public void storeToFile() throws IOException {
		
	};
	
	public void updateToFile() throws IOException {
		
	};
	
	/**
	 * create the directory of iDo folder in user's documents folder
	 */
	protected void createDir() {
		File theDir = new File(findUserDocDir() + FOLDERNAME);
		// if the directory does not exist, create it
		if (!theDir.exists()) {
			System.out.println("creating directory: ");
			boolean result = theDir.mkdir();
			if (result) {
				System.out.println("DIR created");
			}
		}
	}
	
	/**
	 * find user's Documents directory
	 * @return user Documents dir
	 */
	protected String findUserDocDir() {
		return System.getProperty("user.home") + "/Documents/";
	}

	/**
	 * check if target file exists
	 * @param file
	 */

	protected static void checkIfFileExists(File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Cannot create the text file");
			}
		}
	}
}
