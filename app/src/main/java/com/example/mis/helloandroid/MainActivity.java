package com.example.mis.helloandroid;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.util.Arrays;
import java.util.Scanner;
import java.util.zip.Inflater;


public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button mConnectButton;
    private String mText;
    private EditText e;
    private static final String TAG = "Main Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mConnectButton = (Button) findViewById(R.id.connectButton);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                e = (EditText) findViewById(R.id.urlText);
                mText = e.getText().toString();

                final GetURL getURL = new GetURL(MainActivity.this);
                getURL.execute(mText);
                Log.i(TAG, "2"+ mText);
            }
        });
    }

    @Override
    public void onClick(View view) {

    }


    class GetURL extends AsyncTask<String, Void, String> {

        private Context context;
        private View v;
        private PowerManager.WakeLock mWakeLock;

        private URL mUrl;
        private String mContentType;

        private ByteArrayOutputStream mOutputStream;

        public GetURL(Context context) {
            this.context = context;
            this.v = v;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //https://developer.android.com/training/scheduling/wakelock
            // WakeLock to keep the CPU running while we download (even if screen turns off)
            PowerManager power = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();

        }

        @Override
        protected String doInBackground(String... urls) {
            InputStream in = null;
            HttpURLConnection connection = null;

            try {
                mUrl = new URL(urls[0]);

                // https://developer.android.com/reference/java/net/HttpURLConnection
                connection = (HttpURLConnection)mUrl.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return ("Bad connection! " + connection.getResponseCode() + ": " + connection.getResponseMessage());
                }

                mContentType = connection.getContentType();

                if(mContentType.contains("text/html")) {
                    return null;
                }

                in = connection.getInputStream();
                mOutputStream = new ByteArrayOutputStream();

                byte data[] = new byte[4096];
                int count;

                while ((count = in.read(data)) != -1) {
                    mOutputStream.write(data, 0, count);
                }

            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (mOutputStream != null) {
                        mOutputStream.close();
                    }
                    if (in != null){
                        in.close();
                    }
                } catch (Exception e) {
                    return e.toString();
                }
                if (connection != null) {
                    connection.disconnect();
                }
            }
            return null;

        }

        protected void onPostExecute(String result) {

            mWakeLock.release();

            super.onPostExecute(result);

            if (result != null) {
                Toast toast = Toast.makeText(context, "Error " + result, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
                reset();
            }
            else {
                Toast toast = Toast.makeText(context, "Success!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0 ,0);
                toast.show();
                reset();

                if(mContentType.contains("text/plain")) {
                    showText();
                }
                if(mContentType.contains("text/html")){
                    showHTML();
                }
                else {
                    Toast.makeText(context, "Type not supported" +
                            mContentType, Toast.LENGTH_LONG).show();
                }
            }
        }

        // Problem with findViewbyId - solved with: https://stackoverflow.com/questions/4979454/the-method-findviewbyidint-is-undefined
        private void showText(){
            TextView text = null;
            text = (TextView) MainActivity.this.findViewById(R.id.showPlaintext);
            Toast toast = Toast.makeText(context, "Show Text", Toast.LENGTH_LONG);
            toast.show();
            text.setVisibility(View.VISIBLE);
            text.setText(mOutputStream.toString());
        }

        private void showHTML(){
            WebView website = null;
            website = (WebView) MainActivity.this.findViewById(R.id.showWeb);
            Toast toast = Toast.makeText(context, "Show Website", Toast.LENGTH_LONG);
            toast.show();
            website.setVisibility(View.VISIBLE);
            website.loadUrl(mUrl.toString());
        }

        private void reset(){
            TextView text = (TextView) MainActivity.this.findViewById(R.id.showPlaintext);
            WebView web = (WebView) findViewById(R.id.showWeb);
                text.setVisibility(View.GONE);
                web.setVisibility(View.GONE);
            }
        }
}
