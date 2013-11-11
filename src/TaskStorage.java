import java.io.File;
import java.io.IOException;
import java.io.FileWriter;
import java.lang.String;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.JDOMException;

//@author A0105667B
public class TaskStorage extends Storage {
	private static final String INDEXID = "indexID";
	private static final String WORK_INFO ="workInfo";
	private static final String START_DATE = "startDate";
	private static final String END_DATE = "endDate";
	private static final String TAG = "tag";
	private static final String REPETITION= "repetition";
	private static final String IS_IMPORTANT = "isImportant";
	private static final String INDEX_IN_LIST = "indexInList";
	private static final String MODIFIED_DATE = "modifiedDate";
	private static final String CURRENT_OCCURRENCE = "currentOccurrence";
	private static final String NUM_OCCURRENCE = "numOccurrences";
	private static final String STATUS = "status";
	private static final String NEW = "new";
	private static final String UNCHANGED = "unchanged";
	private static final String DELETED = "deleted";
	private static final String ADDED_WHEN_SYNC = "added_when_sync";
	private static final String DELETED_WHEN_SYNC = "deleted_when_sync";
	
	static final String PENDING = "pending";
	static final String COMPLETE = "complete";
	static final  String TRASH = "trash";

	private static Logger log = Logger.getLogger("TaskStorage");
	
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
		//Initialize the elements in the XML file
		Element root = new Element("root");
		Document doc = new Document(root);
		Element pending = new Element(PENDING);
		Element complete = new Element(COMPLETE);
		Element trash = new Element(TRASH);
		//Add task info to its corresponding element in XML file
		pending = addTasksToXMLFile(pending, PENDING, model.getPendingList());
		complete = addTasksToXMLFile(complete, COMPLETE, model.getCompleteList());
		trash = addTasksToXMLFile(trash, TRASH, model.getTrashList());
		//append elements to root
		doc.getRootElement().getChildren().add(pending);
		doc.getRootElement().getChildren().add(complete);
		doc.getRootElement().getChildren().add(trash);
		//Outupt to XML file
		XMLOutputter xmlOutput = new XMLOutputter();
		xmlOutput.setFormat(Format.getPrettyFormat());
		xmlOutput.output(doc, new FileWriter(xmlFile));
		log.log(Level.INFO, "Data saved.");
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
		try {//retrieve the elements
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element pending = rootNode.getChild(PENDING);
			Element trash = rootNode.getChild(TRASH);
			Element complete = rootNode.getChild(COMPLETE);
			//Retrieve tasks from the elements and add to model
			addTasksToModel(pending, PENDING);
			addTasksToModel(complete, COMPLETE);
			addTasksToModel(trash, TRASH);
			} catch (IOException io) {
			throw io;
		  } catch (JDOMException jdomex) {
			log.log(Level.WARNING, jdomex.getMessage());
		  }
	}

