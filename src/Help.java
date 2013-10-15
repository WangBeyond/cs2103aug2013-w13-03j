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
import javafx.scene.layout.HBox;
import javafx.scene.Group;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// TODO: close function
public class Help {
	
	final KeyCombination nextPage = new KeyCodeCombination(KeyCode.RIGHT);
	final KeyCombination backPage = new KeyCodeCombination(KeyCode.LEFT);
	
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
	
	private boolean onFirstPage;
	private boolean onSecondPage;
	
	public Help(){
		helpPage = new ImageView();
		onFirstPage = true;
		onSecondPage = false;
		root = new Group();
		buttons = new Group();
		
		setupInitialStage();
		setupButtons();
		changeToFirstPage();	
		root.getChildren().add(helpPage);
		root.getChildren().add(buttons);
		helpScene = new Scene(root, 600, 730);
		helpScene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
		helpStage.setScene(helpScene);
		setupDraggable();
	}
	
	public void showHelpPage(){
		changeToFirstPage();
		helpStage.show();
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
		onSecondPage = false;
		onFirstPage = true;
	}
	
	private void changeToSecondPage(){
		setupImage(getSecondHelpImage());
		backButton.setVisible(true);
		backButton.setDisable(false);
		nextButton.setVisible(false);
		nextButton.setDisable(true);
		onSecondPage = true;
		onFirstPage = false;
	}
	
	private void setupInitialStage(){
		helpStage = new Stage();
		helpStage.initStyle(StageStyle.UNDECORATED);
		helpStage.setWidth(600);
		helpStage.setHeight(730);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		helpStage.setX((screenBounds.getWidth() - helpStage.getWidth()) / 2);
		helpStage.setY((screenBounds.getHeight() - helpStage.getHeight()) / 2);
	}
	
	private void setupImage(Image helpImage){
		helpPage.setImage(helpImage);
		helpPage.setFitWidth(600);
		helpPage.setPreserveRatio(true);
		helpPage.setSmooth(true);
		helpPage.setCache(true);
	}
	
	private Image getFirstHelpImage(){
		Image firstHelpImage = new Image(getClass().getResourceAsStream("help_1.png"));		
		return firstHelpImage;
	}
	
	private Image getSecondHelpImage(){
		Image secondHelpImage = new Image(getClass().getResourceAsStream("help_2.png"));		
		return secondHelpImage;
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
}
