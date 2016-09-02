package eu.ludiq.sgn.rooster;

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import eu.ludiq.sgn.rooster.util.DataStorage;

/**
 * An activity for signing in with a username and a password.
 * 
 * @author Ludo Pulles
 * 
 */
public class SignInActivity extends Activity {

	public static final String ACTION_SIGN_IN = "sign_in";
	public static final String ACTION_SIGNED_IN = "signed_in";
	public static final String ACTION_APP_START = "app_start";
	public static final String EXTRA_WRONG_PASS = "wrong_password";

	private EditText user;
	private EditText pass;
	private TextView wrongPass;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Inloggen");

		setContentView(R.layout.sign_in);
		user = (EditText) findViewById(R.id.user);
		pass = (EditText) findViewById(R.id.pass);
		wrongPass = (TextView) findViewById(R.id.incorrect_wachtwoord);

		Intent intent = getIntent();
		if (intent != null) {
			boolean visible = intent.getBooleanExtra(EXTRA_WRONG_PASS, false);
			wrongPass.setVisibility(visible ? View.VISIBLE : View.GONE);
		}

		Preferences preferences = DataStorage.readPreferences(this);
		if (preferences != null) {
			// puts the cursor at the end
			user.setText("");
			user.append(preferences.getUsername());
			pass.setText("");
			pass.append(preferences.getPassword());
			if (!isStartedByTimetableActivity()) {
				finish();
				overridePendingTransition(R.anim.fade_in_enter,
						R.anim.fade_in_exit);

				Intent startIntent = new Intent(this, TimetableActivity.class);
				startIntent.setAction(ACTION_APP_START);
				startActivity(startIntent);
			}
		}

		user.addTextChangedListener(new UsernameListener());
		pass.addTextChangedListener(new PasswordListener());
	}

	public void onOK(View view) {
		String username = user.getText().toString();
		if (username != null && username.length() > 0) {
			String password = pass.getText().toString();
			DataStorage.savePreferences(this, new Preferences(username,
					password));

			setResult(RESULT_OK, null);
			finish();
			if (!isStartedByTimetableActivity()) {
				Intent signedInIntent = new Intent(this,
						TimetableActivity.class);
				signedInIntent.setAction(ACTION_SIGNED_IN);
				startActivity(signedInIntent);
			}
		}
	}

	public void onCancel(View view) {
		setResult(RESULT_CANCELED);
		finish();
	}

	private boolean isStartedByTimetableActivity() {
		return getIntent() != null && getIntent().getAction() != null
				&& getIntent().getAction().equals(ACTION_SIGN_IN);
	}

	private class PasswordListener implements TextWatcher {

		public void afterTextChanged(Editable s) {
			wrongPass.setVisibility(View.GONE);
			pass.removeTextChangedListener(this);
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}

	private class UsernameListener implements TextWatcher {

		public void afterTextChanged(Editable s) {
			int inputType = InputType.TYPE_CLASS_TEXT
					| InputType.TYPE_TEXT_VARIATION_NORMAL;
			if (s == null || s.length() == 0) {
				// empty string
				pass.setText("");
			} else if (Pattern.matches("\\d+", s)) {
				// als username alleen cijfers heeft, kopieer naar password
				pass.setText(s);
			} else {
				// heeft geen cijfers erin zitten
				inputType = InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD;
			}
			pass.setInputType(inputType);
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}
	}
}
