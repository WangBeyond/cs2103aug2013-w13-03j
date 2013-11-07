import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// Pop up settings dialog.
public class Settings {
	
	private static final boolean STORE_SUCCESSFUL = true;
	private static final boolean STORE_FAIL = false;


	private static Settings oneSettingsPage;
	private Model model;
	private Storage settingStore;

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
	private RadioButton autoSync;
	private RadioButton manualSync;
	private RadioButton remaining;
	private RadioButton exact;
	private ComboBox<String> colourSchemes;
	private TextField syncPeriodTextfield;
	private ImageView bgImage;

	private Settings(Model model, Storage settingStore) {
		initializeKeyVariables(model, settingStore);
	
		setupStage();
		setupContent();
		setupButtons();
		setupScene();
		setupShortcuts();
		setupDraggable();
	}

	public Stage getSettingsStage() {
		return settingsStage;
	}

	public static Settings getInstanceSettings(Model model, Storage settingStore) {
		if (oneSettingsPage == null) {
			oneSettingsPage = new Settings(model, settingStore);
		}
		return oneSettingsPage;
	}

	public void showSettingsPage() {
		googleAccountTextfield.setText(model.getUsername());
		pwBox.setText(model.getPassword());
		pwRetypeBox.setText(model.getPassword());
		if (model.doDisplayRemaining())
			remaining.setSelected(true);
		else
			exact.setSelected(true);
		if (model.getThemeMode().equals(Common.DAY_MODE)) {
			dayMode.setSelected(true);
		} else {
			nightMode.setSelected(true);
		}
		if (model.getColourScheme() != null)
			colourSchemes.setValue(model.getColourScheme());
		if (model.hasAutoSync() == true) {
			autoSync.setSelected(true);
		} else {
			manualSync.setSelected(true);
		}
		syncPeriodTextfield.setText(String.valueOf(model.getSyncPeriod()));
		settingsScene.getStylesheets().clear();
		if (model.getThemeMode().equals(Common.DAY_MODE)) {
			settingsScene.getStylesheets().addAll(
					getClass().getResource("customize.css").toExternalForm());
			bgImage.setImage(new Image(getClass().getResourceAsStream(
					"settings.png")));

		} else {
			settingsScene.getStylesheets().addAll(
					getClass().getResource("customize2.css").toExternalForm());
			bgImage.setImage(new Image(getClass().getResourceAsStream(
					"settingsNight.png")));
		}
		settingsStage.showAndWait();
	}

	private void initializeKeyVariables(Model model, Storage settingStore){
		this.model = model;
		this.settingStore = settingStore;
	}
	
