package eu.ludiq.sgn.rooster.net;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import eu.ludiq.sgn.rooster.exception.ServerException;
import eu.ludiq.sgn.rooster.exception.WrongPasswordException;
import eu.ludiq.sgn.rooster.rooster.Timetable;

/**
 * Helper class for requesting a website.
 * 
 * @author Ludo Pulles
 * 
 */
public class HttpRequest {

	private static final String TAG = "Http Request";

	private Handler mHandler;
	private String mUrl = "", mUserAgent = "";
	private List<HttpCallback> mCallbacks = new ArrayList<HttpCallback>();
	private List<BasicNameValuePair> mParameters = new ArrayList<BasicNameValuePair>();
	private HttpClient mHttpClient = null;
	private volatile boolean mStop = false;

	public HttpRequest() {
		this.mHandler = new Handler(new Handler.Callback() {

			public boolean handleMessage(Message msg) {
				Iterator<HttpCallback> i = mCallbacks.iterator();
				while (i.hasNext()) {
					HttpUtils.handleCallback(i.next(), msg);
				}
				return true;
			}
		});
	}

	public void addCallback(HttpCallback callback) {
		this.mCallbacks.add(callback);
	}

	public void clearParameters() {
		this.mParameters.clear();
	}

	public void addParameter(BasicNameValuePair parameter) {
		this.mParameters.add(parameter);
	}

	public void setUrl(String url) {
		this.mUrl = url;
	}

	public void setUserAgent(String userAgent) {
		this.mUserAgent = userAgent;
	}

	public void stop() {
		new Thread(new Runnable() {

			public void run() {
				if (!mStop) {
					mStop = true;
					if (mHttpClient != null) {
						Log.w(TAG, "Shutting down http request!");
						mHttpClient.getConnectionManager().shutdown();
					}
				}
			}
		}, "Stop thread").start();
	}

	public void request() {
		new Thread(new Runnable() {
			public void run() {
				_request();
			}
		}, "Http request thread").start();
	}

	private void _request() {
		mHttpClient = HttpUtils.createHttpClient();

		try {
			String str = HttpUtils.getContent(mHttpClient, mUrl, mUserAgent,
					mParameters);
			JSONObject object = new JSONObject(str);

			try {
				Timetable timetable = new Timetable(object);
				
				mHandler.sendMessage(mHandler.obtainMessage(
						HttpUtils.STATUS_OK, timetable));
			} catch (WrongPasswordException e) {
				Log.w(TAG, "while loading: " + e.getMessage());
				mHandler.sendMessage(mHandler.obtainMessage(
						HttpUtils.ERROR_WRONG_PASSWORD, e));
			} catch (JSONException e) {
				Log.w(TAG, "while loading: " + e.getMessage());
				mHandler.sendMessage(mHandler.obtainMessage(
						HttpUtils.ERROR_TIMETABLE_PARSE, e));
			} catch (ServerException e) {
				Log.w(TAG, "while loading: " + e.getMessage());
				mHandler.sendMessage(mHandler.obtainMessage(
						HttpUtils.ERROR_SERVER_ERROR, e));
			}
		} catch (IllegalStateException e) {
			// on particular devices, the shutdown (caused by stop()) causes the
			// http request to throw an illegal state exception, e.g. Wolfgang
			Log.w(TAG, "while loading: " + e.getMessage());
			mHandler.sendEmptyMessage(HttpUtils.STATUS_STOPPED_LOADING);
		} catch (IOException e) {
			Log.w(TAG, "while loading: " + e.getMessage());
			if (mStop) {
				mHandler.sendEmptyMessage(HttpUtils.STATUS_STOPPED_LOADING);
			} else {
				mHandler.sendMessage(mHandler.obtainMessage(
						HttpUtils.ERROR_INTERNET, e));
			}
		} catch (JSONException e) {
			Log.w(TAG, "while loading: " + e.getMessage());
			mHandler.sendMessage(mHandler.obtainMessage(
					HttpUtils.ERROR_PARSING, e));
		}

	}
}
