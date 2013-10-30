import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.String;

import javafx.collections.ObservableList;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;

public class DataStorage extends Store {
	
	/*public static void main (String[] args) {
		WriteXMLFile xml = new WriteXMLFile();
	}*/

	static enum TASK_TYPES {
		PENDING, COMPLETE, TRASH
	};

	private final static String PENDING_TITLE = "pending";
	private final static String COMPLETE_TITLE = "complete";
	private final static String TRASH_TITLE = "trash";
	private final static String SPLITLINE = "**************";

	private BufferedReader in;
	private PrintWriter out;

	public DataStorage(String fileName, Model model) {
		createDir();
		textFile = new File(findUserDocDir() + FOLDERNAME + "/" + fileName);
		checkIfFileExists(textFile);
		this.model = model;
	}
	
	public void loadFromFile() throws IOException {
		in = new BufferedReader(new FileReader(textFile));

		String newLine = in.readLine();
		if (newLine == null)
			return;
		while (!newLine.equals(COMPLETE_TITLE)) {
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.PENDING);
			newLine = in.readLine();
			if (meetSplitLine)
				break;
		}

		while (!newLine.equals(TRASH_TITLE)) {
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.COMPLETE);
			newLine = in.readLine();
			if (meetSplitLine)
				break;
		}

		while (newLine != null) {
			boolean meetSplitLine = addTaskToModel(in, TASK_TYPES.TRASH);
			newLine = in.readLine();
			if (meetSplitLine)
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

	private boolean addTaskToModel(BufferedReader in, TASK_TYPES taskType)
			throws IOException {
		Task newTask = new Task();
		String textLine = in.readLine();
		if (textLine == null || textLine.equals(SPLITLINE))
			return true;
		newTask.setIndexId(textLine);
		newTask.setWorkInfo(in.readLine());
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setStartDate(new CustomDate(textLine));
		if (!(textLine = in.readLine()).equals("-"))
			newTask.setEndDate(new CustomDate(textLine));
		newTask.setTag(new Tag(in.readLine(), in.readLine()));
		newTask.setIsImportant((in.readLine()).equals(TRUE) ? true : false);
		newTask.setIndexInList(Integer.parseInt(in.readLine()));
		String latestDateString = in.readLine();
		String second = latestDateString.substring(latestDateString.lastIndexOf(":") + 1);
		String remains = latestDateString.substring(0, latestDateString.lastIndexOf(":"));
		newTask.setLatestModifiedDate(new CustomDate(remains));
		newTask.getLatestModifiedDate().setSecond(Integer.parseInt(second));
		newTask.setCurrentOccurrence(Integer.parseInt(in.readLine()));
		newTask.setNumOccurrences(Integer.parseInt(in.readLine()));
		String statusString = in.readLine();
		if (statusString.equals("new"))
			newTask.setStatus(Task.Status.NEWLY_ADDED);
		else if (statusString.equals("unchanged"))
			newTask.setStatus(Task.Status.UNCHANGED);
		else
			newTask.setStatus(Task.Status.DELETED);
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
			out.println(CustomDate.convertString(targetTask.getLatestModifiedDate()) +":"+ targetTask.getLatestModifiedDate().getSecond());
			out.println(targetTask.getCurrentOccurrence());
			out.println(targetTask.getNumOccurrences());
			if(targetTask.getStatus() == Task.Status.NEWLY_ADDED)
				out.println("new");
			else if(targetTask.getStatus() == Task.Status.UNCHANGED)
				out.println("unchanged");
			else
				out.println("deleted");
			out.println();
		}
	}
}



 

/**  
*   
* @author hongliang.dinghl  
* JDOM generateXML file  
*   
*/
class SyncStore extends Store {
	
	String dir;
	
	public SyncStore (String fileName, Model model) {
		createDir();
		dir = findUserDocDir() + FOLDERNAME + "/" + fileName;
		xmlFile = new File(dir);
		checkIfFileExists(xmlFile);
		this.model = model;
	}
	
	public void storeAccount() {
		try {
			Element root = new Element("root");
			Document doc = new Document(root);
			Element account = new Element("account");
			doc.getRootElement().getChildren().add(account);
			
			account.addContent(new Element("username").setText(model.getUsername()));
			account.addContent(new Element("password").setText(model.getPassword()));
			
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
			if (model.getPassword() != null) {
				account.getChild("password").setText(model.getPassword());			
			}
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
	
	public void retrieveAccount() {
		 
		SAXBuilder builder = new SAXBuilder();
		  try {
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element account = rootNode.getChild("account");
			Element username = account.getChild("username");

			if (username == null)
				System.out.println("no account info");
			else {
				model.setUsername(username.getText());
				model.setPassword(account.getChildText("password"));
			}			
		  } catch (IOException io) {
			System.out.println(io.getMessage());
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}
}
  