package digitalgarden.selectfilesafx.selectfile;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.documentfile.provider.DocumentFile;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import digitalgarden.selectfilesafx.MainActivity;
import digitalgarden.selectfilesafx.fileoperations.FileOperations;
import digitalgarden.selectfilesafx.R;

public class SelectFileActivity extends AppCompatActivity
    {

    // Viewmodel will be cleared, if NOT configuration change destroys activity
    // https://stackoverflow.com/questions/50983738/when-is-the-viewmodel-oncleared-called
    public static class Variables extends ViewModel
        {
        // Data in ViewModel will be saved during configuration changes
        int positionOfFileSection;
        int positionOfTopEntry = -1;
        int topOfTopEntry;

        DataFile currentFolder = null; // null means, this is a fresh start
        DataFile previousDir = null;
        }

    private Variables variables;

    // UI
    private ListView list;
    private EditText filter;
    private TextView ending;

    private static final int OPEN_TREE_REQUEST_CODE = 1;


    ActivityResultLauncher<Uri> launchSAFOPenDocumentTree = registerForActivityResult(
            new ActivityResultContracts.OpenDocumentTree(),
            new ActivityResultCallback<Uri>()
                {
                @Override
                public void onActivityResult(Uri uri)
                    {
                    if (uri != null)
                        {
                        getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION |
                                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                        // go to the newly selected dir
                        populate(new DataFile(
                                DocumentFile.fromTreeUri(SelectFileActivity.this, uri)));
                        }
                    }
                });



    @Override
    public void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView( R.layout.select_file_activity );

        variables = new ViewModelProvider( this ).get( Variables.class );

        list = findViewById( R.id.list );
        filter = findViewById( R.id.filter );
        ending = findViewById( R.id.ending );
        // Elem kiválasztásakor
        // DIR - továbblépünk a könyvtárra
        // PARENT_DIR - eggyel vissza - ilyenkor a jelenlegi könyvtár lesz a lista első eleme
        // FILE (minden más) - a kiválasztott file adataival (setMonthlyData) visszatérünk
        list.setOnItemClickListener( new AdapterView.OnItemClickListener()
            {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
                {
                SelectFileEntry entry = (SelectFileEntry) list.getItemAtPosition(position);

                switch (entry.getType())
                    {
                    case SelectFileEntry.FOLDER:
                    case SelectFileEntry.LINKED_FOLDER:
                    case SelectFileEntry.BACK:
                    case SelectFileEntry.HOME:
                        populate(entry.getDataFile());
                        break;

                    case SelectFileEntry.LINK_FOLDER:
                        // TODO: Drop message to allow a new folder!!!
                        launchSAFOPenDocumentTree.launch(
                                FileOperations.getStartingDirectoryAsUri(SelectFileActivity.this));
                        break;

                    case SelectFileEntry.UNLINK_FOLDER:
                        /* Problem: In case of folders (trees) DocumentFile uri and UriPermissions
                        uri (which is a "file" uri) differs.
                        Solution 1. - Store uri-s for linked folders (inside SelectFileEntry) It
                        seems easier. BUT! Populate() works with DocumentFiles - so we do not know
                        the original uri of the current folder. (Linked folder do not have parent
                        folder - this is how we know them.)
                        Solution 2. - Transform Uripermissions to Documentfile (tree), and than back
                        to uri (this will be a tree uri) which can be compared to stored
                        Documentfile-uri */

                        Uri uriPermissionUri;
                        List<UriPermission> uriPermissions = getContentResolver().getPersistedUriPermissions();
                        for (UriPermission uriPermission : uriPermissions)
                            {
                            if (uriPermission.getUri().equals(entry.getDataFile().getUri(SelectFileActivity.this)))
                                {
                                // TODO: Are there any other flags? How to get flags?
                                getContentResolver().releasePersistableUriPermission(uriPermission.getUri(),
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                                }
                            }

                        // Go back to main folder
                        populate(null);
                        break;

                    case SelectFileEntry.NEW_FILE:
                        SelectFileDialog.showNewDialog(SelectFileActivity.this,
                                SelectFileDialog.Type.CREATE_FILE, filter.getText().toString());
                        break;

                    case SelectFileEntry.NEW_FOLDER:
                        SelectFileDialog.showNewDialog(SelectFileActivity.this,
                                SelectFileDialog.Type.CREATE_FOLDER, filter.getText().toString());
                        break;

                    case SelectFileEntry.FILE:
                        // Entry icon was tapped
                        if (view.getId() == R.id.file_entry_icon)
                            {
                            // MIME type for file is not easy
                            // https://stackoverflow.com/questions/8589645/how-to-determine-mime
                            // Or: https://stackoverflow.com/questions/6265298/action-view-intent-for-a-file-with-unknown-mimetype-type-of-file-in-android

                            Intent intent2 = new Intent(Intent.ACTION_VIEW);
                            intent2.setData(entry.getDataFile().getUri(SelectFileActivity.this));
                            intent2.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent
                                    .FLAG_GRANT_READ_URI_PERMISSION);

                            // Intent.createChooser() could be used
                            try
                                {
                                startActivity(intent2);
                                }
                            catch (ActivityNotFoundException e)
                                {
                                Toast.makeText(SelectFileActivity.this, "Couldn't find app to open file",
                                        Toast.LENGTH_SHORT).show();
                                }
                            }
                        else
                            {
                            Intent returnIntent = new Intent();

                            returnIntent.setData(entry.getDataFile().getUri(SelectFileActivity.this));
                            setResult(RESULT_OK, returnIntent);
                            finish();
                            }
                        break;
                    case SelectFileEntry.HEADER:
                        Toast.makeText(SelectFileActivity.this,
                                (entry.getDataFile() == null ? "Main folder " :
                                        entry.getDataFile().getUri(SelectFileActivity.this)) +
                                        " was selected",
                                Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } );

        // szöveg beírásakor szűrjük a listát
        // erre két lehetőség lenne
        // 1. újra leválogatjuk a könyvtárat (lassú)
        // 2. adapter saját filterével a teljes leválogatott könyvtárat szűkítjük tovább (ez a megvalósított)
        filter = (EditText)findViewById( R.id.filter );
        filter.addTextChangedListener( new TextWatcher()
            {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                SelectFileAdapter adapter = ((SelectFileAdapter)list.getAdapter());
                if (adapter != null)
                    {
                    adapter.getFilter().filter(s);
                    // if ( s.length() > 0 )
                    //     list.setSelectionFromTop( variables.positionOfFileSection, 0 );
                    }
                }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {
                // TODO Auto-generated method stub
                }

            @Override
            public void afterTextChanged(Editable s)
                {
                // TODO Auto-generated method stub
                }
            } );

        populate( variables.currentFolder );
        }


    /**
     * Populates list for folder.
     * Folder is null for the main folder
     * Main folder contains together the data of the private folder (root folder) AND the
     * staring point of each linked folder.
     * @param folder folder to populate
     */
    private void populate( DataFile folder )
        {
        List<SelectFileEntry> entries = new ArrayList<SelectFileEntry>();

        // private folder
        // DocumentFile privateFolder = DocumentFile.fromFile(getExternalFilesDir(null)); // app's
        // private folder
        DataFile privateFolder = new DataFile( getFilesDir() ); // app's private
        // folder
            /* These are private folders as well - but not needed to list here
               Log.d("UP", "Dirs: " + getFilesDir() + " , " + getCacheDir() + " , "
               + getExternalFilesDir( Environment.DIRECTORY_PICTURES ) + " , " +
               getExternalMediaDirs()); */

        // main folder contains the private folder AND each linked folder
        // populate( folder )
        // folder is null - this is the starting point
        // folder is DataFile which isNull - BACK from linked folders
        // folder is privateFolder - BACK from direct folders of private folder and HOME
        boolean mainFolder = ( folder == null || folder.isNull() || (folder.equals(privateFolder)));

        // Populate list with commands, folders and files
        // This is the "main" - contents of private folder and linked folderes together
        if ( mainFolder )
            {
            folder = privateFolder;

            // Add header for main folder
            entries.add(new SelectFileEntry( null, SelectFileEntry.HEADER ));

            // Add link command
            entries.add(new SelectFileEntry(null, SelectFileEntry.LINK_FOLDER));

            // Add linked folders
            List<UriPermission> uriPermissions = getContentResolver().getPersistedUriPermissions();
            for (UriPermission uriPermission : uriPermissions)
                {
                DataFile dataFile = new DataFile( this, uriPermission );
                entries.add(new SelectFileEntry( dataFile, SelectFileEntry.LINKED_FOLDER));
                }
            }
        else // This is a real folder, or linked folder
            {
            // Add header
            entries.add(new SelectFileEntry( folder, SelectFileEntry.HEADER ));


            DataFile parentFolder = folder.getParentFolder();
            // parentFolder cannot be null, as folder is valid (not main folder)

            // Add HOME to private folder command
            // These are NOT first level subfolders (parent is not the main folder)
            if ( !parentFolder.isNull() && !parentFolder.equals(privateFolder))
                entries.add(new SelectFileEntry( privateFolder, SelectFileEntry.HOME));

            // Add BACK (to parentFolder) command
            entries.add(new SelectFileEntry( parentFolder, SelectFileEntry.BACK));

            // Add UNLINK folder command - if parent folder is null
            if ( parentFolder.isNull() )
                entries.add(new SelectFileEntry(folder, SelectFileEntry.UNLINK_FOLDER));
            }

        // If ( isCreateAllowed() )
        entries.add( new SelectFileEntry( null, SelectFileEntry.NEW_FILE));
        entries.add( new SelectFileEntry( null, SelectFileEntry.NEW_FOLDER));

        // Add contents of folder (files and folders)
        DataFile[] filesInFolder = folder.listFiles(); // It cannot be null in folders
        for(DataFile file: filesInFolder)
            {
            entries.add( new SelectFileEntry( file, file.isDirectory() ?
                    SelectFileEntry.FOLDER : SelectFileEntry.FILE ));
                    // ????? if ( file.getName().endsWith( getFileEnding() ) )
            }

        // variables.positionOfFileSection = entriesFirstPart.size();

        Collections.sort( entries );

        SelectFileAdapter adapter = new SelectFileAdapter(SelectFileActivity.this, entries);
        list.setAdapter(adapter);

        adapter.getFilter().filter( filter.getText().toString() );

        if ( variables.currentFolder != null )
            {
            if ( variables.positionOfTopEntry != -1 )
                {
                list.setSelectionFromTop(variables.positionOfTopEntry, variables.topOfTopEntry);
                variables.positionOfTopEntry = -1;
                }
            else if ( filter.length() > 0 )
                {
                // TODO ez nincs beállítva!! list.setSelectionFromTop( variables
                //  .positionOfFileSection, 0 );
                }
            }
/*        else // if (previousDir != null)
            {
            for (int i=0; i < adapter.getCount(); i++)
                {
                if ( folder != null && adapter.getItem(i).getType() == SelectFileEntry.Type.FOLDER && adapter.getItem(i).getFile().equals( folder.getParentFile() ) )
                    {
                    list.setSelectionFromTop(i, 0);
                    break;
                    }
                if ( adapter.getItem(i).getType() == SelectFileEntry.Type.FILE )
                    break;
                }
            }
 */

        variables.currentFolder = folder;
        }


    // Positions are saved during pause
    @Override
    public void onPause()
        {
        super.onPause();

        // currentFolder was alreday set
        variables.positionOfTopEntry = list.getFirstVisiblePosition();
        View v = list.getChildAt(0);
        variables.topOfTopEntry = (v == null) ? 0 : v.getTop();
        }


    // Dialogusokhoz szükséges kommunikáció
    public void onDialogPositiveResult( SelectFileDialog.Type type, String text)
        {
        switch (type)
            {
            case CREATE_FOLDER:
                DataFile newFolder = variables.currentFolder.createFolder( text );
                if ( newFolder != null )
                    {
                    populate( newFolder );
                    }
                else
                    {
                    Toast.makeText(this, this.getString(R.string.dialog_error_cannot_create) + " [" + text + "]", Toast.LENGTH_SHORT).show();
                    SelectFileDialog.showNewDialog(this, SelectFileDialog.Type.CREATE_FOLDER, text);
                    }
                break;

            case CREATE_FILE: // Export to non-exsisting file
                if (variables.currentFolder.createFile( text ))
                    {
                    // TODO: return selected file !!!
                    populate(variables.currentFolder);
                    }
                else
                    {
                    Toast.makeText(this, this.getString(R.string.dialog_error_cannot_create) + " [" + text + "]", Toast.LENGTH_SHORT).show();
                    SelectFileDialog.showNewDialog(this, SelectFileDialog.Type.CREATE_FILE, text);
                    }
                break;
            }
        }
    }

