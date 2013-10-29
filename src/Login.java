import java.util.logging.Level;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;


public class Login {
	
	static final boolean STORE_SUCCESSFUL = true;
	static final boolean STORE_FAIL = false;
	private final KeyCombination saveInput = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	private final KeyCombination cancelLogin = new KeyCodeCombination(KeyCode.ESCAPE);
	
	private static Login oneLoginPage;
	private Model model;
	
	private Scene loginScene;
	private Stage loginStage;
	private Group root;
	private Group buttons;
	private GridPane grid;
	private double dragAnchorX;
	private double dragAnchorY;
	private TextField googleAccountTextfield;
	private PasswordField pwBox;
	private PasswordField pwRetypeBox;
	
	private Login(Model model){
		this.model = model;
		setupStage();
		setupForm();
		setupButtons();
		setupScene();
		setupDraggable();
		setupShortcuts();
	}
	
	public static Login getInstanceLogin(Model model){
		if (oneLoginPage == null){
			oneLoginPage = new Login(model);
		}		
		return oneLoginPage;
	}
	
	public void showLoginPage(){
		loginStage.showAndWait();
	}
	
	private void setupStage(){
		loginStage = new Stage();
		loginStage.initStyle(StageStyle.UNDECORATED);
		loginStage.initModality(Modality.APPLICATION_MODAL);
		loginStage.setWidth(415.5);
		loginStage.setHeight(315);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		loginStage.setX((screenBounds.getWidth() - loginStage.getWidth()) / 2);
		loginStage.setY((screenBounds.getHeight() - loginStage.getHeight()) / 2);
		loginStage.setTitle("iDo Login");
		loginStage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
	}
	
	private void setupForm(){
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(85, 25, 25, 40));
		setupTextfields();
	}
	
	private void setupButtons(){
		buttons = new Group();
		buttons.getChildren().add(setupSaveButton());
		buttons.getChildren().add(setupExitButton());
		buttons.setLayoutX(340);
		buttons.setLayoutY(240);
	}
	
	private void setupScene(){
		root = new Group();
		root.getChildren().add(setupBackground());
		root.getChildren().add(grid);
		root.getChildren().add(buttons);
		loginScene = new Scene(root, 415.5, 315);
		loginScene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
		loginStage.setScene(loginScene);
	}
	
	private void setupTextfields(){
		Label googleAccount = new Label("Google account:");
		grid.add(googleAccount, 0, 1);
		googleAccountTextfield = new TextField();
		googleAccountTextfield.requestFocus();
		googleAccountTextfield.setId("input");
		grid.add(googleAccountTextfield, 1, 1);

		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);
		pwBox = new PasswordField();
		grid.add(pwBox, 1, 2);
		
		Label pwRetype = new Label("Retype password:");
		grid.add(pwRetype, 0, 3);
		pwRetypeBox = new PasswordField();
		grid.add(pwRetypeBox, 1, 3);
	}
	
	private Button setupSaveButton(){
		Button saveButton = new Button("");
		saveButton.setId("save");
		saveButton.setPrefSize(90, 42);
		saveButton.setTranslateX(-120);
		saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				if (storeUserInfo()){
					loginStage.close();
				} 
			}
		});
		
		return saveButton;
	}
	
	private Button setupExitButton(){
		Button cancelButton = new Button("");
		cancelButton.setId("esc");
		cancelButton.setPrefSize(42, 42);
		cancelButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				loginStage.close();
				if (storeUserInfo()){
					loginStage.close();
				}
			}
		});
		
		return cancelButton;
	}
	
	private ImageView setupBackground(){
		Image loginImage = new Image(getClass().getResourceAsStream("login.png"));
		ImageView loginBg = new ImageView();
		loginBg.setImage(loginImage);
		loginBg.setFitWidth(415.5);
		loginBg.setPreserveRatio(true);
		loginBg.setSmooth(true);
		loginBg.setCache(true);
	
		return loginBg;
	}
	
	private boolean storeUserInfo(){
		String account = googleAccountTextfield.getText();
		String pw = pwBox.getText();
		String pwRetype = pwRetypeBox.getText();
		
		if(account != null){
			if (pw != null && pwRetype != null && pw.equals(pwRetype)) {
				model.setUsername(account);
				model.setPassword(pw);
				return STORE_SUCCESSFUL;
			} else {
				pwRetypeBox.clear();
				pwRetypeBox.setPromptText("Passwords do not match!");
			}
		}
		return STORE_FAIL;
	}
	
	private void setupShortcuts(){
		root.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e) {
				if (cancelLogin.match(e)) {
					loginStage.close();
				} else if (saveInput.match(e)){
					if (storeUserInfo()){
						loginStage.close();
					} 
				}
			}
		});
	}
	
	private void setupDraggable() {
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - loginStage.getX();
				dragAnchorY = me.getScreenY() - loginStage.getY();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				loginStage.setX(me.getScreenX() - dragAnchorX);
				loginStage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}
}
