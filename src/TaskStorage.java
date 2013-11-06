import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.lang.String;
import java.util.List;

import javafx.collections.ObservableList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;

public class TaskStorage extends Storage {
	
	/*public static void main (String[] args) {
		WriteXMLFile xml = new WriteXMLFile();
	}*/

	private final static String PENDING = "pending";
	private final static String COMPLETE = "complete";
	private final static String TRASH = "trash";


	public TaskStorage(String fileName, Model model) {
		createDir();
		xmlFile = new File(findUserDocDir() + FOLDERNAME + "/" + fileName);
		checkIfFileExists(xmlFile);
		this.model = model;
	}
	
	/************************** store and load task list  **************************/
	
	@Override
	/**
	 * Store task list to XML file of task storage
	 */
	public void storeToFile() throws IOException {
		Element root = new Element("root");
		Document doc = new Document(root);
		Element pending = new Element(PENDING);
		Element complete = new Element(COMPLETE);
		Element trash = new Element(TRASH);
		pending = addTasksToXMLFile(pending, PENDING, model.getPendingList());
		complete = addTasksToXMLFile(complete, COMPLETE, model.getCompleteList());
		trash = addTasksToXMLFile(trash, TRASH, model.getTrashList());
		doc.getRootElement().getChildren().add(pending);
		doc.getRootElement().getChildren().add(complete);
		doc.getRootElement().getChildren().add(trash);
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(xmlFile));
		System.out.println("data saved");
	}
	
	@Override
	public void updateToFile() throws IOException {
		storeToFile();
	}
	
	@Override
	/**
	 * Load task list from XML file of task storage
	 */
	public void loadFromFile() throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element pending = rootNode.getChild(PENDING);
			Element trash = rootNode.getChild(TRASH);
			Element complete = rootNode.getChild(COMPLETE);
			addTasksToModel(pending, PENDING);
			addTasksToModel(complete, COMPLETE);
			addTasksToModel(trash, TRASH);
		} catch (IOException io) {
			throw io;
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}
	
	/**
	 * retrieve the elements in the XML file of task storage, building the corresponding tasks and store them to model
	 * @param element   task list element in XML file: pending, complete or trash
	 * @param taskType    "pending", "complete" or "trash"  
	 * @throws IOException
	 */
	private void addTasksToModel(Element element, String taskType)
			throws IOException {
		List<Element> taskList = element.getChildren();
		for(int i = 0; i<taskList.size();i++) {
			Task newTask = new Task();
			Element targetElement = taskList.get(i);
			newTask.setIndexId(targetElement.getChildText("indexID"));
			newTask.setWorkInfo(targetElement.getChildText("workInfo"));
			if(!targetElement.getChildText("startDate").equals("-"))
				newTask.setStartDate(new CustomDate(targetElement.getChildText("startDate")));
			if(!targetElement.getChildText("endDate").equals("-"))
				newTask.setEndDate(new CustomDate(targetElement.getChildText("endDate")));
			newTask.setTag(new Tag(targetElement.getChildText("tag"),targetElement.getChildText("repetition")));
			newTask.setIsImportant(targetElement.getChildText("isImportant").equals(TRUE) ? true : false);
			newTask.setIndexInList(Integer.parseInt(targetElement.getChildText("indexInList")));
			String latestDateString = targetElement.getChildText("modifiedDate");
			String second = latestDateString.substring(latestDateString.lastIndexOf(":") + 1);
			String remains = latestDateString.substring(0, latestDateString.lastIndexOf(":"));
			newTask.setLatestModifiedDate(new CustomDate(remains));
			newTask.getLatestModifiedDate().setSecond(Integer.parseInt(second));
			newTask.setCurrentOccurrence(Integer.parseInt(targetElement.getChildText("currentOccurrence")));
			newTask.setNumOccurrences(Integer.parseInt(targetElement.getChildText("numOccurrences")));
			String statusString = targetElement.getChildText("status");
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
		}
	}
	
	/**
	 * Retrieve the tasks from model and store their task informations to the XML file of task storage 
	 * @param element    task list element in XML file: pending, complete or trash
	 * @param taskType    "pending", "complete" or "trash"  
	 * @param taskList    pendingList, completeList or trashList
	 * @return
	 */
	private Element addTasksToXMLFile(Element element, String taskType,
			ObservableList<Task> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			Task targetTask = taskList.get(i);
			Element newTask = new Element(taskType+""+i);
			element.getChildren().add(newTask);
			newTask.addContent(new Element("indexID").setText(targetTask.getIndexId()));
			newTask.addContent(new Element("workInfo").setText((targetTask.getWorkInfo())));
			newTask.addContent(new Element("startDate").setText((CustomDate.convertString(targetTask.getStartDate()))));
			newTask.addContent(new Element("endDate").setText((CustomDate.convertString(targetTask.getEndDate()))));
			newTask.addContent(new Element("tag").setText((targetTask.getTag().getTag())));
			newTask.addContent(new Element("repetition").setText((targetTask.getTag().getRepetition())));
			newTask.addContent(new Element("isImportant").setText(((targetTask.getIsImportant() == true ? TRUE : FALSE))));
			newTask.addContent(new Element("indexInList").setText((targetTask.getIndexInList()+"")));
			newTask.addContent(new Element("modifiedDate").setText((CustomDate.convertString(targetTask.getLatestModifiedDate()) +":"+ targetTask.getLatestModifiedDate().getSecond())));
			newTask.addContent(new Element("currentOccurrence").setText((targetTask.getCurrentOccurrence()+"")));
			newTask.addContent(new Element("numOccurrences").setText((targetTask.getNumOccurrences()+"")));
			if(targetTask.getStatus() == Task.Status.NEWLY_ADDED)
				newTask.addContent(new Element("status").setText(("new")));
			else if(targetTask.getStatus() == Task.Status.UNCHANGED)
				newTask.addContent(new Element("status").setText(("unchanged")));
			else if(targetTask.getStatus() == Task.Status.DELETED)
				newTask.addContent(new Element("status").setText(("deleted")));
			else if(targetTask.getStatus() == Task.Status.ADDED_WHEN_SYNC)
				newTask.addContent(new Element("status").setText(("added_when_sync")));
			else 
				newTask.addContent(new Element("status").setText(("deleted_when_sync")));
				
		}
		return element;
	}
}
