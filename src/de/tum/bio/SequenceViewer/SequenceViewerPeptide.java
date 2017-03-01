package de.tum.bio.SequenceViewer;

import java.util.List;
import java.util.Map.Entry;

import de.tum.bio.proteomics.AminoAcid;
import de.tum.bio.proteomics.Modification;
import de.tum.bio.proteomics.Peptide;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.TextAlignment;

public class SequenceViewerPeptide extends StackPane {
	
	private final double HEIGHT = 17.0;
	
	private Peptide peptide;
	private double columnWidth;
	private boolean recalculated;
	private List<AminoAcid> sequenceList;
	private String sequenceString;
	private HBox hBox;
	
	private int startPosition;
	private int endPosition;
	
	public SequenceViewerPeptide (Peptide peptide, double columnWidth) {
		this.peptide = peptide;
		this.columnWidth = columnWidth;
		this.recalculated = false;
		this.sequenceList = peptide.getSequenceAsList();
		this.sequenceString = peptide.getSequenceAsString();
		setStartPosition(peptide.getStartPosition());
		setEndPosition(peptide.getEndPosition());
		init();
	}
	
	public SequenceViewerPeptide (Peptide peptide, double columnWidth, int startPosition, int endPosition) {
		this.peptide = peptide;
		this.columnWidth = columnWidth;
		this.recalculated = true;
		this.sequenceList = peptide.getSequenceAsList();
		this.sequenceString = peptide.getSequenceAsString();
		setStartPosition(startPosition);
		setEndPosition(endPosition);
		init();
	}
	
	private void init() {
		// Create the line
		Line line = new Line();
		line.setStartX(0.0);
		line.endXProperty().bind(this.widthProperty());
		line.setStrokeLineCap(StrokeLineCap.ROUND);
		line.setStrokeWidth(HEIGHT);
		// Set color in dependence of status
		if (!recalculated) {
			line.setStroke(Color.valueOf("#3070b3"));
		} else {
			line.setStroke(Color.valueOf("#f9423a"));
		}
		line.setOpacity(0.6);
		getChildren().add(line);
		line.toBack();
		
		hBox = new HBox();
		hBox.setMinHeight(HEIGHT);
		hBox.setMaxHeight(HEIGHT);
		for (char c : sequenceString.toCharArray()) {
			Label label = new Label(String.valueOf(c));
			label.setMinWidth(columnWidth);
			label.setMinHeight(HEIGHT);
			label.setMaxHeight(HEIGHT);
			label.setAlignment(Pos.BOTTOM_CENTER);
			label.setTextAlignment(TextAlignment.CENTER);
			hBox.getChildren().add(label);
		}
		Tooltip.install(hBox, new Tooltip("Peptide ID: " + String.valueOf(peptide.getId())));
		getChildren().add(hBox);
		hBox.toFront();
		
		setAlignment(Pos.BOTTOM_CENTER);
		showModifications();
	}
	
	private void showModifications() {
		if (peptide.getModifications().size() > 0) {
			for (Entry<Integer, Modification> entry : peptide.getModifications().entrySet()) {
				SequenceViewerModification modRepresentation = new SequenceViewerModification(entry.getValue(), columnWidth, HEIGHT);
				modRepresentation.setTranslateX(columnWidth * (entry.getKey() - 1));
				modRepresentation.setTranslateY(-0.8 * HEIGHT);
				getChildren().add(modRepresentation);
				modRepresentation.toFront();
			}
		}
	}
	
	public Peptide getPeptide() {
		return peptide;
	}
	
	public List<AminoAcid> getSequenceAsList() {
		return sequenceList;
	}
	
	public String getSequenceAsString() {
		return sequenceString;
	}
	
	private void setStartPosition(int startPosition) {
		this.startPosition = startPosition;
	}
	
	public int getStartPosition() {
		return startPosition;
	}
	
	private void setEndPosition(int endPosition) {
		this.endPosition = endPosition;
	}
	
	public int getEndPosition() {
		return endPosition;
	}
	
	public int getLength() {
		return sequenceString.length();
	}
	
	public int getPeptideId() {
		return peptide.getId();
	}
}
