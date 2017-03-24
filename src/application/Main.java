package application;
	
import java.util.Optional;
import java.util.ResourceBundle;

import de.tum.bio.analysis.AnalysisHandler;
import de.tum.bio.proteomics.ProteinGroup;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;


public class Main extends Application {
	
	private Stage primaryStage;
	private MainController controller;
	@SuppressWarnings("unused")
	private AnalysisHandler analysisHandler;
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			this.primaryStage = primaryStage;
			primaryStage.setTitle("ProteomeDiver");
			
			// Load fonts
	        Font.loadFont(Main.class.getResourceAsStream("/fontawesome-webfont.ttf"), 10);

			FXMLLoader loader = new FXMLLoader(getClass().getResource("Main.fxml"));
			loader.setResources(ResourceBundle.getBundle("fontawesome"));
			BorderPane root = (BorderPane) loader.load();
			Scene scene = new Scene(root, 1500, 900);
			scene.getStylesheets().add(getClass().getResource("main.css").toExternalForm());
			
			// Generate a singleton of analysis handler
			analysisHandler = AnalysisHandler.getInstance();
			
			primaryStage.setScene(scene);
			primaryStage.setMaximized(true);
			
			// Link to controller
			controller = loader.getController();
			controller.init(this);
			
			// Register handler on close request to ask the user if this is ok
			primaryStage.setOnCloseRequest(event -> {
				if (!shutdown()) {
					event.consume();
				}
			});
			
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * User dialog asking if program can be closed.
	 * @return yes/no
	 */
	private boolean shutdown() {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Close program");
		alert.setHeaderText("ProteomeDiver is going to close.");
		alert.setContentText("Are you ok with this?");
		Optional<ButtonType> result = alert.showAndWait();
		if (result.get() == ButtonType.OK) {
		    return true;
		} else {
		    return false;
		}
	}
	
	/**
	 * Gets the primary stage.
	 * @return primary stage
	 */
	public Stage getStage() {
		return primaryStage;
	}
	
	public ProgressBar getProgressBar() {
		return controller.getProgressBar();
	}
	
	public Label getStatusLabel() {
		return controller.getStatusLabel();
	}
	
	public TableView<ProteinGroup> getProteinGroupsTableView() {
		return controller.getProteinGroupsTableView();
	}
}