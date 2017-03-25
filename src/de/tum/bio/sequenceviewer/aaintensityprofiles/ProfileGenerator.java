package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import de.tum.bio.proteomics.ProteinGroup;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class ProfileGenerator extends Stage {

	private ProfileViewController controller;
	
	public ProfileGenerator(Stage owner) {
		initOwner(owner);
	}
	
	public void make(Map<String, Map<Integer, Long>> experimentIntensityMap, ProteinGroup protein) {
		AaProfiler container = new AaProfiler(protein);
		
		Map<Integer, Map<String, Long>> profileMap = new HashMap<>();
		
		for (Entry<String, Map<Integer, Long>> experimentEntry : experimentIntensityMap.entrySet()) {
			for (Entry<Integer, Long> positionEntry : experimentEntry.getValue().entrySet()) {
				if (!profileMap.containsKey(positionEntry.getKey())) {
					profileMap.put(positionEntry.getKey(), new HashMap<>());
				}
				Long intensity;
				if (positionEntry.getValue() == 0.0) {
					intensity = null;
				} else {
					intensity = positionEntry.getValue();
				}
				profileMap.get(positionEntry.getKey()).put(experimentEntry.getKey(), intensity);
			}
		}
	
		container.addProfileMap(profileMap);
		showWindow(container);
	}
	
	private void showWindow(AaProfiler aaProfiler) {
		try {
			//Font.loadFont(AaProfileGenerator.class.getResourceAsStream("/fontawesome-webfont.ttf"), 10);
			
			setTitle("Profile view of " + aaProfiler.getProteinIds());
			
			FXMLLoader loader = new FXMLLoader(getClass().getResource("ProfileView.fxml"));
			loader.setResources(ResourceBundle.getBundle("fontawesome"));
			BorderPane root = (BorderPane) loader.load();
			controller = loader.getController();
			controller.init(this);
			
			Scene scene = new Scene(root, 1200, 900);
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			setScene(scene);
			
			setOnCloseRequest(event -> {
				// Get data from controller here
				close();
			});
			
			setAlwaysOnTop(true);
			
			setOnShown(event -> {
				controller.setAaProfiler(aaProfiler);
			});
			
			show();
			
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
			alert.showAndWait();
		}
	}

}
