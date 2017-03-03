package application;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.xmlpull.v1.XmlPullParserException;

import com.compomics.util.experiment.biology.Enzyme;
import com.compomics.util.experiment.biology.EnzymeFactory;

import de.tum.bio.analysis.Analysis;
import de.tum.bio.analysis.AnalysisComponent;
import de.tum.bio.analysis.AnalysisComponentType;
import de.tum.bio.analysis.AnalysisHandler;
import de.tum.bio.analysis.VolcanoPlotDot;
import de.tum.bio.proteomics.FastaFile;
import de.tum.bio.proteomics.Peptide;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.ProteinGroupsTableHeaders;
import de.tum.bio.proteomics.StatisticsFile;
import de.tum.bio.proteomics.StatisticsTableHeaders;
import de.tum.bio.proteomics.Tools;
import de.tum.bio.sequenceviewer.SequenceViewer;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.StringConverter;

public class MainController {
	
	private Main mainApp;
	private Stage stage;
	private AnalysisHandler analysisHandler = AnalysisHandler.getInstance();
	
	private SequenceViewer sequenceViewer;
	
	private EnzymeFactory enzymes = EnzymeFactory.getInstance();
	
	@FXML
	MenuItem menuItemOpenMaxQuant;
	@FXML
	MenuItem menuItemOpenPerseus;
	@FXML
	MenuItem menuItemOpenFasta;
	
	@FXML
	Button buttonOpenMaxQuant;
	@FXML
	Button buttonOpenPerseus;
	@FXML
	Button buttonOpenFasta;
	@FXML
	Button buttonCombineSequences;
	@FXML
	Button buttonCombineStatistics;
	
	
	@FXML
	TreeView<? extends AnalysisComponent> treeView;
	@FXML
	TableView<ProteinGroup> tableProteinGroups;
	@FXML
	TableView<Peptide> tablePeptides;
	@FXML
	ScatterChart<Number, Number> volcanoPlot;
	@FXML
	NumberAxis volcanoPlotXAxis;
	@FXML
	NumberAxis volcanoPlotYAxis;

	@FXML
	VBox sequenceViewParent;
	@FXML
	Label sequenceViewHeading;
	@FXML
	ChoiceBox<Enzyme> choiceEnzyme;
	
	@FXML
	Label statusLabel;
	@FXML
	ProgressBar progressBar;
	
	@FXML
	Label infoLabel;
	
