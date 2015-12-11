package eu.ludiq.sgn.rooster.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;

import android.os.Message;
import android.util.Log;
import eu.ludiq.sgn.rooster.rooster.Timetable;

/**
 * Utility methods
 * 
 * @author Ludo Pulles
 * 
 */
public class HttpUtils {

	private static final String TAG = "Http Utils";

	public static final String ENCODING = "iso-8859-1";

	/** When everything is OK */
	public static final int STATUS_OK = 0;

	/** The loading must be stopped */
	public static final int STATUS_STOPPED_LOADING = 1;

	/** Couldn't connect with the server */
	public static final int ERROR_INTERNET = 2;

	/** The response couldn't be parsed */
	public static final int ERROR_PARSING = 3;

	/** The response was a malformed timetable */
	public static final int ERROR_TIMETABLE_PARSE = 4;
	
	/** The server encountered an error. */
	public static final int ERROR_SERVER_ERROR = 5;
	
	/** The user gave a wrong password */
	public static final int ERROR_WRONG_PASSWORD = 6;

	public static void handleCallback(HttpCallback callback, Message message) {
		if (message.what == STATUS_OK) {
			callback.onLoaded((Timetable) message.obj);
		} else if (message.what == STATUS_STOPPED_LOADING) {
			callback.onLoadingStopped();
		} else if (message.obj instanceof Exception) {
			callback.handleError(message.what, (Exception) message.obj);
		}
	}

	public static HttpClient createHttpClient() {
		BasicHttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
		HttpConnectionParams.setSoTimeout(httpParams, 5000);
		return new DefaultHttpClient(httpParams);
	}

	public static String getContent(HttpClient httpClient, String url,
			String userAgent, List<BasicNameValuePair> parameters)
			throws IOException, IllegalStateException {
		Log.i(TAG, "request start: " + url);
		if (userAgent != null && userAgent.length() > 0) {
			httpClient.getParams().setParameter(CoreProtocolPNames.USER_AGENT,
					userAgent);
		}

		HttpPost post = new HttpPost(url);
		// set up the post parameters if there are any
		if (parameters != null && parameters.size() > 0) {
			post.setEntity(new UrlEncodedFormEntity(parameters, ENCODING));
		}

		HttpResponse response = httpClient.execute(post);
		Log.i(TAG, "request done, status code: "
				+ response.getStatusLine().getStatusCode());

		BufferedReader in = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent()));
		StringBuilder builder = new StringBuilder();
		String line = in.readLine();
		while (line != null) {
			builder.append(line);
			line = in.readLine();
		}
		in.close();
		return builder.toString();
	}

}
