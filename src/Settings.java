import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.RadioButtonBuilder;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Pop up settings dialog.
public class Settings {

	static final boolean STORE_SUCCESSFUL = true;
	static final boolean STORE_FAIL = false;
	private final KeyCombination esc = new KeyCodeCombination(KeyCode.ESCAPE);
	private final KeyCombination saveSettings = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	
	private static Settings oneSettingsPage;
	private Model model;
	
	private Stage settingsStage;
	private Scene settingsScene;
	private GridPane grid;
	private Group root;
	private Group buttons;
	private double dragAnchorX;
	private double dragAnchorY;
	private TextField googleAccountTextfield;
	private PasswordField pwBox;
	private PasswordField pwRetypeBox;
	private RadioButton dayMode;
	private RadioButton nightMode;
	private RadioButton remaining;
	private RadioButton exact;
	
	private Settings(Model model){
		this.model = model;
		setupStage();
		setupContent();
		setupButtons();
		setupScene();
		setupShortcuts();
		setupDraggable();
	}
	
	public static Settings getInstanceSettings(Model model){
		if (oneSettingsPage == null){
			oneSettingsPage = new Settings(model);
		}		
		return oneSettingsPage;
	}
	
	public void showSettingsPage(){
		settingsStage.showAndWait();
	}
	
	private void setupContent(){
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(85, 25, 25, 40));
		setupTextfields();
		setupTimeFormat();
		setupThemeMode();
	}
	
	private void setupTextfields(){
		Label googleAccount = new Label("Google account:");
		grid.add(googleAccount, 0, 1);
		googleAccountTextfield = new TextField();
		googleAccountTextfield.setText(model.getUsername());
		googleAccountTextfield.setId("input");
		grid.add(googleAccountTextfield, 1, 1);

		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);
		pwBox = new PasswordField();
		pwBox.setText(model.getPassword());
		grid.add(pwBox, 1, 2);
		
		Label pwRetype = new Label("Retype password:");
		grid.add(pwRetype, 0, 3);
		pwRetypeBox = new PasswordField();
		pwRetypeBox.setText(model.getPassword());
		grid.add(pwRetypeBox, 1, 3);
	}
	
	private void setupTimeFormat(){
		Label timeFormat = new Label("Time format:");
		grid.add(timeFormat, 0, 4);
		ToggleGroup toggleGroup = new ToggleGroup();
		remaining = RadioButtonBuilder.create().text("Show remaining time").toggleGroup(toggleGroup).build();
		exact = RadioButtonBuilder.create().text("Show exact time").toggleGroup(toggleGroup).build();
		if(model.doDisplayRemaining())
			remaining.setSelected(true);
		else
			exact.setSelected(true);
		grid.add(remaining, 1, 4);
		grid.add(exact, 2, 4);
	}
	
	private void setupThemeMode(){
		Label themeMode = new Label("Theme mode: ");
		grid.add(themeMode, 0, 5);
		ToggleGroup toggleGroup = new ToggleGroup();
		dayMode = RadioButtonBuilder.create().text("Day mode").toggleGroup(toggleGroup).selected(true).build();
		nightMode = RadioButtonBuilder.create().text("Night mode").toggleGroup(toggleGroup).build();
		if(model.getThemeMode().equals(Model.DAY_MODE))
			dayMode.setSelected(true);
		else
			nightMode.setSelected(true);
		grid.add(dayMode, 1, 5);
		grid.add(nightMode, 2, 5);
	}
	
	private void setupScene(){
		root = new Group();
		root.getChildren().add(setupBackground());
		root.getChildren().add(grid);
		root.getChildren().add(buttons);
		settingsScene = new Scene(root, Color.rgb(70, 70, 70));
		settingsScene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
		settingsStage.setScene(settingsScene);
	}
	
	private void setupStage(){
		settingsStage = new Stage();
		settingsStage.initStyle(StageStyle.UNDECORATED);
		settingsStage.initModality(Modality.APPLICATION_MODAL);
		settingsStage.setWidth(599);
		settingsStage.setHeight(450);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		settingsStage.setX((screenBounds.getWidth() - settingsStage.getWidth()) / 2);
		settingsStage.setY((screenBounds.getHeight() - settingsStage.getHeight()) / 2);
		settingsStage.setTitle("iDo Settings");
		settingsStage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
	}
	
	private ImageView setupBackground(){
		Image bg = new Image(getClass().getResourceAsStream("settings.png"));
		ImageView bgImage = new ImageView();
		bgImage.setImage(bg);
		bgImage.setFitWidth(600);
		bgImage.setPreserveRatio(true);
		bgImage.setSmooth(true);
		bgImage.setCache(true); 
		
		return bgImage;
	}
	
	private void setupButtons(){
		buttons = new Group();
		buttons.getChildren().add(setupSaveButton());
		buttons.getChildren().add(setupExitButton());
		buttons.setLayoutX(520);
		buttons.setLayoutY(375);
	}
	
	private boolean storeSettingChanges(){
		String account = googleAccountTextfield.getText();
		String pw = pwBox.getText();
		String pwRetype = pwRetypeBox.getText();
		
		if(dayMode.isSelected())
			model.setThemeMode(Model.DAY_MODE);
		else
			model.setThemeMode(Model.NIGHT_MODE);
		
		if(remaining.isSelected())
			model.setDisplayRemaining(true);
		else
			model.setDisplayRemaining(false);
		
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
	
	private Button setupSaveButton(){
		Button saveButton = new Button("");
		saveButton.setId("save");
		saveButton.setPrefSize(90, 42);
		saveButton.setTranslateX(-120);
		saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				if (storeSettingChanges()){
					settingsStage.close();
				} 
			}
		});
		
		return saveButton;
	}
	
	private Button setupExitButton(){
		Button exitButton = new Button("");
		exitButton.setId("esc");
		exitButton.setPrefSize(42, 42);
		exitButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				settingsStage.close();
			}
		});
		
		return exitButton;
	}
	
	private void setupShortcuts(){
		root.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e) {
				if (esc.match(e)) {
					settingsStage.close();
				} else if (saveSettings.match(e)){
					if (storeSettingChanges()){
						settingsStage.close();
					} 
				}
			}
		});
	}
	
	private void setupDraggable() {
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - settingsStage.getX();
				dragAnchorY = me.getScreenY() - settingsStage.getY();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				settingsStage.setX(me.getScreenX() - dragAnchorX);
				settingsStage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}
}
