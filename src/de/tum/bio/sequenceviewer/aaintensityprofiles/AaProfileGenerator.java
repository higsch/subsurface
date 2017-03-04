package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class AaProfileGenerator extends Stage {

	private AaProfileViewController controller;
	
	public AaProfileGenerator(Stage owner) {
		initOwner(owner);
	}
	
	public void make(Map<String, Map<Integer, Long>> experimentIntensityMap, String sequence) {
		AaProfileContainer container = new AaProfileContainer(sequence);
		
		Map<Integer, Map<String, Long>> profileMap = new HashMap<>();
		
		for (Entry<String, Map<Integer, Long>> experimentEntry : experimentIntensityMap.entrySet()) {
			for (Entry<Integer, Long> positionEntry : experimentEntry.getValue().entrySet()) {
				if (!profileMap.containsKey(positionEntry.getKey())) {
					profileMap.put(positionEntry.getKey(), new HashMap<>());
				}
				profileMap.get(positionEntry.getKey()).put(experimentEntry.getKey(), positionEntry.getValue());
			}
		}
	
		container.addProfileMap(profileMap);
		showWindow(container);
	}
	
	private void showWindow(AaProfileContainer aaProfileContainer) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AaProfileView.fxml"));
			BorderPane root = (BorderPane) loader.load();
			controller = loader.getController();
			controller.setAaProfileContainer(aaProfileContainer);
			
			Scene scene = new Scene(root, 600, 200);
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			setScene(scene);
			
			setOnCloseRequest(event -> {
				// Get data from controller here
				close();
			});
			
			initModality(Modality.APPLICATION_MODAL); 
			showAndWait();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
			alert.showAndWait();
		}
	}

}
