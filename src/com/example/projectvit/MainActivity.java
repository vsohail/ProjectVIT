package com.example.projectvit;



import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.TokenPair;

public class MainActivity extends Activity {

	private boolean mLoggedIn;
    private Button mSubmit;
    private Button mStart;
    private Button mComment;
    private Button mRefresh;
    private LinearLayout mDisplay;
    private ImageView mImage;
    private EditText mEdit;
    private TextView mText;
	final static private String APP_KEY = "s5rgc0k4hqlvv95";
	final static private String APP_SECRET = "rrayor2qc4kqp5d";
	final static private AccessType ACCESS_TYPE = AccessType.APP_FOLDER;
	final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
    private final String PHOTO_DIR = "/";
    // In the class declaration section:
	private DropboxAPI<AndroidAuthSession> mApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AndroidAuthSession session = buildSession();
        mApi = new DropboxAPI<AndroidAuthSession>(session);
		setContentView(R.layout.activity_main);
		
        mSubmit = (Button)findViewById(R.id.auth_button);
        mSubmit.setOnClickListener(new OnClickListener() {   
        	public void onClick(View v) {
                
        		// This logs you out if you're logged in, or vice versa
                if (mLoggedIn) {
                    logOut();
                } else {
                    // Start the remote authentication
                    mApi.getSession().startAuthentication(MainActivity.this);
                }
            }
        });
        mImage = (ImageView)findViewById(R.id.imageView1);
        mDisplay = (LinearLayout)findViewById(R.id.linearLayout1);
        mRefresh = (Button)findViewById(R.id.auth_button2);
        mRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				RefreshIt ref = new RefreshIt(MainActivity.this, mApi, PHOTO_DIR);
		        ref.execute();
			}
		});
        mStart = (Button)findViewById(R.id.button1);
        mStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DownloadRandomPicture download = new DownloadRandomPicture(MainActivity.this, mApi, PHOTO_DIR, mImage);
                download.execute();
			}
		});
        mEdit   = (EditText)findViewById(R.id.editText1);
        mComment = (Button)findViewById(R.id.auth_button1);
        mText = (TextView)findViewById(R.id.textView1);
        mText.setMovementMethod(new ScrollingMovementMethod());
        mComment.setOnClickListener(
            new View.OnClickListener()
            {
                public void onClick(View view)
                {
                	
    		       
                    String Comment=mEdit.getText().toString();
                    try{
                    	
                        FileWriter fstream = new FileWriter("/data/data/com.example.projectvit/cache/comments.txt",true);
                        BufferedWriter fbw = new BufferedWriter(fstream);
                        fbw.newLine();
                        if(Comment!=null)
                        {
                        fbw.write(Comment);
                        }
                        fbw.close();
                    }catch (Exception e) {
                        Log.i("Error: ",e.getMessage());
                    }
                    
                   
                    try {
            			File file = new File("/data/data/com.example.projectvit/cache/comments.txt");
            			FileReader fileReader = new FileReader(file);
            			BufferedReader bufferedReader = new BufferedReader(fileReader);
            			StringBuffer stringBuffer = new StringBuffer();
            			String line;
            			mText.setText("");
            			while ((line = bufferedReader.readLine()) != null) {
                            mText.append(line+"\n");
            			}
            			fileReader.close();
     
            		} catch (IOException e) {
            			Log.i("Error: ",e.getMessage());
            		}
                    
                    CheckCommFiles download = new CheckCommFiles(MainActivity.this, mApi, PHOTO_DIR);
                    download.execute();
                    
                    
                }
            });
       
        
	}
	 private void logOut() {
	        // Remove credentials from the session
	        mApi.getSession().unlink();

	        // Clear our stored keys
	        clearKeys();
	        // Change UI state to display logged out version
	        setLoggedIn(false);
	    }

	    /**
	     * Convenience function to change UI state based on being logged in
	     */
	    private void setLoggedIn(boolean loggedIn) {
	    	mLoggedIn = loggedIn;
	    	if (loggedIn) {
	    		mSubmit.setText("Unlink from Dropbox");
	            mDisplay.setVisibility(View.VISIBLE);
	    	} else {
	    		mSubmit.setText("Link with Dropbox");
	            mDisplay.setVisibility(View.GONE);
	            mImage.setImageDrawable(null);
	    	}
	    }
	    private AndroidAuthSession buildSession() {
	        AppKeyPair appKeyPair = new AppKeyPair(APP_KEY, APP_SECRET);
	        AndroidAuthSession session;

	        String[] stored = getKeys();
	        if (stored != null) {
	            AccessTokenPair accessToken = new AccessTokenPair(stored[0], stored[1]);
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE, accessToken);
	        } else {
	            session = new AndroidAuthSession(appKeyPair, ACCESS_TYPE);
	        }

	        return session;
	    }
	    private String[] getKeys() {
	        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
	        String key = prefs.getString(ACCESS_KEY_NAME, null);
	        String secret = prefs.getString(ACCESS_SECRET_NAME, null);
	        if (key != null && secret != null) {
	        	String[] ret = new String[2];
	        	ret[0] = key;
	        	ret[1] = secret;
	        	return ret;
	        } else {
	        	return null;
	        }
	    }

	 protected void onResume() {
	        super.onResume();
	        AndroidAuthSession session = mApi.getSession();

	    
	        if (session.authenticationSuccessful()) {
	            try {
	                // Mandatory call to complete the auth
	                session.finishAuthentication();

	                // Store it locally in our app for later use
	                TokenPair tokens = session.getAccessTokenPair();
	                storeKeys(tokens.key, tokens.secret);
	                setLoggedIn(true);
	                RefreshIt ref = new RefreshIt(MainActivity.this, mApi, PHOTO_DIR);
	                ref.execute();
	            } catch (IllegalStateException e) {
	            }
	        }
	    }

    private void storeKeys(String key, String secret) {
        // Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }

    private void clearKeys() {
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.clear();
        edit.commit();
    }
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

}
