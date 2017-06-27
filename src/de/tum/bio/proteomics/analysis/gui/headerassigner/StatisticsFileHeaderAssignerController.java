package de.tum.bio.proteomics.analysis.gui.headerassigner;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class StatisticsFileHeaderAssignerController {
	
	private Stage stage;
	private String[] selections = {null, null, null};
	
	@FXML
	private ChoiceBox<String> choiceId;
	@FXML
	private ChoiceBox<String> choiceEnrichment;
	@FXML
	private ChoiceBox<String> choicePValue;
	
	@FXML
	private Button buttonOK;
	@FXML
	private Button buttonCancel;
	
	public void init(Stage stage, ObservableList<String> fileHeaders) {
		this.stage = stage;
		choiceId.setItems(fileHeaders);
		choiceEnrichment.setItems(fileHeaders);
		choicePValue.setItems(fileHeaders);
	}
	
	@FXML
	protected void handleButtonAction(ActionEvent event) {
		if (event.getSource().equals(buttonOK)) {
			initSelections();
		}
		if (event.getSource().equals(buttonCancel)) {
			// empty
		}
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}
	
	public void initSelections() {
		if (!choiceId.getSelectionModel().isEmpty()) {
			selections[0] = choiceId.getSelectionModel().getSelectedItem().toString();
		}
		if (!choiceEnrichment.getSelectionModel().isEmpty()) {
			selections[1] = choiceEnrichment.getSelectionModel().getSelectedItem().toString();
		}
		if (!choicePValue.getSelectionModel().isEmpty()) {
			selections[2] = choicePValue.getSelectionModel().getSelectedItem().toString();
		}
	}
	
	public String[] getSelections() {
		return selections;
	}
}