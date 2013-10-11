import java.io.File;
import java.io.IOException;

public abstract class Store {
	
	protected final static String TRUE = "true";
	protected final static String FALSE = "false";
	protected final static boolean DONE_READING = true;
	protected final static boolean UNDONE_READING = false;
	public final static String FOLDERNAME = "iDo Files";
	protected Model model;
	protected File textFile;
	
	public void loadFromFile() throws IOException {
		
	};
	
	public void storeToFile() throws IOException {
		
	};
}
