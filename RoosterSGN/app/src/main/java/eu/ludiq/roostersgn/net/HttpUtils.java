package eu.ludiq.roostersgn.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import android.os.Message;
import android.util.Log;

import eu.ludiq.roostersgn.rooster.Timetable;

/**
 * Utility methods
 *
 * @author Ludo Pulles
 */
public class HttpUtils {

    private static final String TAG = "Http Utils";

    public static final String ENCODING = "iso-8859-1";

    /**
     * When everything is OK
     */
    public static final int STATUS_OK = 0;

    /**
     * The loading must be stopped
     */
    public static final int STATUS_STOPPED_LOADING = 1;

    /**
     * Couldn't connect with the server
     */
    public static final int ERROR_INTERNET = 2;

    /**
     * The response couldn't be parsed
     */
    public static final int ERROR_PARSING = 3;

    /**
     * The response was a malformed timetable
     */
    public static final int ERROR_TIMETABLE_PARSE = 4;

    /**
     * The server encountered an error.
     */
    public static final int ERROR_SERVER_ERROR = 5;

    /**
     * The user gave a wrong password
     */
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

    public static String getContent(HttpURLConnection connection, HashMap<String, String> parameters)
            throws IOException {
        Log.i(TAG, "request start: " + connection.getURL());

        connection.setReadTimeout(5000);
        connection.setConnectTimeout(5000);
        connection.setRequestMethod("POST");
        connection.setDoInput(true);
        connection.setDoOutput(true);

        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(os, "UTF-8"));
        writer.write(getQuery(parameters));
        writer.flush();
        writer.close();
        os.close();

        connection.connect();
        connection.getInputStream();

        Log.i(TAG, "request done, status code: "
                + connection.getResponseCode() + " " + connection.getResponseMessage());

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder();
        String line;
        while ((line = in.readLine()) != null)
            builder.append(line);
        in.close();
        return builder.toString();
    }

    /**
     * Creates a string that is in the format for posting (key,value)-pairs
     * See https://stackoverflow.com/questions/9767952/how-to-add-parameters-to-httpurlconnection-using-post
     * @param params all the params that need to be concatenated
     * @return a concatenation of the params
     * @throws UnsupportedEncodingException
     */
    private static String getQuery(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> pair : params.entrySet()) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.getValue(), "UTF-8"));
        }

        return result.toString();
    }

}
