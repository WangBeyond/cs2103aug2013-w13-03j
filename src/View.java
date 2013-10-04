import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderPaneBuilder;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HBoxBuilder;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;

public class View {
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

		hideInSystemTray();
		Platform.setImplicitExit(false);
		createContent();
		setDraggable();
		setupScene();
	}

	private void createContent() {
		createTopSection();
		createCenterSection();
		createBottomSection();

		root = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();
	}

	private void setupScene() {
		scene = new Scene(root, Color.rgb(70, 70, 70));
		customizeGUIWithCSS();
	}

	private void customizeGUIWithCSS() {
		scene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
	}

	private void createBottomSection() {
		bottom = new VBox();
		bottom.setSpacing(5);
		bottom.setPadding(new Insets(0, 0, 5, 44));

		HBox upperPart = createUpperPartInBottom();

		feedback = TextBuilder.create().styleClass("feedback")
				.fill(Color.WHITE).text("Please enter a command").build();

		bottom.getChildren().addAll(upperPart, feedback);
	}

	private HBox createUpperPartInBottom() {
		HBox temp = new HBox();
		temp.setSpacing(10);

		commandLine = new TextField();
		commandLine.setPrefWidth(670);

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
		Image iDo = new Image(getClass().getResourceAsStream("iDo.png"), 110,
				54, true, true);
		ImageView title = new ImageView(iDo);
		title.getStyleClass().add("title");
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
				System.exit(0);
			}
		});
		return targetButton;
	}

	public void createTable(TableView<Task> taskList, ObservableList<Task> list) {
		taskList.setItems(list);

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
										text = item.getTag() + "\n#"
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
					removeTopAndCenter();
					showOrHide.setId("larger");
					collapseAnimation();
				} else {
					showOrHide.setId("smaller");
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
		trayIcon = new TrayIcon(iconImage, "iDo", popupMenu);
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
				System.exit(0);
			}
		};
	}

	private java.awt.Image getIconImage() {
		try {
			java.awt.Image image = ImageIO.read(getClass().getResource(
					"close.png"));
			return image;
		} catch (IOException e) {
			System.out.println(e);
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
					System.exit(0);
				}
			}
		});
	}
}
