package de.tum.bio.sequenceviewer.aaintensityprofiles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.controlsfx.control.CheckListView;

import de.tum.bio.proteomics.tools.Toolbox;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ProfileViewController {

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
	Button buttonUpdateExperiments;
	@FXML
	VBox vBoxExperiments;
	
	@FXML
	VBox vBoxContent;
	@FXML
	VBox vBoxCorrelationMatrix;
	@FXML
	LineChart<Integer, Double> chartRankedPearsonCorrelations;
	@FXML
	NumberAxis xAxisRankedPearsonCorrelation;
	@FXML
	NumberAxis yAxisRankedPearsonCorrelation;
	@FXML
	LineChart<Integer, Double> chartRankedSpearmanCorrelations;
	@FXML
	NumberAxis xAxisRankedSpearmanCorrelation;
	@FXML
	NumberAxis yAxisRankedSpearmanCorrelation;
	
	@FXML
	Label statusMessage;
	@FXML
	ProgressBar progressBar;
	
	private CheckListView<String> listViewExperiments;
	
	public void init(Stage stage) {
		this.stage = stage;

		statusMessage.setText("Initializing...");

		xAxis.setLabel("Experiment");
		yAxis.setLabel("log2(intensity)");
		xAxisNormalized.setLabel("Experiment");
		yAxisNormalized.setLabel("Normalized log2(intensity)");
		xAxisRankedPearsonCorrelation.setLabel("Rank");
		yAxisRankedPearsonCorrelation.setLabel("Pearson correlation");
		xAxisRankedSpearmanCorrelation.setLabel("Rank");
		yAxisRankedSpearmanCorrelation.setLabel("Spearman correlation");
		
		LinkedList<String> residues = new LinkedList<>(Arrays.asList(Toolbox.aminoAcidsSingleLetter()));
		residues.addFirst("All");
		
		choiceAminoAcid.setItems(FXCollections.observableArrayList(residues));
		choiceAminoAcid.getSelectionModel().select("All");
		
		spinnerOffset.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(-10, 10));
		spinnerOffset.getValueFactory().setValue(0);
		
		listViewExperiments = new CheckListView<>();
		listViewExperiments.getStyleClass().add("listViewExperiments");
		vBoxExperiments.getChildren().add(listViewExperiments);
		VBox.setVgrow(listViewExperiments, Priority.ALWAYS);
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
		
		listViewExperiments.setItems(aaProfiler.getExperiments());
		for (String item : listViewExperiments.getItems()) {
			if (!item.contains("DMSO")) {
				listViewExperiments.getCheckModel().check(item);
			}
		}
		aaProfiler.setSelectedExperiments(listViewExperiments.getCheckModel().getCheckedItems());
		
		aaProfiler.readyProperty().addListener(event -> {
			choiceAminoAcid.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
				if (n == "All") {
					update(null);
				} else {
					update(getAaListFromString(n));
				}
			});
			
			spinnerOffset.valueProperty().addListener((obs, o, n) -> {
				if (choiceAminoAcid.getSelectionModel().getSelectedItem() == "All") {
					update(null);
				} else {
					update(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()));
				}
			});
			
			update(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()));
			chart.setData(aaProfiler.getSeries());
			chartNormalized.setData(aaProfiler.getNormalizedSeries());
			chartRankedPearsonCorrelations.setData(aaProfiler.getPearsonCorrelations());
			chartRankedSpearmanCorrelations.setData(aaProfiler.getSpearmanCorrelations());
		});
	}
	
	public void cluster() {
		
	}
	
	private void update(List<Character> residues) {
		yAxis.setForceZeroInRange(false);
		yAxisNormalized.setForceZeroInRange(false);
		if (residues == null) {
			aaProfiler.getAllXYChartSeries();
			aaProfiler.getAllNormalizedXYChartSeries();
			showRankedCorrelations();
		} else {
			aaProfiler.calculateAllXYChartSeriesByResidue(residues, spinnerOffset.getValue());
			aaProfiler.calculateAllNormalizedXYChartSeriesByResidue(residues, spinnerOffset.getValue());
			showRankedCorrelations();
		}
	}
	
	private void showRankedCorrelations() {
		aaProfiler.updateCorrelationsByExperiments(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), spinnerOffset.getValue());
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
	
	public void updateExperiments() {
		aaProfiler.updateCorrelationsByExperiments(getAaListFromString(choiceAminoAcid.getSelectionModel().getSelectedItem()), spinnerOffset.getValue());
		showRankedCorrelations();
	}
}