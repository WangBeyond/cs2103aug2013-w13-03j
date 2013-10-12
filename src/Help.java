import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

// TODO: close function, show the alternate time formats
public class Help {

	private Scene helpScene;
	private Stage helpStage;
	private StackPane root;
	
	double dragAnchorX;
	double dragAnchorY;
	
	public Help(){
		setupStage();
		root = new StackPane();
		root.getChildren().add(getHelpImage());
		helpScene = new Scene(root, 600, 730);
		helpStage.setScene(helpScene);
		setupDraggable();
	}
	
	public void showHelpPage(){
		helpStage.show();
	}
	
	private void setupStage(){
		helpStage = new Stage();
		helpStage.initStyle(StageStyle.UNDECORATED);
		helpStage.setWidth(600);
		helpStage.setHeight(730);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		helpStage.setX((screenBounds.getWidth() - helpStage.getWidth()) / 2);
		helpStage.setY((screenBounds.getHeight() - helpStage.getHeight()) / 2);
	}
	
	private ImageView getHelpImage(){
		Image helpImage = new Image(getClass().getResourceAsStream("help.png"));
		ImageView helpPage = new ImageView();
		helpPage.setImage(helpImage);
		helpPage.setFitWidth(600);
		helpPage.setPreserveRatio(true);
		helpPage.setSmooth(true);
		helpPage.setCache(true);
		
		return helpPage;
	}
	
	private void setupDraggable() {
		root.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				dragAnchorX = me.getScreenX() - helpStage.getX();
				dragAnchorY = me.getScreenY() - helpStage.getY();
			}
		});

		root.setOnMouseDragged(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent me) {
				helpStage.setX(me.getScreenX() - dragAnchorX);
				helpStage.setY(me.getScreenY() - dragAnchorY);
			}
		});
	}
}
