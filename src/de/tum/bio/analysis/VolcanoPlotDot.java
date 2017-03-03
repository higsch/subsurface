package de.tum.bio.analysis;

import application.MainController;
import de.tum.bio.proteomics.ProteinGroup;
import de.tum.bio.proteomics.ProteinGroupsTableHeaders;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class VolcanoPlotDot extends StackPane {
	
	private int id;
	@SuppressWarnings("unused")
	private MainController controller;
	private Label label = new Label();
	@SuppressWarnings("unused")
	private ProteinGroupsTableHeaders labelType;
	@SuppressWarnings("unused")
	private ProteinGroup proteinGroup;
	
	private BooleanProperty selectedProperty = new SimpleBooleanProperty();
	
	public VolcanoPlotDot(ProteinGroupsTableHeaders labelType, ProteinGroup proteinGroup, MainController controller) {
		this.id = proteinGroup.getId();
		this.labelType = labelType;
		this.proteinGroup = proteinGroup;
		this.controller = controller;
		setLabelText(labelType, proteinGroup);
		
		selectedProperty.addListener((c, o, n) -> {
			if (n == true) {
				select();
			} else {
				deSelect();
			}
		});
	
		setOnMouseEntered(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent mouseEvent) {
				getChildren().setAll(label);
				label.toFront();
				toFront();
	        }
	    });
		
	    setOnMouseExited(new EventHandler<MouseEvent>() {
	    	@Override
	    	public void handle(MouseEvent mouseEvent) {
	    		getChildren().clear();
	        }
	    });
	    
	    setOnMouseClicked(new EventHandler<MouseEvent>() {
	    	@Override
	    	public void handle(MouseEvent mouseEvent) {
	    		controller.selectProteinGroup(proteinGroup);
	    	}
	    });
	}
	
	public int getProteinGroupsId() {
		return id;
	}
	
	public boolean isSelected() {
		return selectedProperty.get();
	}
	
	public void setSelected(boolean value) {
		this.selectedProperty.set(value);
	}
	
	public BooleanProperty selectedProperty() {
		return selectedProperty;
	}
	
	private void setLabelText(ProteinGroupsTableHeaders labelType, ProteinGroup proteinGroup) {
		String text = "";
		switch (labelType) {
			case GENE_NAMES:
				text = proteinGroup.getGeneNames();
				break;
			case NAMES:
				text = proteinGroup.getNames();
				break;
			default:
				text = String.valueOf(id);
				break;
		}
		
		if (text == null) {
			text = "no name";
		}
		
		label.setText(text);
		label.setWrapText(true);
		label.setMinWidth(Region.USE_PREF_SIZE);
		label.setMinHeight(Region.USE_PREF_SIZE);
		label.setTranslateY(-15.0);
	}
	
	private void select() {
		getStyleClass().add("selected-point");
		toFront();
	}
	
	void deSelect() {
		getStyleClass().remove("selected-point");
	}
}
