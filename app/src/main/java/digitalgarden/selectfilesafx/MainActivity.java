package digitalgarden.selectfilesafx;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;

import androidx.appcompat.widget.SwitchCompat;
import digitalgarden.selectfilesafx.fileoperations.FileOperations;
import digitalgarden.selectfilesafx.selectfile.SelectFileActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

/**
 * Sample project to demonstrate file save/load by SAF (built-in file browser) and by an
 * experimental file explorer.
 *
 * TO SAVE FILE by SAF
 * 1. Built-in file browser is started inside saveFile() by
 *    launchSAFCreateDocument.launch( FILE NAME );
 * 2. ActivityResultLauncher<> launchSAFCreateDocument (which sets MIME-TYPE to "text/plain")
 *    is created using:
 * 3. ContractCreateDocumentInFolder which extends ActivityResultContracts.CreateDocument
 *    Above API 29 - EXTRA_INITIAL_URI (starting folder) is added here (currently DOCUMENTS)
 *      FileOperations.getStartingDirectoryAsUri()
 *          (returns "DIR" from preferences) or calls
 *      FileOperations.getRootOfOpenDocumentTree() and
 *      FileOperations.changeStartingDirectory() which changes starting folder to "Documents"
 *      ((This part is similar inside load and save))
 * 4. Built-in browser returns to launchSAFCreateDocument.onActivityResult() which calls
 *    saveFile(Uri uri) to save content to the selected file
 *
 * TO LOAD FILE by SAF
 * 1. Built-in file browser is started inside loadFile() by
 *    launchSAFOpenDocument.launch( MIME TYPES[] ); where mime type is "* / *"
 * 2. ActivityResultLauncher<> launchSAFOpenDocument
 *    is created using:
 * 3. ContractOpenDocumentInFolder which extends ActivityResultContracts.OpenDocument
 *    Above API 29 - EXTRA_INITIAL_URI (starting folder) is added here (currently DOCUMENTS)
 *      FileOperations.getStartingDirectoryAsUri()
 *          (returns "DIR" from preferences) or calls
 *      FileOperations.getRootOfOpenDocumentTree() and
 *      FileOperations.changeStartingDirectory() which changes starting folder to "Documents"
 *      ((This part is similar inside load and save))
 * 4. Built-in browser returns to launchSAFOpenDocument.onActivityResult() which calls
 *    loadFile(Uri uri) to load content from the selected file
 */
