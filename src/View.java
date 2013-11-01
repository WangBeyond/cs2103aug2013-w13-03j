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
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.ScrollBar;
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
	private ImageView title;
	private ImageView syncBar;
	public Scene scene;
	public BorderPane root;
	public StackPane root2;
	public Stage stage;
	JScrollPane scrollPane;
	SwingNode textField;
	private ChangeListener<Boolean> caretListener;
	// Store the coordinates of the anchor of the window
	double dragAnchorX;
	double dragAnchorY;
	private Color defaultColor;
	private Color commandColor;
	public JTextPane txt;
	public ArrayList<Text> feedbackList = new ArrayList<Text>();
	static String[] COMMAND_TYPES = { "add", "remove", "delete", "edit",
			"modify", "search", "find", "clear", "mark", "unmark", "complete",
			"incomplete", "all", "list", "today", "help", "del", "exit", "rm",
			"show", "ls", "clr", "done", "undone", "settings", "sync" };
	public HBox feedbacks;
	private static Color IDO_GREEN = Color.rgb(130, 255, 121);
	private static final String WELCOME_MESSAGE = "Welcome back, %s";
	// private HBox multiColorCommand;
	ScrollBar pendingBar;
	ScrollBar completeBar;
	ScrollBar trashBar;
	// The 3 sections
	VBox bottom;
	HBox center;
	AnchorPane top;

	// Icon in the system tray
	TrayIcon trayIcon;

	Model model;
	Setting settingStore;

	/**
	 * This is the constructor for class View. It will create the content in the
	 * GUI and setup the scene for the stage in Control class.
	 * 
	 * @param model
	 *            model of lists of tasks
	 * @param primaryStage
	 *            main stage of the GUI
	 */
	public View(final Model model, final Stage primaryStage, Setting settingStore) {
		stage = primaryStage;
		this.model = model;
		this.settingStore = settingStore;

		setupHelpPage();
		setupSettingsPage();
		setupStage();
		loadLibrary();
		checkIntellitype();
		initGlobalHotKey();

		hideInSystemTray();
		Platform.setImplicitExit(false);
		createContent();
		setDraggable();
		showLoginPage();
		setupScene();
		setupShortcuts();
		showInitialMessage();
	}
	
	private void showInitialMessage(){
		if (model.getUsername() != null)
			setFeedbackStyle(
					0,
					String.format(WELCOME_MESSAGE,
							model.getUsername().replace("@gmail.com", "")),
					defaultColor);
		else
			setFeedbackStyle(0, "Welcome to iDo!", defaultColor);
	}

	private void showLoginPage() {
		if (checkFirstTimeLogin()) {
			loginPage = Login.getInstanceLogin(model);
			loginPage.showLoginPage();
		}
	}

	private boolean checkFirstTimeLogin() {
		return model.getUsername() == null;
	}

	private void setupHelpPage() {
		helpPage = Help.getInstanceHelp();
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
		settingsPage = Settings.getInstanceSettings(model,settingStore);
	}

	public void showSettingsPage() {
		settingsPage.showSettingsPage();
	}

	private void setupScrollBar(){
		for (Node n: taskPendingList.lookupAll(".scroll-bar")) {
			  if (n instanceof ScrollBar) {
			    pendingBar = (ScrollBar) n;
			    if(pendingBar.getOrientation() == Orientation.VERTICAL)
			    	break;
			 }
		}
		
		for (Node n: taskCompleteList.lookupAll(".scroll-bar")) {
			  if (n instanceof ScrollBar) {
			    completeBar = (ScrollBar) n;
			    if(completeBar.getOrientation() == Orientation.VERTICAL)
			    	break;
			 }
		}
		
		for (Node n: taskTrashList.lookupAll(".scroll-bar")) {
			  if (n instanceof ScrollBar) {
			    trashBar = (ScrollBar) n;
			    if(trashBar.getOrientation() == Orientation.VERTICAL)
			    	break;
			 }
		}
		
		InputMap map = txt.getInputMap();
		KeyStroke scrollUpKey = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_UP, 0);
		Action scrollUpAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if(getTabIndex() == 0 && pendingBar.getValue() > pendingBar.getMin())
							pendingBar.setValue(pendingBar.getValue() - 0.05);
						else if(getTabIndex() == 1 && completeBar.getValue() > completeBar.getMin())
							completeBar.setValue(completeBar.getValue() - 0.05);
						else if(getTabIndex() == 2 && trashBar.getValue() > trashBar.getMin())
							trashBar.setValue(trashBar.getValue() - 0.05);
					}
				});
			}
		};
		map.put(scrollUpKey, scrollUpAction);
		
		KeyStroke scrollDownKey = KeyStroke.getKeyStroke(com.sun.glass.events.KeyEvent.VK_DOWN, 0);
		Action scrollDownAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						if(getTabIndex() == 0 && pendingBar.getValue() < pendingBar.getMax())
							pendingBar.setValue(pendingBar.getValue() + 0.05);
						else if(getTabIndex() == 1 && completeBar.getValue() < completeBar.getMax())
							completeBar.setValue(completeBar.getValue() + 0.05);
						else if(getTabIndex() == 2 && trashBar.getValue() < trashBar.getMax())
							trashBar.setValue(trashBar.getValue() + 0.05);
					}
				});
			}
		};
		map.put(scrollDownKey, scrollDownAction);
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
					textField.temp = 1;
					textField.setJDialogOnTop();
					txt.requestFocus();
					int pos = txt.getCaretPosition();
					if (pos > 0) {
						txt.setText(txt.getText().substring(0, pos - 1)
								+ txt.getText().substring(pos));
						txt.setCaretPosition(pos - 1);
					}
				} else if(e.getCode() == KeyCode.UP){
					if(getTabIndex() == 0 && pendingBar.getValue() > pendingBar.getMin())
						pendingBar.setValue(pendingBar.getValue() - 0.05);
					else if(getTabIndex() == 1 && completeBar.getValue() > completeBar.getMin())
						completeBar.setValue(completeBar.getValue() - 0.05);
					else if(getTabIndex() == 2 && trashBar.getValue() > trashBar.getMin())
						trashBar.setValue(trashBar.getValue() - 0.05);
				} else if(e.getCode() == KeyCode.DOWN){
					if(getTabIndex() == 0 && pendingBar.getValue() < pendingBar.getMax())
						pendingBar.setValue(pendingBar.getValue() + 0.05);
					else if(getTabIndex() == 1 && completeBar.getValue() < completeBar.getMax())
						completeBar.setValue(completeBar.getValue() + 0.05);
					else if(getTabIndex() == 2 && trashBar.getValue() < trashBar.getMax())
						trashBar.setValue(trashBar.getValue() + 0.05);
				}else if (e.getCode() == KeyCode.RIGHT) {
					textField.temp = 1;
					textField.setJDialogOnTop();
					txt.requestFocus();
					int pos = txt.getCaretPosition();
					if (pos != txt.getText().length())
						txt.setCaretPosition(pos + 1);
				} else if (e.getCode() == KeyCode.LEFT) {
					textField.temp = 1;
					textField.setJDialogOnTop();
					txt.requestFocus();
					int pos = txt.getCaretPosition();
					if (pos != 0)
						txt.setCaretPosition(pos - 1);
				} else {
					textField.temp = 1;
					textField.setJDialogOnTop();
					txt.requestFocus();
					if (e.getCode() != KeyCode.ENTER) {

						int pos = txt.getCaretPosition();
						txt.setText(txt.getText().substring(0, pos)
								+ e.getText() + txt.getText().substring(pos));
						if (pos != txt.getText().length())
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
		txt = new JTextPane();
		txt.setAutoscrolls(false);

		setFont(txt);
		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(txt);
		scrollPane = new JScrollPane(noWrapPanel);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);

		textField = new SwingNode(stage, scrollPane);
		textField.setTranslateX(36);
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
		scene = new Scene(root2);
		customizeGUI();
		stage.setScene(scene);
		stage.show();
		removeTopAndCenter();
		showOrHide.setId("larger");
		txt.requestFocus();
		txt.setCaretPosition(txt.getText().length());
		setupScrollBar();
	}
	
	public void customizeGUI() {
		if (caretListener != null)
			stage.focusedProperty().removeListener(caretListener);
	
		scene.getStylesheets().clear();
		if (model.getThemeMode().equals(Model.DAY_MODE)) {
			scene.getStylesheets().addAll(
					getClass().getResource("customize.css").toExternalForm());
			txt.setBackground(java.awt.Color.white);
			caretListener = new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
						Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue() == false)
						txt.setCaretColor( java.awt.Color.black);
					else
						txt.setCaretColor(new java.awt.Color(0,0,0,0));
				}
			};
			title.setImage(new Image(getClass().getResourceAsStream("ido_new.png")));
			txt.setStyledDocument(new CustomStyledDocumentForDayMode());
			defaultColor = Color.WHITE;
			commandColor = IDO_GREEN;
			
			stage.focusedProperty().addListener(caretListener);
		} else {
			scene.getStylesheets().addAll(
					getClass().getResource("customize2.css").toExternalForm());
			txt.setBackground(new java.awt.Color(50, 50, 50));
			caretListener = new ChangeListener<Boolean>() {
				public void changed(ObservableValue<? extends Boolean> ov,
						Boolean oldValue, Boolean newValue) {
					if (newValue.booleanValue() == false)
						txt.setCaretColor(java.awt.Color.white);
					else
						txt.setCaretColor(new java.awt.Color(0,0,0,0));
				}
			};
			txt.setStyledDocument(new CustomStyledDocumentForNightMode());
			title.setImage(new Image(getClass().getResourceAsStream("ido_new_night.png")));
			defaultColor = Color.rgb(250, 250, 250);
			commandColor = Color.rgb(89, 213, 100);
			stage.focusedProperty().addListener(caretListener);
		}
	}
	
	public Color getDefaultColor(){
		return defaultColor;
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
					.fill(defaultColor).text("").build();
			feedbackList.add(feedbackPiece);
			feedbacks.getChildren().add(feedbackList.get(i));
		}
		
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

		title = createTitle();
		syncBar = createSyncBar();
		syncBar.setImage(new Image(getClass().getResourceAsStream("sync.gif")));
		syncBar.setFitWidth(600);
		syncBar.setFitHeight(15);
		syncBar.setVisible(false);
		HBox buttons = createModifyingButtons();

		setupLayout(title, syncBar, buttons);

	}

	public void setSyncBarVisible(boolean isVisible) {
		syncBar.setVisible(isVisible);
	}
	
	private void setDraggable() {
		// Get the position of the mouse in the stage
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				if (me.getScreenX() < stage.getX())
					System.out.println("bleble");
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
	
	private void setupLayout(ImageView title, ImageView syncBar, HBox buttons) {
		top.getChildren().addAll(title, syncBar, buttons);
		AnchorPane.setLeftAnchor(title, 10.0);
		AnchorPane.setLeftAnchor(syncBar, 10.0);
		AnchorPane.setTopAnchor(buttons, 25.0);
		AnchorPane.setTopAnchor(syncBar, 80.0);
		AnchorPane.setTopAnchor(title, 30.0);
		AnchorPane.setRightAnchor(buttons, 5.0);
	}

	private ImageView createTitle() {
		ImageView title = new ImageView();
		title.setFitWidth(110);
		title.setPreserveRatio(true);
		title.setSmooth(true);
		title.setCache(true);
		return title;
	}
	
	private ImageView createSyncBar() {
		ImageView syncBar = new ImageView();
		syncBar.setFitWidth(80);
		syncBar.setPreserveRatio(true);
		syncBar.setSmooth(true);
		syncBar.setCache(true);
		return syncBar;
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
		taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		final Text emptyTableSign = new Text(
				"There is currently no task in this tab");
		emptyTableSign.setFont(new Font(15));
		emptyTableSign.getStyleClass().add("text");
		taskList.setPlaceholder(emptyTableSign);
		final TableColumn<Task, String> indexColumn = createIndexColumn();
		final TableColumn<Task, String> occurrenceColumn = createOccurrenceColumn();
		final TableColumn<Task, String> taskInfoColumn = createTaskInfoColumn();
		final TableColumn<Task, Tag> tagColumn = createTagColumn();
		final TableColumn<Task, String> startDateColumn = createStartDateColumn();
		final TableColumn<Task, String> endDateColumn = createEndDateColumn();
		final TableColumn<Task, RowStatus> rowStatusColumn = createRowStatusColumn();

		final ObservableList<TableColumn<Task, ?>> columns = taskList.getColumns();
		columns.add(indexColumn);
		columns.add(occurrenceColumn);
		columns.add(taskInfoColumn);
		columns.add(startDateColumn);
		columns.add(endDateColumn);
		columns.add(tagColumn);
		columns.add(rowStatusColumn);
		columns.addListener(new ListChangeListener<TableColumn<Task, ?>>() {
	        @Override
	        public void onChanged(Change<? extends TableColumn<Task, ?> > change) {
	          change.next();
	          if(change.wasReplaced()) {
	              columns.clear();
	            columns.add(indexColumn);
	      		columns.add(occurrenceColumn);
	      		columns.add(taskInfoColumn);
	      		columns.add(startDateColumn);
	      		columns.add(endDateColumn);
	      		columns.add(tagColumn);
	      		columns.add(rowStatusColumn);
	          }
	        }
	    });
	}

	private TableColumn<Task, RowStatus> createRowStatusColumn() {
		TableColumn<Task, RowStatus> tempColumn = TableColumnBuilder
				.<Task, RowStatus> create().resizable(false).visible(true).text("").prefWidth(1)
				.build();
		setupRowStatusProperty(tempColumn);
		setupRowStatusUpdateFormat(tempColumn);
		return tempColumn;
	}

	private void setupRowStatusProperty(TableColumn<Task, RowStatus> tempColumn) {
		tempColumn
				.setCellValueFactory(new PropertyValueFactory<Task, RowStatus>(
						"rowStatus"));
	}

	private void setupRowStatusUpdateFormat(
			TableColumn<Task, RowStatus> tempColumn) {
		tempColumn
				.setCellFactory(new Callback<TableColumn<Task, RowStatus>, TableCell<Task, RowStatus>>() {

					@Override
					public TableCell<Task, RowStatus> call(
							TableColumn<Task, RowStatus> param) {
						TableCell<Task, RowStatus> tc = new TableCell<Task, RowStatus>() {
							@Override
							public void updateItem(RowStatus item, boolean empty) {
								super.updateItem(item, empty);
								if (item != null) {
									clearStyle();
									setStyle(item);
								}
							}

							private void clearStyle() {
								getTableRow().getStyleClass().removeAll(
										"table-row-cell", "unimportant",
										"important", "unimportant-odd",
										"unimportant-last", "important-last",
										"unimportant-odd-last");

							}

							private void setStyle(RowStatus rowStatus) {
								Task t = (Task) getTableRow().getItem();
								boolean isLastOverdue = rowStatus
										.getIsLastOverdue();
								boolean isOdd = getTableRow().getIndex() % 2 != 0;
								boolean isImportant = rowStatus
										.getIsImportant();
								if (isLastOverdue) {
									if (isImportant) {
										getTableRow().getStyleClass().add(
												"important-last");
									} else {
										if (isOdd) {
											getTableRow().getStyleClass().add(
													"unimportant-odd-last");
										} else {
											getTableRow().getStyleClass().add(
													"unimportant-last");
										}
									}
								} else {
									if (isImportant) {
										getTableRow().getStyleClass().add(
												"important");
									} else {
										if (isOdd) {
											getTableRow().getStyleClass().add(
													"unimportant-odd");
										} else {
											getTableRow().getStyleClass().add(
													"unimportant");
										}
									}
								}
							}
						};
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createIndexColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).visible(true).text("").prefWidth(28)
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
				.<Task, String> create().resizable(false).visible(true).text("").prefWidth(28)
				.sortable(false).resizable(false).build();
		setupOccurrenceProperty(tempColumn);
		setupOccurrenceUpdateFormat(tempColumn);
		return tempColumn;
	}

	private void setupOccurrenceProperty(TableColumn<Task, String> tempColumn) {
		tempColumn.setCellValueFactory(new PropertyValueFactory<Task, String>(
				"occurrence"));
	}

	private void setupOccurrenceUpdateFormat(
			final TableColumn<Task, String> tempColumn) {
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
									text = new Text(item);
									// text.wrappingWidthProperty().bind(tempColumn.widthProperty());
									text.setFill(Color.DARKCYAN);
									text.setFont(Font.font("Verdana", 10));
									setGraphic(text);

								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
	}

	private TableColumn<Task, String> createEndDateColumn() {
		TableColumn<Task, String> tempColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).text("End").sortable(false)
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
				.<Task, String> create().resizable(false).text("Start").prefWidth(110)
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
				.<Task, Tag> create().resizable(false).text("Tag").sortable(false)
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
											.equals(Parser.NULL)) {
										if (item.getTag().equals("-"))
											text = new Text("             -");
										else {
											text = new Text(appendTab(item
													.getTag()));
										}
									} else
										text = new Text((item.getTag().equals(
												"-") ? "" : appendTab(item
												.getTag()) + "\n")
												+ appendTab("#"
														+ item.getRepetition()));
									text.getStyleClass().add("text");
									text.wrappingWidthProperty().bind(
											tempColumn.widthProperty());
									setGraphic(text);
								}
							}

							private String appendTab(String info) {
								if (info.length() < 8)
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
				.<Task, String> create().resizable(false).text("Task").sortable(false)
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
									text.wrappingWidthProperty().bind(
											tempColumn.widthProperty());
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
			setFeedbackStyle(0, feedback, defaultColor);
			emptyFeedback(1);
		} else {
			switch (feedback) {
			case Control.MESSAGE_ADD_TIP:
				setFeedbackStyle(0, "add", commandColor);
				setFeedbackStyle(1, "<workflow>", defaultColor);
				setFeedbackStyle(2, "<start time>", Color.rgb(18, 235, 166));
				setFeedbackStyle(3, "<end time>", Color.rgb(92, 190, 247));
				setFeedbackStyle(4, "<importance *>", Color.RED);
				setFeedbackStyle(5, "<#tag>", Color.ORANGE);
				emptyFeedback(6);
				break;
			case Control.MESSAGE_EDIT_TIP:
				setFeedbackStyle(0, "edit", commandColor);
				setFeedbackStyle(1, "<index>", Color.ORCHID);
				setFeedbackStyle(2, "<workflow>", defaultColor );
				setFeedbackStyle(3, "<start time>", Color.rgb(18, 235, 166));
				setFeedbackStyle(4, "<end time>", Color.rgb(92, 190, 247));
				setFeedbackStyle(5, "<importance *>", Color.RED);
				setFeedbackStyle(6, "<#tag>", Color.ORANGE);
				emptyFeedback(7);
				break;
			case Control.MESSAGE_REMOVE_INDEX_TIP:
				setFeedbackStyle(0, "remove", commandColor);
				setFeedbackStyle(1, "<index>", Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_REMOVE_INFO_TIP:
				setFeedbackStyle(0, "remove", commandColor);
				setFeedbackStyle(1, "<workflow>", defaultColor);
				setFeedbackStyle(2, "<start time>", Color.rgb(18, 235, 166));
				setFeedbackStyle(3, "<end time>", Color.rgb(92, 190, 247));
				setFeedbackStyle(4, "<importance *>", Color.RED);
				setFeedbackStyle(5, "<#tag>", Color.ORANGE);
				emptyFeedback(6);
				break;
			case Control.MESSAGE_SEARCH_TIP:
				setFeedbackStyle(0, "search", commandColor);
				setFeedbackStyle(1, "<workflow>", defaultColor);
				setFeedbackStyle(2, "<start time>", Color.rgb(18, 235, 166));
				setFeedbackStyle(3, "<end time>", Color.rgb(92, 190, 247));
				setFeedbackStyle(4, "<importance *>", Color.RED);
				setFeedbackStyle(5, "<#tag>", Color.ORANGE);
				emptyFeedback(6);
				break;
			case Control.MESSAGE_MARK_TIP:
				setFeedbackStyle(0, "<mark>", commandColor);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_UNMARK_TIP:
				setFeedbackStyle(0, "<unmark>", commandColor);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_COMPLETE_TIP:
				setFeedbackStyle(0, "<complete/done>", commandColor);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			case Control.MESSAGE_INCOMPLETE_TIP:
				setFeedbackStyle(0, "<incomplete/undone>", commandColor);
				setFeedbackStyle(1, "<index1> <index2> <index3> ...",
						Color.ORCHID);
				emptyFeedback(2);
				break;
			default:
				emptyFeedback(0);
				ArrayList<String> availCommands = getAvailCommandNum(feedback
						.trim());
				for (int i = 0; i < availCommands.size(); i++) {
					setFeedbackStyle(i + 1, availCommands.get(i), commandColor);
				}
				setFeedbackStyle(0,
						availCommands.size() > 0 ? "Available commands: "
								: Control.MESSAGE_REQUEST_COMMAND, defaultColor);
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

class CustomStyledDocumentForDayMode extends DefaultStyledDocument {
	final StyleContext cont = StyleContext.getDefaultStyleContext();

	final AttributeSet attrRed = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 41, 41));
	final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(84, 173, 225));
	final AttributeSet attrDarkCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(44, 62, 80));
	final AttributeSet attrDarkBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(5, 82, 199));
	final AttributeSet attrOrange = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(255, 165, 0));
	final AttributeSet attrGreen = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(39, 174, 96));
	final AttributeSet attrCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(16, 217, 153));
	final AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(189, 195, 199));
	final AttributeSet attrMagenta = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(155, 89, 182));

	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offset, str, a);

		setColor();
	}
	
	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);
		setColor();
	}
	
	private void setColor() throws BadLocationException{
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

class CustomStyledDocumentForNightMode extends DefaultStyledDocument {
	final StyleContext cont = StyleContext.getDefaultStyleContext();

	final AttributeSet attrRed = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(247, 139, 139));
	final AttributeSet attrBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(110, 242, 243));
	final AttributeSet attrWhite = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(252, 252, 252));
	final AttributeSet attrDarkBlue = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(66, 185, 254));
	final AttributeSet attrOrange = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(254, 186, 63));
	final AttributeSet attrGreen = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(108, 248, 134));
	final AttributeSet attrCyan = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(63, 248, 189));
	final AttributeSet attrGray = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(220, 220, 220));
	final AttributeSet attrMagenta = cont.addAttribute(cont.getEmptySet(),
			StyleConstants.Foreground, new java.awt.Color(238, 152, 233));

	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offset, str, a);

		setColor();
	}
	
	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);
		setColor();
	}
	
	private void setColor() throws BadLocationException{
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
						.length(), attrWhite, false);
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