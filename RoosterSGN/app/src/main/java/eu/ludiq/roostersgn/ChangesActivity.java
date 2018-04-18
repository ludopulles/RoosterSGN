package eu.ludiq.roostersgn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * An activity showing all changes made to the default timetable.
 * 
 * @author Ludo Pulles
 * 
 */
public class ChangesActivity extends Activity {

	public static final String EXTRA_CHANGES = "changes";

	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Wijzigingen");

		this.listView = new ListView(this);
		this.listView.setLayoutParams(new LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		setContentView(this.listView);

		Intent intent = getIntent();
		if (intent != null) {
			String[] changes = intent.getStringArrayExtra(EXTRA_CHANGES);
			if (changes == null || changes.length == 0) {
				finish();
			} else {
				ListAdapter adapter = new ArrayAdapter<String>(this,
						R.layout.change, changes);
				this.listView.setAdapter(adapter);
			}
		}
	}
}
