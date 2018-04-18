package eu.ludiq.roostersgn.debug;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class CrashActivity extends Activity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		TextView view = new TextView(this);
		view.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		final String crash = getIntent().getStringExtra("fout");
		view.setText(crash);

		ScrollView scroll = new ScrollView(this);
		scroll.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));

		Button button = new Button(this);
		button.setText("Verzend per e-mail!");
		button.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_SENDTO);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_EMAIL, "ludo.pulles@gmail.com");
				intent.putExtra(Intent.EXTRA_SUBJECT, "Mijn app crashte!");
				intent.putExtra(Intent.EXTRA_TEXT, "Fix het!\n" + crash);
				startActivity(Intent.createChooser(intent, "Send Email"));
			}
		});

		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(button);
		layout.addView(view);
		scroll.addView(layout);
		setContentView(scroll);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			getSharedPreferences("debug", MODE_PRIVATE).edit().clear().commit();
		}
	}

}
