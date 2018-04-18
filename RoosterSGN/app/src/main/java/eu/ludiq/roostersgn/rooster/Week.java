package eu.ludiq.roostersgn.rooster;

import org.json.JSONException;
import org.json.JSONObject;

public class Week {

	private static final int DAYS = 5;
	
	private String source;
	private Day[] days = new Day[DAYS];

	public Week(JSONObject week) throws JSONException {
		this.source = week.toString();
		
		for (int i = 0; i < days.length; i++) {
			days[i] = new Day(week.getJSONObject("d" + (i + 1)));
		}
	}

	public Day getDay(int day) {
		return days[day];
	}

	public int length() {
		return days.length;
	}
	
	@Override
	public String toString() {
		return source;
	}

}
