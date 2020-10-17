package com.aggreyah.notekeeper;

import android.app.job.JobParameters;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.aggreyah.notekeeper.NoteKeeperProviderContract.NotesTable;


/**
 * Created by aggreyah.
 */

public class NoteUploader {
    private final String TAG = getClass().getSimpleName();

    private final Context mContext;
    private boolean mCanceled;

    public NoteUploader(Context context) {
        mContext = context;
    }

    public boolean isCanceled() {
        return mCanceled;
    }

    public void cancel() {
        mCanceled = true;
    }

    public void doUpload(Uri dataUri) {
        String[] columns = {
                NotesTable.COLUMN_COURSE_ID,
                NotesTable.COLUMN_NOTE_TITLE,
                NotesTable.COLUMN_NOTE_TEXT,
        };

        Cursor cursor = mContext.getContentResolver().query(dataUri, columns, null, null, null);
        int courseIdPos = cursor.getColumnIndex(NotesTable.COLUMN_COURSE_ID);
        int noteTitlePos = cursor.getColumnIndex(NotesTable.COLUMN_NOTE_TITLE);
        int noteTextPos = cursor.getColumnIndex(NotesTable.COLUMN_NOTE_TEXT);

        Log.i(TAG, ">>>*** UPLOAD START - " + dataUri + " ***<<<");
        mCanceled = false;
        while(!mCanceled && cursor.moveToNext()) {
            String courseId = cursor.getString(courseIdPos);
            String noteTitle = cursor.getString(noteTitlePos);
            String noteText = cursor.getString(noteTextPos);

            if(!noteTitle.equals("")) {
                Log.i(TAG, ">>>Uploading Note<<< " + courseId + "|" + noteTitle + "|" + noteText);
                simulateLongRunningWork();
            }
        }
        if(mCanceled)
            Log.i(TAG, ">>>*** UPLOAD !!CANCELED!! - " + dataUri + " ***<<<");
        else
            Log.i(TAG, ">>>*** UPLOAD COMPLETE - " + dataUri + " ***<<<");
        cursor.close();
    }

    private static void simulateLongRunningWork() {
        try {
            Thread.sleep(3000);
        } catch(Exception ex) {}
    }

}
