import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Help {

	private Scene helpScene;
	private Stage helpStage;
	private StackPane root;
	
	public Help(){
		setupStage();
		root = new StackPane();
		root.getChildren().add(getHelpImage());
		helpScene = new Scene(root, 600, 730);
		helpStage.setScene(helpScene);
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
}
