package application.about;

import java.io.IOException;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AboutDialog extends Stage {
	
	private Application mainApp;
	private AboutDialogController controller;
	
	public AboutDialog(Application mainApp, Stage owner) {
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		setResizable(false);
		setTitle("About ProteomeDiver");
	}
	
	public void present() {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AboutDialog.fxml"));
			BorderPane root = (BorderPane) loader.load();
			controller = loader.getController();
			controller.init(mainApp, this);
			
			Scene scene = new Scene(root, 600, 400);
			scene.getStylesheets().add(getClass().getResource("/css/about.css").toExternalForm());
			setScene(scene);
			
			show();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
			alert.showAndWait();
		}
	}
	
	public AboutDialogController getController() {
		return controller;
	}
}
