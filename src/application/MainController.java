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
import de.tum.bio.proteomics.Toolbox;
import de.tum.bio.sequenceviewer.SequenceViewer;
import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ScatterChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class MainController {
	
	private Main mainApp;
	private Stage stage;
	private AnalysisHandler analysisHandler = AnalysisHandler.getInstance();
	
	private SequenceViewer sequenceViewer = null;
	
	private EnzymeFactory enzymes = EnzymeFactory.getInstance();
	private InvalidationListener sequenceViewerReadyListener = null;
	
	private IntegerProperty shownAnalysisId = new SimpleIntegerProperty(-1);
	private IntegerProperty shownPeptideIdId = new SimpleIntegerProperty(-1);
	
	private Image analysisIcon = new Image(getClass().getResourceAsStream("/icons/analysis.png"));
	private Image peptideIdIcon = new Image(getClass().getResourceAsStream("/icons/peptideId.png"));
	private Image statisticsIcon = new Image(getClass().getResourceAsStream("/icons/statistics.png"));
	private Image fastaFileIcon = new Image(getClass().getResourceAsStream("/icons/fastaFile.png"));
	
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
	TreeView<AnalysisTreeObject> treeView;
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
	VBox vBoxSequenceView;
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
		
		treeView.setCellFactory(new Callback<TreeView<AnalysisTreeObject>, TreeCell<AnalysisTreeObject>>() {
	        
			@Override
			public TreeCell<AnalysisTreeObject> call(TreeView<AnalysisTreeObject> param) {
	            TreeCell<AnalysisTreeObject> treeCell = new TreeCell<AnalysisTreeObject>() {
	                
	            	@Override
	                protected void updateItem(AnalysisTreeObject item, boolean empty) {
	                    super.updateItem(item, empty);
	                    if (!empty && item != null) {
	                        setText(item.toString());
	                        setGraphic(getTreeItem().getGraphic());

	                        final ContextMenu contextMenu = new ContextMenu();

	                        MenuItem menuItem = new MenuItem("Delete");
	                        menuItem.setOnAction(new EventHandler<ActionEvent>() {
	                            public void handle(ActionEvent e) {
	                                analysisHandler.removeItem(item.getAnalysisId(), item.getItemId(), item.getAnalysisComponentType());
	                                if (item.getAnalysisComponentType() == AnalysisComponentType.Analysis || item.getAnalysisComponentType() == AnalysisComponentType.PeptideId) {
	                                	if ((item.getAnalysisId() == shownAnalysisId.get() && item.getItemId() == -1) || (item.getAnalysisId() == shownAnalysisId.get() && item.getItemId() == shownPeptideIdId.get())) {
		                                	clearSurface();
	                                	}
	                                }
	                            }
	                        });
	                        contextMenu.getItems().addAll(menuItem);
	                        setContextMenu(contextMenu);

	                    } else {
	                        setText(null);
	                        setGraphic(null);
	                        setContextMenu(null);
	                    }
	                }
	            };
	            return treeCell;
	        }

	    });
		
		
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
		volcanoPlotYAxis.setLabel("-log10 p-value");
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
		treeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TreeItem<AnalysisTreeObject>>) c -> {
			boolean combiningSequencesIsDisabled = true;
			boolean combiningStatisticsIsDisabled = true;
			if (c.getList().size() == 2) {
				List<AnalysisComponentType> list = new ArrayList<>(2);
				int index = 0;
				for (TreeItem<AnalysisTreeObject> item : c.getList()) {
					list.add(index, item.getValue().getAnalysisComponentType());
					index++;
				}
				
				// Activate sequence combination button only if correct analysis components are selected
				if ((list.get(0) == AnalysisComponentType.PeptideId) && (list.get(1) == AnalysisComponentType.Fasta)) {
					combiningSequencesIsDisabled = false;
				} else if ((list.get(1) == AnalysisComponentType.PeptideId) && (list.get(0) == AnalysisComponentType.Fasta)) {
					combiningSequencesIsDisabled = false;
				}
				
				// Activate statistics combination button only if correct analysis components are selected
				if ((list.get(0) == AnalysisComponentType.PeptideId) && (list.get(1) == AnalysisComponentType.Statistics)) {
					combiningStatisticsIsDisabled = false;
				} else if ((list.get(1) == AnalysisComponentType.PeptideId) && (list.get(0) == AnalysisComponentType.Statistics)) {
					combiningStatisticsIsDisabled = false;
				}
			}
			buttonCombineSequences.setDisable(combiningSequencesIsDisabled);
			buttonCombineStatistics.setDisable(combiningStatisticsIsDisabled);
		
			// Action, if only one item was selected
			if (c.getList().size() == 1) {
				// if this is a PeptideId
				if (treeView.getSelectionModel().getSelectedItem().getValue().getAnalysisComponentType() == AnalysisComponentType.PeptideId) {
					analysisHandler.setSelectedAnalysisId(treeView.getSelectionModel().getSelectedItem().getValue().getAnalysisId());
					analysisHandler.getAnalysis().setSelectedPeptideIdId(treeView.getSelectionModel().getSelectedItem().getValue().getItemId());
					shownAnalysisId.set(treeView.getSelectionModel().getSelectedItem().getValue().getAnalysisId());
					shownPeptideIdId.set(treeView.getSelectionModel().getSelectedItem().getValue().getItemId());
					PeptideId peptideId = analysisHandler.getAnalysis().getPeptideId();
					if (peptideId != null) {
						tableProteinGroups.setItems(FXCollections.observableArrayList(peptideId.getAllProteinGroups().values()));
						updateVolcanoPlot(peptideId);
						selectProteinGroup(peptideId.getSelectedProteinGroup());
					} else {
						tableProteinGroups.setItems(null);
						updateVolcanoPlot(null);
					}
				}
			}
		});

		// React to protein group selection
		tableProteinGroups.getSelectionModel().getSelectedItems().addListener((ListChangeListener<ProteinGroup>) c -> {
			ProteinGroup selectedProteinGroup = c.getList().get(0);
			if (selectedProteinGroup != null) {
				int proteinGroupId = selectedProteinGroup.getId();
				PeptideId peptideId = analysisHandler.getAnalysis().getPeptideId();
				if (peptideId != null) {
					tablePeptides.setItems(peptideId.getPeptidesByProteinGroupsId(proteinGroupId));
				} else {
					tablePeptides.setItems(null);
				}
				analysisHandler.getAnalysis().getPeptideId().setSelectedProteinGroupId(proteinGroupId);
				updateSequenceView();
			}
		});
		
		// React to peptide selection
		tablePeptides.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Peptide>) c -> {
			Peptide selectedPeptide = c.getList().get(0);
			if (selectedPeptide != null) {
				int peptideId = selectedPeptide.getId();
				analysisHandler.getAnalysis().getPeptideId().setSelectedPeptideId(peptideId);
			}
		});
	}
	
	public TreeView<AnalysisTreeObject> getTreeView() {
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
		for (TreeItem<AnalysisTreeObject> item : treeView.getSelectionModel().getSelectedItems()) {
			if (item.getValue().getAnalysisComponentType() == AnalysisComponentType.PeptideId) {
				peptideId = analysisHandler.getAnalysis(item.getValue().getAnalysisId()).getPeptideId(item.getValue().getItemId());
			} else if (item.getValue().getAnalysisComponentType() == AnalysisComponentType.Fasta) {
				fastaFile = analysisHandler.getAnalysis(item.getValue().getAnalysisId()).getFastaFile(item.getValue().getItemId());
			}
		}
		Toolbox.combineSequencesAndProteinGroups(fastaFile, peptideId.getAllProteinGroups(), mainApp);
		peptideId.setSequencesAdded(true);
	}
	
	public void handleCombineStatisticsAndProteinGroups(ActionEvent event) {
		PeptideId peptideId = null;
		StatisticsFile statisticsFile = null;
		for (TreeItem<AnalysisTreeObject> item : treeView.getSelectionModel().getSelectedItems()) {
			if (item.getValue().getAnalysisComponentType() == AnalysisComponentType.PeptideId) {
				peptideId = analysisHandler.getAnalysis(item.getValue().getAnalysisId()).getPeptideId(item.getValue().getItemId());
			} else if (item.getValue().getAnalysisComponentType() == AnalysisComponentType.Statistics) {
				statisticsFile = analysisHandler.getAnalysis(item.getValue().getAnalysisId()).getStatisticsFile(item.getValue().getItemId());
			}
		}
		if (peptideId.getId() == analysisHandler.getAnalysis().getSelectedPeptideIdId()) {
			peptideId.statisticsAddedProperty().addListener((c, o, n) -> {
				updateVolcanoPlot(null);
			});
		}
		Toolbox.combineStatisticsAndProteinGroups(statisticsFile, peptideId, mainApp);
	}
	
	public void openAboutDialog(ActionEvent event) {
		
	}
	
	public void selectProteinGroup(ProteinGroup proteinGroup) {
		tableProteinGroups.getSelectionModel().select(proteinGroup);
		tableProteinGroups.scrollTo(proteinGroup);
	}
	
	private void updateVolcanoPlot(PeptideId peptideId) {
		if (volcanoPlot.getData() != null) {
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
		TreeItem<AnalysisTreeObject> item = treeView.getSelectionModel().getSelectedItem();
		if (item != null) {
			while (true) {
				if (item.getValue().getAnalysisComponentType() == AnalysisComponentType.Analysis) {
					break;
				} else {
					item = item.getParent();
				}
			}
			id = item.getValue().getAnalysisId();
		}
		return id;
	}
	
	private void updateSequenceView() {
		if (sequenceViewer != null) {
			sequenceViewer.readyProperty().removeListener(sequenceViewerReadyListener);;
		} else {
			sequenceViewerReadyListener = c -> {
				sequenceViewParent.getChildren().clear();
				sequenceViewParent.getChildren().add(sequenceViewer);
			};
		}
		
		if (analysisHandler.getAnalysis().getPeptideId() != null) {
			sequenceViewer = new SequenceViewer(mainApp.getStage(), analysisHandler.getAnalysis().getPeptideId(), analysisHandler.getAnalysis().getPeptideId().getSelectedProteinGroupId(), sequenceViewParent.heightProperty());
			progressBar.progressProperty().bind(sequenceViewer.progressProperty());
			statusLabel.textProperty().bind(sequenceViewer.statusProperty());
			sequenceViewer.readyProperty().addListener(sequenceViewerReadyListener);
		} else {
			sequenceViewParent.getChildren().clear();
		}
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
	
	public void buildTreeView() {
		TreeItem<AnalysisTreeObject> selectedItem = treeView.getSelectionModel().getSelectedItem();
		TreeItem<AnalysisTreeObject> root = new TreeItem<>();
		for (Entry<Integer, Analysis> analysis : analysisHandler.getAllAnalyses().entrySet()) {
			TreeItem<AnalysisTreeObject> analysisItem = new TreeItem<>();
			analysisItem.setValue(new AnalysisTreeObject(analysis.getValue().getName(), analysis.getValue().getId(), -1, AnalysisComponentType.Analysis));
			analysisItem.setGraphic(new ImageView(analysisIcon));
			for (Entry<Integer, PeptideId> peptideId : analysis.getValue().getPeptideIds().entrySet()) {
				TreeItem<AnalysisTreeObject> peptideIdItem = new TreeItem<>();
				peptideIdItem.setValue(new AnalysisTreeObject(peptideId.getValue().toString(), analysis.getValue().getId(), peptideId.getValue().getId(), AnalysisComponentType.PeptideId));
				peptideIdItem.setGraphic(new ImageView(peptideIdIcon));
				analysisItem.getChildren().add(peptideIdItem);
			}
			for (Entry<Integer, StatisticsFile> statisticsFile : analysis.getValue().getStatisticsFiles().entrySet()) {
				TreeItem<AnalysisTreeObject> statisticsFileItem = new TreeItem<>();
				statisticsFileItem.setValue(new AnalysisTreeObject(statisticsFile.getValue().toString(), analysis.getValue().getId(), statisticsFile.getValue().getId(), AnalysisComponentType.Statistics));
				statisticsFileItem.setGraphic(new ImageView(statisticsIcon));
				analysisItem.getChildren().add(statisticsFileItem);
			}
			for (Entry<Integer, FastaFile> fastaFile : analysis.getValue().getFastaFiles().entrySet()) {
				TreeItem<AnalysisTreeObject> fastaFileItem = new TreeItem<>();
				fastaFileItem.setValue(new AnalysisTreeObject(fastaFile.getValue().toString(), analysis.getValue().getId(), fastaFile.getValue().getId(), AnalysisComponentType.Fasta));
				fastaFileItem.setGraphic(new ImageView(fastaFileIcon));
				analysisItem.getChildren().add(fastaFileItem);
			}
			if (!analysisItem.isLeaf()) {
				analysisItem.setExpanded(true);
			}
			root.getChildren().add(analysisItem);
		}
		treeView.setRoot(root);
		treeView.getSelectionModel().select(selectedItem);
	}
	
	private void clearSurface() {
		tableProteinGroups.setItems(null);
    	tablePeptides.setItems(null);
    	volcanoPlot.setData(null);
    	sequenceViewParent.getChildren().clear();
	}
}