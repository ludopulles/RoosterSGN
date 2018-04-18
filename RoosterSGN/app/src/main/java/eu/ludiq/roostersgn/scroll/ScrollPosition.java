package eu.ludiq.roostersgn.scroll;

/**
 * A helper class for the position of the {@link FlingAndScrollViewer}
 * 
 * @author Ludo Pulles
 * 
 */
public class ScrollPosition {

	public int page;
	public float percentage;

	public ScrollPosition(int page, float percentage) {
		this.page = page;
		this.percentage = percentage;
	}
}