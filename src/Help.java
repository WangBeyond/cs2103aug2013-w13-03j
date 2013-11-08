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

public class Help{
	private static Logger log = Logger.getLogger("Help");
	private final KeyCombination nextPage = new KeyCodeCombination(KeyCode.RIGHT, KeyCombination.CONTROL_DOWN);
	private final KeyCombination backPage = new KeyCodeCombination(KeyCode.LEFT, KeyCombination.CONTROL_DOWN);
	private final KeyCombination esc = new KeyCodeCombination(KeyCode.ESCAPE);
	
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
	
	private Help(Model model){	
		initializeModel(model);
		setupInitialStage();	
		setupScene();
		setupShortcuts();
		setupDraggable();
	}
	
	/************************** public methods ****************************/
	public static Help getInstanceHelp(Model model){
		if (oneHelpPage == null){
			oneHelpPage = new Help(model);
		}		
		return oneHelpPage;
	}
	
	public void showHelpPage(){
		changeToFirstPage();
		helpStage.show();
	}
	
	/************************** private methods ****************************/
	private void initializeModel(Model model){
		this.model = model;
	}
	
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
	private void setupScene(){
		root = new Group();
		root.getChildren().add(helpPage);
		root.getChildren().add(buttons);
		helpScene = new Scene(root, 600, 730);
		helpScene.getStylesheets().addAll(
				getClass().getResource("dayCustomization.css").toExternalForm());
		helpStage.setScene(helpScene);
	}
	
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
				} else if (esc.match(e)){
					log.log(Level.INFO, "Pressing esc for help page...");
					helpStage.close();
				}
			}
		});
	}
	
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
	
	private void changeToFirstPage(){
		setupImage(getFirstHelpImage());
		nextButton.setVisible(true);
		nextButton.setDisable(false);
		backButton.setVisible(false);
		backButton.setDisable(true);
	}
	
	private void changeToSecondPage(){
		setupImage(getSecondHelpImage());
		backButton.setVisible(true);
		backButton.setDisable(false);
		nextButton.setVisible(false);
		nextButton.setDisable(true);
	}
	
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

	private void setupImage(Image helpImage){
		helpPage.setImage(helpImage);
		helpPage.setFitWidth(600);
		helpPage.setPreserveRatio(true);
		helpPage.setSmooth(true);
		helpPage.setCache(true);
	}
	
	private Image getFirstHelpImage(){
		Image firstHelpImage;
		if (model.getThemeMode().equals(Common.DAY_MODE)){
			firstHelpImage = new Image(getClass().getResourceAsStream("helpPage1.png"));
		} else {
			firstHelpImage = new Image(getClass().getResourceAsStream("helpNightPage1.png"));
		}
		return firstHelpImage;
	}
	
	private Image getSecondHelpImage(){
		Image secondHelpImage;
		if (model.getThemeMode().equals(Common.DAY_MODE)){
			secondHelpImage = new Image(getClass().getResourceAsStream("helpPage2.png"));	
		} else{
			secondHelpImage = new Image(getClass().getResourceAsStream("helpNightPage2.png"));	
		}
		return secondHelpImage;
	}
}
