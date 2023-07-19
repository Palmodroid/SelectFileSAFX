package digitalgarden.selectfilesafx.selectfile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;


public class FileOperations
    {
    /**
     * Helper method to get starting folder from createOpenDocumentTreeIntent()
     * @param context application context
     * @return ACTION_OPEN_DOCUMENT_TREE intent contains starting folder as parcelable extra
     * "android.provider.extra.INITIAL_URI". This Uri is returned, or
     * null below API 29
     */
    private static Uri getRootOfOpenDocumentTree(Context context)
        {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q)
            {
            StorageManager storageManager = (StorageManager)
                    context.getSystemService(Context.STORAGE_SERVICE);

            Intent intent =
                    storageManager.getPrimaryStorageVolume().createOpenDocumentTreeIntent();

            return intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
            }
        // createOpenDocumentTree() works only above API 29 (Q)
        return null;
        }

    /**
     * Sets initial Uri to "Documents" (or other) folder, manipulating
     * Uri returned by getRootOfOpenDocumentTree(). Works only above API 29.
     * Source: <a href="https://stackoverflow.com/questions/67552027/android-11-action-open-document-tree-set-initial-uri-to-the-documents-folder/67553040#67553040">...</a>
     * @param uri Uri returned by getRootOfOpenDocumentTree()
     * @return Uri of the Documents (or other hard-coded) folder
     */
    private static Uri changeInitialFolder(Uri uri )
        {
        if ( uri != null )
            {
            //String startDir = "Android";
            //String startDir = "Download"; // Not choosable on an Android 11 device
            //String startDir = "DCIM";
            //String startDir = "DCIM/Camera";  // replace "/", "%2F"
            //String startDir = "DCIM%2FCamera";
            String startDir = "Documents";

            // Modify uri returned from Intent.ACTION_OPEN_DOCUMENT_TREE
            String scheme = uri.toString();

            Log.d("URI", "INITIAL_URI scheme: " + scheme);

            scheme = scheme.replace("/root/", "/document/");
            scheme += "%3A" + startDir;

            return Uri.parse(scheme);
            }
        // createOpenDocumentTree() works only above API 29 (Q),
        // so uri could be null
        return null;
        }


    /**
     * Returns the uri of "Documents" or other hard-coded folder to use it as initial folder for
     * OPEN_DOCUMENT_TREE. Works only above API 29
     * @param context application context
     * @return Uri of initial folder (currently "Documents") or null below API 29
     */
    public static Uri getStartingFolderAsUri(Context context )
        {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String dirString = sharedPreferences.getString("DIR", null);
        if (dirString != null)
            {
            return Uri.parse(dirString);
            }
        else
            {
            Uri uri = getRootOfOpenDocumentTree( context );
            return changeInitialFolder( uri );
            }
        }

    public static File getPrivateFolder(Context context )
        {
        return context.getExternalFilesDir(null); // app's private folder
        // <external-files-path name="external_files" path="." /> is needed inside file_provider_paths.xml

//        return context.getFilesDir(); // app's private folder
        // <files-path name="files" path="."/> is needed inside file_provider_paths.xml


        /* These are private folders as well - but not needed to list here
           Log.d("UP", "Dirs: " + getFilesDir() + " , " + getCacheDir() + " , "
           + getExternalFilesDir( Environment.DIRECTORY_PICTURES ) + " , " +
           getExternalMediaDirs()); */
        }


    /**
     * Helper method to close a stream without throwing IOException.
     * Android supports try-with only above API 19!
     * @param closeable stream to close
     */
    public static void closeSilently( Closeable closeable )
        {
        if ( closeable != null )
            {
            try
                {
                closeable.close();
                }
            catch ( IOException ioe )
                {
                ; // do nothing, this error cannot be noted
                }
            }
        }


    public static Uri findFileInFolder( Context context, String fileName, Uri folderUri )
        {
        DocumentFile folder = getDocumentFileFromFolderUri( context, folderUri);

        if ( folder != null )
            {
            DocumentFile file = folder.findFile(fileName);
            if ( file != null )
                {
                return file.getUri();
                }
            }
        return null;

        /*
        // This was a previous solution only for private folder
        // It was preserved to see, how to deal with file-based methods
        if ( folderUri == null )  // private folder
            {
            File folder = getPrivateFolder(context);
            File[] files = folder.listFiles();
            if ( files != null )
                {
                for (File file : files)
                    {
                    if (file.getName().equals(fileName))
                        {
                        fileUri = FileProvider.getUriForFile( context,
                                context.getApplicationContext().getPackageName() + ".provider",
                                file);
                        break;
                        }
                    }
                }
            }
         */
        }

    /**
     * Returns DocumentFile from folder-uri.
     * Folder uri can be null - private folder is used (check getPrivateFolder())
     * Folder uri can be file - these are sub-folders of private folder
     * Folder uri can be content - these are tree-uris returned by open-document-tree
     * Problem: whether "file" is folder or file - can be decided by isFile() or isDirectory()
     * BUT!!
     * How to decide if uri is tree-uri or single-uri?? isDocumentUri ?? It always resulted false.
     * @param context application context
     * @param folderUri uri of the folder
     * @return folder as DocumentFile
     */
    public static DocumentFile getDocumentFileFromFolderUri( Context context, Uri folderUri )
        {
        if (folderUri == null)
            {
            return DocumentFile.fromFile( FileOperations.getPrivateFolder(context) );
            }

        if (folderUri.getScheme().equals("file"))
            {
            return DocumentFile.fromFile(new File(folderUri.getPath()));
            }

        return DocumentFile.fromTreeUri(context, folderUri);
        }

    /*
     * getStartingDirectoryAsIntent() was needed by StartActivityForResult()
     * As that is deprecated, this method is not needed any more.
     * ActivityResultContracts.OpenDocumentTree() needs starting folder as Uri

    public static Intent getStartingDirectoryAsIntent( Context context )
        {
        Intent intent = getRootOfOpenDocumentTree( context );
        Uri uri;

        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String dirString = sharedPreferences.getString("DIR", null);
        if (dirString != null)
            {
            uri = Uri.parse(dirString);
            }
        else
            {
            uri = intent.getParcelableExtra("android.provider.extra.INITIAL_URI");
            uri = changeStartingDirectory( uri );
            }

        // if initial intent is needed
        intent.putExtra("android.provider.extra.INITIAL_URI", uri);

        return intent;
        }
     */

    }
