package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import de.tum.bio.proteomics.Toolbox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class AaProfileViewController {

	private Stage stage;
	private AaProfiler aaProfiler;
	
	@FXML
	Label labelProteinId;
	@FXML
	ChoiceBox<String> choiceAminoAcid;
	@FXML
	Button buttonCorrelation;
	
	@FXML
	LineChart<String, Double> chart;
	@FXML
	CategoryAxis xAxis;
	@FXML
	NumberAxis yAxis;
	
	@FXML
	VBox vBoxContent;
	
	
	WebView webViewPearson = new WebView();
	boolean webViewPearsonAdded = false;
	
	public void init(Stage stage) {
		this.stage = stage;
		
		xAxis.setLabel("Experiment");
		yAxis.setLabel("log2(intensity)");
		
		LinkedList<String> residues = new LinkedList<>(Arrays.asList(Toolbox.aminoAcidsSingleLetter()));
		residues.addFirst("All");
		
		choiceAminoAcid.setItems(FXCollections.observableArrayList(residues));
		choiceAminoAcid.getSelectionModel().select(0);
		choiceAminoAcid.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
			if (n == "All") {
				buttonCorrelation.setDisable(true);
				updateChart(null);
			} else {
				buttonCorrelation.setDisable(false);
				updateChart(getAaListFromString(n));
			}
		});
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void setAaProfiler(AaProfiler aaProfiler) {
		this.aaProfiler = aaProfiler;
		
		choiceAminoAcid.getSelectionModel().select("K");
		updateChart(getAaListFromString("K"));
		
		labelProteinId.setText(aaProfiler.getProteinIds());
	}
	
	public void computeCorrelationMatrix() {
		webViewPearson.getEngine().loadContent(aaProfiler.getCorrelationMatrixAsHTML(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem())), "text/html");
		if (!webViewPearsonAdded) {
			vBoxContent.getChildren().add(webViewPearson);
			webViewPearsonAdded = true;
		}
	}
	
	private void updateChart(List<Character> residues) {
		yAxis.setForceZeroInRange(false);
		if (residues == null) {
			chart.setData(aaProfiler.getAllXYChartSeries());
			if (webViewPearsonAdded) {
				vBoxContent.getChildren().remove(webViewPearson);
				webViewPearsonAdded = false;
			}
		} else {
			chart.setData(aaProfiler.getAllXYChartSeriesByResidue(residues));
			if (webViewPearsonAdded) {
				computeCorrelationMatrix();
			}
		}
	}
	
	private List<Character> getAaListFromString(String string) {
		List<Character> list = new ArrayList<>();
		
		list.add(string.charAt(0));
		
		return list;
	}
	
}