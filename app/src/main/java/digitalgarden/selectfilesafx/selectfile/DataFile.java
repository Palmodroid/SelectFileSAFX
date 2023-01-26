package digitalgarden.selectfilesafx.selectfile;

import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

import androidx.core.content.FileProvider;
import androidx.documentfile.provider.DocumentFile;

/**
 * There are two main ways to store data in files
 * 1. App-specific storage (Internal or external) uses 'File' system
 * 2. SAF (Storage Access Framework) uses 'DocumentFile' system
 *
 * Eg. 'File' should be presented to other apps through FileProvider,
 * while DocumentFile-s cannot be presented on this way.
 * DocumentFile folders can be opened by the built-in file chooser, while File system can be
 * reached without the system.
 *
 * These two ways should be managed separately.
 * One of documentFile and file should be null.
 * (Both can be null)
 *
 * (Media content, Datasets and other ways are not covered yet)
 */
class DataFile
    {
    private final File file;
    private final DocumentFile documentFile;
    private final Uri uri;


    DataFile( File file )
        {
        this.file = file;
        this.documentFile = null;
        this.uri = null;
        }

    DataFile( DocumentFile documentFile )
        {
        this.file = null;
        this.documentFile = documentFile;
        this.uri = null;
        }

    DataFile( Context context, UriPermission uriPermission ) // tree-uri
        {
        this.file = null;
        this.uri = uriPermission.getUri();
        this.documentFile = DocumentFile.fromTreeUri(context, uri);
        }


    boolean isFileSystem()
        {
        return documentFile == null;
        }

    boolean isNull()
        {
        return file==null && documentFile==null;
        }

    boolean isDirectory()
        {
        if ( file != null )
            return file.isDirectory();

        if ( documentFile != null )
            return documentFile.isDirectory();

        return false; // both are null
        }

    File getFile()
        {
        return file;
        }

    DocumentFile getDocumentFile()
        {
        return documentFile;
        }

    String getName()
        {
        if ( file != null )
            return file.getName();

        if ( documentFile != null )
            return documentFile.getName();

        return "ERROR!";
        }

    long length()
        {
        if ( file != null )
            {
            if ( file.isDirectory() )
                return file.listFiles().length; // If it is a folder, list cannot be null
            else
                return file.length();
            }
        if ( documentFile != null )
            {
            if ( documentFile.isDirectory() )
                return documentFile.listFiles().length; // If it is a folder, list cannot be null
            else
                return documentFile.length();
            }

        return 0L;
        }

    long lastModified()
        {
        if ( file != null )
            return file.lastModified();

        if ( documentFile != null )
            return documentFile.lastModified();

        return 0L;
        }

    // URI has different uses:
    // Permission's URI - uri of a SAF folder (not the same as tree-uri from DocumentFile!)
    // DocumentFile's URI - getUri() - content-uri, handeld by documentfile
    // File's uri - Uri.fromFile() - file-uri, cannot be used to provide file to other apps
    // File-provider's uri - generated from file-uri, to provide this (content-uri) to other apps
    Uri getUri(Context context)
        {
        if ( uri != null )
            // Permission's uri could be stored along generated documentFile
            // NOT the same with getURi()!!
            return uri;

        if ( file != null )
            // File-provider's uri
            return FileProvider.getUriForFile( context,
                    context.getApplicationContext().getPackageName() + ".provider",
                    file);

        if ( documentFile != null )
            // DocumentFile's uri
            return documentFile.getUri();

        // File's uri cannot be returned
        return null;
        }

    public DataFile getParentFolder()
        {
        if ( file != null )
            return new DataFile( file.getParentFile() );

        if ( documentFile != null )
            return new DataFile( documentFile.getParentFile() );

        return null;
        }

    public DataFile[] listFiles()
        {
        DataFile[] dataFiles = null;

        if ( file != null )
            {
            File[] files = file.listFiles(); // It cannot be null?? @NotNull denotes it
            dataFiles = new DataFile[files.length];
            for (int i = 0; i < files.length; i++)
                {
                dataFiles[i] = new DataFile(files[i]);
                }
            }

        if ( documentFile != null )
            {
            DocumentFile[] documentFiles = documentFile.listFiles(); // It cannot be null??
            // @NotNull denotes it
            dataFiles = new DataFile[documentFiles.length];
            for (int i = 0; i < documentFiles.length; i++)
                {
                dataFiles[i] = new DataFile(documentFiles[i]);
                }
            }

        return dataFiles;
        }

    public DataFile createFolder(String folderName)
        {
        if ( file != null && file.isDirectory() )
            {
            File newFolder = new File ( file, folderName );
            if ( newFolder.mkdir() )
                {
                return new DataFile( newFolder );
                }
            }

        else if ( documentFile != null && documentFile.isDirectory() )
            {
            DocumentFile newFolder = documentFile.createDirectory( folderName );
            if ( newFolder != null )
                {
                return new DataFile( newFolder );
                }
            }

        return null;
        }

    public boolean createFile(String fileName)
        {
        if ( file != null && file.isDirectory() )
            {
            try
                {
                File newFile = new File( file, fileName );
                return newFile.createNewFile();
                }
            catch (IOException e)
                {
                return false;
                }
            }

        else if ( documentFile != null && documentFile.isDirectory() )
            {
            DocumentFile newFile = documentFile.createFile("text/plain", fileName);
            return newFile != null;
            }

        return false;
        }

    @Override
    public boolean equals(Object other)
        {
        if (this == other)
            return true;

        if (!(other instanceof DataFile))
            return false;

        DataFile otherDataFile = (DataFile) other;

        if ( file != null )
            {
            if ( otherDataFile.file != null )
                // Canonical path is the best unique property for file,
                // but file-uri is much more simple to get
                // (file.equals compares abs path)
                return Uri.fromFile( file ).equals( Uri.fromFile( otherDataFile.file ));
            else
                return false;
            }

        // file IS null, but we don't know anything about dataFile.file

        if ( documentFile != null )
            {
            if ( otherDataFile.documentFile != null )
                // documentFile.equals only checks whether they point to the same object
                // let's compare document-uri-s
                return documentFile.getUri().equals( otherDataFile.documentFile.getUri() );
            else
                return false;
            }

        // file and documentFile IS null, but we don't know anything
        // about dataFile.file or datafile.documentFile

        // Equals if both are null
        return otherDataFile.file == null && otherDataFile.documentFile == null;
        }

    }
