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
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AaProfileViewController {

	private Stage stage;
	private AaProfiler aaProfiler;
	
	final double SCALE_DELTA = 1.1;
	
	@FXML
	Label labelProteinId;
	@FXML
	ChoiceBox<String> choiceAminoAcid;
	@FXML
	Spinner<Integer> spinnerOffset;
	@FXML
	TextField numberOfClusters;
	
	@FXML
	TabPane tabPane;
	
	@FXML
	LineChart<String, Double> chart;
	@FXML
	CategoryAxis xAxis;
	@FXML
	NumberAxis yAxis;
	@FXML
	LineChart<String, Double> chartNormalized;
	@FXML
	CategoryAxis xAxisNormalized;
	@FXML
	NumberAxis yAxisNormalized;
	
	
	@FXML
	VBox vBoxContent;
	@FXML
	VBox vBoxCorrelationMatrix;
	@FXML
	LineChart<Integer, Double> chartRankedCorrelations;
	@FXML
	NumberAxis xAxisRankedCorrelation;
	@FXML
	NumberAxis yAxisRankedCorrelation;
	
	@FXML
	Label statusMessage;
	@FXML
	ProgressBar progressBar;
	
	public void init(Stage stage) {
		this.stage = stage;
		
		statusMessage.setText("Initializing...");

		xAxis.setLabel("Experiment");
		yAxis.setLabel("log2(intensity)");
		xAxisNormalized.setLabel("Experiment");
		yAxisNormalized.setLabel("Normalized log2(intensity)");
		xAxisRankedCorrelation.setLabel("Rank");
		yAxisRankedCorrelation.setLabel("Correlation");
		
		LinkedList<String> residues = new LinkedList<>(Arrays.asList(Toolbox.aminoAcidsSingleLetter()));
		residues.addFirst("All");
		
		choiceAminoAcid.setItems(FXCollections.observableArrayList(residues));
		choiceAminoAcid.getSelectionModel().select("K");
		
		spinnerOffset.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-1, 1));
		spinnerOffset.getValueFactory().setValue(0);
	}
	
	public Stage getStage() {
		return stage;
	}
	
	public void setAaProfiler(AaProfiler aaProfiler) {
		this.aaProfiler = aaProfiler;
		
		labelProteinId.setText(aaProfiler.getProteinIds());
		statusMessage.textProperty().bind(aaProfiler.statusProperty());
		progressBar.progressProperty().bind(aaProfiler.progressProperty());
		
		aaProfiler.init();
		
		aaProfiler.readyProperty().addListener(event -> {
			choiceAminoAcid.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
				if (n == "All") {
					update(null, 0);
				} else {
					update(getAaListFromString(n), spinnerOffset.getValue());
				}
			});
			
			spinnerOffset.valueProperty().addListener((obs, o, n) -> {
				if (choiceAminoAcid.getSelectionModel().getSelectedItem() == "All") {
					update(null, 0);
				} else {
					update(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), n);
				}
			});
			
			update(getAaListFromString("K"), spinnerOffset.getValue());
			chart.setData(aaProfiler.getSeries());
			chartNormalized.setData(aaProfiler.getNormalizedSeries());
			chartRankedCorrelations.setData(aaProfiler.getCorrelations());
		});
	}
	
	public void cluster() {
		
	}
	
	private void update(List<Character> residues, int offset) {
		yAxis.setForceZeroInRange(false);
		yAxisNormalized.setForceZeroInRange(false);
		if (residues == null) {
			aaProfiler.getAllXYChartSeries();
			aaProfiler.getAllNormalizedXYChartSeries();
			showRankedCorrelations();
		} else {
			aaProfiler.calculateAllXYChartSeriesByResidue(residues, offset);
			aaProfiler.calculateAllNormalizedXYChartSeriesByResidue(residues, offset);
			showRankedCorrelations();
		}
	}
	
	private void showRankedCorrelations() {
		aaProfiler.calculateAllRankedCorrelationSeriesByResidue(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), spinnerOffset.getValue());
	}
	
	private List<Character> getAaListFromString(String string) {
		List<Character> list = new ArrayList<>();
		if (string == "All") {
			for (String entry : Toolbox.aminoAcidsSingleLetter()) {
				list.add(entry.charAt(0));
			}
		} else {
			list.add(string.charAt(0));
		}
		return list;
	}
	
}