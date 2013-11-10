import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.Group;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

//@author A0100927M
/**
 * 
 * This class provides the main graphic user interface for the Help panel.
 * 
 */
public class Help{
	private static Logger log = Logger.getLogger("Help");
	private final KeyCombination nextPage = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
	private final KeyCombination backPage = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
	
	private static Help oneHelpPage;
	public Model model;
	
	private Scene helpScene;
	private Stage helpStage;
	private Group root;
	private Group buttons;
	private ImageView helpPage;
	private Button backButton;
	private Button nextButton;
	private Button exitButton;
	private double dragAnchorX;
	private double dragAnchorY;
	
	/**
	 * This is the constructor for class Help. 
	 * 
	 * @param model
	 *            model of lists of tasks
	 */
	private Help(Model model){	
		initializeModel(model);
		setupInitialStage();	
		setupScene();
		setupShortcuts();
		setupDraggable();
	}
	
	/**
	 * This creates one instance of Help
	 * 
	 * @param model
	 *            model of lists of tasks
	 * @return the instance of Help
	 */
	public static Help getInstanceHelp(Model model){
		if (oneHelpPage == null){
			oneHelpPage = new Help(model);
		}		
		return oneHelpPage;
	}
	
	// shows the Help page at page 1
	public void showHelpPage(){
		changeToFirstPage();
		helpStage.show();
	}
	
	/************************** sets up general GUI of Help ****************************/
	/**
	 * This initializes the model of lists of tasks
	 * 
	 * @param model
	 *            model of lists of tasks
	 */
	private void initializeModel(Model model){
		this.model = model;
	}
	
	// set up the stage for the Help page
	private void setupInitialStage(){
		helpPage = new ImageView();
		buttons = new Group();
		helpStage = new Stage();
		helpStage.initStyle(StageStyle.UNDECORATED);
		helpStage.setWidth(600);
		helpStage.setHeight(750);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		helpStage.setX((screenBounds.getWidth() - helpStage.getWidth()) / 2);
		helpStage.setY((screenBounds.getHeight() - helpStage.getHeight()) / 2);
		helpStage.setTitle("iDo Help Page");
		helpStage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
		setupButtons();
		changeToFirstPage();
	}
	
	// create the scene of the stage
	private void setupScene(){
		root = new Group();
		root.getChildren().add(helpPage);
		root.getChildren().add(buttons);
		helpScene = new Scene(root, 600, 730);
		helpScene.getStylesheets().addAll(
				getClass().getResource("dayCustomization.css").toExternalForm());
		helpStage.setScene(helpScene);
	}
	
	// create shortcut keys for Help page
	private void setupShortcuts(){
		root.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e) {
				log.log(Level.INFO, "Executing shortcut key...");
				if (nextPage.match(e)) {
					log.log(Level.INFO, "Pressing ctrl + right...");
					changeToSecondPage();
				} else if (backPage.match(e)){
					log.log(Level.INFO, "Pressing ctrl + left...");
					changeToFirstPage();
				} else if (Common.esc.match(e)){
					log.log(Level.INFO, "Pressing esc for help page...");
					helpStage.close();
				}
			}
		});
	}
	
	// set up draggable
	private void setupDraggable() {
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - helpStage.getX();
				dragAnchorY = me.getScreenY() - helpStage.getY();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				helpStage.setX(me.getScreenX() - dragAnchorX);
				helpStage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}
	
	// set up all buttons on help page
	private void setupButtons() {
		setupBackButton();
		setupNextButton();
		setupExitButton();
		buttons.getChildren().add(backButton);
		buttons.getChildren().add(nextButton);
		buttons.getChildren().add(exitButton);
		buttons.setLayoutX(510);
		buttons.setLayoutY(25);
	}
	
	/************************** changes display image of Help ****************************/
	// changes the image shown on Help page to page 1
	private void changeToFirstPage(){
		setupImage(getFirstHelpImage());
		nextButton.setVisible(true);
		nextButton.setDisable(false);
		backButton.setVisible(false);
		backButton.setDisable(true);
	}
	
	// changes the image shown on Help page to page 2
	private void changeToSecondPage(){
		setupImage(getSecondHelpImage());
		backButton.setVisible(true);
		backButton.setDisable(false);
		nextButton.setVisible(false);
		nextButton.setDisable(true);
	}
	
	/**
	 * This sets up the help image as the background of the stage
	 * 
	 * @param helpImage
	 *            images of the Help background
	 */
	private void setupImage(Image helpImage){
		helpPage.setImage(helpImage);
		helpPage.setFitWidth(600);
		helpPage.setPreserveRatio(true);
		helpPage.setSmooth(true);
		helpPage.setCache(true);
	}
	
	/**
	 * Get function of page 1 Help image
	 * 
	 * @return firstHelpImage
	 *            image page 1 of Help
	 */
	private Image getFirstHelpImage(){
		Image firstHelpImage;
		if (model.getThemeMode().equals(Common.DAY_MODE)){
			firstHelpImage = new Image(getClass().getResourceAsStream("helpPage1.png"));
		} else {
			firstHelpImage = new Image(getClass().getResourceAsStream("helpNightPage1.png"));
		}
		return firstHelpImage;
	}
	
	/**
	 * Get function of page 2 Help image
	 * 
	 * @return secondHelpImage
	 *            image page 2 of Help
	 */
	private Image getSecondHelpImage(){
		Image secondHelpImage;
		if (model.getThemeMode().equals(Common.DAY_MODE)){
			secondHelpImage = new Image(getClass().getResourceAsStream("helpPage2.png"));	
		} else{
			secondHelpImage = new Image(getClass().getResourceAsStream("helpNightPage2.png"));	
		}
		return secondHelpImage;
	}
	
	/************************** sets up the individual Buttons ****************************/
	// sets up Button to go from page 1 to 2
	private void setupNextButton() {
		nextButton = new Button("");
		nextButton.setId("next");
		nextButton.setPrefSize(30, 30);
		nextButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				changeToSecondPage();
			}
		});
	}
	
	// sets up Button to go from page 2 to 1
	private void setupBackButton() {
		backButton = new Button("");
		backButton.setId("back");
		backButton.setPrefSize(30, 30);
		backButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				changeToFirstPage();
			}
		});
	}
	
	// sets up Exit Button to exit Help
	private void setupExitButton() {
		exitButton = new Button("");
		exitButton.setId("close_help");
		exitButton.setPrefSize(25, 25);
		exitButton.setTranslateX(40);
		exitButton.setTranslateY(3);
		exitButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				helpStage.close();
			}
		});
	}	
	
}
