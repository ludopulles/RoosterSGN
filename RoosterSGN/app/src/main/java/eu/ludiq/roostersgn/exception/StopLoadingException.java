package eu.ludiq.roostersgn.exception;

/**
 * An exception for situation where the loading of the timetable has to be
 * stopped.
 * 
 * @author Ludo Pulles
 * 
 */
public class StopLoadingException extends Exception {

	public StopLoadingException() {
		super("The loading must be stopped.");
	}
}
