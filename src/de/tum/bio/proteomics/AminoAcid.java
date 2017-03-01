package de.tum.bio.proteomics;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents an amino acid.
 * @author Matthias Stahl
 *
 */

public class AminoAcid {
	private char letter;
	private List<Modification> modifications;

	public AminoAcid(char letter) {
		this.letter = Character.toUpperCase(letter);
		modifications = new ArrayList<Modification>();
	}
	
	public char getSingleLetterCode() {
		return letter;
	}
	
	public void setModification(Modification modification) {
		modifications.add(modification);
	}
	
	public void removeModification(Modification modification) {
		modifications.remove(modification);
	}
	
	public List<Modification> getModifications() {
		return modifications;
	}
}