public class MainActivity extends AppCompatActivity
    {
    private SwitchCompat useExperimentalFileExplorerSwitch;
    private EditText fileNameOfSavedFile;
    private EditText contentOfSavedFile;
    private EditText fileNameOfLoadedFile;
    private EditText contentOfLoadedFile;


    /** onCreate just connects to layout elements, and sets SAVE and LOAD buttons */
    @Override
    protected void onCreate(Bundle savedInstanceState)
        {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(findViewById(R.id.toolbar));

        /*
        binding.fab.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View view)
                {
                Intent intent = new Intent( MainActivity.this, SelectFileActivity.class);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            });
         */

        useExperimentalFileExplorerSwitch = findViewById(R.id.switchButton);

        fileNameOfSavedFile = findViewById(R.id.ed1);
        contentOfSavedFile = findViewById(R.id.ed2);
        fileNameOfLoadedFile = findViewById(R.id.ed3);
        contentOfLoadedFile = findViewById(R.id.ed4);

        Button buttonSave = findViewById(R.id.button1);
        buttonSave.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                saveFile();
                }
            });

        Button buttonLoad = findViewById(R.id.button2);
        buttonLoad.setOnClickListener(new View.OnClickListener()
            {
            @Override
            public void onClick(View v)
                {
                loadFile();
                }
            });
        }


    /***** S A V E  P A R T *****/
    private void saveFile()
        {
        if (useExperimentalFileExplorerSwitch.isChecked())
            {
            Intent intent = new Intent(this, SelectFileActivity.class);
            launchSave.launch(intent);
            }
        else
            {
            launchSAFCreateDocument.launch(fileNameOfSavedFile.getText().toString());
            }
        }

    /**
     * ActivityResultContracts.CreateDocument defines only
     * - mime type (check launchSAFCreate and ContractCreateDocumentInFolder constructor)
     * - suggested file name (check launchSAFCreate.launch() in saveFile())
     *
     * Extended CreateDocument contract adds EXTRA_INITIAL_URI to the generated intent
     * !! It works only above API 29 !!
     * This uri originates from FileOperations.getRootOfOpenDocumentTree()
     * where createOpenDocumentTree() works only above API 29 (Q)
     * Currently it always starts from Documents
     */
    private static class ContractCreateDocumentInFolder extends ActivityResultContracts.CreateDocument
        {
        public ContractCreateDocumentInFolder(@NonNull String mimeType)
            {
            super(mimeType);
            }

        @NonNull
        @Override
        @CallSuper
        public Intent createIntent(@NonNull Context context, @NonNull String input)
            {
            Intent intent = super.createIntent(context, input);

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when your app creates the document.
            // Otherwise it will start from the last directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                // EXTRA_INITIAL_URI IS AVAILABLE ONLY ABOVE 26
                // createOpenDocumentTree() IS AVAILABLE ONLY ABOVE API 29 (Q)
                // so, this will work only above API 29
                {
                Uri uri;
                if ((uri = FileOperations.getStartingDirectoryAsUri(context))!= null)
                    {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                    }
                }

            return intent;
            }
        }

    /**
     * launchSAFCreateDocument uses extended ContractCreateDocumentFolder to help launch SAF
     * built-in file browser to save file.
     * Content will be saved (after returning from file-chooser) inside onActivityResult()
     */
    ActivityResultLauncher<String> launchSAFCreateDocument = registerForActivityResult(
            new ContractCreateDocumentInFolder("text/plain"),
            new ActivityResultCallback<Uri>()
                {
                @Override
                public void onActivityResult(Uri uri)
                    {
                    saveFile( uri );
                    }
                });

    /**
     * Content will be saved eventually here; after choosing appropriate file
     * @param uri file uri from built-in or experimental file explorer
     */
    private void saveFile(Uri uri)
        {
        if (uri == null)
            {
            fileNameOfSavedFile.setText("ERROR!");
            }
        else
            {
            fileNameOfSavedFile.setText(uri.toString());
            String content = contentOfSavedFile.getText().toString();
            content = FileOperations.writeFileContent(MainActivity.this, uri, content);
            contentOfSavedFile.setText(content);
            }
        }


    /***** L O A D  P A R T *****/
    private void loadFile()
        {
        if ( useExperimentalFileExplorerSwitch.isChecked() )
            {
            Intent intent = new Intent(this, SelectFileActivity.class);
            launchLoad.launch(intent);
            }
        else
            {
            launchSAFOpenDocument.launch(new String[] {"*/*"});
            }
        }


    /**
     * ActivityResultContracts.OpenDocument defines only
     * - mime types (check launchSAFOPenDocument.launch() in loadFile()
     *
     * Extended CreateDocument contract adds EXTRA_INITIAL_URI to the generated intent
     * !! It works only above API 29 !!
     * This uri originates from FileOperations.getRootOfOpenDocumentTree()
     * where createOpenDocumentTree() works only above API 29 (Q)
     * Currently it always starts from Documents
     */
    static private class ContractOpenDocumentInFolder extends androidx.activity.result.contract.ActivityResultContracts.OpenDocument
        {
        @NonNull
        @Override
        @CallSuper
        public Intent createIntent(@NonNull Context context, @NonNull String[] input)
            {
            Intent intent = super.createIntent(context, input);

            // Optionally, specify a URI for the directory that should be opened in
            // the system file picker when your app creates the document.
            // Otherwise it will start from the last directory
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                // EXTRA_INITIAL_URI IS AVAILABLE ONLY ABOVE 26
                // createOpenDocumentTree() IS AVAILABLE ONLY ABOVE API 29 (Q)
                // so, this will work only above API 29
                {
                Uri uri;
                if ((uri = FileOperations.getStartingDirectoryAsUri(context))!= null)
                    {
                    intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
                    }
                }
            return intent;
            }
        }

    /**
     * launchSAFOPenDocument uses extended ContractOpenDocumentFolder to help launch SAF built-in
     * file browser to load file.
     * Content will be loaded (after returning from file-chooser) inside onActivityResult()
     */
    ActivityResultLauncher<String[]> launchSAFOpenDocument = registerForActivityResult(
            new ContractOpenDocumentInFolder(),
            new ActivityResultCallback<Uri>()
                {
                @Override
                public void onActivityResult(Uri uri)
                    {
                    loadFile(uri);
                    }
                });

    /**
     * Content will be loaded eventually here; after choosing appropriate file
     * @param uri file uri from built-in or experimental file explorer
     */
    private void loadFile(Uri uri)
        {
        if (uri == null)
            {
            fileNameOfLoadedFile.setText("ERROR!");
            }
        else
            {
            fileNameOfLoadedFile.setText(uri.toString());
            String content = FileOperations.readFileContent(MainActivity.this, uri);
            contentOfLoadedFile.setText(content);
            }
        }











    ActivityResultLauncher<Intent> launchLoad = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
                {
                @Override
                public void onActivityResult(ActivityResult result)
                    {
                    if (result.getResultCode() != RESULT_OK)
                        {
                        Log.e("ERROR!", "Something went wrong!");
                        }

                    Intent resultData = result.getData();

                    Uri uri = null;
                    if (resultData != null)
                        {
                        uri = resultData.getData();
                        // Perform operations on the document using its URI.
                        }

                    if (uri == null)
                        {
                        fileNameOfLoadedFile.setText("ERROR!");
                        }
                    else
                        {
                        fileNameOfLoadedFile.setText(uri.toString());
                        String content = FileOperations.readFileContent(MainActivity.this,
                                uri);
                        contentOfLoadedFile.setText(content);
                        }
                    }
                });


    ActivityResultLauncher<Intent> launchSave = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
                {
                @Override
                public void onActivityResult(ActivityResult result)
                    {
                    if (result.getResultCode() != RESULT_OK)
                        {
                        Log.e("ERROR!", "Something went wrong!");
                        }

                    Intent resultData = result.getData();

                    Uri uri = null;
                    if (resultData != null)
                        {
                        uri = resultData.getData();
                        // Perform operations on the document using its URI.
                        }

                    if (uri == null)
                        {
                        fileNameOfSavedFile.setText("ERROR!");
                        }
                    else
                        {
                        fileNameOfSavedFile.setText(uri.toString());
                        String content = contentOfSavedFile.getText().toString();
                        content = FileOperations.writeFileContent(MainActivity.this, uri, content);
                        contentOfSavedFile.setText(content);
                        }
                    }
                });

    /**  These are original methods  **/

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
        {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
        {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
            {
            return true;
            }

        return super.onOptionsItemSelected(item);
        }

    }