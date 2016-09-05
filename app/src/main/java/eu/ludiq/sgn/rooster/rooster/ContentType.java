package eu.ludiq.sgn.rooster.rooster;

import eu.ludiq.sgn.rooster.TimetableActivity;

/**
 * The ContentType represents the type of a timetable.
 * 
 * @author Ludo Pulles
 * 
 */
public enum ContentType {

	STANDARD("", 900), NO_TIMETABLE("Geen rooster voor deze week.", 3600);

	private final String description;
	private final int refreshTime;

	private ContentType(String description, int refreshTime) {
		this.description = description;
		this.refreshTime = refreshTime;
	}

	/**
	 * Returns a possible message if a timetable has this content type.
	 * 
	 * @return a description of this content type.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Returns how old the timetable must be, in order to refresh the timetable
	 * when the user navigates to {@link TimetableActivity}, or -1 if the
	 * timetable doesn't need to be refreshed.
	 * 
	 * @return the refresh time in seconds
	 */
	public int getRefreshTime() {
		return refreshTime;
	}

}