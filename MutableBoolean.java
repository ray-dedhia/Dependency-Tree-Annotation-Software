package dt;

/**Latest version. Used to hold boolean values that may be changed. (See MyEditorMenuBar2 => Options) */
public class MutableBoolean {

	private boolean mValue;
	
	public MutableBoolean(boolean value) {
		setValue(value);
	}
	
	public void setValue(boolean value) {
		mValue = value;
	}
	
	public boolean getValue() {
		return mValue;
	}
	
}
