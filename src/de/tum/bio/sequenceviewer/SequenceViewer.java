package de.tum.bio.sequenceviewer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.compomics.util.experiment.biology.Enzyme;

import de.tum.bio.proteomics.Peptide;
import de.tum.bio.proteomics.PeptideId;
import de.tum.bio.sequenceviewer.aaintensityprofiles.ProfileGenerator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

public class SequenceViewer extends BorderPane {
	
	private final int COLUMN_WIDTH = 20;
	private final int CANVAS_SIZE = 200;
	private final int DESCRIPTION_ROW = 1;
	private final int DESCRIPTION_COLUMN = 1;
	private final int CONTENT_ROW = 2;
	private final int CONTENT_COLUMN = 2;
	
	private final int VGAP = 16;
	
	private Stage owner;
	
	private ReadOnlyDoubleProperty parentHightProperty;
	
	private ScrollPane centerScrollPane;
	private VBox centerVBox;
	
	private GridPane mainGrid;
	private GridPane peptideGrid;
	private GridPane intensityGrid;
	private GridPane inSilicoDigestGrid = null;
	private VBox experimentIntensities = null;
	
	private PeptideId peptideId;
	private String proteinGroupId;
	
	private String proteinSequence;
	/*
	 * Holds the peptide Ids for each amino acid position of the protein.
	 */
	private Map<Integer, List<String>> peptideMap;
	
	/*
	 * Holds the intensities for each amino acid position and specific experiment.
	 */
	private Map<String, Map<Integer, Long>> experimentIntensityMap;
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty(0.0);
	private StringProperty statusProperty = new SimpleStringProperty();
	private BooleanProperty readyProperty = new SimpleBooleanProperty(false);
	
	public SequenceViewer(Stage owner, PeptideId peptideId, String id, ReadOnlyDoubleProperty parentHightProperty) {
		this.owner = owner;
		setPeptideId(peptideId);
		setProteinGroupId(id);
		this.parentHightProperty = parentHightProperty;
		proteinSequence = peptideId.getProteinGroupById(id).getSequenceAsString();
		
		init();
	}
	
	public void inSilicoDigest(Enzyme enzyme) {
		centerVBox.getChildren().remove(inSilicoDigestGrid);
		
		if (!peptideId.hasDigestionAssay(enzyme, proteinGroupId)) {
			List<String> sequences = enzyme.digest(proteinSequence, 2, 7, 30);
			peptideId.addPeptides(sequences, proteinGroupId, enzyme);
		}
		
		List<Peptide> peptides = peptideId.getInSilicoPeptides(enzyme, proteinGroupId);

		inSilicoDigestGrid = buildGrid(null, true);
		inSilicoDigestGrid.setVgap(inSilicoDigestGrid.getVgap() * 0.3);
		generateDescription(inSilicoDigestGrid, "In silico digest with " + enzyme.getName(), true);
		generatePeptideRepresentations(CONTENT_COLUMN, CONTENT_ROW, inSilicoDigestGrid, peptides, false);
	}
	
