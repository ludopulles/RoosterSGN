package eu.ludiq.roostersgn.net;

import eu.ludiq.roostersgn.rooster.Timetable;

/**
 * A callback interface which is called when a timetable has been loaded, or has
 * failed to load a timetable.
 * 
 * @author Ludo Pulles
 * 
 */
public interface HttpCallback {

	public void onLoaded(Timetable timetable);

	public void handleError(int errorCode, Exception e);

	public void onLoadingStopped();
}
