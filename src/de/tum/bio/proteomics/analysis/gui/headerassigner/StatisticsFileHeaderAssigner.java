package de.tum.bio.proteomics.analysis.gui.headerassigner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.tum.bio.proteomics.headers.StatisticsTableHeaders;
import de.tum.bio.utils.SeparatedTextReader;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class StatisticsFileHeaderAssigner extends Stage {
	
	private StatisticsFileHeaderAssignerController controller;
	private String[] selections = {null, null, null};
	
	public StatisticsFileHeaderAssigner(String filePath, Stage owner) {
		initOwner(owner);
		initModality(Modality.WINDOW_MODAL);
		setResizable(false);
		setTitle("Assign headers");
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("StatisticsFileHeaderAssigner.fxml"));
			BorderPane root = (BorderPane) loader.load();
			controller = loader.getController();
			controller.init(this, FXCollections.observableArrayList(SeparatedTextReader.readFirstLine(filePath)));
			
			Scene scene = new Scene(root, 600, 200);
			scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
			setScene(scene);
			
			setOnCloseRequest(event -> {
				selections = controller.getSelections();
				close();
			});
			
			showAndWait();
		} catch (IOException e) {
			Alert alert = new Alert(AlertType.ERROR, e.getMessage(), ButtonType.OK);
			alert.showAndWait();
		}
	}
	
	public Map<StatisticsTableHeaders, String> getSelections() {
		Map<StatisticsTableHeaders, String> selectionMap = new HashMap<>();
		selectionMap.put(StatisticsTableHeaders.NAME, selections[0]);
		selectionMap.put(StatisticsTableHeaders.LOG2_ENRICHMENT, selections[1]);
		selectionMap.put(StatisticsTableHeaders.MINUS_LOG10_PVALUE, selections[2]);
		if (selectionMap.get(StatisticsTableHeaders.NAME) == null) {
			return null;
		}
		return selectionMap;
	}
}