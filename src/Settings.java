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
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Pop up settings dialog.
public class Settings {

	private final KeyCombination esc = new KeyCodeCombination(KeyCode.ESCAPE);
	private final KeyCombination saveSettings = new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN);
	
	private Stage settingsStage;
	private Scene settingsScene;
	private GridPane grid;
	private Group root;
	private double dragAnchorX;
	private double dragAnchorY;
	
	public Settings(){
		setupStage();
		setupContent();
		setupButtons();
		setupShortcuts();
		setupDraggable();
		setupScene();
	}
	
	public void showSettingsPage(){
		settingsStage.show();
	}
	
	private void setupContent(){
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(85, 25, 25, 40));
		setupTextfields();
		
		root.getChildren().add(grid);
	}
	
	private void setupTextfields(){
		Label googleAccount = new Label("Google account:");
		grid.add(googleAccount, 0, 1);
		TextField googleAccountTextfield = new TextField();
		googleAccountTextfield.setId("input");
		grid.add(googleAccountTextfield, 1, 1);

		Label pw = new Label("Password:");
		grid.add(pw, 0, 2);
		PasswordField pwBox = new PasswordField();
		pwBox.setId("input");
		grid.add(pwBox, 1, 2);
		
		Label pwRetype = new Label("Retype password:");
		grid.add(pwRetype, 0, 3);
		PasswordField pwRetypeBox = new PasswordField();
		pwRetypeBox.setId("input");
		grid.add(pwRetypeBox, 1, 3);
	}
	
	private void setupScene(){
		settingsScene = new Scene(root, Color.rgb(70, 70, 70));
		settingsScene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
		settingsStage.setScene(settingsScene);
	}
	
	private void setupStage(){
		root = new Group();
		settingsStage = new Stage();
		settingsStage.initStyle(StageStyle.UNDECORATED);
		settingsStage.setWidth(599);
		settingsStage.setHeight(450);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		settingsStage.setX((screenBounds.getWidth() - settingsStage.getWidth()) / 2);
		settingsStage.setY((screenBounds.getHeight() - settingsStage.getHeight()) / 2);
		settingsStage.setTitle("iDo Settings");
		settingsStage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
		setupBackground();
	}
	
	private void setupBackground(){
		Image bg = new Image(getClass().getResourceAsStream("settings.png"));
		ImageView bgImage = new ImageView();
		bgImage.setImage(bg);
		bgImage.setFitWidth(600);
		bgImage.setPreserveRatio(true);
		bgImage.setSmooth(true);
		bgImage.setCache(true); 
		root.getChildren().add(bgImage);
	}
	
	private void setupButtons(){
		Button exitButton = new Button("");
		exitButton.setId("esc");
		exitButton.setPrefSize(42, 42);
		exitButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				settingsStage.close();
			}
		});
	}
	
	private void setupShortcuts(){
		root.setOnKeyPressed(new EventHandler<KeyEvent>(){
			public void handle(KeyEvent e) {
				if (esc.match(e)) {
					settingsStage.close();
				} else if (saveSettings.match(e)){
					// TODO: save changes
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
