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

public class DataStorage extends Store {
	
	/*public static void main (String[] args) {
		WriteXMLFile xml = new WriteXMLFile();
	}*/

	private final static String PENDING = "pending";
	private final static String COMPLETE = "complete";
	private final static String TRASH = "trash";


	public DataStorage(String fileName, Model model) {
		createDir();
		xmlFile = new File(findUserDocDir() + FOLDERNAME + "/" + fileName);
		checkIfFileExists(xmlFile);
		this.model = model;
	}
	
	public void loadFromFile()  {
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
			System.out.println(io.getMessage());
		  } catch (JDOMException jdomex) {
			System.out.println(jdomex.getMessage());
		  }
	}

	// write to file
	public void storeToFile() {
		try {
			Element root = new Element("root");
			Document doc = new Document(root);
			Element pending = new Element(PENDING);
			Element complete = new Element(COMPLETE);
			Element trash = new Element(TRASH);
			pending = addTasksToXMLFile(PENDING, pending, model.getPendingList());
			complete = addTasksToXMLFile(COMPLETE, complete, model.getCompleteList());
			trash = addTasksToXMLFile(TRASH, trash, model.getTrashList());
			doc.getRootElement().getChildren().add(pending);
			doc.getRootElement().getChildren().add(complete);
			doc.getRootElement().getChildren().add(trash);
			XMLOutputter xmlOutput = new XMLOutputter();
			xmlOutput.setFormat(Format.getPrettyFormat());
			xmlOutput.output(doc, new FileWriter(xmlFile));
			System.out.println("data saved");
		} catch (IOException io) {
			System.out.println(io.getMessage());
		}
	}

	private void addTasksToModel(Element element, String taskType)
			throws IOException {
		List<Element> taskList = element.getChildren();
		for(int i = 0; i<taskList.size();i++) {
			Task newTask = new Task();
			Element targetElement = taskList.get(i);
			newTask.setIndexId(targetElement.getChildText("indexID"));
			newTask.setWorkInfo(targetElement.getChildText("workInfo"));
			if(!targetElement.getChild("startDate").equals("-"))
				newTask.setStartDate(new CustomDate(targetElement.getChildText("startDate")));
			if(!targetElement.getChild("endDate").equals("-"))
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

	private Element addTasksToXMLFile(String taskType, Element element,
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
			else
				newTask.addContent(new Element("status").setText(("deleted")));
		}
		return element;
	}
}
