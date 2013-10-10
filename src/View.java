import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabBuilder;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBuilder;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextBuilder;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;
import javafx.util.Duration;

public class View implements HotkeyListener {
	final KeyCombination collapseWindow = new KeyCodeCombination(KeyCode.UP,
			KeyCombination.CONTROL_DOWN);
	final KeyCombination expandWindow = new KeyCodeCombination(KeyCode.DOWN,
			KeyCombination.CONTROL_DOWN);
	final KeyCombination hideWindow = new KeyCodeCombination(KeyCode.H,
			KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN);
	final KeyCombination changeTab = new KeyCodeCombination(KeyCode.TAB,
			KeyCombination.CONTROL_DOWN);

	// Help page
	public Help helpPage;
	
	// Command Line for user to input command
	public TextField commandLine;
	// Instant feedback
	public Text feedback;
	// Button to expand or collapse window
	public Button showOrHide;

	// Tab Pane to contain 3 tables
	public TabPane tabPane;

	// Table View in 3 tabs
	public TableView<Task> taskPendingList;
	public TableView<Task> taskCompleteList;
	public TableView<Task> taskTrashList;

	public Scene scene;
	public BorderPane root;
	public Stage stage;

	// Store the coordinates of the anchor of the window
	double dragAnchorX;
	double dragAnchorY;

	// colored text sequences
	public ArrayList<Text> textList = new ArrayList<Text>();
	// Color indicatin: darkgrey: uselessInfo, green: commandType, black:
	// workflow, orange: TAG, blue: startTime,
	// darkcyan: endTime, red: impt, darkkhaki: repeating, purple: index
	public Paint[] colors = { Color.DARKGREY, Color.GREEN, Color.BLACK,
			Color.ORANGE, Color.BLUE, Color.DARKCYAN, Color.RED,
			Color.DARKKHAKI, Color.PURPLE };
	public static boolean isTextColored = true;
	public ArrayList<Text> feedbackList = new ArrayList<Text>();
	private static String[] COMMAND_TYPES = { "add", "remove", "rm", "delete",
			"del", "edit", "mod", "modify", "search", "find", "clear", "clr",
			"undo", "redo", "mark", "unmark", "complete", "done", "incomplete",
			"undone", "exit", "sync", "show", "all", "list", "ls", "today" ,"help"};
	public HBox feedbacks;
	private static Color IDO_GREEN = Color.rgb(130, 255, 121);
	//private HBox multiColorCommand;
	
	// The 3 sections
	VBox bottom;
	HBox center;
	AnchorPane top;

	// Icon in the system tray
	TrayIcon trayIcon;

	Model model;

	/**
	 * This is the constructor for class View. It will create the content in the
	 * GUI and setup the scene for the stage in Control class.
	 * 
	 * @param model
	 *            model of lists of tasks
	 * @param primaryStage
	 *            main stage of the GUI
	 */
	public View(final Model model, final Stage primaryStage) {
		stage = primaryStage;
		this.model = model;
		
		setupHelpPage();
		setupStage();
		loadLibrary();
		checkIntellitype();
		initGlobalHotKey();

		// add 10 texts to textList
		textList.clear();
		for (int i = 0; i < 10; i++) {
			textList.add(new Text());
		}
		hideInSystemTray();
		Platform.setImplicitExit(false);
		createContent();
		setupShortcuts();
		setDraggable();
		setupScene();
	}

	private void setupHelpPage(){
		helpPage = new Help();
	}
	
	public void showHelpPage(){
		helpPage.showHelpPage();
	}
	
