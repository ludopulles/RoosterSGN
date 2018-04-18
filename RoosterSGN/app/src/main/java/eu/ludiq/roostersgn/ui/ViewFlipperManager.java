package eu.ludiq.roostersgn.ui;

import eu.ludiq.roostersgn.rooster.Timetable;
import eu.ludiq.roostersgn.scroll.ScrollPosition;
import eu.ludiq.roostersgn.scroll.ViewFlipperCompat;
import eu.ludiq.roostersgn.util.DayUtil;
import eu.ludiq.roostersgn.util.TimeUtil;

/**
 * A helper class for connecting {@link DateView} and {@link ViewFlipperCompat}
 * 
 * @author Ludo Pulles
 * 
 */
public class ViewFlipperManager {

	private WeekLoader weekLoader;
	private DateComponent controller;
	private FlipperComponent scrollView;
	private int week, day;
	private boolean hasTimetable = false;

	public ViewFlipperManager(WeekLoader weekLoader, DateComponent controller,
			FlipperComponent scrollView) {
		this.weekLoader = weekLoader;
		this.controller = controller;
		this.scrollView = scrollView;

		this.controller.setListener(this);
		this.scrollView.setListener(this);
	}

	public void setInitialWeekAndDay(int week, int day) {
		this.week = week;
		this.day = day;

		controller.setWeek(week);
		controller.setDay(day);
		scrollView.setInitialDay(day);
	}

	public void setWeekAndDay(int week, int day) {
		this.week = week;
		this.day = day;

		controller.setWeek(week);
		controller.setDay(day);
		scrollView.setDay(day);
	}

	public void setDay(int day) {
		setWeekAndDay(this.week, day);
	}

	public void setTimetable(Timetable timetable) {
		hasTimetable = true;
		controller.setTimetable(timetable);
		scrollView.setTimetable(timetable);
	}

	public int getDay() {
		return day;
	}

	public int getWeek() {
		return week;
	}

	public void goLeft() {
		if (canScroll() && this.day >= DayUtil.MONDAY) {
			// previous day
			scrollView.scrollLeft();
		}
	}

	public void goRight() {
		if (canScroll() && this.day <= DayUtil.FRIDAY) {
			// next day
			scrollView.scrollRight();
		}
	}

	private boolean canLoadWeek() {
		return canScroll() && week != -1 && weekLoader != null
				&& weekLoader.canLoadWeek();
	}

	public void previousWeek() {
		if (canLoadWeek()) {
			weekLoader.loadWeek(TimeUtil.prevWeek(week), false);
		} else {
			setDay(DayUtil.MONDAY);
		}
	}

	public void nextWeek() {
		if (canLoadWeek()) {
			weekLoader.loadWeek(TimeUtil.nextWeek(week), true);
		} else {
			setDay(numberOfPages() - 1);
		}
	}

	public void stopLoading() {
		weekLoader.stopLoadingTimetable();
	}

	public boolean canScroll() {
		return hasTimetable;
	}

	public void setScrollPosition(ScrollPosition position) {
		controller.setScrollPosition(position);
		this.day = position.page;
	}

	public int numberOfPages() {
		return scrollView.numberOfPages();
	}

	// ------------------------------------------------------------------------
	// *** INTERFACES *********************************************************
	// ------------------------------------------------------------------------

	public static interface WeekLoader {

		public void loadWeek(int week, boolean forward);

		public boolean canLoadWeek();

		public void stopLoadingTimetable();
	}

	public static interface DateComponent {

		public void setDay(int day);

		public void setWeek(int week);

		public void setScrollPosition(ScrollPosition position);

		public void setTimetable(Timetable timetable);

		public void setListener(ViewFlipperManager listener);
	}

	public static interface FlipperComponent {

		public void setListener(ViewFlipperManager viewSyncer);

		public void setInitialDay(int day);

		public void setDay(int day);

		public void scrollLeft();

		public void scrollRight();

		public void setTimetable(Timetable timetable);

		public int numberOfPages();
	}

}
