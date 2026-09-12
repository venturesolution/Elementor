package fasofts.element.ui;

import android.content.*;
import android.os.*;
import android.util.*;
import java.io.*;
import java.net.*;
//import fasofts.element.ui.HomeScreenActivity;

public class AppUpdateChecker extends AsyncTask<String, String, String> {
    private static final String TAG = "FASOFTS";
    private static final String PrivateKey = "FASOFTS";
    private Context context;
    private Listener listener;
    private String url;

    public interface Listener {
        void onLoading();
        void onCompleted(String config) throws Exception;
        void onCancelled();
        void onException(String ex);
    }

    public AppUpdateChecker(Context context, String url, Listener listener) {
        this.context = context;
        this.url = url;
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        listener.onLoading();
    }
    
    @Override
    protected String doInBackground(String... urlArg) {
  StringBuilder sb = new StringBuilder();
  try {
   String api = url;
            if(!api.startsWith("http")) {
                api = new StringBuilder().append("http://").append(url).toString();
            }

   URL url = new URL(api);
   HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
   httpURLConnection.setRequestMethod("GET");
   httpURLConnection.setConnectTimeout(90000);
            httpURLConnection.setReadTimeout(90000);
   httpURLConnection.setDoInput(true);
   httpURLConnection.connect();

   InputStreamReader isr = new InputStreamReader(httpURLConnection.getInputStream());
   BufferedReader bufferedReader = new BufferedReader(isr); 
            while (true) {
    String readLine = bufferedReader.readLine();
    if (readLine == null) {
     break;
    }
    sb.append(readLine);
   }

   bufferedReader.close();
   httpURLConnection.disconnect();
   if (httpURLConnection != null) {
    try {
     httpURLConnection.disconnect();
    } catch (Exception e3) {
     e3.printStackTrace();
    }
   }
   return sb.toString();
  } catch (Exception e) {
   e.printStackTrace();
            return "Error on getting data: " + e.getMessage();
  }
 }

    @Override
    protected void onCancelled() {
        super.onCancelled();
  // Log.i(TAG, "Cancelled");
  // pd.dismiss();
        listener.onCancelled();
    }

    @Override
    protected void onPostExecute(String result) {
        // wakeLock.release();
        // nm.cancel(1);
  // pd.dismiss();
  Log.i(TAG, PrivateKey);
  Log.i(TAG, "error while verifying the privateKey");
        try {
   if (result.equals("error")) {
    listener.onException(result);
   }
   else {
    listener.onCompleted(result);
   }
  }
  catch (Exception e){
   listener.onException(e.getMessage());
  }
    }

}
