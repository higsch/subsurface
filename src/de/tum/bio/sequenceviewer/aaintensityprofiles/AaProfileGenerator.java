package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.io.IOException;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class AaProfileGenerator extends Stage {
	
	private AaProfileViewController controller;
	
	public static void make(Map<String, Map<Integer, Long>> experimentIntensityMap, String sequence) {
		
	}
	
	private void showWindow(AaProfileContainer aaProfileContainer) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AaProfileView.fxml"));
			BorderPane root = (BorderPane) loader.load();
			controller = loader.getController();
			// some calls in controller?
			
			Scene scene = new Scene(root, 600, 200);
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			setScene(scene);
			
			setOnCloseRequest(event -> {
				// Get data from controller here
				close();
			});
			
			showAndWait();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
			alert.showAndWait();
		}
	}

}