	private void generateDescription(GridPane targetGrid, String text, boolean closeButton) {
		HBox hBox = new HBox();
		Label label = new Label(text);
		label.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 15));
		label.setTextFill(Color.valueOf("#3070b3"));
		hBox.getChildren().add(label);
		if (closeButton) {
			Button close = new Button("X");
			close.getStyleClass().add("round-button");
			close.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					centerVBox.getChildren().remove(targetGrid);
				}
			});
			hBox.getChildren().add(close);
		}
		hBox.setSpacing(5.0);
		hBox.setAlignment(Pos.CENTER_LEFT);
		targetGrid.add(hBox, DESCRIPTION_COLUMN, DESCRIPTION_ROW, 30, 1);
	}
	
	private void initPeptideMap() {
		peptideMap = new HashMap<>();
		for (int i = 1; i <= proteinSequence.length(); i++) {
			peptideMap.put(i, new ArrayList<String>(0));
		}
	}

	private void init() {
		if (proteinSequence != null) {
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					updateProgress(-1.0, 1.0);
					updateMessage("Compute sequence view...");
					
					// Build view components
					centerVBox = new VBox();
					centerVBox.setMaxHeight(Double.MAX_VALUE);
					centerScrollPane = new ScrollPane(centerVBox);
					setCenter(centerScrollPane);
					setMaxHeight(Double.MAX_VALUE);
					prefHeightProperty().bind(parentHightProperty);
					
					// Initialize initial grid
					initPeptideMap();
					mainGrid = buildGrid(null, true);
					
					// Main grid
					generateSequenceRepresentation(CONTENT_COLUMN, CONTENT_ROW, mainGrid);
					peptideGrid = buildGrid(mainGrid, true);
					generateDescription(peptideGrid, "Detected peptides", false);
					generatePeptideRepresentations(CONTENT_COLUMN, CONTENT_ROW, peptideGrid, peptideId.getPeptidesByProteinGroupsId(proteinGroupId), true);
					
					// Intensity grid
					intensityGrid = buildGrid(peptideGrid, true);
					generateDescription(intensityGrid, "Total peptide intensities", false);
					generateTotalIntensityGraph(CONTENT_COLUMN, CONTENT_ROW, intensityGrid);
					return null;
				}
						
				@Override
				protected void succeeded() {
					super.succeeded();
					updateProgress(0.0, 1.0);
					updateMessage("Done.");
				}
						
				@Override
				protected void failed() {
					super.failed();
					updateProgress(0.0, 1.0);
					updateMessage("Cancelled.");
				}
			};
			task.setOnSucceeded(workerStateEvent -> {
				readyProperty.set(true);
			});
			task.setOnFailed(workerStateEvent -> {
				task.getException().printStackTrace(System.out);
				Alert alert = new Alert(AlertType.ERROR, task.getException().getMessage(), ButtonType.OK);
				alert.showAndWait();
			});
			
			progressProperty.bind(task.progressProperty());
			statusProperty.bind(task.messageProperty());
			new Thread(task).start();
		}
	}
	
	private GridPane buildGrid(GridPane positionAfter, boolean putInParent) {
		GridPane grid = new GridPane();
		grid.getStyleClass().add("sequence-viewer");
		List<ColumnConstraints> columnList = new ArrayList<>();
		for (int i = 1; i <= proteinSequence.length() + CONTENT_COLUMN + 1; i++) {
			columnList.add(new ColumnConstraints(COLUMN_WIDTH));
		}
		grid.getColumnConstraints().addAll(columnList);
		grid.setVgap(VGAP);
		
		VBox.setVgrow(grid, Priority.ALWAYS);
		
		// Position grid in given VBox
		if (putInParent) {
			if (positionAfter == null) {
				centerVBox.getChildren().add(0, grid);
			} else {
				centerVBox.getChildren().add(centerVBox.getChildren().indexOf(positionAfter)+1, grid);
			}
		}
		
		//grid.setGridLinesVisible(true);
		return grid;
	}
	
	private int generateSequenceRepresentation(int column, int row, GridPane grid) {
		for (int i = 0; i <= ((int) Math.ceil(proteinSequence.length() / CANVAS_SIZE)); i++) {
			if (!proteinSequence.substring((i)*CANVAS_SIZE).isEmpty()) {
				grid.add(createSequenceCanvas(proteinSequence.substring((i)*CANVAS_SIZE)), column+(i*CANVAS_SIZE), row, proteinSequence.substring((i)*CANVAS_SIZE).length(), 1);
			}
		}
		
		for (int i = 1; i <= proteinSequence.length(); i++) {
			if (i % 10 == 0) {
				grid.add(createNumberingCanvas(i), i+column-CONTENT_COLUMN, row+1, 3, 1);
			}
		}
		return row+2;
	}

	private Canvas createSequenceCanvas(String subSequence) {
		int canvasLength = COLUMN_WIDTH * CANVAS_SIZE;
		if (subSequence.length() < CANVAS_SIZE) {
			canvasLength = subSequence.length() * COLUMN_WIDTH;
		}
		Canvas canvas = new Canvas(canvasLength, 20);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font(20.0));
        int i = 1;
		for (char c : subSequence.toCharArray()) {
			gc.setFill(Color.BLACK);
	        gc.fillText(
	            String.valueOf(c),
	            Math.round(i * COLUMN_WIDTH - COLUMN_WIDTH / 2),
	            Math.round(canvas.getHeight() / 2)
	        );
	        if (i >= CANVAS_SIZE) {
	        	break;
	        }
	        i++;
		}
		return canvas;
	}
	
	private Canvas createNumberingCanvas(int number) {
		Canvas canvas = new Canvas(COLUMN_WIDTH * 3, 20);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setFont(new Font(10.0));
	    gc.fillText(
	    	String.valueOf(number), 
	        Math.round(COLUMN_WIDTH * 3 / 2), 
	        Math.round(canvas.getHeight() / 2)
	    );
		return canvas;
	}
	
	private int generatePeptideRepresentations(int column, int row, GridPane grid, List<Peptide> peptides, boolean updatePeptideMap) {
		List<SequenceViewerPeptide> peptideRepresentations = buildPeptideRepresentationsList(peptides);
		peptideRepresentations.sort(new Comparator<SequenceViewerPeptide>() {
			@Override
			public int compare(SequenceViewerPeptide o1, SequenceViewerPeptide o2) {
				if (o1.getStartPosition() == o2.getStartPosition()) {
					return 1;
				}
				if (o1.getStartPosition() > o2.getStartPosition()) {
					return 1;
				}
				if (o1.getStartPosition() < o2.getStartPosition()) {
					return -1;
				}
				return 1;
			}
		});
		Map<SequenceViewerPeptide, Integer> peptideRepresentationsMap = assignPeptideToRow(peptideRepresentations, row);
		
		int lastRow = 0;
		for (Entry<SequenceViewerPeptide, Integer> entry : peptideRepresentationsMap.entrySet()) {
			grid.add(entry.getKey(), entry.getKey().getStartPosition() + column-1, entry.getValue(), entry.getKey().getSequenceAsString().length(), 1);
			if (updatePeptideMap) {
				for (int i = entry.getKey().getStartPosition(); i <= entry.getKey().getEndPosition(); i++) {
					peptideMap.get(i).add(entry.getKey().getPeptideId());
				}
			}
			if (entry.getValue() > lastRow) {
				lastRow = entry.getValue();
			}
		}
		return lastRow+1;
	}
	
	private List<SequenceViewerPeptide> buildPeptideRepresentationsList(List<Peptide> peptides) {
		List<SequenceViewerPeptide> peptideRepresentationsList = new ArrayList<>();
		for (Peptide peptide : peptides) {
			if (proteinSequence.substring(peptide.getStartPosition()-1, peptide.getEndPosition()).equals(peptide.getSequenceAsString())) {
				peptideRepresentationsList.add(new SequenceViewerPeptide(peptide, COLUMN_WIDTH));
			} else {
				int index = proteinSequence.indexOf(peptide.getSequenceAsString());
				if ((index >= 0) && ((index + peptide.getSequenceAsString().length()) <= proteinSequence.length())) {
					SequenceViewerPeptide seqPeptide = new SequenceViewerPeptide(peptide, COLUMN_WIDTH, index+1, index+peptide.getSequenceAsString().length());
					peptideRepresentationsList.add(seqPeptide);
				}
			}
		}
		return peptideRepresentationsList;
	}
	
	private Map<SequenceViewerPeptide, Integer> assignPeptideToRow(List<SequenceViewerPeptide> peptideRepresentations, int row) {
		Map<SequenceViewerPeptide, Integer> result = new HashMap<>();
		List<SequenceViewerPeptide> tmp = new ArrayList<>(peptideRepresentations);
		int previousEnd = -1;
		for (SequenceViewerPeptide peptideRepresentation : peptideRepresentations) {
			if (peptideRepresentation.getStartPosition() > previousEnd) {
				result.put(peptideRepresentation, row);
				previousEnd = peptideRepresentation.getEndPosition();
				tmp.remove(peptideRepresentation);
			}
		}
		if (tmp.size() > 0) {
			result.putAll(assignPeptideToRow(tmp, row+1));
		}
		return result;
	}
	
	private int generateTotalIntensityGraph(int column, int row, GridPane grid) {
		Map<Integer, Long> totalIntensityMap = new HashMap<>();
		long maxIntensity = 0;
		long minIntensity = 0;
		boolean firstEntry = true;
		for (Entry<Integer, List<String>> idList : peptideMap.entrySet()) {
			long totalIntensity = 0;
			for (String id : idList.getValue()) {
				totalIntensity += peptideId.getPeptideById(id).getTotalIntensity();
			}
			totalIntensityMap.put(idList.getKey(), totalIntensity);
			if (totalIntensity > maxIntensity) {
				maxIntensity = totalIntensity;
			}
			if (firstEntry || totalIntensity < minIntensity) {
				minIntensity = totalIntensity;
				firstEntry = false;
			}
		}
		row = assembleIntensityRow(grid, totalIntensityMap, maxIntensity, minIntensity, column, row);
		return row+1;
	}
	
	public void showExperimentIntensities() {
		if (experimentIntensities != null) {
			centerVBox.getChildren().remove(experimentIntensities);
		}
		
		// Initialize experimentIntensityMap
		experimentIntensityMap = new HashMap<>();
		for (String experimentName : peptideId.getSummary().getExperimentNames()) {
			experimentIntensityMap.put(experimentName, new HashMap<>());
		}
		
		// Compile experimentIntensityMap
		long maxIntensity = 0;
		for (Entry<Integer, List<String>> idList : peptideMap.entrySet()) {
			// for each position
			Map<String, Long> tmpExperimentIntensity = new HashMap<>();
			for (String experimentName : peptideId.getSummary().getExperimentNames()) {
				tmpExperimentIntensity.put(experimentName, (long) 0);
			}
			for (String id : idList.getValue()) {
				for (Entry<String, Long> entry : peptideId.getPeptideById(id).getExperimentIntensities().entrySet()) {
					if (tmpExperimentIntensity.containsKey(entry.getKey())) {
						tmpExperimentIntensity.put(entry.getKey(), entry.getValue() + tmpExperimentIntensity.get(entry.getKey()));
					} else {
						tmpExperimentIntensity.put(entry.getKey(), entry.getValue());
					}
				}
			}
			for (Entry<String, Long> experiment : tmpExperimentIntensity.entrySet()) {
				experimentIntensityMap.get(experiment.getKey()).put(idList.getKey(), tmpExperimentIntensity.get(experiment.getKey()));
				if (tmpExperimentIntensity.get(experiment.getKey()) > maxIntensity) {
					maxIntensity = tmpExperimentIntensity.get(experiment.getKey());
				}
			}
		}
		
		experimentIntensities = new VBox();
		for (String experimentName : peptideId.getSummary().getExperimentNames()) {
			GridPane grid = buildGrid(null, false);
			assembleIntensityRow(grid, experimentIntensityMap.get(experimentName), maxIntensity, 0, CONTENT_COLUMN, CONTENT_ROW);
			generateDescription(grid, experimentName, false);
			Tooltip tooltip = new Tooltip();
			tooltip.setText(experimentName);
			Tooltip.install(grid, tooltip);
			experimentIntensities.getChildren().add(grid);
		}
		
		centerVBox.getChildren().add(experimentIntensities);
	}
	
	public void generateAaIntensityProfiles() {
		if (experimentIntensityMap == null) {
			showExperimentIntensities();
		}
		
		ProfileGenerator aaProfileGenerator = new ProfileGenerator(owner);
		aaProfileGenerator.make(experimentIntensityMap, peptideId.getProteinGroupById(proteinGroupId));
	}
	
	private int assembleIntensityRow(GridPane grid, Map<Integer, Long> intensityMap, long maxIntensity, long minIntensity, int column, int row) {
		for (int i = 0; i <= ((int) Math.ceil(intensityMap.size() / CANVAS_SIZE)); i++) {
			int start = (i*CANVAS_SIZE) + 1;
			if (intensityMap.size() - (i*CANVAS_SIZE+column) > 0) {
				grid.add(createIntensityCanvas(intensityMap, start, maxIntensity, minIntensity), i*CANVAS_SIZE+column, row, intensityMap.size() - (i*CANVAS_SIZE+column), 1);
			}
		}
		return row+1;
	}
	
	private Canvas createIntensityCanvas(Map<Integer, Long> intensityMap, int start, long max, long min) {
		int canvasLength = COLUMN_WIDTH * CANVAS_SIZE;
		if ((intensityMap.size() - start) < CANVAS_SIZE) {
			canvasLength = (intensityMap.size() - start + 1) * COLUMN_WIDTH;
		}
		Canvas canvas = new Canvas(canvasLength, 50);
	    GraphicsContext gc = canvas.getGraphicsContext2D();
	    int i = 1;
	    for (int j = start; j <= intensityMap.size(); j++) {
		    gc.setFill(Color.valueOf("#f9423a"));
		    double y = canvas.getHeight()-canvas.getHeight()*intensityMap.get(j)/max;
		    double height = canvas.getHeight() - y;
		    gc.fillRect((i-1)*COLUMN_WIDTH, y, COLUMN_WIDTH, height);
	        if (i >= CANVAS_SIZE) {
		    	break;
		    }
		    i++;
	    }
		return canvas;
	}
	
	public void setPeptideId(PeptideId peptideId) {
		this.peptideId = peptideId;
	}
	
	public PeptideId getPeptideId() {
		return peptideId;
	}
	
	public void setProteinGroupId(String id) {
		this.proteinGroupId = id;
	}
	
	public String getProteinGroupId() {
		return proteinGroupId;
	}

	public DoubleProperty progressProperty() {
		return progressProperty;
	}

	public StringProperty statusProperty() {
		return statusProperty;
	}

	public BooleanProperty readyProperty() {
		return readyProperty;
	}
	
	public boolean isReady() {
		return readyProperty.get();
	}
}