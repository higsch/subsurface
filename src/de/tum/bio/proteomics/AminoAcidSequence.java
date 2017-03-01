package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * This class represents a general amino acid sequence.
 * @author Matthias Stahl
 *
 */

public abstract class AminoAcidSequence {
	
	private StringProperty stringSequence = new SimpleStringProperty();
	private IntegerProperty length = new SimpleIntegerProperty();
	private List<AminoAcid> listSequence;

	public AminoAcidSequence() {
		// Empty constructor
	}
	
	public AminoAcidSequence(String sequence) {
		setSequenceString(sequence);
	}
	
	public AminoAcidSequence(List<AminoAcid> sequence) {
		setSequenceList(sequence);
	}
	
	public String getSequenceAsString() {
		return stringSequence.get();
	}
	
	public void setSequenceString(String sequence) {
		this.stringSequence.set(sequence);
		length.set(sequence.length());
		listSequence = generateSequenceListFromString(sequence);
	}
	
	public StringProperty sequenceProperty() {
		return stringSequence;
	}
	
	public List<AminoAcid> getSequenceAsList() {
		return listSequence;
	}
	
	public void setSequenceList(List<AminoAcid> sequence) {
		this.listSequence = sequence;
		stringSequence.set(generateSequenceStringFromList(sequence));
		length.set(stringSequence.get().length());
	}
	
	public AminoAcid getAminoAcidAtPosition(int position) {
		return listSequence.get(position-1);
	}
	
	public char getAminoAcidAsSingleLetterAtPosition(int position) {
		if (position-1 < listSequence.size()) {
			return listSequence.get(position-1).getSingleLetterCode();
		}
		return "X".charAt(0);
	}
	
	public int getLength() {
		return length.get();
	}
	
	public IntegerProperty lengthProperty() {
		return length;
	}
	
	public void assignModificationToAminoAcid(Modification modification, int position) {
		// Calculate real position
		List<List<Integer>> indices = Tools.simpleMap(stringSequence.get(), modification.getSequenceWindow());
		for (List<Integer> index : indices) {
			getAminoAcidAtPosition(position - index.get(0) + 1).setModification(modification);
		}
	}
	
	public void removeModificationFromAminoAcid(Modification modification, int position) {
		getAminoAcidAtPosition(position).removeModification(modification);
	}
	
	public Map<Integer, Modification> getModifications() {
		Map<Integer, Modification> result = new HashMap<>();
		for (AminoAcid aminoAcid : listSequence) {
			if (aminoAcid.getModifications().size() > 0) {
				for (Modification modification : aminoAcid.getModifications()) {
					result.put(listSequence.indexOf(aminoAcid) + 1, modification);
				}
			}
		}
		return result;
	}
	
	private String generateSequenceStringFromList(List<AminoAcid> listSequence) {
		StringBuilder tmp = new StringBuilder();
		for (AminoAcid letter : listSequence) {
			tmp.append(String.valueOf(letter.getSingleLetterCode()));
		}
		return tmp.toString();
	}
	
	private List<AminoAcid> generateSequenceListFromString(String stringSequence) {
		listSequence = new ArrayList<AminoAcid>();
		final int length = stringSequence.length();
		for (int i = 0; i < length; i++) {
			listSequence.add(new AminoAcid(stringSequence.charAt(i)));
		}
		return listSequence;
	}
}
