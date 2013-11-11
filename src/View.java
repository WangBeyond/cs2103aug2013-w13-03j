import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import javax.swing.text.StyledDocument;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
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
import javafx.scene.control.ProgressIndicator;
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


/**
 * 
 * This class provides the main graphic user interface for the application
 * 
 */
public class View implements HotkeyListener {
	private static final double MIN_HEIGHT = 70.0;
	private static final double MAX_HEIGHT = 540.0;
	private static final Logger logger = Logger.getLogger("View");
	private static final String GMAIL_ACCOUNT_SUFFIX = "@gmail.com";
	private static final String UNLOGIN_WELCOME_MESSAGE = "Welcome to iDo!";
	private static final String BRIGHT_COLOUR_THEME = "Bright";
	private static final String GOLDFISH_COLOUR_THEME = "Goldfish";
	
	// The list of colours in a specific colour scheme for feedback and command
	// line
	private static Color[] colourScheme;
	protected static AttributeSet[] colourSchemeCommandLine;
	// Sub windows apart from the main window
	private Help helpPage;
	private Settings settingsPage;
	private Login loginPage;

	// Button to expand or collapse window
	private Button expandOrCollapse;
	// Tab Pane to contain 3 tables
	private TabPane tabPane;
	// Menu shown when clicking on icon in tray
	private PopupMenu popupMenu;
	// Table View in 3 tabs
	private TableView<Task> taskPendingList;
	private TableView<Task> taskCompleteList;
	private TableView<Task> taskTrashList;
	// The image for the title of the application
	private ImageView title;

	// The actual window
	private Stage stage;
	// Scene controlling how the user see in the window
	private Scene scene;
	// The main groups that scene will show to user
	private BorderPane subRoot;
	private StackPane mainRoot;

	// The command line
	private JTextPane commandLine; // this node is in Swing
	private SwingNode textField; // we will use this custom class to display a
									// Swing node in JavaFX environment
	private ChangeListener<Boolean> caretColourListenerForCommandLine;

	// The current position of the mouse relatively to the stage
	private double dragAnchorX;
	private double dragAnchorY;

	// Sync progress indicator
	private ProgressIndicator syncProgress;
	// No internet access indicator
	private ImageView netAccessIndicator;
	// The default color for display in the application
	private Color defaultColor;

	// Feedback text in the application
	private ArrayList<Text> feedbackList = new ArrayList<Text>();

	// Vertical scroll bar for 3 tables in each corresponding tab
	private ScrollBar pendingBar;
	private ScrollBar completeBar;
	private ScrollBar trashBar;

	// The 3 separate sections in the main window
	private AnchorPane top;
	private HBox center;
	private VBox bottom;

	// Icon in the system tray
	private TrayIcon trayIcon;

	// The model of settings and task info
	private Model model;
	
