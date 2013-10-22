import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
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

	public Help helpPage;
	public Settings settingsPage;
	public Login loginPage;

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
	public StackPane root2;
	public Stage stage;
	JScrollPane scrollPane;
	SwingNode textField;

	// Store the coordinates of the anchor of the window
	double dragAnchorX;
	double dragAnchorY;

	public JTextPane txt;
	public ArrayList<Text> feedbackList = new ArrayList<Text>();
	static String[] COMMAND_TYPES = { "add", "remove", "delete", "edit",
			"modify", "search", "find", "clear", "mark", "unmark", "complete",
			"incomplete", "all", "list", "today", "help", "del", "exit", "rm",
			"show", "ls", "clr", "done", "undone", "settings", "sync" };
	public HBox feedbacks;
	private boolean firstTimeLogin = true;
	private static Color IDO_GREEN = Color.rgb(130, 255, 121);
	private static final String WELCOME_MESSAGE = "Welcome back, %s";
	// private HBox multiColorCommand;

	// The 3 sections
	VBox bottom;
	HBox center;
	AnchorPane top;

	// Icon in the system tray
	TrayIcon trayIcon;

	Model model;
	SyncStore syncStore;

	/**
	 * This is the constructor for class View. It will create the content in the
	 * GUI and setup the scene for the stage in Control class.
	 * 
	 * @param model
	 *            model of lists of tasks
	 * @param primaryStage
	 *            main stage of the GUI
	 */
	public View(final Model model, final Stage primaryStage, SyncStore syncStore) {
		stage = primaryStage;
		this.model = model;
		this.syncStore = syncStore;

		showLoginPage();
		setupHelpPage();
		setupSettingsPage();
		setupStage();
		loadLibrary();
		checkIntellitype();
		initGlobalHotKey();

		hideInSystemTray();
		Platform.setImplicitExit(false);
		createContent();
		setupShortcuts();
		setDraggable();
		setupScene();
	}
	
	private void showLoginPage(){
		if (checkFirstTimeLogin()){
			loginPage = Login.getInstanceLogin(syncStore);
			loginPage.showLoginPage();
		} 
	}
	
	private boolean checkFirstTimeLogin() {
		return syncStore.retrieveAccount()[0] == null;
	}
	
	private void setupHelpPage() {
		helpPage = new Help();
	}

	public void showHelpPage() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				helpPage.showHelpPage();
			}
		});

	}

	private void setupSettingsPage() {
		settingsPage = new Settings();
	}

	public void showSettingsPage() {
		Platform.runLater(new Runnable() {

			@Override
			public void run() {
				settingsPage.showSettingsPage();
			}
		});

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
				} else if (e.getCode() == KeyCode.BACK_SPACE) {
					textField.setJDialogOnTop();
					txt.requestFocus();
					int pos = txt.getCaretPosition();
					if (pos > 0) {
						txt.setText(txt.getText().substring(0, pos - 1)
								+ txt.getText().substring(pos));
						txt.setCaretPosition(pos - 1);
					}
				} else if (!e.isAltDown() && !e.isControlDown()
						&& !e.isShiftDown() && !e.isShortcutDown()) {
					textField.setJDialogOnTop();
					txt.requestFocus();
					if (e.getCode() != KeyCode.ENTER) {
						int pos = txt.getCaretPosition();
						txt.setText(txt.getText().substring(0, pos)
								+ e.getText() + txt.getText().substring(pos));
						txt.setCaretPosition(pos + 1);
					}
				}
			}
		});

		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov,
					Boolean oldVal, Boolean newVal) {
				if (newVal.booleanValue() == true) {
					root.requestFocus();
					textField.setSwingComponentAlwaysOnTop(false);
				}
			}
		});

		/* Get KeyStroke for Ctrl+Tab key */
		InputMap map = txt.getInputMap();
		txt.setFocusTraversalKeysEnabled(false);
		KeyStroke changeTabKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_TAB,
				java.awt.event.InputEvent.CTRL_DOWN_MASK);
		Action changeTabAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {

					@Override
					public void run() {
						int index = tabPane.getSelectionModel()
								.getSelectedIndex();
						if (index != 2)
							tabPane.getSelectionModel().selectNext();
						else
							tabPane.getSelectionModel().selectFirst();
					}
				});
			}
		};

		map.put(changeTabKey, changeTabAction);

		txt.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent e) {
			}

			@Override
			public void keyReleased(java.awt.event.KeyEvent e) {
			}

			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				if ((e.getKeyCode() == java.awt.event.KeyEvent.VK_UP)
						&& e.isControlDown()) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {

							collapseAnimation();
						}
					});

				} else if ((e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN)
						&& e.isControlDown()) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {

							expandAnimation();
						}
					});

				} else if ((e.getKeyCode() == java.awt.event.KeyEvent.VK_H
						&& e.isControlDown() && e.isShiftDown())) {
					Platform.runLater(new Runnable() {

						@Override
						public void run() {
							hide();
						}
					});
				}

			}
		});
	}

	private void createContent() {
		createTopSection();
		createCenterSection();
		createBottomSection();
		txt = new JTextPane(new CustomStyledDocument());
		txt.setAutoscrolls(false);
		setFont(txt);
		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(txt);
		scrollPane = new JScrollPane(noWrapPanel);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

		textField = new SwingNode(stage, scrollPane);
		textField.setTranslateX(34);
		textField.setTranslateY(-93);

		root2 = new StackPane();
		root = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();
		root2.getChildren().addAll(textField, root);
	}

	private void setFont(JTextPane txt) {
		MutableAttributeSet attrs = txt.getInputAttributes();
		java.awt.Font customFont = new java.awt.Font("Calibri",
				java.awt.Font.PLAIN, 17);
		try {
			InputStream myFont = new BufferedInputStream(new FileInputStream(
					"resources/fonts/ubuntub.ttf"));
			customFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT,
					myFont);
			customFont = customFont.deriveFont(java.awt.Font.PLAIN, 16);
			GraphicsEnvironment ge = GraphicsEnvironment
					.getLocalGraphicsEnvironment();
			ge.registerFont(customFont);
		} catch (Exception e) {
			e.printStackTrace();
		}
		StyleConstants.setFontFamily(attrs, customFont.getFamily());
		StyleConstants.setFontSize(attrs, customFont.getSize());
		StyledDocument doc = txt.getStyledDocument();
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
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
		stage.setHeight(70.0);
		scene = new Scene(root2, Color.rgb(70, 70, 70));
		customizeGUIWithCSS();
		stage.setScene(scene);
		stage.show();
		removeTopAndCenter();
		showOrHide.setId("larger");
		txt.requestFocus();
		txt.setCaretPosition(txt.getText().length());
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
		upperPart = createUpperPartInBottom();

		feedbacks = new HBox();
		feedbacks.setSpacing(10);
		feedbackList.clear();
		for (int i = 0; i < 10; i++) {
			Text feedbackPiece = TextBuilder.create().styleClass("feedback")
					.fill(Color.WHITE).text("").build();
			feedbackList.add(feedbackPiece);
			feedbacks.getChildren().add(feedbackList.get(i));
		}
		String account = syncStore.retrieveAccount()[0];
		setFeedbackStyle(0, String.format(WELCOME_MESSAGE,account), Color.WHITE);
		bottom.getChildren().addAll(upperPart, feedbacks);
	}

	private HBox createUpperPartInBottom() {
		HBox temp = new HBox();
		temp.setSpacing(10);
		commandLine = new TextField();
		commandLine.setPrefWidth(630);
		commandLine.opacityProperty().set(0.0);

		showOrHide = new Button();
		showOrHide.setPrefSize(30, 30);
		showOrHide.setId("smaller");
		hookUpEventForShowOrHide();
		temp.getChildren().addAll(commandLine, showOrHide);

		return temp;
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
		TableColumn<Task, String> indexColumn = createIndexColumn();
		TableColumn<Task, String> occurrenceColumn = createOccurrenceColumn();
		TableColumn<Task, String> taskInfoColumn = createTaskInfoColumn();
		TableColumn<Task, Tag> tagColumn = createTagColumn();
		TableColumn<Task, String> startDateColumn = createStartDateColumn();
		TableColumn<Task, String> endDateColumn = createEndDateColumn();
		TableColumn<Task, Boolean> isImportantColumn = createIsImportantColumn();

		taskList.setStyle("-fx-table-cell-border-color: transparent;");

		ObservableList<TableColumn<Task, ?>> columns = taskList.getColumns();
		columns.add(indexColumn);
		columns.add(occurrenceColumn);
		columns.add(taskInfoColumn);
		columns.add(startDateColumn);
		columns.add(endDateColumn);
		columns.add(tagColumn);
		columns.add(isImportantColumn);
	}

	private TableColumn<Task, String> createIndexColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().visible(true).text("").prefWidth(20)
				.sortable(false).resizable(false).build();
		setupEndDateProperty(tempColumn);
		setupIndexUpdateFormat(tempColumn);
		return tempColumn;
	}

	private void setupIndexUpdateFormat(TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> param) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null) {
									setText(getTableRow().getIndex() + 1 + ".");
								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
	}
	
	private TableColumn<Task, String> createOccurrenceColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().visible(true).text("").prefWidth(20)
				.sortable(false).resizable(false).build();
		setupOccurrenceProperty(tempColumn);
		setupOccurrenceUpdateFormat(tempColumn);
		return tempColumn;
	}
	
	private void setupOccurrenceProperty(TableColumn<Task, String> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, String>(
				"occurrence"));
	}

	private void setupOccurrenceUpdateFormat(final TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> param) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							Text text;
							
							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null) {
									System.out.println("item "+item);
									text = new Text(item);
									//text.wrappingWidthProperty().bind(tempColumn.widthProperty());
									text.setFill(Color.DARKCYAN);
									text.setFont(Font.font("Verdana",10)); 
									setGraphic(text);
									
								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
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
										"important", "unimportant-odd" );
					
							}

							private void setStyle(boolean isImportant) {
								if (isImportant)
									getTableRow().getStyleClass().add(
											"important");
								else{
									if(getTableRow().getIndex() % 2 == 0){
									getTableRow().getStyleClass().add("unimportant");
									} else{
									getTableRow().getStyleClass().add("unimportant-odd");
									}
								}
							}
						};
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createEndDateColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().text("End").sortable(false)
				.resizable(false).prefWidth(110).build();

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
								if (item != null) {
									if (item.equals("OVERDUE")) {
										setId("overdue");
									} else {
										setId("empty");
									}

									setText(item);
								}
							}
						};
						tc.setAlignment(Pos.TOP_CENTER);
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createStartDateColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().text("Start").prefWidth(110)
				.resizable(false).sortable(false).build();

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
						tc.setAlignment(Pos.TOP_CENTER);
						return tc;
					}
				});
	}

	private TableColumn<Task, Tag> createTagColumn() {
		TableColumn<Task, Tag> tempColumn = TableColumnBuilder
				.<Task, Tag> create().text("Tag").sortable(false)
				.resizable(false).prefWidth(110).build();

		setupTagProperty(tempColumn);
		setupTagUpdateFormat(tempColumn);

		return tempColumn;
	}

	private void setupTagProperty(TableColumn<Task, Tag> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, Tag>(
				"tag"));
	}

	private void setupTagUpdateFormat(final TableColumn<Task, Tag> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, Tag>, TableCell<Task, Tag>>() {

					@Override
					public TableCell<Task, Tag> call(
							TableColumn<Task, Tag> param) {
						TableCell<Task, Tag> tc = new TableCell<Task, Tag>() {
							Text text;
							public void updateItem(Tag item, boolean empty) {
								if (item != null) {
									if (item.getRepetition()
											.equals(Parser.NULL)){
										if(item.getTag().equals("-"))
											text = new Text("             -");
										else{
											text = new Text(appendTab(item.getTag()));
										}
									}else
										text = new Text((item.getTag().equals("-") ? ""
												: appendTab(item.getTag()) + "\n")
												+ appendTab("#"
												+ item.getRepetition()));
									text.getStyleClass().add("text");
									text.wrappingWidthProperty().bind(tempColumn.widthProperty());
									setGraphic(text);
								}
							}
							
							private String appendTab(String info){
								if(info.length() < 8)
									return "\t" + info;
								else
									return info;
							}
						};
						tc.setAlignment(Pos.TOP_CENTER);
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

	private void setupTaskInfoUpdateFormat(
			final TableColumn<Task, String> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> arg0) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							Text text;

							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null) {
									text = new Text(item);
									text.getStyleClass().add("text");
									text.wrappingWidthProperty().bind(tempColumn.widthProperty());
									setGraphic(text);
								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
	}

	private void hookUpEventForShowOrHide() {
		showOrHide.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (stage.getHeight() == 540) {
					collapseAnimation();
				} else if (stage.getHeight() == 70) {
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
		stage.setMinHeight(70.0);
		Timer animTimer = new Timer();
		animTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > 70.0) {
					double i = stage.getHeight() - 10.0;
					stage.setMaxHeight(i);
					stage.setHeight(i);
				} else {
					this.cancel();
				}
			}
		}, 0, 5);
	}

	private void expandAnimation() {
		Timer animTimer = new Timer();
		root.setTop(top);
		showOrHide.setId("smaller");
		stage.setMaxHeight(540.0);
		animTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > 530) {
					setCenterWithFadeTransition();
				}

				if (stage.getHeight() < 540.0) {
					double i = stage.getHeight() + 10.0;
					stage.setMinHeight(i);
					stage.setHeight(i);
				} else {
					this.cancel();
				}
			}
		}, 0, 5);
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
		settingsItem.addActionListener(createPreferencesListener());
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

	private ActionListener createPreferencesListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						showSettingsPage();
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

	/**
	 * set the real-time multicolor feedback to remind some misuse or give some
	 * suggestion
	 * 
	 * @param feedback
	 */
	void setFeedback(String feedback) {
		emptyFeedback(0);
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
				emptyFeedback(0);
				ArrayList<String> availCommands = getAvailCommandNum(feedback.trim());
				for (int i = 0; i < availCommands.size(); i++) {
					setFeedbackStyle(i + 1, availCommands.get(i), IDO_GREEN);
				}
				setFeedbackStyle(0,
						availCommands.size() > 0 ? "Available commands: "
								: Control.MESSAGE_REQUEST_COMMAND, Color.WHITE);
				break;
			}
		}
	}

	ArrayList<String> getAvailCommandNum(String feedback) {
		ArrayList<String> availCommands = new ArrayList<String>();
		for (int i = 0; i < COMMAND_TYPES.length; i++) {
			String command = COMMAND_TYPES[i];
			if (command.indexOf(feedback) == 0 && !command.equals(feedback))
				availCommands.add(command);
		}
		return availCommands;
	}

	void setFeedbackStyle(int index, String text, Color color) {
		feedbackList.get(index).setText(text);
		feedbackList.get(index).setFill(color);
	}

	public void emptyFeedback(int startIndex) {
		for (int i = startIndex; i < feedbackList.size(); i++)
			feedbackList.get(i).setText("");
	}

	public int getTabIndex() {
		return tabPane.getSelectionModel().getSelectedIndex();
	}
}

