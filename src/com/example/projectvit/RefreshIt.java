package com.example.projectvit;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.DropboxFileInfo;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxIOException;
import com.dropbox.client2.exception.DropboxParseException;
import com.dropbox.client2.exception.DropboxPartialFileException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.exception.DropboxUnlinkedException;

/**
 * Here we show getting metadata for a directory and downloading a file in a
 * background thread, trying to show typical exception handling and flow of
 * control for an app that downloads a file from Dropbox.
 */

public class RefreshIt extends AsyncTask<Void, Long, Boolean> {


    private Context mContext;
    private DropboxAPI<?> mApi;
    private String mPath;
    private boolean mCanceled;
    private String mErrorMsg;
    private final ProgressDialog mDialog;
    private FileOutputStream mFos;
    // Note that, since we use a single file name here for simplicity, you
    // won't be able to use this code for two simultaneous downloads.
    private final static String COMMENT_FILE_NAME = "comments.txt";

    public RefreshIt(Context context, DropboxAPI<?> api,
            String dropboxPath) {
        // We set the context this way so we don't accidentally leak activities
        mContext = context.getApplicationContext();

        mApi = api;
        mPath = dropboxPath;
        mDialog = new ProgressDialog(context);
        mDialog.setMessage("Refreshing");
        mDialog.setButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mCanceled = true;
                mErrorMsg = "Canceled";

                // This will cancel the getThumbnail operation by closing
                // its stream
                if (mFos != null) {
                    try {
                        mFos.close();
                    } catch (IOException e) {
                    }
                }
            }
        });

        mDialog.show();
    }
    @Override
    protected Boolean doInBackground(Void... params) {
       // Get file.
		String cachePath = mContext.getCacheDir().getAbsolutePath() + "/" + COMMENT_FILE_NAME;
		FileOutputStream outputStream = null;
		try {
		    File file = new File(cachePath);
		    outputStream = new FileOutputStream(file);
		    DropboxFileInfo info = mApi.getFile("/Comments/comments.txt", null, outputStream, null);
		    Log.i("path:comment:",cachePath);
		} catch (DropboxException e) {
		    Log.e("DbExampleLog", "Something went wrong while downloading.");
		} catch (FileNotFoundException e) {
		    Log.e("DbExampleLog", "File not found.");
		} finally {
		    if (outputStream != null) {
		        try {
		            outputStream.close();
		        } catch (IOException e) {}
		    }
		}
		return true;
    }
    @Override
    protected void onPostExecute(Boolean result) {
    	mDialog.dismiss();
        if (result) {
        	
        } else {
            // Couldn't download it, so show an error
            showToast(mErrorMsg);
        }
    }

    private void showToast(String msg) {
        Toast error = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
        error.show();
    }


}
