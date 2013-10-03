import java.util.Timer;
import java.util.TimerTask;

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
import javafx.scene.control.CheckBox;
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
import javafx.util.Callback;

public class View {
	public TextField commandLine;
	public Text feedback;
	public Button showOrHide;
	public TableView<Task> taskPendingList;
	public TableView<Task> taskCompleteList;
	public TableView<Task> taskTrashList;
	public Scene scene;
	public BorderPane root;
	public TabPane tabPane;
	double dragAnchorX;
	double dragAnchorY;
	VBox bottom;
	HBox center;
	AnchorPane top;

	Stage stage;

	public View(final Model model, final Stage primaryStage) {
		stage = primaryStage;
		/* Top */
		top = new AnchorPane();
		top.setPadding(new Insets(-15, 15, -30, 44));
		Image iDo = new Image(getClass().getResourceAsStream("iDo.png"), 110,
				54, true, true);
		ImageView title = new ImageView(iDo);
		title.getStyleClass().add("title");

		Button minimizeButton = new Button("");
		minimizeButton.setPrefSize(20, 20);
		minimizeButton.setId("minimize");
		minimizeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				primaryStage.setIconified(true);
			}
		});

		Button closeButton = new Button("");
		closeButton.setPrefSize(20, 20);
		closeButton.setId("close");
		closeButton.setOnMouseClicked(new EventHandler<MouseEvent>() {
			public void handle(MouseEvent e) {
				primaryStage.close();
				System.exit(0);
			}
		});
		HBox hb = new HBox();
		hb.getChildren().add(minimizeButton);
		hb.getChildren().add(closeButton);
		hb.setSpacing(10);
		hb.setAlignment(Pos.BOTTOM_CENTER);

		top.getChildren().addAll(title, hb);
		AnchorPane.setLeftAnchor(title, 10.0);
		AnchorPane.setTopAnchor(hb, 25.0);
		AnchorPane.setTopAnchor(title, 30.0);
		AnchorPane.setRightAnchor(hb, 5.0);

		/* Center */
		taskPendingList = new TableView<Task>();
		createTable(taskPendingList,
				(ObservableList<Task>) model.getPendingList());

		taskCompleteList = new TableView<Task>();
		createTable(taskCompleteList,
				(ObservableList<Task>) model.getCompleteList());

		taskTrashList = new TableView<Task>();
		createTable(taskTrashList, (ObservableList<Task>) model.getTrashList());

		tabPane = new TabPane();
		Tab pending = TabBuilder.create().content(taskPendingList)
				.text("PENDING").closable(false).build();
		Tab complete = TabBuilder.create().content(taskCompleteList)
				.text("COMPLETE").closable(false).build();
		Tab trash = TabBuilder.create().content(taskTrashList).text("TRASH")
				.closable(false).build();
		tabPane.getTabs().addAll(pending, complete, trash);

		center = HBoxBuilder.create().padding(new Insets(0, 44, 0, 44))
				.children(tabPane).build();

		/* Bottom */
		bottom = new VBox();
		bottom.setSpacing(5);
		bottom.setPadding(new Insets(0, 0, 5,44));

		HBox temp = new HBox();
		temp.setSpacing(10);
		commandLine = new TextField();
		commandLine.setPrefWidth(670);
		showOrHide = new Button();
		showOrHide.setPrefSize(30, 30);
		showOrHide.setId("smaller");
		hookUpEventForCheckBox();

		temp.getChildren().addAll(commandLine, showOrHide);

		feedback = TextBuilder.create().styleClass("feedback")
				.fill(Color.WHITE).text("Please enter a command").build();
		bottom.getChildren().addAll(temp, feedback);

		root = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();

		setDraggable(root, primaryStage);

		scene = new Scene(root, Color.rgb(70, 70, 70));
		scene.getStylesheets().addAll(
				getClass().getResource("customize.css").toExternalForm());
	}

	public void setDraggable(BorderPane root, final Stage primaryStage) {
		// Get the position of the mouse in the stage
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - primaryStage.getX();
				dragAnchorY = me.getScreenY() - primaryStage.getY();
			}
		});

		// Moving with the stage with the mouse at constant position relative to
		// the stage
		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				primaryStage.setX(me.getScreenX() - dragAnchorX);
				primaryStage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}

	public void createTable(TableView<Task> taskList, ObservableList<Task> list) {
		taskList.setItems(list);

		TableColumn<Task, String> taskColumn = TableColumnBuilder
				.<Task, String> create().text("Task")
				.cellValueFactory(new PropertyValueFactory("workInfo"))
				.sortable(false).prefWidth(300).build();
		taskColumn
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

		TableColumn<Task, Tag> tag = TableColumnBuilder.<Task, Tag> create()
				.text("Tag").cellValueFactory(new PropertyValueFactory("tag"))
				.sortable(false).prefWidth(120).build();
		tag.setCellFactory(new Callback<TableColumn<Task, Tag>, TableCell<Task, Tag>>() {

			@Override
			public TableCell<Task, Tag> call(TableColumn<Task, Tag> param) {
				TableCell<Task, Tag> tc = new TableCell<Task, Tag>() {
					public void updateItem(Tag item, boolean empty) {
						if (item != null) {
							String text;
							if (item.getRepetition().equals(Parser.NULL))
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

		TableColumn<Task, String> startDate = TableColumnBuilder
				.<Task, String> create().text("Start").prefWidth(120)
				.sortable(false).build();
		startDate.setCellValueFactory(new PropertyValueFactory(
				"startDateString"));
		startDate
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

		TableColumn<Task, String> endDate = TableColumnBuilder
				.<Task, String> create().text("End").sortable(false)
				.prefWidth(120).build();
		endDate.setCellValueFactory(new PropertyValueFactory("endDateString"));
		endDate.setCellFactory(new Callback<TableColumn<Task, String>, TableCell<Task, String>>() {

			@Override
			public TableCell<Task, String> call(TableColumn<Task, String> param) {
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

		TableColumn<Task, Boolean> isImportant = TableColumnBuilder
				.<Task, Boolean> create().visible(true).prefWidth(10).build();
		isImportant
				.setCellValueFactory(new PropertyValueFactory("isImportant"));
		isImportant
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

		taskList.setStyle("-fx-table-cell-border-color: transparent;");
		taskList.getColumns().addAll(taskColumn, startDate, endDate, tag,
				isImportant);
	}

	private void hookUpEventForCheckBox() {
		showOrHide.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent e) {
				if (stage.getHeight() > 500) {
					root.setTop(null);
					root.setCenter(null);
					showOrHide.setId("larger");
					collapseAnimation();
				} else {
					showOrHide.setId("smaller");
					expandAnimation();
				}
			}
		});
	}

	private void collapseAnimation() {
		Timer animTimer = new Timer();

		animTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (stage.getHeight() != 70) {
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
				if (stage.getHeight() > 450) {
					Platform.runLater(new Runnable() {
						public void run() {
							root.setCenter(center);
						}
					});
				}

				if (stage.getHeight() < 540) {
					stage.setHeight(stage.getHeight() + 10);
				} else {
					this.cancel();
				}
			}
		}, 0, 3);
	}
}
