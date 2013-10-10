import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.String;

import javafx.collections.ObservableList;

public class DataStorage extends Store {

	static enum TASK_TYPES {
		PENDING, COMPLETE, TRASH
	};

	private final static String TRUE = "true";
	private final static String FALSE = "false";
	private final static String PENDING_TITLE = "pending";
	private final static String COMPLETE_TITLE = "complete";
	private final static String TRASH_TITLE = "trash";
	private final static String SPLITLINE = "**************";

	private BufferedReader in;
	private PrintWriter out;
	private Model model;
	private File textFile;
	/*
	public static void main(String args[]) {

		DataStorage dataStorage = new DataStorage("dataStorage.txt");
		try {
			//System.out.println(Control.executeCommand("add go to library from 12:00 to 13:00"));
			//System.out.println(Control.getModel().getPendingList().size());
			dataStorage.storeToFile();
			dataStorage.loadFromFile();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	*/
	public DataStorage(String fileName, Model model) {
		createDir();
		textFile = new File(findUserDocDir()+FOLDERNAME+"/"+fileName);
		checkIfFileExists(textFile);
		this.model = model;
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
		return System.getProperty("user.home") + "/Documents/" ;
	}
	
	private static void checkIfFileExists(File file) {
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				System.out.println("Cannot create the text file");
			}
		}
	}

	public void loadFromFile() throws IOException {
		in = new BufferedReader(new FileReader(textFile));

		String newLine = in.readLine();
		if(newLine == null)
			return;
		while (!newLine.equals(COMPLETE_TITLE)){
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.PENDING);
			newLine = in.readLine();
			if(meetSplitLine)
				break;
		}
		
		while (!newLine.equals(TRASH_TITLE)) {
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.COMPLETE);
			newLine = in.readLine();
			if(meetSplitLine)
				break;
		}
		
		while (newLine != null) {
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.TRASH);
			newLine = in.readLine();
			if(meetSplitLine)
				break;
		}
		in.close();
	}

	// write to file
	public void storeToFile() throws IOException {
		try {
			out = new PrintWriter(new FileWriter(textFile, false));

			out.println(PENDING_TITLE);
			addTaskinfoToWriter(out, model.getPendingList());
			out.println(SPLITLINE);
			out.println(COMPLETE_TITLE);
			addTaskinfoToWriter(out, model.getCompleteList());
			out.println(SPLITLINE);
			out.println(TRASH_TITLE);
			addTaskinfoToWriter(out, model.getTrashList());
			out.println(SPLITLINE);
			out.close();

		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
	}

	private boolean addTaskToModel(BufferedReader in,
			TASK_TYPES taskType) throws IOException {
		Task newTask = new Task();
		String textLine = in.readLine();
		if(textLine == null || textLine.equals(SPLITLINE))
			return true;
		newTask.setIndexId(Integer.parseInt(textLine));
		newTask.setWorkInfo(in.readLine());
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setStartDate(new CustomDate(textLine));
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setEndDate(new CustomDate(textLine));
		newTask.setTag(new Tag(in.readLine(), in.readLine()));
		newTask.setIsImportant((in.readLine()).equals(TRUE) ? true : false);
		newTask.setIndexInList(Integer.parseInt(in.readLine()));
		switch (taskType) {
		case PENDING:
			model.addTaskToPending(newTask);
			break;
		case COMPLETE:
			model.addTaskToComplete(newTask);
			break;
		case TRASH:
			model.addTaskToTrash(newTask);
			break;
		}
		return false;
	}

	private void addTaskinfoToWriter(PrintWriter out,
			ObservableList<Task> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			Task targetTask = taskList.get(i);
			out.println(targetTask.getIndexId());
			out.println(targetTask.getWorkInfo());
			out.println(CustomDate.convertString(targetTask.getStartDate()));
			out.println(CustomDate.convertString(targetTask.getEndDate()));
			out.println(targetTask.getTag().getTag());
			out.println(targetTask.getTag().getRepetition());
			out.println((targetTask.getIsImportant() == true ? TRUE : FALSE));
			out.println(targetTask.getIndexInList());
			out.println();
		}
	}
}
