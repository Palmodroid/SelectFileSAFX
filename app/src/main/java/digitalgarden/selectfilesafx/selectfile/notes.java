package digitalgarden.selectfilesafx.selectfile;

/**
 * {@link SelectFileActivity} is a file browser to select a file, or even create a new one.
 * <p>
 * READ/WRITE permission is needed, but not checked (TODO)
 * <p>
 * It could handle file content. Intent filter to define:
 * <pre>{@code
 * <intent-filter>
 * <action android:name="android.intent.action.GET_CONTENT" />
 * <category android:name="android.intent.category.DEFAULT" />
 * <data android:mimeType="file/*" />
 * </intent-filter>
 * }</pre>
 * <p>
 * Extra data to set (Intent)
 * <p> Extra-string: Folder to open first (inside sd-card)
 * public static String DIRECTORY_SUB_PATH = "DIRECTORY_SUB_PATH";
 * <p> Extra-string: Only files with this ending will be shown
 * public static String FILE_ENDING = "FILE_ENDING";
 * <p> Extra-boolean: Creating of new files, dirs is allowed
 * public static String CREATE_ALLOWED = "CREATE_ALLOWED";
 * <p> Extra-string: Custom title
 * public static String CUSTOM_TITLE = "CUSTOM_TITLE";
 * <p>
 * Returned (extra) data
 * <p> Data: URL of the selected file (check whether file really exists!)
 * <p> Extra-string: Folder of the selected file
 * DIRECTORY_SUB_PATH
 * <p> Extra-stirng: name of the selected file
 * public static String SELECTED_FILE_NAME = "SELECTED_FILE_NAME";
 * <p> Extra-string: full path of the file (data url is difficult to use)
 * public static String SELECTED_FILE = "SELECTED_FILE";
 * <p>
 */
class notes
    {
    }
