package com.aggreyah.notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public class NoteKeeperProviderContract {
    private NoteKeeperProviderContract(){}
    private static final String AUTHORITY = "com.aggreyah.notekeeper.provider";
    private static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    protected interface CourseIdColumns{
        public static final String COLUMN_COURSE_ID = "course_id";
    }

    protected interface CoursesColumns{
        public static final String COLUMN_COURSE_TITLE = "course_title";
    }

    protected interface NotesColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
    }

    public static final class Courses implements BaseColumns, CoursesColumns, CourseIdColumns{
        public static final String PATH = "courses";
//        content://com.aggreyah.notekeeper.provider/courses
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    public static final class Notes implements BaseColumns, NotesColumns, CourseIdColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }
}