	private void setupContent() {
		grid = new GridPane();
		grid.setAlignment(Pos.CENTER_LEFT);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(85, 25, 25, 40));
		setupTextfields();
		setupTimeFormat();
		setupThemeMode();
		setupColourScheme();
		setupSyncMode();
		setupSyncPeriod();
	}

	private void setupTextfields() {
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

	private void setupColourScheme() {
		Label colourScheme = new Label("Colour scheme:");
		grid.add(colourScheme, 0, 7);

		ObservableList<String> colourOptions = FXCollections
				.observableArrayList(Common.DAY_MODE, Common.NIGHT_MODE,
						"Goldfish", "Bright");

		colourSchemes = new ComboBox<String>(colourOptions);
		colourSchemes.setPrefWidth(175);
		if (model.getColourScheme() != null) {
			colourSchemes.setValue(model.getColourScheme());
		} else {
			if (model.getThemeMode().equals(Common.NIGHT_MODE)) {
				colourSchemes.setValue(Common.NIGHT_MODE);
			} else {
				colourSchemes.setValue(Common.DAY_MODE);
			}
		}
		grid.add(colourSchemes, 1, 7);
	}

	private void setupTimeFormat() {
		Label timeFormat = new Label("Time format:");
		grid.add(timeFormat, 0, 4);
		ToggleGroup toggleGroup = new ToggleGroup();
		remaining = RadioButtonBuilder.create().text("Show remaining time")
				.toggleGroup(toggleGroup).build();
		exact = RadioButtonBuilder.create().text("Show exact time")
				.toggleGroup(toggleGroup).build();
		if (model.doDisplayRemaining())
			remaining.setSelected(true);
		else
			exact.setSelected(true);
		grid.add(remaining, 1, 4);
		grid.add(exact, 2, 4);
	}

	private void setupThemeMode() {
		Label themeMode = new Label("Theme mode: ");
		grid.add(themeMode, 0, 5);
		ToggleGroup toggleGroup = new ToggleGroup();
		dayMode = RadioButtonBuilder.create().text(Common.DAY_MODE)
				.toggleGroup(toggleGroup).selected(true).build();
		nightMode = RadioButtonBuilder.create().text(Common.NIGHT_MODE)
				.toggleGroup(toggleGroup).build();
		if (model.getThemeMode().equals(Common.DAY_MODE)) {
			dayMode.setSelected(true);
		} else {
			nightMode.setSelected(true);
		}
		grid.add(dayMode, 1, 5);
		grid.add(nightMode, 2, 5);
	}

	private void setupSyncMode() {
		Label syncMode = new Label("Sync mode: ");
		grid.add(syncMode, 0, 6);
		ToggleGroup toggleGroup = new ToggleGroup();
		autoSync = RadioButtonBuilder.create().text("Auto sync")
				.toggleGroup(toggleGroup).selected(true).build();
		manualSync = RadioButtonBuilder.create().text("Manual sync")
				.toggleGroup(toggleGroup).build();
		if (model.hasAutoSync() == true) {
			autoSync.setSelected(true);
		} else {
			manualSync.setSelected(true);
		}
		grid.add(autoSync, 1, 6);
		grid.add(manualSync, 2, 6);
	}
	
	private void setupSyncPeriod(){
		Label syncPeriod = new Label("Sync period: ");
		grid.add(syncPeriod, 0, 8);
		syncPeriodTextfield = new TextField();
		syncPeriodTextfield.textProperty().addListener(new ChangeListener<String>() {
		    @Override
		    public void changed(ObservableValue<? extends String> observable,
		            String oldValue, String newValue) {
		        try {
		            // force numeric value by resetting to old value if exception is thrown
		            Integer.parseInt(newValue);
		            // force correct length by resetting to old value if longer than maxLength
		            if(newValue.length() > 4 || Integer.parseInt(newValue) == 0)
		                syncPeriodTextfield.setText(oldValue);
		        } catch (Exception e) {
		        	if(!newValue.equals(""))
		            syncPeriodTextfield.setText(oldValue);
		        }
		    }
		});
		
		syncPeriodTextfield.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov, Boolean oldVal, Boolean newVal){
				if(newVal == false){
					if(syncPeriodTextfield.getText().equals(""))
							syncPeriodTextfield.setText("1");
				}
			}
		});
		syncPeriodTextfield.setMaxWidth(50);
		syncPeriodTextfield.setText(String.valueOf(model.getSyncPeriod()));
		Label minutes = new Label("minute(s)");
		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER_LEFT);
		hb.setSpacing(10);
		hb.getChildren().addAll(syncPeriodTextfield, minutes);
		grid.add(hb, 1, 8);
	}

	private void setupScene() {
		root = new Group();
		root.getChildren().add(setupBackground());
		root.getChildren().add(grid);
		root.getChildren().add(buttons);
		settingsScene = new Scene(root, Color.rgb(70, 70, 70));
		settingsScene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
		settingsStage.setScene(settingsScene);
	}

	private void setupStage() {
		settingsStage = new Stage();
		settingsStage.initStyle(StageStyle.UNDECORATED);
		settingsStage.initModality(Modality.APPLICATION_MODAL);
		settingsStage.setWidth(599);
		settingsStage.setHeight(450);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		settingsStage
				.setX((screenBounds.getWidth() - settingsStage.getWidth()) / 2);
		settingsStage.setY((screenBounds.getHeight() - settingsStage
				.getHeight()) / 2);
		settingsStage.setTitle("iDo Settings");
		settingsStage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
	}

	private ImageView setupBackground() {
		bgImage = new ImageView();
		bgImage.setFitWidth(600);
		bgImage.setPreserveRatio(true);
		bgImage.setSmooth(true);
		bgImage.setCache(true);

		return bgImage;
	}

	private void setupButtons() {
		buttons = new Group();
		buttons.getChildren().add(setupSaveButton());
		buttons.getChildren().add(setupExitButton());
		buttons.setLayoutX(520);
		buttons.setLayoutY(375);
	}

	private boolean storeSettingChanges() {
		boolean successfulChange = STORE_FAIL;

		String account = googleAccountTextfield.getText();
		String pw = pwBox.getText();
		String pwRetype = pwRetypeBox.getText();
		
		if (account != null) {
			if (pw != null && pwRetype != null && pw.equals(pwRetype)) {
				model.setUsername(account);
				model.setPassword(pw);
				successfulChange = STORE_SUCCESSFUL;
			} else {
				pwRetypeBox.clear();
				pwRetypeBox.setPromptText("Passwords do not match!");
				return successfulChange;
			}
		}
		
		if (dayMode.isSelected()) {
			model.setThemeMode(Common.DAY_MODE);
		} else {
			model.setThemeMode(Common.NIGHT_MODE);
		}

		if (colourSchemes.getValue() != null) {
			String schemeChosen = colourSchemes.getValue().toString();
			model.setColourScheme(schemeChosen);
		}

		if (remaining.isSelected()) {
			model.setDisplayRemaining(true);
		} else {
			model.setDisplayRemaining(false);
		}

		if (autoSync.isSelected()) {
			model.setAutoSync(true);
		} else {
			model.setAutoSync(false);
		}
		
		model.setSyncPeriod(Integer.parseInt(syncPeriodTextfield.getText()));
		successfulChange = STORE_SUCCESSFUL;
		
		try {
			settingStore.updateToFile();
		} catch (IOException io) {
			successfulChange = STORE_FAIL;
		}
		return successfulChange;
	}

	private Button setupSaveButton() {
		Button saveButton = new Button("");
		saveButton.setId("save");
		saveButton.setPrefSize(76, 42);
		saveButton.setTranslateX(-95);
		saveButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				if (storeSettingChanges()) {
					settingsStage.close();
				}
			}
		});
		return saveButton;
	}

	private Button setupExitButton() {
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

	private void setupShortcuts() {
		root.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (Common.esc.match(e)) {
					settingsStage.close();
				} else if (Common.saveSettings.match(e)) {
					if (storeSettingChanges()) {
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
