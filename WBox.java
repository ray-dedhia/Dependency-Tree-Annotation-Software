package dt;

public class WBox {
	public int x, y, h, w;
	public String word;
	
	public WBox (String val, int xI, int yI, int hI, int wI) {
		x = xI;
		y = yI;
		h = hI;
		w = wI;
		word = val;
		
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getW() {
		return w;
	}
	
	public int getH() {
		return h;
	}
	
	public String getWord() {
		return word;
	}
	
	public void changeX(int newX) {
		x = newX;
	}
	
	public void changeY(int newY) {
		y = newY;
	}
	
	public void changeH(int newH) {
		h = newH;
	}
	
	public void changeW(int newW) {
		w = newW;
	}
	
	public void changeWord(String newWord) {
		word = newWord;
	}
}