	private void setupShortcuts() {
		root.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent e) {
				if (changeTab.match(e)) {
					int index = tabPane.getSelectionModel().getSelectedIndex();
					if (index != 2)
						tabPane.getSelectionModel().selectNext();
					else
						tabPane.getSelectionModel().selectFirst();
				}
				if (collapseWindow.match(e)) {
					collapseAnimation();
				} else if (expandWindow.match(e)) {
					expandAnimation();
				} else if (hideWindow.match(e)) {
					hide();
				} else if (!e.isAltDown() && !e.isControlDown()
						&& !e.isShiftDown() && !e.isShortcutDown()) {
					commandLine.requestFocus();
					commandLine.appendText(e.getCharacter());
				}
			}
		});
	}

	private void createContent() {
		createTopSection();
		createCenterSection();
		createBottomSection();

		root = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();
	}

	private void setupStage() {
		stage.setWidth(760);
		stage.setHeight(540);
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((primaryScreenBounds.getHeight() - stage.getHeight()) / 2);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("iDo V0.1");
		stage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
	}

	private void setupScene() {
		scene = new Scene(root, Color.rgb(70, 70, 70));
		customizeGUIWithCSS();
		stage.setScene(scene);
		stage.show();
		removeTopAndCenter();
		showOrHide.setId("larger");
		stage.setHeight(70);
	}

	private void customizeGUIWithCSS() {
		scene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
	}

	private void createBottomSection() {
		bottom = new VBox();
		bottom.setSpacing(5);
		bottom.setPadding(new Insets(0, 0, 5, 44));

		HBox upperPart = new HBox();
		if (isTextColored)
			upperPart = createNewUpperPartInBottom();
		else
			upperPart = createUpperPartInBottom();

		feedbacks = new HBox();
		feedbacks.setSpacing(5);
		feedbackList.clear();
		for (int i = 0; i < 10; i++) {
			Text feedbackPiece = TextBuilder.create().styleClass("feedback")
					.fill(Color.WHITE).text("").build();
			feedbackList.add(feedbackPiece);
			feedbacks.getChildren().add(feedbackList.get(i));
		}
		feedbackList.get(0).setText("Please enter a command");
		bottom.getChildren().addAll(upperPart,
				feedbacks);
	}

	private HBox createUpperPartInBottom() {
		HBox temp = new HBox();
		temp.setSpacing(10);
		commandLine = new TextField();
		commandLine.setPrefWidth(630);

		showOrHide = new Button();
		showOrHide.setPrefSize(30, 30);
		showOrHide.setId("smaller");
		hookUpEventForShowOrHide();
		temp.getChildren().addAll(commandLine, showOrHide);

		return temp;
	}

	void hideCursor() {
		commandLine.setStyle("-fx-text-fill: white;");
	}

	void displayCursor() {
		commandLine.setStyle("-fx-text-fill: darkgrey;");
	}

	private HBox createNewUpperPartInBottom() {
		HBox temp2 = new HBox();
		HBox multiColorCommandFront = new HBox();
		HBox multiColorCommandRear = new HBox();
		HBox multiColorCommand = new HBox();
		Pane temp = new Pane();
		temp2.setSpacing(10);
		
		commandLine = new TextField();
		commandLine.setPrefWidth(630);
		commandLine.setStyle("-fx-text-fill: darkgrey;");
		
		showOrHide = new Button();
		showOrHide.setPrefSize(30, 30);
		showOrHide.setId("smaller");
		hookUpEventForShowOrHide();
		//multiColorCommand add texts of different colors.
		for (int i =0 ;i<4;i++) {
			multiColorCommandFront.getChildren().add(textList.get(i));
		}
		for (int i=4;i<textList.size();i++) {
			multiColorCommandRear.getChildren().add(textList.get(i));
		}
		multiColorCommand.getChildren().addAll(multiColorCommandFront,multiColorCommandRear);
		multiColorCommandFront.setSpacing(-0.50001);
		multiColorCommandRear.setSpacing(-0.50001);
		multiColorCommand.setLayoutX(7);
		multiColorCommand.setLayoutY(5);
		multiColorCommand.setSpacing(1);
		temp.getChildren().addAll(commandLine, multiColorCommand);
		temp2.getChildren().addAll(temp, showOrHide);
		return temp2;
	}
	
	void updateMultiColorCommand(String temporaryCommand) {
		System.out.println("command " + temporaryCommand);
		emptyTextList();
		try {
			ArrayList<InfoWithIndex> infoList = Parser
					.parseForView(temporaryCommand);
			for (int i = 0; i < infoList.size(); i++) {
				InfoWithIndex info = infoList.get(i);
				Text text = textList.get(i);
				text.setText(info.getInfo());
				text.setStyle("-fx-font: 15.0px Ubantu;");
				text.setTextAlignment(TextAlignment.LEFT);
				text.setFill(colors[info.getInfoType() + 2]);
				System.out.print(info.getInfo()+" "+info.getInfoType()+"  ");
			}
			System.out.println();
			for(int i =5;i<textList.size();i++) {
				textList.get(i).setLayoutX(10);
			}
			displayCursor();
		} catch (Exception ex) {
			if(Parser.doesArrayContain(COMMAND_TYPES , temporaryCommand.trim())) {
				textList.get(0).setFill(Color.GREEN);;
				textList.get(0).setText(temporaryCommand);
			} else {
				textList.get(0).setStyle("-fx-font: 15.0px Ubantu;");
				textList.get(0).setText(temporaryCommand);
				textList.get(0).setFill(Color.DARKGRAY);
				emptyFeedback(0);
				setFeedbackStyle(0, ex.getMessage() , Color.WHITE);
			}
		}
	}
	
	private void createCenterSection() {
		createTabPane();

		center = HBoxBuilder.create().padding(new Insets(0, 44, 0, 44))
				.children(tabPane).build();
	}

	private void createTabPane() {
		taskPendingList = new TableView<Task>();
		createTable(taskPendingList, model.getPendingList());

		taskCompleteList = new TableView<Task>();
		createTable(taskCompleteList, model.getCompleteList());

		taskTrashList = new TableView<Task>();
		createTable(taskTrashList, model.getTrashList());

		tabPane = new TabPane();
		Tab pending = TabBuilder.create().content(taskPendingList)
				.text("PENDING").closable(false).build();
		Tab complete = TabBuilder.create().content(taskCompleteList)
				.text("COMPLETE").closable(false).build();
		Tab trash = TabBuilder.create().content(taskTrashList).text("TRASH")
				.closable(false).build();

		tabPane.getTabs().addAll(pending, complete, trash);
		setTabChangeListener();
	}

	private void createTopSection() {
		top = new AnchorPane();
		top.setPadding(new Insets(-15, 15, -30, 44));

		ImageView title = createTitle();

		HBox buttons = createModifyingButtons();

		setupLayout(title, buttons);

	}

	private void setDraggable() {
		// Get the position of the mouse in the stage
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - stage.getX();
				dragAnchorY = me.getScreenY() - stage.getY();
			}
		});

		// Moving with the stage with the mouse at constant position relative to
		// the stage
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				stage.setX(me.getScreenX() - dragAnchorX);
				stage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}

	private void setupLayout(ImageView title, HBox buttons) {
		top.getChildren().addAll(title, buttons);
		AnchorPane.setLeftAnchor(title, 10.0);
		AnchorPane.setTopAnchor(buttons, 25.0);
		AnchorPane.setTopAnchor(title, 30.0);
		AnchorPane.setRightAnchor(buttons, 5.0);
	}

	private ImageView createTitle() {
		Image iDo = new Image("iDo_new.png");
		ImageView title = new ImageView();
		title.setImage(iDo);
		title.setFitWidth(110);
		title.setPreserveRatio(true);
		title.setSmooth(true);
		title.setCache(true);
		return title;
	}

	private HBox createModifyingButtons() {
		Button minimizeButton = createMinimizeButton();
		Button closeButton = createExitButton();

		HBox hb = new HBox();
		hb.getChildren().add(minimizeButton);
		hb.getChildren().add(closeButton);
		hb.setSpacing(10);
		hb.setAlignment(Pos.BOTTOM_CENTER);
		return hb;
	}

	private Button createMinimizeButton() {
		Button targetButton = new Button("");
		targetButton.setPrefSize(20, 20);
		targetButton.setId("minimize");
		targetButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				hide();
			}
		});
		return targetButton;
	}

	private Button createExitButton() {
		Button targetButton = new Button("");
		targetButton.setPrefSize(20, 20);
		targetButton.setId("close");
		targetButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				JIntellitype.getInstance().cleanUp();
				System.exit(0);
			}
		});
		return targetButton;
	}

	public void createTable(TableView<Task> taskList, ObservableList<Task> list) {
		taskList.setItems(list);
		final Text emptyTableSign = new Text(
				"There is currently no task in this tab");
		emptyTableSign.setFont(new Font(15));
		taskList.setPlaceholder(emptyTableSign);
		TableColumn<Task, String> taskInfoColumn = createTaskInfoColumn();
		TableColumn<Task, Tag> tagColumn = createTagColumn();
		TableColumn<Task, String> startDateColumn = createStartDateColumn();
		TableColumn<Task, String> endDateColumn = createEndDateColumn();
		TableColumn<Task, Boolean> isImportantColumn = createIsImportantColumn();

		taskList.setStyle("-fx-table-cell-border-color: transparent;");

		ObservableList<TableColumn<Task, ?>> columns = taskList.getColumns();
		columns.add(taskInfoColumn);
		columns.add(startDateColumn);
		columns.add(endDateColumn);
		columns.add(tagColumn);
		columns.add(isImportantColumn);
	}

	private TableColumn<Task, Boolean> createIsImportantColumn() {
		TableColumn<Task, Boolean> tempColumn = TableColumnBuilder
				.<Task, Boolean> create().visible(true).prefWidth(10).build();
		setupIsImportantProperty(tempColumn);
		setupIsImportantUpdateFormat(tempColumn);
		return tempColumn;
	}

	private void setupIsImportantProperty(TableColumn<Task, Boolean> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, Boolean>(
				"isImportant"));
	}

	private void setupIsImportantUpdateFormat(
			TableColumn<Task, Boolean> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, Boolean>, TableCell<Task, Boolean>>() {

					@Override
					public TableCell<Task, Boolean> call(
							TableColumn<Task, Boolean> param) {
						TableCell<Task, Boolean> tc = new TableCell<Task, Boolean>() {
							@Override
							public void updateItem(Boolean item, boolean empty) {
								super.updateItem(item, empty);
								if (item != null) {
									clearStyle();
									setStyle(item);
								}
							}

							private void clearStyle() {
								getTableRow().getStyleClass().removeAll(
										"table-row-cell", "unimportant",
										"important");
							}

							private void setStyle(boolean isImportant) {
								if (isImportant)
									getTableRow().getStyleClass().add(
											"important");
								else
									getTableRow().getStyleClass().add(
											"unimportant");
							}
						};
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createEndDateColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().text("End").sortable(false)
				.prefWidth(120).build();

		setupEndDateProperty(tempColumn);
		setupEndDateUpdateFormat(tempColumn);

		return tempColumn;
	}

	private void setupEndDateProperty(TableColumn<Task, String> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, String>(
				"endDateString"));
	}

	private void setupEndDateUpdateFormat(TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> param) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null)
									setText(item);
							}
						};
						tc.setAlignment(Pos.CENTER);
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createStartDateColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().text("Start").prefWidth(120)
				.sortable(false).build();

		setupStartDateProperty(tempColumn);
		setupStartDateUpdateFormat(tempColumn);

		return tempColumn;
	}

	private void setupStartDateProperty(TableColumn<Task, String> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, String>(
				"startDateString"));
	}

	private void setupStartDateUpdateFormat(TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> param) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							public void updateItem(String item, boolean empty) {
								if (item != null)
									setText(item);
							}
						};
						tc.setAlignment(Pos.CENTER);
						return tc;
					}
				});
	}

	private TableColumn<Task, Tag> createTagColumn() {
		TableColumn<Task, Tag> tempColumn = TableColumnBuilder
				.<Task, Tag> create().text("Tag").sortable(false)
				.prefWidth(120).build();

		setupTagProperty(tempColumn);
		setupTagUpdateFormat(tempColumn);

		return tempColumn;
	}

	private void setupTagProperty(TableColumn<Task, Tag> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, Tag>(
				"tag"));
	}

	private void setupTagUpdateFormat(TableColumn<Task, Tag> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, Tag>, TableCell<Task, Tag>>() {

					@Override
					public TableCell<Task, Tag> call(
							TableColumn<Task, Tag> param) {
						TableCell<Task, Tag> tc = new TableCell<Task, Tag>() {
							public void updateItem(Tag item, boolean empty) {
								if (item != null) {
									String text;
									if (item.getRepetition()
											.equals(Parser.NULL))
										text = item.getTag();
									else
										text = (item.getTag().equals("-") ? ""
												: item.getTag() + "\n")
												+ "#"
												+ item.getRepetition();
									setText(text);
								}
							}
						};
						tc.setAlignment(Pos.CENTER);
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createTaskInfoColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().text("Task").sortable(false)
				.prefWidth(300).build();

		setupTaskInfoProperty(tempColumn);
		setupTaskInfoUpdateFormat(tempColumn);

		return tempColumn;
	}

	private void setupTaskInfoProperty(TableColumn<Task, String> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, String>(
				"workInfo"));
	}

	private void setupTaskInfoUpdateFormat(TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> arg0) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null)
									setText((getTableRow().getIndex() + 1)
											+ ". " + item);
							}
						};
						return tc;
					}
				});
	}

	private void hookUpEventForShowOrHide() {
		showOrHide.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (stage.getHeight() > 500) {
					collapseAnimation();
				} else {
					expandAnimation();
				}
			}
		});
	}

	private void removeTopAndCenter() {
		root.setTop(null);
		root.setCenter(null);
	}

	private void collapseAnimation() {
		removeTopAndCenter();
		showOrHide.setId("larger");
		Timer animTimer = new Timer();
		animTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > 70) {
					stage.setHeight(stage.getHeight() - 10);
				} else {
					this.cancel();
				}
			}
		}, 0, 3);
	}

	private void expandAnimation() {
		Timer animTimer = new Timer();
		root.setTop(top);
		showOrHide.setId("smaller");
		animTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > 530) {
					setCenterWithFadeTransition();
				}

				if (stage.getHeight() < 540) {
					stage.setHeight(stage.getHeight() + 10);
				} else {
					this.cancel();
				}
			}
		}, 0, 3);
	}

	private void setCenterWithFadeTransition() {
		Platform.runLater(new Runnable() {
			public void run() {
				root.setCenter(center);
				FadeTransition fade = new FadeTransition(Duration.millis(500),
						center);
				fade.setFromValue(0.0);
				fade.setToValue(1.0);
				fade.play();
			}
		});
	}

	private void hideInSystemTray() {
		if (SystemTray.isSupported()) {
			SystemTray tray = SystemTray.getSystemTray();
			java.awt.Image iconImage = getIconImage();

			PopupMenu popupMenu = createPopupMenu();

			createTrayIcon(iconImage, popupMenu);

			try {
				tray.add(trayIcon);
			} catch (AWTException e) {
				System.err.println(e);
			}
		}
	}

	private void createTrayIcon(java.awt.Image iconImage, PopupMenu popupMenu) {
		trayIcon = new TrayIcon(iconImage, "iDo V0.1", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(createShowListener());
	}

	private PopupMenu createPopupMenu() {
		final PopupMenu popup = new PopupMenu();

		MenuItem showItem = new MenuItem("Show the main window");
		showItem.addActionListener(createShowListener());
		popup.add(showItem);

		popup.addSeparator();

		MenuItem settingsItem = new MenuItem("Preferences");
		popup.add(settingsItem);

		MenuItem closeItem = new MenuItem("Exit");
		closeItem.addActionListener(createExitListener());
		popup.add(closeItem);

		return popup;
	}

	private ActionListener createShowListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						stage.toFront();
						stage.show();
					}
				});
			}
		};
	}

	private ActionListener createExitListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JIntellitype.getInstance().cleanUp();
				System.exit(0);
			}
		};
	}

	private java.awt.Image getIconImage() {
		try {
			java.awt.Image image = ImageIO.read(getClass().getResource(
					"iDo_traybar.png"));
			java.awt.Image rescaled = image.getScaledInstance(15, 15,
					java.awt.Image.SCALE_SMOOTH);
			return rescaled;
		} catch (IOException e) {
			return null;
		}
	}

	private void hide() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				if (SystemTray.isSupported()) {
					stage.hide();
				} else {
					JIntellitype.getInstance().cleanUp();
					System.exit(0);
				}
			}
		});
	}

	private void loadLibrary() {
		System.loadLibrary("JIntellitype");
	}

	private void checkIntellitype() {
		// first check to see if an instance of this application is already
		// running, use the name of the window title of this JFrame for checking
		if (JIntellitype.checkInstanceAlreadyRunning("iDo V0.1")) {
			System.exit(1);
		}

		// next check to make sure JIntellitype DLL can be found and we are on
		// a Windows operating System
		if (!JIntellitype.isJIntellitypeSupported()) {
			System.exit(1);
		}
	}

	private void initGlobalHotKey() {
		try {
			// initialize JIntellitype with the frame so all windows commands
			// can
			// be attached to this window
			JIntellitype.getInstance().addHotKeyListener(this);
			JIntellitype.getInstance().registerHotKey(90,
					JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, 'D');
		} catch (RuntimeException ex) {
			System.out
					.println("Either you are not on Windows, or there is a problem with the JIntellitype library!");
		}
	}

	public void onHotKey(int keyIdentifier) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.toFront();
				stage.show();
			}
		});
	}

	private void setTabChangeListener() {
		tabPane.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number> ov,
							Number oldValue, Number newValue) {
						TwoWayCommand.setIndexType(TwoWayCommand.SHOWN);
						taskPendingList.setItems(model.getPendingList());
						taskCompleteList.setItems(model.getCompleteList());
						taskTrashList.setItems(model.getTrashList());
					}
				});
	}

	public void setTab(int tabIndex) {
		tabPane.getSelectionModel().select(tabIndex);
	}
	
	public void emptyTextList() {
		for (int i = 0; i < textList.size(); i++)
			textList.get(i).setText("");
	}
	
	/**
	 * set the real-time multicolor feedback to remind some misuse or
	 * give some suggestion
	 * @param feedback
	 */
	void setFeedback(String feedback) {
		if (feedback.equals(Control.MESSAGE_INVALID_COMMAND_TYPE)
				|| feedback.equals(Control.MESSAGE_REQUEST_COMMAND)
				|| feedback.equals(Control.MESSAGE_INVALID_UNDO)
				|| feedback.equals(Control.MESSAGE_INVALID_REDO)
				|| feedback.equals(Control.MESSAGE_EXIT_TIP)
				|| feedback.equals(Control.MESSAGE_TODAY_TIP)
				|| feedback.equals(Control.MESSAGE_SHOW_ALL_TIP)
				|| feedback.equals(Control.MESSAGE_UNDO_TIP)
				|| feedback.equals(Control.MESSAGE_REDO_TIP)) {
			setFeedbackStyle(0, feedback, Color.WHITE);
			emptyFeedback(1);
		} else {
			switch (feedback) {
			case Control.MESSAGE_ADD_TIP:
				setFeedbackStyle(0, "add", IDO_GREEN);
				setFeedbackStyle(1, "<workflow>", Color.WHITE);
				setFeedbackStyle(2, "<start time>", Color.LIGHTBLUE);
				setFeedbackStyle(3, "<end time>", Color.DARKCYAN);
				setFeedbackStyle(4, "<importance *>", Color.RED);
				setFeedbackStyle(5, "<#tag>", Color.ORANGE);
				emptyFeedback(6);
				break;
			case Control.MESSAGE_EDIT_TIP:
				setFeedbackStyle(0, "edit", IDO_GREEN);
				setFeedbackStyle(1, "<index>", Color.ORCHID);
				setFeedbackStyle(2, "<workflow>", Color.WHITE);
				setFeedbackStyle(3, "<start time>", Color.LIGHTBLUE);
				setFeedbackStyle(4, "<end time>", Color.DARKCYAN);
				setFeedbackStyle(5, "<importance *>", Color.RED);
				setFeedbackStyle(6, "<#tag>", Color.ORANGE);
				emptyFeedback(7);
				break;
			case Control.MESSAGE_REMOVE_TIP:
				setFeedbackStyle(0, "remove", IDO_GREEN);
				setFeedbackStyle(1, "<index>", Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_SEARCH_TIP:
				setFeedbackStyle(0, "search", IDO_GREEN);
				setFeedbackStyle(1, "<workflow>", Color.WHITE);
				setFeedbackStyle(2, "<start time>", Color.LIGHTBLUE);
				setFeedbackStyle(3, "<end time>", Color.DARKCYAN);
				setFeedbackStyle(4, "<importance *>", Color.RED);
				setFeedbackStyle(5, "<#tag>", Color.ORANGE);
				emptyFeedback(6);
				break;
			case Control.MESSAGE_MARK_TIP:
				setFeedbackStyle(0, "<mark>", IDO_GREEN);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_UNMARK_TIP:
				setFeedbackStyle(0, "<unmark>", IDO_GREEN);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_COMPLETE_TIP:
				setFeedbackStyle(0, "<complete/done>", IDO_GREEN);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_INCOMPLETE_TIP:
				setFeedbackStyle(0, "<incomplete/undone>", IDO_GREEN);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			default:
				int listIndex = 1;
				boolean isCommandExisting = false;
				for (int i = 0; i < COMMAND_TYPES.length; i++) {
					String command = COMMAND_TYPES[i];
					if (command.indexOf(feedback) == 0
							&& !command.equals(feedback)) {
						setFeedbackStyle(listIndex, command, IDO_GREEN);
						listIndex++;
						isCommandExisting = true;
					}
				}
				emptyFeedback(listIndex);
				setFeedbackStyle(0, isCommandExisting ? "available commands: "
						: Control.MESSAGE_REQUEST_COMMAND, Color.WHITE);
				break;
			}
		}
	}

	void setFeedbackStyle(int index, String text, Color color) {
		feedbackList.get(index).setText(text);
		feedbackList.get(index).setFill(color);
	}

	public void emptyFeedback(int startIndex) {
		for (int i = startIndex; i < feedbackList.size(); i++)
			feedbackList.get(i).setText("");
	}
	
	public int getTabIndex(){
		return tabPane.getSelectionModel().getSelectedIndex();
	}
}