	@SuppressWarnings("unchecked")
	public void init(Main mainApp) {
		this.mainApp = mainApp;
		stage = mainApp.getStage();
		analysisHandler.setController(this);
		treeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		
		// Initialize protein groups table
		TableColumn<ProteinGroup, String> colProteinGroupsDatabaseIds = new TableColumn<>("IDs");
        colProteinGroupsDatabaseIds.setCellValueFactory(new PropertyValueFactory<ProteinGroup, String>("databaseIds"));
        TableColumn<ProteinGroup, String> colProteinGroupsNames = new TableColumn<>("Names");
        colProteinGroupsNames.setCellValueFactory(new PropertyValueFactory<ProteinGroup, String>("names"));
        TableColumn<ProteinGroup, String> colProteinGroupsGeneNames = new TableColumn<>("Gene names");
        colProteinGroupsGeneNames.setCellValueFactory(new PropertyValueFactory<ProteinGroup, String>("geneNames"));
        TableColumn<ProteinGroup, String> colProteinGroupsSequence = new TableColumn<>("Sequence");
        colProteinGroupsSequence.setCellValueFactory(new PropertyValueFactory<ProteinGroup, String>("sequence"));
        TableColumn<ProteinGroup, Integer> colProteinGroupsLength = new TableColumn<>("Length");
        colProteinGroupsLength.setCellValueFactory(new PropertyValueFactory<ProteinGroup, Integer>("length"));
        TableColumn<ProteinGroup, Double> colProteinGroupsSequenceCoverage = new TableColumn<>("Sequence coverage [%]");
        colProteinGroupsSequenceCoverage.setCellValueFactory(new PropertyValueFactory<ProteinGroup, Double>("sequenceCoverage"));
        TableColumn<ProteinGroup, Double> colProteinGroupsEnrichment = new TableColumn<>("Log2 enrichment");
        colProteinGroupsEnrichment.setCellValueFactory(new PropertyValueFactory<ProteinGroup, Double>("log2Enrichment"));
        TableColumn<ProteinGroup, Double> colProteinGroupsPValue = new TableColumn<>("-Log10 p-value");
        colProteinGroupsPValue.setCellValueFactory(new PropertyValueFactory<ProteinGroup, Double>("minusLog10PValue"));
        tableProteinGroups.getColumns().addAll(colProteinGroupsDatabaseIds, colProteinGroupsNames, colProteinGroupsGeneNames, colProteinGroupsSequence, colProteinGroupsLength, colProteinGroupsSequenceCoverage, colProteinGroupsEnrichment, colProteinGroupsPValue);
		
        // Initialize peptides table
        TableColumn<Peptide, String> colPeptidesSequence = new TableColumn<>("Sequence");
        colPeptidesSequence.setMinWidth(150.0);
        colPeptidesSequence.setCellValueFactory(new PropertyValueFactory<Peptide, String>("sequence"));
        TableColumn<Peptide, Integer> colPeptidesStartPosition = new TableColumn<>("Start");
        colPeptidesStartPosition.setCellValueFactory(new PropertyValueFactory<Peptide, Integer>("startPosition"));
        TableColumn<Peptide, Integer> colPeptidesEndPosition = new TableColumn<>("End");
        colPeptidesEndPosition.setCellValueFactory(new PropertyValueFactory<Peptide, Integer>("endPosition"));
        TableColumn<Peptide, Long> colPeptidesTotalIntensity = new TableColumn<>("Intensity");
        colPeptidesTotalIntensity.setCellValueFactory(new PropertyValueFactory<Peptide, Long>("totalIntensity"));
        TableColumn<Peptide, Long> colPeptidesMsmsCount = new TableColumn<>("MS/MS Count");
        colPeptidesMsmsCount.setCellValueFactory(new PropertyValueFactory<Peptide, Long>("msmsCount"));
        tablePeptides.getColumns().addAll(colPeptidesSequence, colPeptidesStartPosition, colPeptidesEndPosition, colPeptidesTotalIntensity, colPeptidesMsmsCount);
        
        // Initialize volcano plot
        volcanoPlotXAxis.setLabel("log2 Enrichment");
		volcanoPlotYAxis.setLabel("-Log10 p-value");
		volcanoPlot.legendVisibleProperty().set(false);
		
		// Initialize sequence viewer wrapper
		try {
			enzymes.importEnzymes(new File("resources/enzymes.xml"));
		} catch (IOException | XmlPullParserException e) {
			e.printStackTrace();
		}
		choiceEnzyme.setConverter(new StringConverter<Enzyme>() {
			@Override
			public String toString(Enzyme enzyme) {
				return enzyme.getName();
			}

			@Override
			public Enzyme fromString(String string) {
				return null;
			}
		});
		choiceEnzyme.setItems(FXCollections.observableArrayList(enzymes.getEnzymes()));
		choiceEnzyme.getSelectionModel().select(enzymes.getEnzyme("Trypsin"));
		
		sequenceViewParent.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		
		// React to changes in the selection model of treeview
		treeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<? extends AnalysisComponent>>) c -> {
			boolean combiningSequencesIsDisabled = true;
			boolean combiningStatisticsIsDisabled = true;
			if (c.getList().size() == 2) {
				List<AnalysisComponent> list = new ArrayList<>(2);
				int index = 0;
				for (TreeItem<? extends AnalysisComponent> item : c.getList()) {
					list.add(index, item.getValue());
					index++;
				}
				
				// Activate sequence combination button only if correct analysis components are selected
				if ((list.get(0) instanceof PeptideId) && (list.get(1) instanceof FastaFile)) {
					combiningSequencesIsDisabled = false;
				} else if ((list.get(1) instanceof PeptideId) && (list.get(0) instanceof FastaFile)) {
					combiningSequencesIsDisabled = false;
				}
				
				// Activate statistics combination button only if correct analysis components are selected
				if ((list.get(0) instanceof PeptideId) && (list.get(1) instanceof StatisticsFile)) {
					combiningStatisticsIsDisabled = false;
				} else if ((list.get(1) instanceof PeptideId) && (list.get(0) instanceof StatisticsFile)) {
					combiningStatisticsIsDisabled = false;
				}
			}
			buttonCombineSequences.setDisable(combiningSequencesIsDisabled);
			buttonCombineStatistics.setDisable(combiningStatisticsIsDisabled);
		
			if (c.getList().size() == 1) {
				if (treeView.getSelectionModel().getSelectedItem().getValue() instanceof PeptideId) {
					PeptideId peptideId = (PeptideId) treeView.getSelectionModel().getSelectedItem().getValue();
					tableProteinGroups.setItems(FXCollections.observableArrayList(peptideId.getAllProteinGroups().values()));
					analysisHandler.setSelectedAnalysisId(c.getList().get(0).getParent().getValue().getId());
					analysisHandler.setSelectedPeptideIdId(peptideId.getId());
					updateVolcanoPlot(peptideId);
				}
			}
		});
		
		// React to protein group selection
		tableProteinGroups.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ProteinGroup>) c -> {
			ProteinGroup selectedProteinGroup = c.getList().get(0);
			if (selectedProteinGroup != null) {
				int proteinGroupId = selectedProteinGroup.getId();
				tablePeptides.setItems(analysisHandler.getAnalysis().getPeptideId().getPeptidesByProteinGroupsId(proteinGroupId));
				analysisHandler.setSelectedProteinGroupId(proteinGroupId);
				updateSequenceView();
			}
		});
		
		// React to peptide selection
		tablePeptides.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Peptide>) c -> {
			Peptide selectedPeptide = c.getList().get(0);
			if (selectedPeptide != null) {
				int peptideId = selectedPeptide.getId();
				analysisHandler.setSelectedPeptideId(peptideId);
			}
		});
	}
	
	public TreeView<? extends AnalysisComponent> getTreeView() {
		return treeView;
	}
	
	public ScatterChart<Number, Number> getVolcanoPlot() {
		return volcanoPlot;
	}
	
	public Label getStatusLabel() {
		return statusLabel;
	}
	
	public ProgressBar getProgressBar() {
		return progressBar;
	}
	
	public TableView<ProteinGroup> getProteinGroupsTableView() {
		return tableProteinGroups;
	}

	public void handleMenuCloseEvent(ActionEvent event) {
		stage.fireEvent(new WindowEvent(stage, WindowEvent.WINDOW_CLOSE_REQUEST));
	}
	
	public void handleNewAnalysis(ActionEvent event) {
		analysisHandler.newAnalysis();
	}
	
	public void handleOpenAnalysisComponent(ActionEvent event) {
		// Get analysis id
		int id = getAnalysisId();
		
		// Choose right opener
		if (event.getSource().equals(buttonOpenMaxQuant) || event.getSource().equals(menuItemOpenMaxQuant)) {
			analysisHandler.addItem(id, AnalysisComponentType.MaxQuant, mainApp);
		}
		if (event.getSource().equals(buttonOpenPerseus) || event.getSource().equals(menuItemOpenPerseus)) {
			analysisHandler.addItem(id, AnalysisComponentType.Perseus, mainApp);
		}
		if (event.getSource().equals(buttonOpenFasta) || event.getSource().equals(menuItemOpenFasta)) {
			analysisHandler.addItem(id, AnalysisComponentType.Fasta, mainApp);
		}
	}
	
	public void handleCombineSequencesAndProteinGroups(ActionEvent event) {
		PeptideId peptideId = null;
		FastaFile fastaFile = null;
		for (TreeItem<? extends AnalysisComponent> item : treeView.getSelectionModel().getSelectedItems()) {
			if (item.getValue() instanceof PeptideId) {
				peptideId = (PeptideId) item.getValue();
			} else if (item.getValue() instanceof FastaFile) {
				fastaFile = (FastaFile) item.getValue();
			}
		}
		Tools.combineSequencesAndProteinGroups(fastaFile, peptideId.getAllProteinGroups(), mainApp);
		peptideId.setSequencesAdded(true);
	}
	
	public void handleCombineStatisticsAndProteinGroups(ActionEvent event) {
		PeptideId peptideId = null;
		StatisticsFile statisticsFile = null;
		for (TreeItem<? extends AnalysisComponent> item : treeView.getSelectionModel().getSelectedItems()) {
			if (item.getValue() instanceof PeptideId) {
				peptideId = (PeptideId) item.getValue();
			} else if (item.getValue() instanceof StatisticsFile) {
				statisticsFile = (StatisticsFile) item.getValue();
			}
		}
		if (peptideId.getId() == analysisHandler.getSelectedPeptideIdId()) {
			peptideId.statisticsAddedProperty().addListener((c, o, n) -> {
				updateVolcanoPlot(null);
			});
		}
		Tools.combineStatisticsAndProteinGroups(statisticsFile, peptideId, mainApp);
	}
	
	public void openAboutDialog(ActionEvent event) {
		
	}
	
	public void selectProteinGroup(ProteinGroup proteinGroup) {
		tableProteinGroups.getSelectionModel().select(proteinGroup);
		tableProteinGroups.scrollTo(proteinGroup);
	}
	
	private void updateVolcanoPlot(PeptideId peptideId) {
		if (!volcanoPlot.getData().isEmpty()) {
			volcanoPlot.getData().clear();
		}
		
		if (peptideId == null) {
			peptideId = analysisHandler.getAnalysis().getPeptideId();
		}
		
		XYChart.Series<Number, Number> series = new XYChart.Series<>();
		for (Entry<Integer, Map<StatisticsTableHeaders, Number>> entry : peptideId.getStatisticsData().entrySet()) {
			XYChart.Data<Number, Number> dataPoint = new XYChart.Data<Number, Number>(entry.getValue().get(StatisticsTableHeaders.LOG2_ENRICHMENT), entry.getValue().get(StatisticsTableHeaders.MINUS_LOG10_PVALUE));
			VolcanoPlotDot node = new VolcanoPlotDot((ProteinGroupsTableHeaders.GENE_NAMES), peptideId.getProteinGroupById(entry.getKey()), this);
			node.selectedProperty().bind(tableProteinGroups.getSelectionModel().selectedItemProperty().isEqualTo(peptideId.getProteinGroupById(entry.getKey())));
			dataPoint.setNode(node);
			series.getData().add(dataPoint);
		}
		volcanoPlot.getData().add(series);
	}
	
	private int getAnalysisId() {
		int id = -1;
		TreeItem<? extends AnalysisComponent> item = treeView.getSelectionModel().getSelectedItem();
		if (item != null) {
			while (true) {
				if (item.getValue() instanceof Analysis) {
					break;
				} else {
					item = item.getParent();
				}
			}
			id = item.getValue().getId();
		}
		return id;
	}
	
	private void updateSequenceView() {
		sequenceViewParent.getChildren().clear();
		sequenceViewer = new SequenceViewer(analysisHandler.getAnalysis().getPeptideId(), analysisHandler.getSelectedProteinGroupId(), sequenceViewParent.heightProperty());
		progressBar.progressProperty().bind(sequenceViewer.progressProperty());
		statusLabel.textProperty().bind(sequenceViewer.statusProperty());
		sequenceViewer.readyProperty().addListener(c -> {
			sequenceViewParent.getChildren().add(sequenceViewer);
		});
	}
	
	public void sequenceViewDigest() {
		if (choiceEnzyme.getSelectionModel().getSelectedItem() != null) {
			sequenceViewer.inSilicoDigest(choiceEnzyme.getSelectionModel().getSelectedItem());
		}
	}
	
	public void sequenceViewShowExperimentIntensities() {
		sequenceViewer.showExperimentIntensities();
	}
	
	public void sequenceViewCalculateAaProfiles() {
		sequenceViewer.generateAaIntensityProfiles();
	}
}