class CustomStyledDocument extends DefaultStyledDocument {
	final StyleContext cont = StyleContext.getDefaultStyleContext();

	final AttributeSet attrRed = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(231, 76, 60));
	final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(92, 190, 247));
	final AttributeSet attrDarkCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(44, 62, 80));
	final AttributeSet attrDarkBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(41, 128, 185));
	final AttributeSet attrOrange = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(241, 196, 15));
	final AttributeSet attrGreen = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(46, 204, 113));
	final AttributeSet attrCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(26, 188, 156));
	final AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(189, 195, 199));
	final AttributeSet attrMagenta = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(155, 89, 182));

	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offset, str, a);

		String text = getText(0, getLength());
		ArrayList<InfoWithIndex> infoList = Parser.parseForView(text);
		for (int i = 0; i < infoList.size(); i++) {
			InfoWithIndex info = infoList.get(i);
			switch (info.getInfoType()) {
			case -2:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrGray, false);
				break;
			case -1:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrGreen, false);
				break;
			case 0:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrDarkCyan, false);
				break;
			case 1:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrOrange, false);
				break;
			case 2:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrCyan, false);
				break;
			case 3:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrBlue, false);
				break;
			case 4:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrRed, false);
				break;
			case 5:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrDarkBlue, false);
				break;
			case 6:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrMagenta, false);
				break;
			}

		}
	}

	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);

		String text = getText(0, getLength());
		ArrayList<InfoWithIndex> infoList = Parser.parseForView(text);
		for (int i = 0; i < infoList.size(); i++) {
			InfoWithIndex info = infoList.get(i);
			switch (info.getInfoType()) {
			case -2:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrGray, false);
				break;
			case -1:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrGreen, false);
				break;
			case 0:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrDarkCyan, false);
				break;
			case 1:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrOrange, false);
				break;
			case 2:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrCyan, false);
				break;
			case 3:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrBlue, false);
				break;
			case 4:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrRed, false);
				break;
			case 5:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrDarkBlue, false);
				break;
			case 6:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), attrMagenta, false);
				break;
			}

		}
	}
};