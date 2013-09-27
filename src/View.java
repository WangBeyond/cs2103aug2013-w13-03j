import javafx.collections.ObservableList;
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
import javafx.scene.control.TableRow;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBuilder;
import javafx.stage.Stage;
import javafx.util.Callback;

public class View {
	public TextField commandLine;
	public Text feedback;
	public Text title;
	public TableView<Task> taskPendingList;
	public TableView<Task> taskCompleteList;
	public TableView<Task> taskTrashList;
	public Scene scene;
	public TabPane tabPane;
	double dragAnchorX;
	double dragAnchorY;
	
	public View(final Model model, final Stage primaryStage) {
		/* Bottom */
		VBox bottom = new VBox();
		bottom.setSpacing(5);
		bottom.setPadding(new Insets(10, 30, 20, 30));
		commandLine = new TextField();
		commandLine.setMaxWidth(600);
		feedback = TextBuilder.create().styleClass("feedback").fill(Color.WHITE)
				.text("Please enter a command").build();
		bottom.getChildren().addAll(commandLine, feedback);

		/* Top */
		AnchorPane top = new AnchorPane();
		top.setPadding(new Insets(30, 30, 5, 30));
		Image iDo = new Image(getClass().getResourceAsStream("iDo.png"), 110, 54, true, true);
		ImageView title = new ImageView(iDo);
		
		/* Buttons */
		Image minimize = new Image(getClass().getResourceAsStream("minimise.png"), 26, 26, true, true);
		Button minimizeButton = new Button("", new ImageView(minimize));
		minimizeButton.setStyle("-fx-background-color:transparent");
		Image cross = new Image(getClass().getResourceAsStream("close.png"), 26, 26, true, true);
		Button closeButton = new Button("", new ImageView(cross));
		closeButton.setStyle("-fx-background-color:transparent");
//		closeButton.cancelButtonProperty();
		HBox hb = new HBox();
		hb.getChildren().add(minimizeButton);
		hb.getChildren().add(closeButton);
		hb.setAlignment(Pos.BOTTOM_CENTER);
		
		top.getChildren().addAll(title, hb);
		AnchorPane.setLeftAnchor(title, 10.0);
		AnchorPane.setTopAnchor(hb, 0.0);
		AnchorPane.setTopAnchor(title, 0.0);
		AnchorPane.setRightAnchor(hb, -5.0); 

		/* Center */
		taskPendingList = new TableView<Task>();
		createTable(taskPendingList, (ObservableList<Task>) model.getPendingList());

		taskCompleteList = new TableView<Task>();
		createTable(taskCompleteList, (ObservableList<Task>) model.getCompleteList());

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

		HBox center = HBoxBuilder.create().padding(new Insets(0, 30, -20, 30))
				.children(tabPane).build();

		BorderPane root = BorderPaneBuilder.create().top(top).center(center)
				.bottom(bottom).build();

		setDraggable(root, primaryStage);

		scene = new Scene(root, 730, 530, Color.rgb(45, 45, 48));
		scene.getStylesheets().addAll(getClass().getResource("customize.css").toExternalForm());
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

		TableColumn<Task, Tag> tag = TableColumnBuilder
				.<Task, Tag> create().text("Tag")
				.cellValueFactory(new PropertyValueFactory("tag"))
				.sortable(false).prefWidth(100).build();
		tag.setCellFactory(new Callback<TableColumn<Task, Tag>, TableCell<Task, Tag>>() {

			@Override
			public TableCell<Task, Tag> call(TableColumn<Task, Tag> param) {
				TableCell<Task, Tag> tc = new TableCell<Task, Tag>() {
					public void updateItem(Tag item, boolean empty) {
						if (item != null){
							String text;
							if(item.getRepetition().equals("-"))
								text = item.getTag();
							else
								text = item.getTag() + "\n#" + item.getRepetition();
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
		
		TableColumn<Task, Boolean> isImportant = TableColumnBuilder.<Task, Boolean> create().visible(true).prefWidth(20).build();
		isImportant.setCellValueFactory(new PropertyValueFactory("isImportant"));
		isImportant.setCellFactory(new Callback<TableColumn<Task,Boolean>, TableCell<Task,Boolean>>() {
			
			@Override
			public TableCell<Task, Boolean> call(TableColumn<Task, Boolean> param) {
				TableCell<Task, Boolean> tc = new TableCell<Task, Boolean>(){
					@Override
					public void updateItem(Boolean item, boolean empty){
						super.updateItem(item, empty);
						if(item != null){
							clearStyle();
							setStyle(item);
						}
					}
					
					private void clearStyle(){
						getTableRow().getStyleClass().removeAll("table-row-cell", "unimportant", "important");
					}
					
					private void setStyle(boolean isImportant){
						if(isImportant)
							getTableRow().getStyleClass().add("important");
						else
							getTableRow().getStyleClass().add("unimportant");
					}
				};
				return tc;
			}
		});
		
		
		taskList.setStyle("-fx-table-cell-border-color: transparent;");
		taskList.getColumns().addAll(taskColumn, startDate, endDate, tag, isImportant);
	}
}
