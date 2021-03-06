package com.aggreyah.notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.aggreyah.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.aggreyah.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.aggreyah.notekeeper.NoteKeeperProviderContract.CoursesTable;
import com.aggreyah.notekeeper.NoteKeeperProviderContract.NotesTable;
import com.aggreyah.notekeeper.NoteKeeperProviderContract.CoursesIdColumns;

public class NoteKeeperProvider extends ContentProvider {
    public static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    public static final int COURSES_ROW = 4;
    public static final int NOTES_EXTENDED_ROW = 5;
    private NoteKeeperOpenHelper mDbOpenHelper;
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static final int COURSES = 0;

    public static final int NOTES = 1;

    public static final int NOTES_EXTENDED = 2;

    public static final int NOTES_ROW = 3;

    static {
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, CoursesTable.PATH, COURSES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NotesTable.PATH, NOTES);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NotesTable.PATH_EXTENDED, NOTES_EXTENDED);
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, NotesTable.PATH + "/#", NOTES_ROW);
    }
    public NoteKeeperProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case NOTES_EXTENDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(CourseInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.delete(NoteInfoEntry.TABLE_NAME, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXTENDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }
        return nRows;
    }

    @Override
    public String getType(Uri uri) {
        String mimeType = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case COURSES:
                // this mime type may return multiple rows.
                /**custom mime types specific to an application as opposed to generic are prefixed
                 * by vnd
                 **/
                //vnd.android.cursor.dir/vnd.com.aggreyah.notekeeper.provider.courses.
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + CoursesTable.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NotesTable.PATH;
                break;
            case NOTES_EXTENDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NotesTable.PATH_EXTENDED;
                break;
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        MIME_VENDOR_TYPE + NotesTable.PATH;
        }

        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDbOpenHelper.getWritableDatabase();
        long rowId = -1;
        Uri rowUri = null;
        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case NOTES:
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values);
                //content://com.aggreyah.notekeeper.provider/notes/1
                rowUri = ContentUris.withAppendedId(NotesTable.CONTENT_URI, rowId);
                break;
            case COURSES:
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(CoursesTable.CONTENT_URI, rowId);
                break;
            case NOTES_EXTENDED:
                // throw an exception this is a read only table.
//                throw  new ReadOnlyTable()
                break;
        }

        return rowUri;
    }

    @Override
    public boolean onCreate() {
        mDbOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch (uriMatch){
            case  COURSES:
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES:
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case NOTES_EXTENDED:
                cursor = notesExtendedQuery(db, projection, selection, selectionArgs, sortOrder);
                break;
            case NOTES_ROW:
                long rowId = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID + " = ?";
                String[] rowSelectionArgs = new String[] {Long.toString(rowId)};
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection,
                        rowSelectionArgs, null, null, null);
                break;
        }
        return cursor;
    }

    private Cursor notesExtendedQuery(SQLiteDatabase db, String[] projection, String selection,
                                      String[] selectionArgs, String sortOrder) {

        String[] columns = new String[projection.length];
        for (int idx = 0; idx < projection.length; idx++){
            columns[idx] = projection[idx].equals(BaseColumns._ID)  ||
                    projection[idx].equals(CoursesIdColumns.COLUMN_COURSE_ID) ?
                    NoteInfoEntry.getQName(projection[idx]) : projection[idx];
        }

        String tablesWithJoin = NoteInfoEntry.TABLE_NAME + " JOIN " + CourseInfoEntry.TABLE_NAME +
                " ON " + NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID) + " = " +
                CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);
        return db.query(tablesWithJoin, columns, selection, selectionArgs,
                null, null, sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        long rowId = -1;
        String rowSelection = null;
        String[] rowSelectionArgs = null;
        int nRows = -1;
        SQLiteDatabase db = mDbOpenHelper.getReadableDatabase();

        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch) {
            case COURSES:
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES:
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case NOTES_EXTENDED:
                // throw exception saying that this is a read-only table
            case COURSES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = CourseInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(CourseInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_ROW:
                rowId = ContentUris.parseId(uri);
                rowSelection = NoteInfoEntry._ID + " = ?";
                rowSelectionArgs = new String[]{Long.toString(rowId)};
                nRows = db.update(NoteInfoEntry.TABLE_NAME, values, rowSelection, rowSelectionArgs);
                break;
            case NOTES_EXTENDED_ROW:
                // throw exception saying that this is a read-only table
                break;
        }

        return nRows;
    }
}
