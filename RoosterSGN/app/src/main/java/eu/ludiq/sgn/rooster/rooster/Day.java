package eu.ludiq.sgn.rooster.rooster;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A day consists of multiple hours.
 * 
 * @author Ludo Pulles
 * 
 * @see Timetable
 * @see Week
 * @see Hour
 * @see Period
 * 
 */
public class Day {

	private List<Hour> hours;

	public Day(JSONObject day) {
		this.hours = new ArrayList<Hour>();
		
		Hour hour;
		int i = 0;
		
		do {
			hour = null;
			try {
				Object obj = day.get("u" + (i + 1));
				if (obj instanceof JSONArray) {
					hour = new Hour((JSONArray) obj, i);
				} else if ("vrij".equalsIgnoreCase(obj.toString())) {
					hour = new Hour(ClassType.FREE, i);
				} else if ("vervallen".equalsIgnoreCase(obj.toString())) {
					hour = new Hour(ClassType.CANCELLED, i);
				}
			} catch (Exception e) {
				// JSONException or NullPointerException
			}
			if (hour != null) {
				this.hours.add(hour);
			}
			i++;
		} while (hour != null);
	}

	public Hour getHour(int index) {
		return this.hours.get(index);
	}

	public int length() {
		return this.hours.size();
	}
}
