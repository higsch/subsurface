package de.tum.bio.proteomics.io.psi;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import de.tum.bio.proteomics.PeptideId;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;

public class MzIdentMLReader {
	
	private DoubleProperty progressProperty = new SimpleDoubleProperty();
	private StringProperty statusProperty = new SimpleStringProperty();
	
	private MzIdentMLControllerImpl mzIdentMlController = null;
	
	public MzIdentMLReader() {
		// empty
	}
	
	public boolean fileExists(String filePath) {
		return Files.exists(Paths.get(filePath));
	}
	
	public PeptideId read(String filePath) throws IOException {
		PeptideId peptideId = null;
		
		File inputFile = new File(filePath);
		mzIdentMlController = new MzIdentMLControllerImpl(inputFile, true);
		mzIdentMlController.close();
		return peptideId;
	}
	
	public ReadOnlyDoubleProperty getProgressProperty() {
		return progressProperty;
	}
	
	@SuppressWarnings("unused")
	private void setProgressProperty(double value) {
		progressProperty.set(value);
	}
	
	public ReadOnlyStringProperty getStatusProperty() {
		return statusProperty;
	}
	
	@SuppressWarnings("unused")
	private void setStatusProperty(String status) {
		statusProperty.set(status);
	}
	
}
