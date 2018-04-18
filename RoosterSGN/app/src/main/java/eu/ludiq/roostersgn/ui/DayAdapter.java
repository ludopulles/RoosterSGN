package eu.ludiq.roostersgn.ui;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.TextView;
import eu.ludiq.roostersgn.R;
import eu.ludiq.roostersgn.rooster.ClassType;
import eu.ludiq.roostersgn.rooster.Day;
import eu.ludiq.roostersgn.rooster.Hour;
import eu.ludiq.roostersgn.rooster.Period;

/**
 * An adapter for a {@link Day}
 * 
 * @author Ludo Pulles
 * 
 */
public class DayAdapter extends BaseAdapter {

	private Context context;
	private Period[] hours;

	public DayAdapter(Context context, Day day) {
		this.context = context;
		ArrayList<Period> list = new ArrayList<Period>();
		for (int h = 0; h < day.length(); h++) {
			Hour hour = day.getHour(h);
			if (hour != null) {
				for (int c = 0; c < hour.length(); c++) {
					list.add(hour.getClass(c));
				}
			}
		}
		hours = list.toArray(new Period[list.size()]);
	}

	public int getCount() {
		return hours.length;
	}

	public Object getItem(int position) {
		return hours[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		Period period = hours[position];
		ClassType type = period.getClassType();
		if (convertView == null || !isSameContentType(convertView, type)) {
			int layoutres = -1;
			switch (type) {
			case CLASS:
				layoutres = R.layout.period_class;
				break;
			case FREE:
			case CANCELLED:
				layoutres = R.layout.period_free;
				break;
			case INVALID:
			default:
				return new TextView(context);
			}
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(layoutres, parent, false);
		} else {
			view = convertView;
			if (type != ClassType.CLASS && type != ClassType.FREE
					&& type != ClassType.CANCELLED) {
				return view;
			}
		}
		// class, free or cancelled
		TextView classHour = (TextView) view.findViewById(R.id.les_uur);
		String hourIndex = String.valueOf(period.getIndex() + 1);
		if (firstOfMultipleHours(position)) {
			classHour.setText(hourIndex);
			classHour.getLayoutParams().width = LayoutParams.WRAP_CONTENT;
		} else {
			classHour.setText("");
			classHour.setWidth((int) classHour.getPaint()
					.measureText(hourIndex));
		}

		if (type == ClassType.CLASS) {
			TextView classSubject = (TextView) view.findViewById(R.id.les_vak);
			TextView classClassRoom = (TextView) view
					.findViewById(R.id.les_lokaal);
			TextView classTeacher = (TextView) view
					.findViewById(R.id.les_docent);
			TextView classGroup = (TextView) view.findViewById(R.id.les_klas);

			classSubject.setText(period.getSubject());
			classClassRoom.setText(period.getClassroom());
			classTeacher.setText(period.getTeacher());
			classGroup.setText(period.getGroup());
		} else {
			// free or cancelled
			TextView lesVrij = (TextView) view.findViewById(R.id.les_vrij);
			lesVrij.setText(type.toString());
		}

		return view;
	}

	private boolean firstOfMultipleHours(int index) {
		if (index == 0)
			return true;
		return hours[index - 1].getIndex() != hours[index].getIndex();
	}

	private boolean isSameContentType(View convertView, ClassType type) {
		// INVALID = TextView
		if (convertView instanceof TextView && type == ClassType.INVALID)
			return true;
		// VRIJ of VERVALLEN (R.layout.list_item_vrij.xml)
		// heeft R.id.les_vrij
		View view = convertView.findViewById(R.id.les_vrij);
		if (view != null
				&& (type == ClassType.FREE || type == ClassType.CANCELLED))
			return true;
		// LES (R.layout.lst_item_les.xml)
		// heeft R.id.les_vak
		view = convertView.findViewById(R.id.les_vak);
		if (view != null && type == ClassType.CLASS)
			return true;
		return false;
	}
}
