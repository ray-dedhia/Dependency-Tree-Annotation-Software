package dt;

public class Arrow {
	public WBox from;
	public WBox to;
	
	public Arrow (WBox f, WBox t) {
		from = f;
		to = t;		
	}
	
	public WBox getFrom() {
		return from;
	}
	
	public WBox getTo() {
		return to;
	}
}
