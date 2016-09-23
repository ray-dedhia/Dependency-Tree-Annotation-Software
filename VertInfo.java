package dt;

import java.util.ArrayList;
import java.util.List;

public class VertInfo {
	public boolean expanded;
	public List<Object> inside = new ArrayList<Object>();
	
	public VertInfo() {
		expanded = true;
	}
	
	public void setB(boolean b) {
		expanded = b;
	}
	
	public boolean isExp() {
		return expanded;
	}
	
	public void addToAL(Object o) {
		inside.add(o);
	}
	
	public int getSize() {
		return inside.size();
	}
	
	public List<Object> getAL() {
		return inside;
	}
	
	public void rem(Object v) {
		if (inside.contains(v)) {
			inside.remove(v);
		}
	}
}
