import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
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

	private static BufferedReader in;
	private static PrintWriter out;

	private File textFile;

	public DataStorage(String fileName) {
		textFile = new File(fileName);
		checkIfFileExists(textFile);
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
		
		while (newLine != null &&!newLine.equals(COMPLETE_TITLE)){
			addTaskToModel(in, TASK_TYPES.PENDING);
			newLine = in.readLine();
		}
		
		while (newLine != null && !newLine.equals(TRASH_TITLE)) {
			addTaskToModel(in, TASK_TYPES.COMPLETE);
			newLine = in.readLine();
		}
		
		while (newLine != null) {
			addTaskToModel(in, TASK_TYPES.TRASH);
			newLine = in.readLine();
		}
		in.close();
	}

	// write to file
	public void storeToFile() throws IOException {
		try {
			out = new PrintWriter(new FileWriter(textFile, false));

			out.println(PENDING_TITLE);
			addTaskinfoToWriter(out, Control.getModel().getPendingList());
			out.println(COMPLETE_TITLE);
			addTaskinfoToWriter(out, Control.getModel().getCompleteList());
			out.println(TRASH_TITLE);
			addTaskinfoToWriter(out, Control.getModel().getTrashList());
			out.close();

		} catch (IOException e) {
			throw new IllegalArgumentException();
		}
	}

	private void addTaskToModel(BufferedReader in,
			TASK_TYPES taskType) throws IOException {
		Task newTask = new Task();
		String textLine = in.readLine();
		if(textLine == null || textLine.equals(COMPLETE_TITLE)|| textLine.equals(TRASH_TITLE))
			return;
		newTask.setIndexId(Integer.parseInt(textLine));
		newTask.setWorkInfo(in.readLine());
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setStartDate(new CustomDate(textLine));
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setEndDate(new CustomDate(textLine));
		newTask.setTag(new Tag(in.readLine(), in.readLine()));
		newTask.setIsImportant((in.readLine()).equals(TRUE) ? true : false);
		switch (taskType) {
		case PENDING:
			Control.getModel().addTaskToPending(newTask);
			break;
		case COMPLETE:
			Control.getModel().addTaskToComplete(newTask);
			break;
		case TRASH:
			Control.getModel().addTaskToTrash(newTask);
			break;
		}
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
			if(i != taskList.size() -1)
			out.println();
		}
	}
}