	//@author A0098077N
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
		initializeKeyVariables(model, primaryStage);
		setupGlobalHotkey();
		setupMainGUI();
		showLoginPage();
		setupScene();
		setupPopupWindows();
		showInitialMessage();
	}

	public Stage getStage(){
		return stage;
	}

	public StackPane getMainRoot(){
		return mainRoot;
	}

	// Get the reference to the command line of the application
	public JTextPane getCommandLine() {
		return commandLine;
	}

	// Get the reference to tray icon of the application
	public TrayIcon getTrayIcon() {
		return trayIcon;
	}

	public TableView<Task> getPendingTable(){
		return taskPendingList;
	}

	public TableView<Task> getCompleteTable(){
		return taskCompleteList;
	}

	public TableView<Task> getTrashTable(){
		return taskTrashList;
	}

	/**
	 * Initialize the key variables in the View class
	 * 
	 * @param model
	 *            the infos of task and setting that will be shown to the user
	 * @param primaryStage
	 *            the main window of the application
	 */
	private void initializeKeyVariables(Model model, Stage primaryStage) {
		stage = primaryStage;
		this.model = model;
	}

	/**
	 * Setup the main window of the application
	 */
	private void setupStage() {
		stage.setWidth(760);
		stage.setHeight(540);
		setInitialPosition();
		stage.initStyle(StageStyle.UNDECORATED);
		stage.setTitle("iDo");
		setIcon();
	}

	/**
	 * Setup the scene to be shown in the main stage i.e main window
	 */
	private void setupScene() {
		stage.setHeight(70.0);
		scene = new Scene(mainRoot);
		customizeGUI();
		stage.setScene(scene);
		stage.show();
		setInitialState();
		setupScrollBar();
	}

	/**
	 * This function is the main function to setup all the important nodes in
	 * the interface
	 */
	private void setupMainGUI() {
		setupStage();
		setupSystemTray();
		Platform.setImplicitExit(false);
		createContent();
		setupDraggable();
		setupShortcuts();
	}
	
	//@author A0105667B
	/**
	 * This function is used to show the initial message when the user first
	 * open the application. The message will be different according to whether
	 * the user has indicated Google account yet.
	 */
	private void showInitialMessage() {
		if (model.getUsername() != null && !model.getUsername().equals("")) {
			setFeedbackStyle(0, String.format(Common.WELCOME_MESSAGE, model
					.getUsername().replace(GMAIL_ACCOUNT_SUFFIX, "")),
					defaultColor);
		} else {
			setFeedbackStyle(0, UNLOGIN_WELCOME_MESSAGE, defaultColor);
		}
	}
	
	//@author A0100927M
	/**
	 * This function will show the login page for the user to input his Google
	 * account when he first uses the application. The user can choose to input
	 * his Google account or not.
	 */
	private void showLoginPage() {
		if (checkFirstTimeLogin()) {
			loginPage = Login.getInstanceLogin(model);
			loginPage.showLoginPage();
		}
	}

	// Check whether this is the first time user uses the application
	private boolean checkFirstTimeLogin() {
		return model.getUsername() == null;
	}

	/**
	 * This function is used to to setup the popup windows appearing when user
	 * type some specific commands. These popup windows comprise help window and
	 * setting window.
	 */
	private void setupPopupWindows() {
		setupHelpPage();
		setupSettingsPage();
	}

	// Setup the content of help window
	private void setupHelpPage() {
		helpPage = Help.getInstanceHelp(model);
	}

	// Process opening the help window
	public void showHelpPage() {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				helpPage.showHelpPage();
			}
		});

	}

	// Setup the content of settings window
	private void setupSettingsPage() {
		settingsPage = Settings.getInstanceSettings(model);
	}

	// Process opening the settings window
	public void showSettingsPage(String checkUsernamePassword) {
		settingsPage.showSettingsPage(checkUsernamePassword);
	}
	
	//@author A0098077N
	/**
	 * Look up for the reference to the vertical scroll bar from the given
	 * TableView object
	 */
	private ScrollBar lookUpVerticalScrollBar(TableView<Task> list) {
		for (Node n : list.lookupAll(".scroll-bar")) {
			if (n instanceof ScrollBar) {
				ScrollBar temp = (ScrollBar) n;
				if (temp.getOrientation() == Orientation.VERTICAL){
					return temp;
				}
			}
		}
		return null;
	}

	// Get the corresponding scroll bar from the given tab index
	private ScrollBar getScrollBar(int tab) {
		assert tab >= 0 && tab <= 2;
		if (tab == Common.PENDING_TAB) {
			return pendingBar;
		} else if (tab == Common.COMPLETE_TAB) {
			return completeBar;
		} else if (tab == Common.TRASH_TAB) {
			return trashBar;
		}
		return null;
	}

	/**
	 * This function is used to setup the scroll bars for all tables in the
	 * application
	 */
	private void setupScrollBar() {
		pendingBar = lookUpVerticalScrollBar(taskPendingList);
		completeBar = lookUpVerticalScrollBar(taskCompleteList);
		trashBar = lookUpVerticalScrollBar(taskTrashList);

		InputMap map = commandLine.getInputMap();
		assert map != null;
		
		setupScrollUpKeyForCommandLine(map);
		setupScrollDownKeyForCommandLine(map);
	}

	// Setup scroll up key
	@SuppressWarnings("serial")
	private void setupScrollUpKeyForCommandLine(InputMap map) {
		KeyStroke scrollUpKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_UP, 0);
		Action scrollUpAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						processScrollingUp();
					}
				});
			}
		};
		map.put(scrollUpKey, scrollUpAction);
	}

	// Setup scroll down key
	@SuppressWarnings("serial")
	private void setupScrollDownKeyForCommandLine(InputMap map) {
		KeyStroke scrollDownKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_DOWN, 0);
		Action scrollDownAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						processScrollingDown();
					}
				});
			}
		};
		map.put(scrollDownKey, scrollDownAction);
	}

	/**
	 * Process scrolling down the table
	 */
	private void processScrollingDown() {
		ScrollBar modifiedScrollBar = getScrollBar(getTabIndex());
		boolean isAtMaxHeight = modifiedScrollBar.getValue() >= modifiedScrollBar
				.getMax();
		if (!isAtMaxHeight) {
			modifiedScrollBar.setValue(modifiedScrollBar.getValue() + 0.2);
		}
	}
	
	/**
	 * Process scrolling up the table
	 */
	private void processScrollingUp() {
		ScrollBar modifiedScrollBar = getScrollBar(getTabIndex());
		boolean isAtMinHeight = modifiedScrollBar.getValue() <= modifiedScrollBar
				.getMin();
		if (!isAtMinHeight) {
			modifiedScrollBar.setValue(modifiedScrollBar.getValue() - 0.2);
		}
	}
	
	/**
	 * Setup the shorcuts in the application
	 */
	private void setupShortcuts() {
		setupStageFocusProperty();
		setupShortcutForJavaFXNodes();
		setupShortcutsForCommandLine();
	}
	
	/**
	 * Setup the stage focus listener for the main window. When the main window is focused, it will automatically focus on the subRoot node.
	 */
	private void setupStageFocusProperty() {
		stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov,
					Boolean oldVal, Boolean newVal) {
				boolean isFocused = newVal.booleanValue() == true;
				if (isFocused) {
					subRoot.requestFocus();
				}
			}
		});
	}
	
	// Setup shortcut for the command line
	private void setupShortcutsForCommandLine() {
		InputMap map = commandLine.getInputMap();
		commandLine.setFocusTraversalKeysEnabled(false);
		setupChangeTabShortcutForCommandLine(map);
		setupOtherKeyShortcutsForCommandLine();
	}
	
	// Setup all shortcuts apart from the change tab shortcuts for the command line
	private void setupOtherKeyShortcutsForCommandLine() {
		commandLine.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(java.awt.event.KeyEvent e) {
			}

			@Override
			public void keyReleased(java.awt.event.KeyEvent e) {
			}
			
			/**
			 * Setup the event handling when certain keys are pressed
			 */
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				boolean isCollapseShortcut = (e.getKeyCode() == java.awt.event.KeyEvent.VK_UP)
						&& e.isControlDown();
				boolean isExpandShortcut = (e.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN)
						&& e.isControlDown();
				boolean isHideShortcut = (e.getKeyCode() == java.awt.event.KeyEvent.VK_H
						&& e.isControlDown() && e.isShiftDown());
				boolean isTraditionalCloseShortcut = (e.getKeyCode() == java.awt.event.KeyEvent.VK_F4 && e
						.isAltDown());
				
				if (isCollapseShortcut) {
					setupCollapseShortuctForCommandLine();
				} else if (isExpandShortcut) {
					setupExpandShortcutForCommandLine();
				} else if (isHideShortcut || isTraditionalCloseShortcut) {
					setupHideShortcutForCommandLine();
				}
			}
			
			// Setup hide shortcut for command line
			private void setupHideShortcutForCommandLine() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						hide();
					}
				});
			}
			
			// Setup the expand shortcut for command line
			private void setupExpandShortcutForCommandLine() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						expandAnimation();
					}
				});
			}
			
			// Setup the collapse shortcut for command line
			private void setupCollapseShortuctForCommandLine() {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						collapseAnimation();
					}
				});
			}
		});
	}
	
	/**
	 * Set change tab shortcut for the command line
	 * @param map the input map of key bindings for command line
	 */
	@SuppressWarnings("serial")
	private void setupChangeTabShortcutForCommandLine(InputMap map) {
		KeyStroke changeTabKey = KeyStroke.getKeyStroke(
				com.sun.glass.events.KeyEvent.VK_TAB,
				java.awt.event.InputEvent.CTRL_DOWN_MASK);
		Action changeTabAction = new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				Platform.runLater(new Runnable() {
					@Override
					public void run() {
						processChangingTab();
					}
				});
			}
		};

		map.put(changeTabKey, changeTabAction);
	}
	
	// Set up the shortcuts for all JavaFX nodes apart from the Swing command line
	private void setupShortcutForJavaFXNodes() {
		subRoot.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent keyEvent) {
				setupChangeTabShortcut(keyEvent);
				if (Common.collapseWindow.match(keyEvent)) {
					collapseAnimation();
				} else if (Common.expandWindow.match(keyEvent)) {
					expandAnimation();
				} else if (Common.hideWindow.match(keyEvent) || Common.traditionalCloseWindow.match(keyEvent)) {
					hide();
				} else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
					setupBackspaceShortcut();
				} else if (keyEvent.getCode() == KeyCode.UP) {
					setupUpShortcut();
				} else if (keyEvent.getCode() == KeyCode.DOWN) {
					setupDownShortcut();
				} else if (keyEvent.getCode() == KeyCode.RIGHT) {
					setupRightShortcut();
				} else if (keyEvent.getCode() == KeyCode.LEFT) {
					setupLeftShortuct();
				} else {
					setupOtherKeyCharactersShortcut(keyEvent);
				}
			}
			
			// Set focus on command line
			private void setFocusOnCommandLine() {
				textField.setKeyEvent(true);
				textField.setCommandLineOnTop();
				commandLine.requestFocus();
			}
			
			// Set LEFT ARROW shortcut. Focus on the command line and move the caret to the left by 1 character
			private void setupLeftShortuct() {
				setFocusOnCommandLine();
				int pos = commandLine.getCaretPosition();
				boolean isAtTheStart = pos == 0;
				if (!isAtTheStart) {
					commandLine.setCaretPosition(pos - 1);
				}
			}
			
			// Set RIGHT ARROW shortcut. Focus on the commandline and move the caret to the right by 1 character
			private void setupRightShortcut() {
				setFocusOnCommandLine();
				int pos = commandLine.getCaretPosition();
				boolean isAtTheEnd = pos == commandLine.getText().length();
				if (!isAtTheEnd) {
					commandLine.setCaretPosition(pos + 1);
				}
			}
			
			// Set DOWN ARROW shortcut. Scroll down the table
			private void setupDownShortcut() {
				processScrollingDown();
			}
			
			// Set UP ARROW shortcut. Scroll up the table
			private void setupUpShortcut() {
				processScrollingUp();
			}
			
			// Set backspace shortcut. Focus on the command line and delete the latest character
			private void setupBackspaceShortcut() {
				setFocusOnCommandLine();
				int pos = commandLine.getCaretPosition();
				boolean isAtTheStart = pos == 0;
				if (!isAtTheStart) {
					commandLine.setText(commandLine.getText().substring(0, pos - 1)
							+ commandLine.getText().substring(pos));
					commandLine.setCaretPosition(pos - 1);
				}
			}
			
			// Set change tab shortcut
			private void setupChangeTabShortcut(KeyEvent e) {
				if (Common.changeTab.match(e)) {
					processChangingTab();
				}
			}
			
			// Set other key characters shortcut apart from all listed keys
			private void setupOtherKeyCharactersShortcut(KeyEvent e) {
				setFocusOnCommandLine();
				if (e.getCode() != KeyCode.ENTER) {
					int pos = commandLine.getCaretPosition();
					commandLine.setText(commandLine.getText().substring(0, pos) + e.getText()
							+ commandLine.getText().substring(pos));
					if (pos != commandLine.getText().length()){
						commandLine.setCaretPosition(pos + 1);
					}
				}
			}
		});
	}
	
	/**
	 * Process the action of changing tab in the tab pane
	 */
	private void processChangingTab() {
		int tabIndex = getTabIndex();
		assert tabIndex >= 0 && tabIndex <= 2;
		if (tabIndex != Common.TRASH_TAB) {
			tabPane.getSelectionModel().selectNext();
		} else {
			tabPane.getSelectionModel().selectFirst();
		}
	}
	
	/**
	 * Create the command line for the application with multi color text
	 */
	private void createCommandLine() {
		commandLine = new JTextPane();
		commandLine.setAutoscrolls(false);
		setFont();
		// Create panel
		JPanel noWrapPanel = new JPanel(new BorderLayout());
		noWrapPanel.add(commandLine);
		// Create scroll pane
		JScrollPane scrollPane = new JScrollPane(noWrapPanel);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollPane.setBorder(null);
		// Create the SwingNode custom class to represent a Swing node in JavaFX
		textField = new SwingNode(stage, scrollPane);
		textField.setTranslateX(36);
		textField.setTranslateY(-93);
	}

	/**
	 * Setup font for the command line
	 */
	private void setFont() {
		MutableAttributeSet attrs = commandLine.getInputAttributes();
		java.awt.Font customFont = getDefaultFont();
		try {
			customFont = loadCustomFont();
			registerFont(customFont);
		} catch (Exception e) {
			logger.log(Level.INFO, "Cannot read font");
		}
		updateCustomFont(attrs, customFont);
	}

	// Update the new custom font for the application
	private void updateCustomFont(MutableAttributeSet attrs,
			java.awt.Font customFont) {
		StyleConstants.setFontFamily(attrs, customFont.getFamily());
		StyleConstants.setFontSize(attrs, customFont.getSize());
		StyledDocument doc = commandLine.getStyledDocument();
		doc.setCharacterAttributes(0, doc.getLength() + 1, attrs, false);
	}

	// Register the font to the system
	private void registerFont(java.awt.Font customFont) {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		ge.registerFont(customFont);
	}

	// Load the custom font
	private java.awt.Font loadCustomFont() throws FileNotFoundException,
			FontFormatException, IOException {
		java.awt.Font temp;
		InputStream myFont = new BufferedInputStream(new FileInputStream(
				"resources/fonts/ubuntub.ttf"));
		temp = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, myFont);
		temp = temp.deriveFont(java.awt.Font.PLAIN, 16);
		return temp;
	}

	/**
	 * Get the default font in the application
	 * @return the default font
	 */
	private java.awt.Font getDefaultFont() {
		return new java.awt.Font("Calibri", java.awt.Font.PLAIN, 17);
	}

	/**
	 * Create the whole content to be displayed in the interface
	 */
	private void createContent() {
		createSubRoot();
		createCommandLine();

		mainRoot = new StackPane();
		mainRoot.getChildren().addAll(textField, subRoot);
	}
	
	/**
	 * Create the group containing all display nodes in the application except for the command line
	 */
	private void createSubRoot() {
		createTopSection();
		createCenterSection();
		createBottomSection();
		subRoot = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();
	}
	
	/**
	 * Set the icon for the application
	 */
	private void setIcon() {	
		stage.getIcons().add(
				new Image(getClass().getResource("iDo_traybar.png")
						.toExternalForm()));
	}
	
	/**
	 * Set the initial position of the stage when the user first opens the application
	 */
	private void setInitialPosition() {
		Rectangle2D primaryScreenBounds = Screen.getPrimary().getVisualBounds();
		stage.setX((primaryScreenBounds.getWidth() - stage.getWidth()) / 2);
		stage.setY((primaryScreenBounds.getHeight() - stage.getHeight()) / 2);
	}
	
	/**
	 * Set the initial state of the application as collapsed state
	 */
	private void setInitialState() {
		removeTopAndCenter();
		expandOrCollapse.setId("larger");
		commandLine.requestFocus();
		commandLine.setCaretPosition(commandLine.getText().length());
	}
	
	// Customize the interface of the application
	public void customizeGUI() {
		clearPreviousCustomization();
		setupNewCustomization();
	}
	
	/**
	 * Setup new customization for the application depending on the chosen mode
	 */
	private void setupNewCustomization() {
		if (model.getThemeMode().equals(Common.DAY_MODE)) {
			setStyleSheetForDayMode();
			setCaretColourListenerForDayMode();
			setTitleForDayMode();
			setColourSchemeForDayMode();
		} else {
			setStyleSheetForNightMode();
			setCaretColourListenerForNightMode();
			setTitleForNightMode();
			setColourSchemeForNightMode();

		}
	}
	
	/**
	 * Set title for night mode
	 */
	private void setTitleForNightMode() {
		title.setImage(new Image(getClass().getResourceAsStream("iDoNight.png")));
	}
	
	/**
	 * Set title for day mode
	 */
	private void setTitleForDayMode() {
		title.setImage(new Image(getClass().getResourceAsStream("iDo.png")));
	}
	
	/**
	 * Set caret colour listener for night mode
	 */
	private void setCaretColourListenerForNightMode() {
		caretColourListenerForCommandLine = new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov,
					Boolean oldValue, Boolean newValue) {
				boolean isFocused = newValue;
				if (!isFocused) {
					commandLine.setCaretColor(ColourPalette.WHITE);
				} else {
					commandLine.setCaretColor(ColourPalette.caretColour);
				}
			}
		};
		stage.focusedProperty().addListener(caretColourListenerForCommandLine);
	}
	
	/**
	 * Set caret colour listener for day mode
	 */
	private void setCaretColourListenerForDayMode() {
		caretColourListenerForCommandLine = new ChangeListener<Boolean>() {
			public void changed(ObservableValue<? extends Boolean> ov,
					Boolean oldValue, Boolean newValue) {
				boolean isFocused = newValue;
				if (!isFocused) {
					commandLine.setCaretColor(ColourPalette.BLACK);
				} else {
					commandLine.setCaretColor(ColourPalette.caretColour);
				}
			}
		};
		stage.focusedProperty().addListener(caretColourListenerForCommandLine);
	}
	
	/** 
	 * Set style sheet for night mode
	 */
	private void setStyleSheetForNightMode() {
		scene.getStylesheets().addAll(getClass().getResource("nightCustomization.css")
						.toExternalForm());
	}
	
	/**
	 * Set style sheet for day mode
	 */
	private void setStyleSheetForDayMode() {
		scene.getStylesheets().addAll(getClass().getResource("dayCustomization.css")
						.toExternalForm());
	}
	
	/**
	 * Change the colour scheme for night mode
	 */
	private void setColourSchemeForNightMode() {
		commandLine.setBackground(ColourPalette.cmdBackgroundColour);
		commandLine.setStyledDocument(new CustomStyledDocument());
		defaultColor = ColourPalette.fxNEAR_WHITE;
		if (model.getColourScheme().equals(Common.DAY_MODE)) {
			model.setColourScheme(Common.NIGHT_MODE);
		}
		setColourScheme(model.getColourScheme());
	}
	
	/**
	 * Change the colour scheme for day mode
	 */
	private void setColourSchemeForDayMode() {
		commandLine.setBackground(ColourPalette.WHITE);
		commandLine.setStyledDocument(new CustomStyledDocument());
		defaultColor = ColourPalette.fxWHITE;
		if (model.getColourScheme().equals(Common.NIGHT_MODE)) {
			model.setColourScheme(Common.DAY_MODE);
		}
		setColourScheme(model.getColourScheme());
	}
	
	/**
	 * Clear previous customization of the application to prepare for new customization
	 */
	private void clearPreviousCustomization() {
		if (caretColourListenerForCommandLine != null) {
			stage.focusedProperty().removeListener(
					caretColourListenerForCommandLine);
		}
		scene.getStylesheets().clear();
	}
	
	// Get the default color in the application
	public Color getDefaultColor() {
		return defaultColor;
	}
	
	/**
	 * Create the BOTTOM section
	 */
	private void createBottomSection() {
		bottom = new VBox();
		bottom.setSpacing(5);
		bottom.setPadding(new Insets(0, 0, 5, 44));

		HBox upperPart = createUpperPartInBottomSection();
		HBox lowerPart = createLowerPartInBottomSection();

		bottom.getChildren().addAll(upperPart, lowerPart);
	}
	
	// Create the lower part in BOTTOM section comprising the list of feedback elements
	private HBox createLowerPartInBottomSection() {
		HBox feedbacks = new HBox();
		feedbacks.setSpacing(10);
		feedbackList.clear();
		for (int i = 0; i < 10; i++) {
			Text feedbackPiece = TextBuilder.create().styleClass("feedback")
					.fill(defaultColor).text("").build();
			feedbackList.add(feedbackPiece);
			feedbacks.getChildren().add(feedbackList.get(i));
		}
		return feedbacks;
	}
	
	// Create the upper part in BOTTOM section
	private HBox createUpperPartInBottomSection() {
		HBox temp = new HBox();
		temp.setSpacing(10);
		TextField invisibleCommandLine = createOverlayTextfield();
		setupExpandOrCollapseButton();
		temp.getChildren().addAll(invisibleCommandLine, expandOrCollapse);

		return temp;
	}

	/**
	 * Create the expand or collapse button
	 */
	private void setupExpandOrCollapseButton() {
		expandOrCollapse = new Button();
		expandOrCollapse.setPrefSize(30, 30);
		expandOrCollapse.setId("smaller");
		hookUpEventForExpandOrCollapse();
	}

	// Create the overlay text field behind the actual text field
	private TextField createOverlayTextfield() {
		TextField invisibleCommandLine = new TextField();
		invisibleCommandLine.setPrefWidth(630);
		invisibleCommandLine.opacityProperty().set(0.0);
		return invisibleCommandLine;
	}

	/**
	 * Create the CENTER section
	 */
	private void createCenterSection() {
		createTabPane();
		center = HBoxBuilder.create().padding(new Insets(0, 44, 0, 44))
				.children(tabPane).build();
	}

	/**
	 * Create the tab pane where content in the tabs will reside in
	 */
	private void createTabPane() {
		createPendingTable();
		createCompleteTable();
		createTrashTable();
		createTabs();
		setTabChangeListener();
	}

	// Create the tabs containing the 3 tables
	private void createTabs() {
		tabPane = new TabPane();
		Tab pending = TabBuilder.create().content(taskPendingList)
				.text("PENDING").closable(false).build();
		Tab complete = TabBuilder.create().content(taskCompleteList)
				.text("COMPLETE").closable(false).build();
		Tab trash = TabBuilder.create().content(taskTrashList).text("TRASH")
				.closable(false).build();
		tabPane.getTabs().addAll(pending, complete, trash);
	}

	// Create the table showing trash tasks
	private void createTrashTable() {
		taskTrashList = new TableView<Task>();
		createTable(taskTrashList, model.getTrashList());
	}

	// Create the table showing completed tasks
	private void createCompleteTable() {
		taskCompleteList = new TableView<Task>();
		createTable(taskCompleteList, model.getCompleteList());
	}

	// Create the table showing pending tasks
	private void createPendingTable() {
		taskPendingList = new TableView<Task>();
		createTable(taskPendingList, model.getPendingList());
	}

	/**
	 * Create the TOP section in the application
	 */
	private void createTopSection() {
		top = new AnchorPane();
		top.setPadding(new Insets(-15, 15, -30, 44));

		title = createTitle();
		createSyncProgressIndicator();
		createInternetAccessIndicator();
		HBox systemButtons = createSystemButtons();

		setupTopLayout(netAccessIndicator, systemButtons);
	}

	// Create the indicator whether there is currently internet connection or
	// not
	private void createInternetAccessIndicator() {
		Image netAcess = new Image(getClass().getResourceAsStream(
				"redCross.png"), 25, 25, true, true);
		netAccessIndicator = new ImageView(netAcess);
		netAccessIndicator.setVisible(false);
	}
	
	//@author A0100927M
	// Create the indicator whether the application is under syncing progress or
	// not
	private void createSyncProgressIndicator() {
		syncProgress = new ProgressIndicator();
		setSyncProgressVisible(false);
	}

	/**
	 * Set the visibility of the sync progress
	 * 
	 * @param isVisible
	 *            indicator whether to show or not
	 */
	public void setSyncProgressVisible(final boolean isVisible) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				syncProgress.setVisible(isVisible);
			}
		});
	}
	
	//@author A0098077N
	/**
	 * Show the signal that there is currently no internet connection The signal
	 * will start to fade out after 2 seconds
	 */
	public void showNoInternetConnection() {
		netAccessIndicator.setVisible(true);
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				FadeTransition fadeIn = new FadeTransition(
						Duration.millis(100), netAccessIndicator);
				fadeIn.setFromValue(1.0);
				fadeIn.setToValue(1.0);
				FadeTransition fadeOut = new FadeTransition(Duration
						.millis(2000), netAccessIndicator);
				fadeOut.setFromValue(1.0);
				fadeOut.setToValue(0.0);
				SequentialTransition seq = new SequentialTransition(fadeIn,
						new PauseTransition(Duration.millis(4000)), fadeOut);
				seq.play();
			}
		});
	}

	/**
	 * Setup the functionality to drag the window
	 */
	private void setupDraggable() {
		// Get the position of the mouse in the stage
		subRoot.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - stage.getX();
				dragAnchorY = me.getScreenY() - stage.getY();
			}
		});

		// Moving with the stage with the mouse at constant position relative to
		// the stage
		subRoot.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				stage.setX(me.getScreenX() - dragAnchorX);
				stage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}

	/**
	 * Setup the layout in the top section
	 * 
	 * @param netAcessIndicator
	 *            the indicator whether there is internect access or not
	 * @param systemButtons
	 *            the buttons functioning the application
	 */
	private void setupTopLayout(ImageView netAcessIndicator, HBox systemButtons) {
		syncProgress.setMinSize(25, 25);
		syncProgress.setMaxSize(25, 25);

		StackPane indicatorPane = new StackPane();
		indicatorPane.getChildren().addAll(netAcessIndicator, syncProgress);

		top.getChildren().addAll(title, indicatorPane, systemButtons);
		setAnchor(systemButtons, indicatorPane);
	}

	// Set the anchor of the nodes in the top section
	private void setAnchor(HBox buttons, StackPane indicatorPane) {
		AnchorPane.setLeftAnchor(title, 10.0);
		AnchorPane.setTopAnchor(buttons, 25.0);
		AnchorPane.setTopAnchor(title, 30.0);
		AnchorPane.setRightAnchor(buttons, 5.0);
		AnchorPane.setRightAnchor(indicatorPane, 305.0);
		AnchorPane.setBottomAnchor(indicatorPane, -28.0);
	}

	/**
	 * Create the image title to be shown in the application
	 * 
	 * @return the created title image
	 */
	private ImageView createTitle() {
		ImageView title = new ImageView();
		title.setFitWidth(110);
		title.setPreserveRatio(true);
		title.setSmooth(true);
		title.setCache(true);
		return title;
	}

	/**
	 * This function is used to create the system buttons in the application
	 * including the exit and minimize ones.
	 * 
	 * @return the horizontal box containing these 2 buttons
	 */
	private HBox createSystemButtons() {
		Button minimizeButton = createMinimizeButton();
		Button closeButton = createExitButton();

		HBox hb = new HBox();
		hb.getChildren().add(minimizeButton);
		hb.getChildren().add(closeButton);
		hb.setSpacing(10);
		hb.setAlignment(Pos.BOTTOM_CENTER);
		return hb;
	}

	/**
	 * Create the minimize button in the application
	 * 
	 * @return the created button
	 */
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

	/**
	 * Create the exit button in the application
	 * 
	 * @return the created button
	 */
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

	/**
	 * This function is used to create the table in the center section.
	 * 
	 * @param taskList
	 *            the table to be created
	 * @param list
	 *            the list of tasks for the table to view
	 */
	public void createTable(TableView<Task> taskList, ObservableList<Task> list) {
		taskList.setItems(list);
		taskList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		setEmptyTableMessage(taskList);

		final ObservableList<TableColumn<Task, ?>> columns = taskList
				.getColumns();
		final TableColumn<Task, String> indexColumn = addIndexColumn(columns);
		final TableColumn<Task, String> occurrenceColumn = addOccurrenceColumn(columns);
		final TableColumn<Task, String> taskInfoColumn = addTaskInfoColumn(columns);
		final TableColumn<Task, String> startDateColumn = addStartDateColumn(columns);
		final TableColumn<Task, String> endDateColumn = addEndDateColumn(columns);
		final TableColumn<Task, Tag> tagColumn = addTagColumn(columns);
		final TableColumn<Task, RowStatus> rowStatusColumn = addRowStatusColumn(columns);
		columns.addListener(new ListChangeListener<TableColumn<Task, ?>>() {
			@Override
			public void onChanged(Change<? extends TableColumn<Task, ?>> change) {
				change.next();
				if (change.wasReplaced()) {
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

	// Set the text display when the table is empty
	private void setEmptyTableMessage(TableView<Task> taskList) {
		final Text emptyTableSign = new Text(
				"There is currently no task in this tab");
		emptyTableSign.getStyleClass().add("text");
		taskList.setPlaceholder(emptyTableSign);
	}

	// Add task info column
	private TableColumn<Task, String> addTaskInfoColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, String> taskInfoColumn = createTaskInfoColumn();
		columns.add(taskInfoColumn);
		return taskInfoColumn;
	}

	/**
	 * Create the column displaying task info
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, String> createTaskInfoColumn() {
		TableColumn<Task, String> taskInfoColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).text("Task")
				.sortable(false).prefWidth(330).build();
	
		setupTaskInfoProperty(taskInfoColumn);
		setupTaskInfoUpdateFormat(taskInfoColumn);
	
		return taskInfoColumn;
	}

	/**
	 * Link up the content of each cell with the task info property in Task
	 * class
	 * 
	 * @param taskInfoColumn
	 *            the linked column
	 */
	private void setupTaskInfoProperty(TableColumn<Task, String> taskInfoColumn) {
		taskInfoColumn
				.setCellValueFactory(new PropertyValueFactory<Task, String>(
						"workInfo"));
	}

	/**
	 * Setup how to display the content of the property
	 * 
	 * @param taskInfoColumn
	 *            the modified column
	 */
	private void setupTaskInfoUpdateFormat(
			final TableColumn<Task, String> taskInfoColumn) {
		taskInfoColumn
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
											taskInfoColumn.widthProperty());
									setGraphic(text);
								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
	}

	// Add index column
	private TableColumn<Task, String> addIndexColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, String> indexColumn = createIndexColumn();
		columns.add(indexColumn);
		return indexColumn;
	}

	/**
	 * Create the index column
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, String> createIndexColumn() {
		TableColumn<Task, String> indexColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).visible(true)
				.text("").prefWidth(28).sortable(false).resizable(false)
				.build();
		setupEndDateProperty(indexColumn); // no specific property as index
											// column does not depend on Task
		setupIndexUpdateFormat(indexColumn);
		return indexColumn;
	}

	/**
	 * Setup how to display the content in the column
	 * 
	 * @param indexColumn
	 *            the modified column
	 */
	private void setupIndexUpdateFormat(TableColumn<Task, String> indexColumn) {
		indexColumn
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

	// Add tag column
	private TableColumn<Task, Tag> addTagColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, Tag> tagColumn = createTagColumn();
		columns.add(tagColumn);
		return tagColumn;
	}

	/**
	 * Create the tag column containing the info of recurring period and
	 * category tag
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, Tag> createTagColumn() {
		TableColumn<Task, Tag> tagColumn = TableColumnBuilder
				.<Task, Tag> create().resizable(false).text("Tag")
				.sortable(false).resizable(false).prefWidth(110).build();
	
		setupTagProperty(tagColumn);
		setupTagUpdateFormat(tagColumn);
	
		return tagColumn;
	}

	/**
	 * Link up the content of each cell with the tag property in Task class
	 * 
	 * @param tagColumn
	 *            the linked column
	 */
	private void setupTagProperty(TableColumn<Task, Tag> tagColumn) {
		tagColumn.setCellValueFactory(new PropertyValueFactory<Task, Tag>("tag"));
	}

	/**
	 * Setup how to display the content of the property
	 * 
	 * @param tagColumn
	 *            the modifed column
	 */
	private void setupTagUpdateFormat(final TableColumn<Task, Tag> tagColumn) {
		tagColumn.setCellFactory(new Callback<TableColumn<Task, Tag>, TableCell<Task, Tag>>() {
					@Override
					public TableCell<Task, Tag> call(
							TableColumn<Task, Tag> param) {
						TableCell<Task, Tag> tc = new TableCell<Task, Tag>() {
							Text text;
	
							public void updateItem(Tag item, boolean empty) {
								if (item != null) {
									boolean isRepetitiveTask = !item
											.getRepetition()
											.equals(Common.NULL);
									boolean hasTag = !item.getTag().equals("-");
									checkRepetitiveTag(item, isRepetitiveTask,
											hasTag);
									text.getStyleClass().add("text");
									text.wrappingWidthProperty().bind(
											tagColumn.widthProperty());
									setGraphic(text);
								}
							}
	
							// Check for repetitive tag
							private void checkRepetitiveTag(Tag item,
									boolean isRepetitiveTask, boolean hasTag) {
								if (!isRepetitiveTask) {
									checkCategoryTagForNormalTask(item, hasTag);
								} else {
									checkCategoryTagForRecurringTask(item,
											hasTag);
								}
							}
	
							// Check for category tag of recurring task
							private void checkCategoryTagForRecurringTask(
									Tag item, boolean hasTag) {
								if (!hasTag) {
									text = new Text(appendSpaceForAlignment("#"+ item.getRepetition()));
								} else {
									text = new Text(appendSpaceForAlignment(item.getTag()) + "\n"
													+ appendSpaceForAlignment("#" + item.getRepetition()));
								}
							}
	
							// Check for category tag of normal task
							private void checkCategoryTagForNormalTask(
									Tag item, boolean hasTag) {
								if (!hasTag) {
									text = new Text(appendSpaceForAlignment("-"));
								} else {
									text = new Text(appendSpaceForAlignment(item.getTag()));
								}
							}
	
							// Append space for alignment according to the
							// content of the info
							private String appendSpaceForAlignment(String info) {
								if (info.equals("-")) {
									return "             " + info;
								}
	
								if (info.length() < 10) {
									return "\t" + info;
								} else if (info.length() < 12) {
									return "    " + info;
								} else {
									return info;
								}
							}
						};
						tc.setAlignment(Pos.TOP_CENTER);
						return tc;
					}
				});
	}

	// Add row status column
	private TableColumn<Task, RowStatus> addRowStatusColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, RowStatus> rowStatusColumn = createRowStatusColumn();
		columns.add(rowStatusColumn);
		return rowStatusColumn;
	}

	/**
	 * Create the row status column
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, RowStatus> createRowStatusColumn() {
		TableColumn<Task, RowStatus> rowStatusColumn = TableColumnBuilder
				.<Task, RowStatus> create().resizable(false).visible(true)
				.text("").prefWidth(1).build();
		setupRowStatusProperty(rowStatusColumn);
		setupRowStatusUpdateFormat(rowStatusColumn);
		return rowStatusColumn;
	}

	/**
	 * Link up the content of each cell with the rowStatus property in Task
	 * class
	 * 
	 * @param rowStatusColumn
	 *            the linked column
	 */
	private void setupRowStatusProperty(
			TableColumn<Task, RowStatus> rowStatusColumn) {
		rowStatusColumn.setCellValueFactory(new PropertyValueFactory<Task, RowStatus>("rowStatus"));
	}

	/**
	 * Setup how to display the content in the column
	 * 
	 * @param rowStatusColumn
	 *            the modified column
	 */
	private void setupRowStatusUpdateFormat(
			TableColumn<Task, RowStatus> rowStatusColumn) {
		rowStatusColumn.setCellFactory(new Callback<TableColumn<Task, RowStatus>, TableCell<Task, RowStatus>>() {
	
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
	
							// Clear the style of the row status
							private void clearStyle() {
								getTableRow().getStyleClass().removeAll(
										"table-row-cell", "unimportant",
										"important", "unimportant-odd",
										"unimportant-last", "important-last",
										"unimportant-odd-last");
	
							}
	
							// Set the style for the row status
							private void setStyle(RowStatus rowStatus) {
								boolean isLastOverdue = rowStatus
										.getIsLastOverdue();
								boolean isOdd = getTableRow().getIndex() % 2 != 0;
								boolean isImportant = rowStatus
										.getIsImportant();
								checkIsLastOverdue(isLastOverdue, isOdd,
										isImportant);
							}
	
							// Check for last overdue task
							private void checkIsLastOverdue(
									boolean isLastOverdue, boolean isOdd,
									boolean isImportant) {
								if (isLastOverdue) {
									checkIsImportantForLastOverdueTask(isOdd,
											isImportant);
								} else {
									checkIsImportantForNormalTask(isOdd,
											isImportant);
								}
							}
	
							// Check for important task of non last overdue task
							private void checkIsImportantForNormalTask(
									boolean isOdd, boolean isImportant) {
								if (isImportant) {
									getTableRow().getStyleClass().add("important");
								} else {
									checkIsOddForLastOverdueTask(isOdd);
								}
							}
	
							// Check for odd task of last overdue task
							private void checkIsOddForLastOverdueTask(
									boolean isOdd) {
								if (isOdd) {
									getTableRow().getStyleClass().add("unimportant-odd");
								} else {
									getTableRow().getStyleClass().add("unimportant");
								}
							}
	
							// Check for important task of last overdue task
							private void checkIsImportantForLastOverdueTask(
									boolean isOdd, boolean isImportant) {
								if (isImportant) {
									getTableRow().getStyleClass().add("important-last");
								} else {
									checkIsOddForNormalTask(isOdd);
								}
							}
	
							// Check for odd task of non last overdue task
							private void checkIsOddForNormalTask(boolean isOdd) {
								if (isOdd) {
									getTableRow().getStyleClass().add("unimportant-odd-last");
								} else {
									getTableRow().getStyleClass().add("unimportant-last");
								}
							}
						};
						return tc;
					}
				});
	}

	// Add start date column
	private TableColumn<Task, String> addStartDateColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, String> startDateColumn = createStartDateColumn();
		columns.add(startDateColumn);
		return startDateColumn;
	}

	/**
	 * Create the start date column
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, String> createStartDateColumn() {
		TableColumn<Task, String> startDateColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).text("Start")
				.prefWidth(90).resizable(false).sortable(false).build();
	
		setupStartDateProperty(startDateColumn);
		setupStartDateUpdateFormat(startDateColumn);
	
		return startDateColumn;
	}

	/**
	 * Link up the content of each cell with the startDateString property in
	 * Task class
	 * 
	 * @param startDateColumn
	 *            the linked column
	 */
	private void setupStartDateProperty(
			TableColumn<Task, String> startDateColumn) {
		startDateColumn.setCellValueFactory(new PropertyValueFactory<Task, String>("startDateString"));
	}

	/**
	 * Setup how to display the content of the property
	 * 
	 * @param startDateColumn
	 *            the modified column
	 */
	private void setupStartDateUpdateFormat(
			TableColumn<Task, String> startDateColumn) {
		startDateColumn
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

	// Add end date column
	private TableColumn<Task, String> addEndDateColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, String> endDateColumn = createEndDateColumn();
		columns.add(endDateColumn);
		return endDateColumn;
	}

	/**
	 * Create the end date column
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, String> createEndDateColumn() {
		TableColumn<Task, String> endDateColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).text("End")
				.sortable(false).resizable(false).prefWidth(90).build();
	
		setupEndDateProperty(endDateColumn);
		setupEndDateUpdateFormat(endDateColumn);
	
		return endDateColumn;
	}

	/**
	 * Link up the content of each cell with the endDateString property in Task
	 * class
	 * 
	 * @param endDateColumn
	 */
	private void setupEndDateProperty(TableColumn<Task, String> endDateColumn) {
		endDateColumn.setCellValueFactory(new PropertyValueFactory<Task, String>("endDateString"));
	}

	/**
	 * Setup how to display the content of the property
	 * 
	 * @param endDateColumn
	 *            the modified column
	 */
	private void setupEndDateUpdateFormat(
			TableColumn<Task, String> endDateColumn) {
		endDateColumn
				.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {
	
					@Override
					public TableCell<Task, String> call(
							TableColumn<Task, String> param) {
						TableCell<Task, String> tc = new TableCell<Task, String>() {
							@Override
							public void updateItem(String item, boolean empty) {
								if (item != null) {
									setText(item);
								}
							}
						};
						tc.setAlignment(Pos.TOP_CENTER);
						return tc;
					}
				});
	}
	
	//@author A0105667B
	// Add occurrence column
	private TableColumn<Task, String> addOccurrenceColumn(
			final ObservableList<TableColumn<Task, ?>> columns) {
		final TableColumn<Task, String> occurrenceColumn = createOccurrenceColumn();
		columns.add(occurrenceColumn);
		return occurrenceColumn;
	}

	/**
	 * Create the occurrence column
	 * 
	 * @return the created column
	 */
	private TableColumn<Task, String> createOccurrenceColumn() {
		TableColumn<Task, String> occurrenceColumn = TableColumnBuilder
				.<Task, String> create().resizable(false).visible(true)
				.text("").prefWidth(40).sortable(false).resizable(false)
				.build();
		setupOccurrenceProperty(occurrenceColumn);
		setupOccurrenceUpdateFormat(occurrenceColumn);
		return occurrenceColumn;
	}

	/**
	 * Link up the content of each cell with the occurrence property in Task
	 * class
	 * 
	 * @param occurrenceColumn
	 *            the linked column
	 */
	private void setupOccurrenceProperty(
			TableColumn<Task, String> occurrenceColumn) {
		occurrenceColumn.setCellValueFactory(new PropertyValueFactory<Task, String>("occurrence"));
	}

	/**
	 * Setup how to display the content of the property
	 * 
	 * @param occurrenceColumn
	 *            the modified column
	 */
	private void setupOccurrenceUpdateFormat(
			final TableColumn<Task, String> occurrenceColumn) {
		occurrenceColumn
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
									text.setFill(ColourPalette.occurrenceColour);
									text.setFont(Font.font("Verdana", 9));
									setAlignment(Pos.TOP_LEFT);
									setGraphic(text);
								}
							}
						};
						tc.setAlignment(Pos.TOP_LEFT);
						return tc;
					}
				});
	}
	
	
	//@author A0098077N
	/**
	 * Setup the event for the expandOrCollapse button
	 */
	private void hookUpEventForExpandOrCollapse() {
		expandOrCollapse.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (stage.getHeight() == MAX_HEIGHT) {
					collapseAnimation();
				} else if (stage.getHeight() == MIN_HEIGHT) {
					expandAnimation();
				}
			}
		});
	}

	// Remove the top and center section when the user collapses the window
	private void removeTopAndCenter() {
		subRoot.setTop(null);
		subRoot.setCenter(null);
	}

	/**
	 * Process the collapse animation of the window
	 */
	private void collapseAnimation() {
		removeTopAndCenter();
		expandOrCollapse.setId("larger");

		stage.setMinHeight(MIN_HEIGHT);
		Timer animTimer = new Timer();
		animTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > MIN_HEIGHT) {
					decrementHeight();
				} else {
					this.cancel();
				}
			}

			// Decrement the height of the window in each phase of the timer
			private void decrementHeight() {
				double i = stage.getHeight() - 10.0;
				stage.setMaxHeight(i);
				stage.setHeight(i);
			}
		}, 0, 5);
	}

	/**
	 * Process the expand animation of the window
	 */
	private void expandAnimation() {
		Timer animTimer = new Timer();
		subRoot.setTop(top);
		expandOrCollapse.setId("smaller");
		stage.setMaxHeight(MAX_HEIGHT);
		animTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() > 530) {
					setCenterWithFadeTransition();
				}

				if (stage.getHeight() < MAX_HEIGHT) {
					incrementHeight();
				} else {
					this.cancel();
				}
			}

			// Increment the height of the window in each phase of the timer
			private void incrementHeight() {
				double i = stage.getHeight() + 10.0;
				stage.setMinHeight(i);
				stage.setHeight(i);
			}
		}, 0, 5);
	}

	/**
	 * Set the transition of fade in for the center section when user expands
	 * the application
	 */
	private void setCenterWithFadeTransition() {
		Platform.runLater(new Runnable() {
			public void run() {
				subRoot.setCenter(center);
				FadeTransition fadeIn = new FadeTransition(
						Duration.millis(500), center);
				fadeIn.setFromValue(0.0);
				fadeIn.setToValue(1.0);
				fadeIn.play();
			}
		});
	}

	/**
	 * Setup the functionality of hiding in system tray for application
	 */
	private void setupSystemTray() {
		if (SystemTray.isSupported()) {
			java.awt.Image iconImage = getIconImage();
			popupMenu = createPopupMenu();
			createTrayIcon(iconImage, popupMenu);
			createSystemTray();
		}
	}

	/**
	 * Register the tray icon in system tray
	 */
	private void createSystemTray() {
		SystemTray tray = SystemTray.getSystemTray();
		try {
			tray.add(trayIcon);
		} catch (AWTException e) {
			logger.log(Level.INFO, e.getMessage());
		}
	}

	/**
	 * Create the tray icon in the system tray
	 * 
	 * @param iconImage
	 *            the image icon
	 * @param popupMenu
	 *            the pop up menu showing up when right click on the icon
	 */
	private void createTrayIcon(java.awt.Image iconImage, PopupMenu popupMenu) {
		trayIcon = new TrayIcon(iconImage, "iDo", popupMenu);
		trayIcon.setImageAutoSize(true);
		trayIcon.addActionListener(createShowListener());
	}

	/**
	 * Create the pop up menu in the system tray
	 * 
	 * @return the created pop up menu
	 */
	private PopupMenu createPopupMenu() {
		final PopupMenu popup = new PopupMenu();
		
		// Create Show Item
		MenuItem showItem = new MenuItem("Show the main window");
		showItem.addActionListener(createShowListener());
		popup.add(showItem);

		popup.addSeparator();
		
		// Create Setting Item
		MenuItem settingsItem = new MenuItem("Preferences");

		popup.add(settingsItem);
		
		// Create Close Item
		MenuItem closeItem = new MenuItem("Exit");
		closeItem.addActionListener(createExitListener());
		popup.add(closeItem);

		return popup;
	}

	/**
	 * This function is used to setup the global hotkey for the application User
	 * can use the assigned hot key to open the application any time while it is
	 * hidden in the system tray
	 */
	private void setupGlobalHotkey() {
		loadLibrary();
		checkIntellitype();
		initGlobalHotKey();
	}

	/**
	 * Get the Settings MenuItem in the pop up menu
	 * 
	 * @return the requested menu item
	 */
	public MenuItem getSettingsItemInPopupMenu() {
		return popupMenu.getItem(2);
	}

	/**
	 * Setup the ActionListener when the user chooses "Show" in the pop up menu
	 * 
	 * @return the requested ActionListener object
	 */
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

	/**
	 * Setup the ActionListener when the user chooses "Exit" in the pop up menu
	 * 
	 * @return the request ActionListner object
	 */
	private ActionListener createExitListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JIntellitype.getInstance().cleanUp();
				System.exit(0);
			}
		};
	}

	/**
	 * Load the icon image for system tray
	 */
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

	/**
	 * Hide the application. If system tray is not supported, the application
	 * will terminate instead.
	 */
	public void hide() {
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

	// Load the native library
	private void loadLibrary() {
		System.loadLibrary("JIntellitype");
	}

	/**
	 * Process of checking existing instance before initialization
	 */
	private void checkIntellitype() {
		// first check to see if an instance of this application is already
		// running, use the name of the window title of this JFrame for checking
		if (JIntellitype.checkInstanceAlreadyRunning("iDo")) {
			System.exit(1);
		}

		// next check to make sure JIntellitype DLL can be found and we are on
		// a Windows operating System
		if (!JIntellitype.isJIntellitypeSupported()) {
			System.exit(1);
		}
	}

	/**
	 * Initialize the Global Hot key feature
	 */
	private void initGlobalHotKey() {
		try {
			// initialize JIntellitype with the frame so all windows commands
			// can
			// be attached to this window
			JIntellitype.getInstance().addHotKeyListener(this);
			JIntellitype.getInstance().registerHotKey(90,
					JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, 'D');
		} catch (RuntimeException ex) {
			logger.log(
					Level.INFO,
					"Either you are not on Windows, or there is a problem with the JIntellitype library!");
		}
	}

	/**
	 * Setup the processed action when press the global hot key
	 */
	@Override
	public void onHotKey(int keyIdentifier) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				stage.toFront();
				stage.show();
			}
		});
	}

	/**
	 * Setup the listener when user changes tab. It will clear all search
	 * results back to the original list
	 */
	private void setTabChangeListener() {
		tabPane.getSelectionModel().selectedIndexProperty()
				.addListener(new ChangeListener<Number>() {
					public void changed(ObservableValue<? extends Number> ov,
							Number oldValue, Number newValue) {
						clearSearchLists();
					}
					
					// Clear the search results back to original list
					private void clearSearchLists() {
						TwoWayCommand.setIndexType(TwoWayCommand.SHOWN);
						taskPendingList.setItems(model.getPendingList());
						taskCompleteList.setItems(model.getCompleteList());
						taskTrashList.setItems(model.getTrashList());
					}
				});
	}

	// Set the view to a specific tab
	public void setTab(int tabIndex) {
		tabPane.getSelectionModel().select(tabIndex);
	}

	// Get the current tab index that the user is viewing
	public int getTabIndex() {
		return tabPane.getSelectionModel().getSelectedIndex();
	}
	
	//@author A0105667B
	/**
	 * This function is the main function to set the real-time multicolor
	 * feedback
	 * 
	 * @param requestFeedback
	 *            the request of feedback from the application. Depending on the
	 *            request, this function will give different feedbacks.
	 */
	public void setFeedback(String requestFeedback) {
		emptyFeedback(0);
		switch (requestFeedback) {
		case Common.MESSAGE_ADD_TIP:
			setFeedbackStyle(0, "<add>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_EDIT_TIP:
			setFeedbackStyle(0, "<edit>", colourScheme[0]);
			setFeedbackStyle(1, "<index>", colourScheme[6]);
			setFeedbackStyle(2, "<workflow>", colourScheme[1]);
			setFeedbackStyle(3, "<start time>", colourScheme[2]);
			setFeedbackStyle(4, "<end time>", colourScheme[3]);
			setFeedbackStyle(5, "<importance *>", colourScheme[4]);
			setFeedbackStyle(6, "<#tag>", colourScheme[5]);
			emptyFeedback(7);
			break;
		case Common.MESSAGE_RECOVER_INDEX_TIP:
			setFeedbackStyle(0, "<recover>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_RECOVER_INFO_TIP:
			setFeedbackStyle(0, "<recover>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_REMOVE_INDEX_TIP:
			setFeedbackStyle(0, "<remove>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_REMOVE_INFO_TIP:
			setFeedbackStyle(0, "<remove>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_SEARCH_TIP:
			setFeedbackStyle(0, "<search>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_UNDO_TIP:
			setFeedbackStyle(0, "<undo>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_REDO_TIP:
			setFeedbackStyle(0, "<redo>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_TODAY_TIP:
			setFeedbackStyle(0, "<today>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_HELP_TIP:
			setFeedbackStyle(0, "<help>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_SETTINGS_TIP:
			setFeedbackStyle(0, "<settings>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_SHOW_ALL_TIP:
			setFeedbackStyle(0, "<show>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_CLEAR_ALL_TIP:
			setFeedbackStyle(0, "<clear>", colourScheme[0]);
			break;
		case Common.MESSAGE_SYNC_TIP:
			setFeedbackStyle(0, "<sync>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_EXIT_TIP:
			setFeedbackStyle(0, "<exit>", colourScheme[0]);
			emptyFeedback(1);
			break;
		case Common.MESSAGE_MARK_INDEX_TIP:
			setFeedbackStyle(0, "<mark>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_MARK_INFO_TIP:
			setFeedbackStyle(0, "<mark>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_UNMARK_INDEX_TIP:
			setFeedbackStyle(0, "<unmark>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_UNMARK_INFO_TIP:
			setFeedbackStyle(0, "<unmark>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_COMPLETE_INDEX_TIP:
			setFeedbackStyle(0, "<done>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_COMPLETE_INFO_TIP:
			setFeedbackStyle(0, "<done>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		case Common.MESSAGE_INCOMPLETE_INDEX_TIP:
			setFeedbackStyle(0, "<undone>", colourScheme[0]);
			setFeedbackWithTaskIndices();
			break;
		case Common.MESSAGE_INCOMPLETE_INFO_TIP:
			setFeedbackStyle(0, "<undone>", colourScheme[0]);
			setFeedbackWithTaskInfos();
			break;
		default:
			displayAvailableCommands(requestFeedback);
			break;
		}
	}

	private void displayAvailableCommands(String requestFeedback) {
		emptyFeedback(0);
		ArrayList<String> availCommands = getAvailCommands(requestFeedback
				.trim());
		for (int i = 0; i < availCommands.size(); i++) {
			setFeedbackStyle(i + 1, availCommands.get(i), colourScheme[0]);
		}
		setFeedbackStyle(0,
				availCommands.size() > 0 ? "Available commands: "
						: Common.MESSAGE_REQUEST_COMMAND, colourScheme[1]);
	}
	
	/**
	 * Append the content of feedback with available index
	 */
	private void setFeedbackWithTaskIndices() {
		setFeedbackStyle(1, "<index1> <index2> <index3> ...", colourScheme[6]);
		emptyFeedback(2);
	}

	/**
	 * Append the content of feedback with available task info
	 */
	private void setFeedbackWithTaskInfos() {
		setFeedbackStyle(1, "<workflow>", colourScheme[1]);
		setFeedbackStyle(2, "<start time>", colourScheme[2]);
		setFeedbackStyle(3, "<end time>", colourScheme[3]);
		setFeedbackStyle(4, "<importance *>", colourScheme[4]);
		setFeedbackStyle(5, "<#tag>", colourScheme[5]);
		emptyFeedback(6);
	}
	
	public String getFeedback() {
		String feedbackStr = "";
		for(Text feedback: feedbackList) {
			feedbackStr += feedback.getText();
		}
		return feedbackStr;
	}

	/**
	 * Get the list of possible commands by processing the input command from
	 * user
	 * 
	 * @param inputCommand
	 *            the command input from the user
	 * @return the list of possible commands
	 */
	private ArrayList<String> getAvailCommands(String inputCommand) {
		ArrayList<String> availCommands = new ArrayList<String>();
		for (int i = 0; i < Common.COMMAND_TYPES_STR.length; i++) {
			String command = Common.COMMAND_TYPES_STR[i];
			boolean hasCommand = command.indexOf(inputCommand) == 0
					&& !command.equals(inputCommand);
			if (hasCommand) {
				availCommands.add(command);
			}
		}
		return availCommands;
	}

	// Set the content and color for a feedback element with given index
	public void setFeedbackStyle(int index, String text, Color color) {
		feedbackList.get(index).setText(text);
		feedbackList.get(index).setFill(color);
	}

	// Empty the feedback list
	public void emptyFeedback(int startIndex) {
		for (int i = startIndex; i < feedbackList.size(); i++) {
			feedbackList.get(i).setText("");
		}
	}
	
	//@author A0100927M
	/**
	 * Set the colour scheme for iDo
	 * 
	 * @param colourOption
	 *            the chosen colour option
	 */
	public void setColourScheme(String colourOption) {
		chooseDayScheme();

		if (colourOption.equals(Common.DAY_MODE)) {
			chooseDayScheme();
		} else if (colourOption.equals(Common.NIGHT_MODE)) {
			chooseNightScheme();
		} else if (colourOption.equals(GOLDFISH_COLOUR_THEME)) {
			chooseGoldfishScheme();
		} else if (colourOption.equals(BRIGHT_COLOUR_THEME)) {
			chooseBrightScheme();
		}
	}

	/*
	 * Choose the "Bright" scheme for the application
	 */
	private void chooseBrightScheme() {
		colourScheme = ColourPalette.brightScheme;
		colourSchemeCommandLine = ColourPalette.brightSchemeSwing;
	}

	/*
	 * Choose the "Goldfish" scheme for the application
	 */
	private void chooseGoldfishScheme() {
		colourScheme = ColourPalette.goldfishScheme;
		colourSchemeCommandLine = ColourPalette.goldfishSchemeSwing;
	}

	/*
	 * Choose the "Default night" scheme for the application
	 */
	private void chooseNightScheme() {
		colourScheme = ColourPalette.defaultNightScheme;
		colourSchemeCommandLine = ColourPalette.defaultNightSchemeSwing;
	}

	/*
	 * Choose the "Default day" scheme for the application
	 */
	private void chooseDayScheme() {
		colourScheme = ColourPalette.defaultScheme;
		colourSchemeCommandLine = ColourPalette.defaultDaySchemeSwing;
	}

	
}

//@author A0098077N
/**
 * 
 * This class defines the style in the command line How the command line has
 * multi color depends on this class
 * 
 */
@SuppressWarnings("serial")
class CustomStyledDocument extends DefaultStyledDocument {
	@Override
	public void insertString(int offset, String str, AttributeSet a)
			throws BadLocationException {
		super.insertString(offset, str, a);
		setColor();
	}

	@Override
	public void remove(int offs, int len) throws BadLocationException {
		super.remove(offs, len);
		setColor();
	}

	private void setColor() throws BadLocationException {
		String text = getText(0, getLength());
		ArrayList<InfoWithIndex> infoList = Parser.parseForView(text.toLowerCase());
		for (int i = 0; i < infoList.size(); i++) {
			InfoWithIndex info = infoList.get(i);
			switch (info.getInfoType()) {
			case Common.INDEX_REDUNDANT_INFO:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[0], false);
				break;
			case Common.INDEX_COMMAND_TYPE:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[1], false);
				break;
			case Common.INDEX_WORK_INFO:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[2], false);
				break;
			case Common.INDEX_TAG:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[3], false);
				break;
			case Common.INDEX_START_DATE:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[4], false);
				break;
			case Common.INDEX_END_DATE:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[5], false);
				break;
			case Common.INDEX_IS_IMPT:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[6], false);
				break;
			case Common.INDEX_REPEATING:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[7], false);
				break;
			case Common.INDEX_INDEX_INFO:
				setCharacterAttributes(info.getStartIndex(), info.getInfo()
						.length(), View.colourSchemeCommandLine[8], false);
				break;
			}
		}
	}
};
