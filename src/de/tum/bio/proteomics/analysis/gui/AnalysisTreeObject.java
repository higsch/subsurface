package de.tum.bio.proteomics.analysis.gui;

import de.tum.bio.proteomics.analysis.AnalysisComponentType;

public class AnalysisTreeObject  {
	
	private String text = "unknown";
	private int analysisId = -1;
	private int itemId = -1;
	private AnalysisComponentType analysisComponentType;
	
	public AnalysisTreeObject(String text, int analysisId, int itemId, AnalysisComponentType analysisComponentType) {
		if (text != null) {
			setText(text);
		}
		setAnalysisId(analysisId);
		setItemId(itemId);
		setAnalysisComponentType(analysisComponentType);
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public void setAnalysisId(int analysisId) {
		this.analysisId = analysisId;
	}
	
	public int getAnalysisId() {
		return analysisId;
	}
	
	public void setItemId(int itemId) {
		this.itemId = itemId;
	}
	
	public int getItemId() {
		return itemId;
	}
	
	public void setAnalysisComponentType(AnalysisComponentType analysisComponentType) {
		this.analysisComponentType = analysisComponentType;
	}
	
	public AnalysisComponentType getAnalysisComponentType() {
		return analysisComponentType;
	}
	
	@Override
	public String toString() {
		return text;
	}
}