import java.io.File;
import java.io.BufferedReader; 
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.lang.String;


public class DataStorage extends Store {
	
	static enum TASK_TYPES { PENDING, COMPLETE, TRASH };
	
	private final static String TRUE = "true";
	private final static String FALSE = "false";
	private final static String PENDING_TITLE = "pending\r\n";
	private final static String COMPLETE_TITLE = "complete\r\n";
	private final static String TRASH_TITLE = "trash\r\n";
	
	private static BufferedReader in;
	private static BufferedWriter out;
	
	private static String fileName;
	
	//Just for test
	/*public static void main(String[] args){
		try{
			fileName = "taskData.txt";
			File f = new File(fileName);
			if(!f.exists()) 
				System.out.println(fileName+"does not exist!");
			//Control.executeCommand("add go home from 3:00 to 4:00 *");
			//Control.executeCommand("add play football start from 13:00 to 16:00 #cs2100");
			//Control.executeCommand("add do CCA start from 5:00 to 8:00");
			//storeToFile();
			loadFromFile();
			System.out.println(Control.modelHandler.pending.size());
			System.out.println(Control.modelHandler.pending.get(0).getWorkInfo());
			System.out.println(Control.modelHandler.pending.get(1).getWorkInfo());
			System.out.println(Control.modelHandler.pending.get(2).getWorkInfo());
		} catch(Exception e){
			System.out.println(e);
		}
	}*/
	
	public static void initialize(String fileNameInput) {
		fileName = fileNameInput;
		
	}
	
	public static void loadFromFile() throws IOException{
		in = new BufferedReader(new FileReader(fileName));
		int lineCount=0;
		String newLine = in.readLine();
		while (!(newLine = in.readLine()).equals("complete")) {
			in = addTaskToModel(newLine,in,TASK_TYPES.PENDING);
		}
		
		lineCount = 0;
		while (!( newLine = in.readLine()).equals("trash")) {
			in = addTaskToModel(newLine,in,TASK_TYPES.COMPLETE);
		}
		
		lineCount = 0;
		while (( newLine = in.readLine())!= null) {
			in = addTaskToModel(newLine,in,TASK_TYPES.TRASH);
		}
		
		in.close();
	}
	
	//write to file
	public static void storeToFile() throws IOException{
		out = new BufferedWriter(new FileWriter(fileName,false));
		out.write(PENDING_TITLE);
		out = addTaskinfoToWriter(out,Control.modelHandler.pending);
		out.write(COMPLETE_TITLE);
		out = addTaskinfoToWriter(out,Control.modelHandler.complete);
		out.write(TRASH_TITLE);
		out = addTaskinfoToWriter(out,Control.modelHandler.trash);		
		out.flush();		
	}
	
	private static BufferedReader addTaskToModel(String newLine, BufferedReader in, TASK_TYPES taskType) throws IOException{
		Task newTask = new Task();
		newTask.setIndexId(Integer.valueOf(newLine));
		newTask.setWorkInfo(in.readLine());
		newTask.setStartDate(new CustomDate(in.readLine()));
		newTask.setEndDate(new CustomDate(in.readLine()));
		newTask.setTag(in.readLine());
		newTask.setIsImportant((in.readLine()).equals(TRUE)?true:false);
		switch(taskType) {
			case PENDING: Control.modelHandler.addTaskToPending(newTask); break;
			case COMPLETE: Control.modelHandler.addTaskToComplete(newTask); break;
			case TRASH: Control.modelHandler.addTaskToTrash(newTask); break;
		}		
		in.readLine();
		return in;
	}
	
	private static BufferedWriter addTaskinfoToWriter(BufferedWriter out, ArrayList<Task> taskList) throws IOException{
		for (int i = 0; i < taskList.size(); i++) {
			Task targetTask = taskList.get(i);
			out.write(targetTask.getIndexId()+"\r\n");
			out.write(targetTask.getWorkInfo()+"\r\n");
			out.write(targetTask.getStartDate().toString()+"\r\n");
			out.write(targetTask.getEndDate().toString()+"\r\n");
			out.write(targetTask.getTag()+"\r\n");
			out.write((targetTask.getIsImportant()==true?TRUE:FALSE)+"\r\n\r\n");
		}
		return out;
	}
	
	
}
