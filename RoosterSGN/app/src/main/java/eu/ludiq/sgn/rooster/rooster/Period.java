package eu.ludiq.sgn.rooster.rooster;

import org.json.JSONObject;

/**
 * Contains a subject, classroom, teacher and group (or there's no class).
 * 
 * @author Ludo Pulles
 * 
 */
public class Period {

	private int index;
	private String subject;
	private String classroom;
	private String teacher;
	private String group;
	private ClassType type;

	public Period(int index, ClassType type) {
		this(index, null, null, null, null, type);
	}

	public Period(int index, JSONObject les) {
		this(index, les.optString("docent"), les.optString("vak"), les
				.optString("lokaal"), les.optString("klas"), ClassType.CLASS);
	}

	private Period(int index, String docent, String vak, String lokaal,
			String klas, ClassType type) {
		this.index = index;
		this.teacher = docent;
		this.subject = vak;
		this.classroom = lokaal;
		this.group = klas;
		this.type = type;
	}

	@Override
	public String toString() {
		return "u=" + index + ",type" + type.toString();
	}

	public int getIndex() {
		return index;
	}

	public String getSubject() {
		return subject;
	}

	public String getClassroom() {
		return classroom;
	}

	public String getTeacher() {
		return teacher;
	}

	public String getGroup() {
		return group;
	}

	public ClassType getClassType() {
		return type;
	}
}