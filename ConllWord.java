package dt;

import java.util.ArrayList;
import java.util.List;

public class ConllWord {
	private String word, arrowLabel, posCG, posFG, posTag;
	private int parentIndex, index;
	private boolean isString = false;
	private List<Object> childCells = new ArrayList<>();
	private List<String[]> childValues = new ArrayList<>();
	
	public ConllWord(String i, String w, String cg, String fg, String pI, String aL) {
		index = Integer.parseInt(i.replace("-0", "")) - 1; 
		word = w;
		arrowLabel = aL;
		posCG = cg;
		posFG = fg;
		// If the fine grain POS tag is not equal to "_", the POS tag is set to that. Otherwise, 
		// the POS tag is set to the coarse grain POS tag.
		posTag = posFG.equals("_")?posCG:posFG;
		parentIndex = Integer.parseInt(pI) - 1;
	}
	
	public ConllWord(int i, String w, String cg, String fg, int pI, String aL) {
		index = i;
		word = w;
		arrowLabel = aL;
		posCG = cg;
		posFG = fg;
		// If the fine grain POS tag is not equal to "_", the POS tag is set to that. Otherwise, 
		// the POS tag is set to the coarse grain POS tag.
		posTag = posFG.equals("_")?posCG:posFG;
		parentIndex = pI;
	}
	
	public ConllWord(String w) { //For just String input (no other data known)
		index = 0; 
		word = w;
		posCG = "";
		posFG = "";
		posTag = "";
		parentIndex = 0;
		arrowLabel = "";
		isString = true;
	}
	
	public ConllWord() { //No data known yet
		index = 0; 
		word = "";
		posCG = "";
		posFG = "";
		posTag = "";
		parentIndex = 0;
		arrowLabel = "";
	}
	
	public String getWord() {
		return word;
	}
	
	public String getAL() {
		return arrowLabel;
	}
	
	public String getPOS() {
		return posTag;
	}
	
	public int getPI() {
		return parentIndex;
	}
	
	public int getIndex() {
		return index;
	}
	
	public String getCG() {
		return posCG;
	}
	
	public String getFG() {
		return posFG;
	}
	
	public ConllWord setPI(int pi) {
		parentIndex = pi;
		return this;
	}
	
	public ConllWord setAL(String al) {
		arrowLabel = al;
		return this;
	}
	
	public boolean isString() {
		return isString;
	}

	public void setPOS(String tag) {
		posCG = tag;
		posTag = tag;
		if (tag.equals("_")) {
			posFG = tag;
		}
	}
	
	public void addChildCells (Object[] ccs) {
		for (Object cc : ccs) {
			childCells.add(cc);
		}
	}
	
	public void addChildCell (Object cc) {
		childCells.add(cc);
	}
	
	public void addChildValue (String word, String pos) {
		childValues.add(new String[] {word, pos});
	}
	
	public List<String[]> getChildValues() {
		return childValues;
	}
	
	public List<Object> getChildCells() {
		return childCells;
	}
	
	public boolean isGroup() {
		return childCells.size()>0 || childValues.size()>0;
	}
	
	public List<Object> removeChildCells() {
		List<Object> hold = childCells;
		childCells.clear();
		return hold;
	}
}
