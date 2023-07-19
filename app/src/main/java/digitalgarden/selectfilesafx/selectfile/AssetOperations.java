package digitalgarden.selectfilesafx.selectfile;


import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.documentfile.provider.DocumentFile;

import static digitalgarden.selectfilesafx.selectfile.FileOperations.closeSilently;

public class AssetOperations
    {
    /**
     * http://stackoverflow.com/a/11212942 - copy asset folder
     * http://stackoverflow.com/a/6187097 - compressed files in assets
     * http://stackoverflow.com/a/27673773 - assets should be created below main
     *
     * Copy assets from assets/files to targetUri. If target Uri is null, then private folder is
     * selected.
     * @param context context
     * @param targetUri treeUri of target folder, or null to copy to private folder
     */
    public static void copyAssets(Context context, Uri targetUri ) throws IOException
        {
        Log.i("ASSET", "Asset starts here");

        DocumentFile targetFolder = FileOperations.getDocumentFileFromFolderUri(context, targetUri);

        if ( targetFolder == null )
            {
            throw new IOException("Could not find target folder to copy assets!");
            }
        Log.i("ASSET", "Target folder to copy assets " + targetFolder);

        // Copying each file from assets
        try
            {
            String[] assetNames = context.getAssets().list("files");
            for ( String assetName : assetNames )
                {
                Log.i("ASSET", "Asset: " + assetName );
                copyAssetFile( context, assetName, targetFolder );
                }
            }
        catch (IOException e)
            {
            Log.e("ASSET","Cannot read assets!");
            }
        }


    public static void copyAssetFile(Context context, String assetName, DocumentFile targetFolder )
            throws IOException
        {
        InputStream assetStream = null;
        InputStream previousStream = null;
        OutputStream targetStream = null;

        try
            {
            assetStream = context.getAssets().open("files/" + assetName);

            // if target file already exists...
            DocumentFile previousFile = targetFolder.findFile(assetName);
            if (previousFile != null)
                {
                // compare these files
                previousStream = context.getContentResolver().openInputStream(previousFile.getUri());
                // ... and it is identical with asset - copy should stop
                if (compareStreams(assetStream, previousStream))
                    {
                    Log.e("ASSET",
                            "Asset and target files are identical, no copy is needed:" + assetName);
                    return;
                    }
                // ... and it is not identical with asset - it should be backed up
                else
                    {
                    String backupString;
                    StringBuilder backupNameBuilder = new StringBuilder();
                    int n = 0;
                    do
                        {
                        backupNameBuilder.setLength(0);
                        backupString = backupNameBuilder
                                .append(n++)
                                .append('_')
                                .append(assetName).toString();
                        } while (targetFolder.findFile(backupString) != null);
                    previousFile.renameTo(backupString);
                    Log.d("ASSET",
                            "Target file with same name is backed up: " + backupString);
                    }
                }

            // Create traget file to copy asset to
            DocumentFile targetFile = targetFolder.createFile("text/x-unknown", assetName);

            if (targetFile != null)
                {
                assetStream.reset();
                targetStream = context.getContentResolver().openOutputStream(targetFile.getUri());
                // Using text/plain mime type will add .txt extension; while text/x-unknown do not
                // How to open these files, does it matter to have "unknown" mime type??

                copyStreams(assetStream, targetStream);
                }
            }
        catch (FileNotFoundException fnfe)
            {
            Log.e("ASSET", "Could not find " + assetName + ", asset is skipped.");
            }
        finally
            {
            closeSilently(targetStream);
            closeSilently(previousStream);
            closeSilently(assetStream);
            }
        }


    /**
     * Compares two streams. The two streams are buffered.
     * (BufferedInputStream would close InputStream behind it, so assetStream would be closed)
     * @param streamA One stream
     * @param streamB Other stream
     * @return true, if streams are identical, false otherwise
     * @throws IOException if reading error occurs
     */
    private static boolean compareStreams( InputStream streamA, InputStream streamB )
        throws IOException
        {
        byte[] bufferA = new byte[1024];
        byte[] bufferB = new byte[1024];

        int length;
        do
            {
            if ( (length = streamA.read(bufferA)) == streamB.read(bufferB) )
                {
                while ( length > 0 )
                    {
                    length--;
                    if (bufferA[length] != bufferB[length])
                        {
                        return false;
                        }
                    }
                }
            else
                {
                return false;
                }
            } while ( length != -1 );

        return true;
        }


    /**
     * Copies inputStream to outputStream.
     * @param inputStream input stream
     * @param outputStream output stream
     * @throws IOException if reading error occurs
     */
    private static void copyStreams( InputStream inputStream, OutputStream outputStream )
            throws IOException
        {
        byte[] buffer = new byte[1024];
        int read;
        while((read = inputStream.read(buffer)) != -1)
            {
            outputStream.write(buffer, 0, read);
            }
        outputStream.flush();
        }
    }
