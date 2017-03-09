package de.tum.bio.sequenceviewer;

import de.tum.bio.proteomics.Modification;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;

public class SequenceViewerModification extends StackPane {
	
	private Modification modification;
	private double columnWidth;
	private double columnHight;
	
	public SequenceViewerModification(Modification modification, double columnWidth, double columnHight) {
		this.modification = modification;
		this.columnWidth = columnWidth;
		this.columnHight = columnHight;
		
		init();
	}
	
	private void init() {
		Rectangle rect = new Rectangle(columnWidth, columnHight);
		rect.setArcWidth(columnWidth/4);
		rect.setArcHeight(columnWidth/4);
		rect.setFill(Color.valueOf(String.format("#%06X", (0xFFFFFF & modification.getName().hashCode()))).desaturate().desaturate().brighter());
		getChildren().add(rect);
		setAlignment(Pos.CENTER_LEFT);
		
		Label label = new Label(modification.getAbbreviation());
		label.setAlignment(Pos.CENTER);
		label.setTextAlignment(TextAlignment.CENTER);
		rect.widthProperty().bind(label.widthProperty());
		
		getChildren().add(label);
		
		Tooltip.install(this, generateInfoTooltip());
	}
	
	private Tooltip generateInfoTooltip() {
		Tooltip tt = new Tooltip();
		
		StringBuilder text = new StringBuilder("");
		text.append("Name: " + modification.getName() + "\n");
		text.append("Localization probability: " + String.valueOf(modification.getLocalizationProbability()) + "\n");
		text.append("Score difference: " + String.valueOf(modification.getScoreDiff()) + "\n");
		text.append("PEP: " + String.valueOf(modification.getPep()) + "\n");
		text.append("Delta score: " + String.valueOf(modification.getDeltaScore()) + "\n");
		text.append("Localization score: " + String.valueOf(modification.getScoreForLocalization()));
		
		tt.setText(text.toString());
		return tt;
	}
	
}