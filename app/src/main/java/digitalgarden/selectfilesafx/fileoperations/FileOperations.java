package digitalgarden.selectfilesafx.fileoperations;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


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
    public static Uri getStartingDirectoryAsUri( Context context )
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


    /**
     * Sample method to read content of file uri using BufferedReader and InputStreamReader
     * @param context application context
     * @param uri uri of the file
     * @return content or some infomation about errors
     */
    public static String readFileContent(Context context, Uri uri)
        {
        // https://docs.oracle.com/javase/7/docs/technotes/guides/language/try-with-resources.html
        try ( BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        context.getContentResolver().openInputStream(uri))))
            {
            StringBuilder stringBuilder = new StringBuilder();
            String currentline;
            while ((currentline = reader.readLine()) != null)
                {
                stringBuilder.append(currentline).append("\n");
                }
            return stringBuilder.toString();
            }
        catch (FileNotFoundException e)
            {
            return "FILE NOT FOUND";
            }
        catch (IOException e)
            {
            return "I/O ERROR";
            }
        }


    /**
     * Sample method to write content to file uri using BufferedWriter and OutputStreamWriter
     * @param context application context
     * @param uri uri of the file
     * @param content String content to write to the file
     * @return content or some infomation about errors
     */
    public static String writeFileContent(Context context, Uri uri, String content)
        {
        try ( BufferedWriter writer = new BufferedWriter
                ( new OutputStreamWriter
                        ( context.getContentResolver().openOutputStream( uri ))))
            {
            writer.write( content );
            return content;
            }

            /* Writer is character based (with encodings), while Stream is binary, data should be
             * encoded previously

            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream fileOutputStream =
                    new FileOutputStream(parcelFileDescriptor.getFileDescriptor());

            fileOutputStream.write( content.getBytes());

            fileOutputStream.close();
            parcelFileDescriptor.close();
             */

        catch (FileNotFoundException e)
            {
            return "FILE NOT FOUND";
            }
        catch (IOException e)
            {
            return "I/O ERROR";
            }
        }

    }
