import java.io.File;
import java.io.IOException;

//@author A0105667B

public abstract class Storage {
	
	protected final static boolean DONE_READING = true;
	protected final static boolean UNDONE_READING = false;
	public final static String FOLDERNAME = "iDo Files";
	protected Model model;
	protected File xmlFile;
	
	/*********************abastract methods which need to be implemented****************/
	
	public void loadFromFile() throws IOException {
		
	};
	
	public void storeToFile() throws IOException {
		
	};
	
	public void updateToFile() throws IOException {
		
	};
	
	
	/*********************create files or check file existence*****************************/
	
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
	
	/**********************methods for test**********************/
	boolean searchTaskInFileForTest(Task task, String taskListType) throws IOException {
		return false;
	}
	
	boolean checkTaskListEmptyForTest(String taskListType) throws IOException{
		return false;
	}
	
	boolean compareModelAndFileForTest() throws IOException {
		return false;
	}
}
