package eu.ludiq.roostersgn.rooster;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * A hour can contain more classes, however, in most cases it just contains one
 * class, and even in some cases none at all.
 * 
 * @author Ludo Pulles
 * 
 */
public class Hour {

	private Period[] classes;
	private ClassType type;

	public Hour(JSONArray array, int hour) {
		this.type = ClassType.CLASS;
		this.classes = new Period[array.length()];

		for (int i = 0; i < array.length(); i++) {
			try {
				classes[i] = new Period(hour, array.getJSONObject(i));
			} catch (JSONException e) {
				classes[i] = new Period(hour, type);
			}

		}
	}

	public Hour(ClassType type, int hour) {
		this.type = type;

		if (type == ClassType.FREE || type == ClassType.CANCELLED) {
			this.classes = new Period[] { new Period(hour, type) };
		} else {
			this.classes = new Period[0];
		}
	}

	public boolean hasClass() {
		return type.hasClass();
	}

	public Period getClass(int index) {
		return classes[index];
	}

	public int length() {
		return classes.length;
	}

	public ClassType getType() {
		return type;
	}
}