/**********************************Generate tasks from XML file and add them to model **************************/
	
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
			newTask = setTaskInfo(newTask, targetElement);
			addToTaskList(newTask, taskType);
		}
	}
	
	/**
	 * set the attributes of a task according to info from the task element retrieved from XML file.
	 * @param newTask
	 * @param targetElement
	 * @return
	 */
	private Task setTaskInfo(Task newTask, Element targetElement) {
		newTask.setIndexId(targetElement.getChildText(INDEXID));
		newTask.setWorkInfo(targetElement.getChildText(WORK_INFO));
		newTask = setDateInfo(newTask, targetElement);
		newTask.setTag(new Tag(targetElement.getChildText(TAG),targetElement.getChildText(REPETITION)));
		newTask.setIsImportant(targetElement.getChildText(IS_IMPORTANT).equals(Common.TRUE) ? true : false);
		newTask.setIndexInList(Integer.parseInt(targetElement.getChildText(INDEX_IN_LIST)));
		newTask = setLastModifiedDate(newTask, targetElement);
		newTask.setCurrentOccurrence(Integer.parseInt(targetElement.getChildText(CURRENT_OCCURRENCE)));
		newTask.setNumOccurrences(Integer.parseInt(targetElement.getChildText(NUM_OCCURRENCE)));
		newTask = setStatus(newTask, targetElement);
		return newTask;
	}
	
	private Task setDateInfo(Task newTask, Element targetElement) {
		if(!targetElement.getChildText(START_DATE).equals("-")) {
			newTask.setStartDate(new CustomDate(targetElement.getChildText(START_DATE)));
		}
		if(!targetElement.getChildText(END_DATE).equals("-")) {
			newTask.setEndDate(new CustomDate(targetElement.getChildText(END_DATE)));
		}
		return newTask;
	}
	
	private Task setLastModifiedDate(Task newTask, Element targetElement) {
		String latestDateString = targetElement.getChildText(MODIFIED_DATE);
		String second = latestDateString.substring(latestDateString.lastIndexOf(":") + 1);
		String remains = latestDateString.substring(0, latestDateString.lastIndexOf(":"));
		newTask.setLatestModifiedDate(new CustomDate(remains));
		newTask.getLatestModifiedDate().setSecond(Integer.parseInt(second));
		return newTask;
	}
	
	private Task setStatus(Task newTask, Element targetElement) {
		String statusString = targetElement.getChildText(STATUS);
		if (statusString.equals(NEW)) {
			newTask.setStatus(Task.Status.NEWLY_ADDED);
		} else if (statusString.equals(UNCHANGED)) {
			newTask.setStatus(Task.Status.UNCHANGED);
		} else if(statusString.equals(DELETED)) {
			newTask.setStatus(Task.Status.DELETED);
		} else if (statusString.equals(ADDED_WHEN_SYNC)) {
			newTask.setStatus(Task.Status.ADDED_WHEN_SYNC);
		} else if (statusString.equals(DELETED_WHEN_SYNC)) {
			newTask.setStatus(Task.Status.DELETED_WHEN_SYNC);
		}
		return newTask;
	}
	
	private void addToTaskList(Task newTask, String taskType) {
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
	
	
	/******************************Generate tasks from model and add them to XML file*****************/
	
	/**
	 * Retrieve the tasks from model and store their task informations to the XML file of task storage 
	 * @param element    task list element in XML file: pending, complete or trash
	 * @param taskType    "pending", "complete" or "trash"  
	 * @param taskList    pendingList, completeList or trashList
	 * @return
	 */
	private Element addTasksToXMLFile(Element element, String taskType,
			List<Task> taskList) {
		for (int i = 0; i < taskList.size(); i++) {
			Task targetTask = taskList.get(i);
			Element newTask = new Element(taskType+""+i);
			element.getChildren().add(newTask);
			newTask = recordInfo(newTask, targetTask);
		}
		return element;
	}
	
	private Element recordInfo(Element newTask, Task targetTask) {
		newTask.addContent(new Element(INDEXID).setText(targetTask.getIndexId()));
		newTask.addContent(new Element(WORK_INFO).setText((targetTask.getWorkInfo())));
		newTask.addContent(new Element(START_DATE).setText((CustomDate.convertString(targetTask.getStartDate()))));
		newTask.addContent(new Element(END_DATE).setText((CustomDate.convertString(targetTask.getEndDate()))));
		newTask.addContent(new Element(TAG).setText((targetTask.getTag().getTag())));
		newTask.addContent(new Element(REPETITION).setText((targetTask.getTag().getRepetition())));
		newTask.addContent(new Element(IS_IMPORTANT).setText(((targetTask.isImportantTask() == true ? Common.TRUE : Common.FALSE))));
		newTask.addContent(new Element(INDEX_IN_LIST).setText((targetTask.getIndexInList()+"")));
		newTask.addContent(new Element(MODIFIED_DATE).setText((CustomDate.convertString(targetTask.getLatestModifiedDate()) +":"+ targetTask.getLatestModifiedDate().getSecond())));
		newTask.addContent(new Element(CURRENT_OCCURRENCE).setText((targetTask.getCurrentOccurrence()+"")));
		newTask.addContent(new Element(NUM_OCCURRENCE).setText((targetTask.getNumOccurrences()+"")));
		newTask = recordStatus(newTask, targetTask);
		return newTask;
	}
	
	private Element recordStatus(Element newTask, Task targetTask) {
		if(targetTask.getStatus() == Task.Status.NEWLY_ADDED) {
			newTask.addContent(new Element(STATUS).setText((NEW)));
		} else if(targetTask.getStatus() == Task.Status.UNCHANGED) {
			newTask.addContent(new Element(STATUS).setText((UNCHANGED)));
		} else if(targetTask.getStatus() == Task.Status.DELETED) {
			newTask.addContent(new Element(STATUS).setText((DELETED)));
		} else if(targetTask.getStatus() == Task.Status.ADDED_WHEN_SYNC) {
			newTask.addContent(new Element(STATUS).setText((ADDED_WHEN_SYNC)));
		} else {
			newTask.addContent(new Element(STATUS).setText((DELETED_WHEN_SYNC)));
		}
		return newTask;
	}
	
	
	/*******************************Methods for testing**********************************************/
	
	public boolean compareModelAndFileForTest() throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {//retrieve the elements
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			Element pending = rootNode.getChild(PENDING);
			Element trash = rootNode.getChild(TRASH);
			Element complete = rootNode.getChild(COMPLETE);
			//Retrieve tasks from the elements and add to model
			if (!compareListAndElement(pending, model.getPendingList())) {
				return false;
			} else if (!compareListAndElement(complete, model.getCompleteList())) {
				return false;
			} else if (!compareListAndElement(trash, model.getTrashList())) {
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
	
	private boolean compareListAndElement (Element element, ObservableList<Task> taskListInModel){
		List<Element> taskListInFile = element.getChildren();
		if (taskListInModel.size() != taskListInFile.size()) {
			return false;
		}
		for(int i = 0; i<taskListInFile.size();i++) {
			Task taskInFile = new Task();
			Element targetElement = taskListInFile.get(i);
			taskInFile = setTaskInfo(taskInFile, targetElement);
			Task taskInModel = taskListInModel.get(i);
			if(!Task.equalTask(taskInFile,taskInModel)) {
				return false;
			}
		}
		return true;
	}	
	
	boolean checkTaskListEmptyForTest(String taskListType) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {//retrive the elements
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			if (taskListType.equals(PENDING)) {
				Element pending = rootNode.getChild(PENDING);
				return pending.getChildren().isEmpty();
			} else if (taskListType.equals(COMPLETE)) {
				Element complete = rootNode.getChild(COMPLETE);
				return complete.getChildren().isEmpty();
			} else if (taskListType.equals(TRASH)) {
				Element trash = rootNode.getChild(TRASH);
				return trash.getChildren().isEmpty();
			} else {
				return false;
			}
		} catch (IOException io) {
			throw io;
		  } catch (JDOMException jdomex) {
			log.log(Level.WARNING, jdomex.getMessage());
			return false;
		  }
	}
	
	boolean searchTaskInFileForTest(Task task, String taskListType) throws IOException {
		SAXBuilder builder = new SAXBuilder();
		try {//retrive the elements
			Document doc = (Document) builder.build(xmlFile);
			Element rootNode = doc.getRootElement();
			if (taskListType.equals(PENDING)) {
				Element pending = rootNode.getChild(PENDING);
				return searchTaskInElement(pending, task);
			} else if (taskListType.equals(COMPLETE)) {
				Element complete = rootNode.getChild(COMPLETE);
				return searchTaskInElement(complete,task);
			} else if (taskListType.equals(TRASH)) {
				Element trash = rootNode.getChild(TRASH);
				return searchTaskInElement(trash, task);
			} else {
				return false;
			}
		} catch (IOException io) {
			throw io;
		  } catch (JDOMException jdomex) {
			log.log(Level.WARNING, jdomex.getMessage());
			return false;
		  }
	}

	private boolean searchTaskInElement(Element element, Task targetTask) {
		List<Element> taskList = element.getChildren();
		for(int i = 0; i<taskList.size();i++) {
			Task taskInFile = new Task();
			Element targetElement = taskList.get(i);
			taskInFile = setTaskInfo(taskInFile, targetElement);
			if(Task.equalTask(taskInFile, targetTask)) {
				return true;
			}
		}
		return false;
}
}
