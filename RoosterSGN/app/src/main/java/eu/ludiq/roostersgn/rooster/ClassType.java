package eu.ludiq.roostersgn.rooster;

/**
 * The ClassType shows if there is a class, or if there's no class.
 * 
 * @author Ludo Pulles
 * 
 */
public enum ClassType {

	/**
	 * Just a normal class
	 */
	CLASS(true),
	/**
	 * A normal class in the timetable where there are no classes.
	 */
	FREE,
	/**
	 * A class where there is no class, because a teacher was ill, etc.
	 */
	CANCELLED,
	/**
	 * Used with exceptions
	 */
	INVALID;

	private final boolean hasClass;

	private ClassType() {
		this(false);
	}

	private ClassType(boolean hasClass) {
		this.hasClass = hasClass;
	}

	public boolean hasClass() {
		return hasClass;
	}

	@Override
	public String toString() {
		switch (this) {
		case CLASS:
			return "les";
		case FREE:
			return "vrij";
		case CANCELLED:
			return "uitval";
		case INVALID:
		default:
			return "";
		}
	}

}
