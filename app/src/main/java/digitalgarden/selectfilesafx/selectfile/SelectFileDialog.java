package digitalgarden.selectfilesafx.selectfile;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import digitalgarden.selectfilesafx.R;

// Dialogusokért felelős fragment-rész
public class SelectFileDialog extends DialogFragment
	{
	public enum Type
		{
		CREATE_FOLDER,
		CREATE_FILE,
		}
	
	public static SelectFileDialog showNewDialog(FragmentActivity activity, Type type, String text)
		{
		SelectFileDialog selectFileDialog;
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		// Ha volt megnyitott dialogus, azt eltüntetjük
    	selectFileDialog = (SelectFileDialog) fragmentManager.findFragmentByTag( "DIALOG" );
    	if ( selectFileDialog != null )
    		{
    		selectFileDialog.dismiss();
    		}
    	
    	// És nyitunk egy újat, amit be is mutatunk
    	selectFileDialog = new SelectFileDialog();
		
		Bundle args = new Bundle();
		args.putSerializable("TYPE", type);
		args.putString("TEXT", text); // vigyázz, ez lehet null!!
		selectFileDialog.setArguments(args);
		
		selectFileDialog.show( activity.getSupportFragmentManager(), "DIALOG");
		
		return selectFileDialog;
		}
	
	SelectFileActivity selectFileActivity;
	
	@Override
	public void onAttach(Context context)
		{
		super.onAttach(context);


		if ( context instanceof SelectFileActivity)
			selectFileActivity = (SelectFileActivity) context;
		else 
            throw new ClassCastException(context.toString() + " must be instanceof " +
					"SelectFileActivity!");
		}
	
	@Override
	public void onDetach() 
		{
		super.onDetach();
		
		selectFileActivity = null;
		}
		
	

	@NonNull
	@SuppressLint("ResourceType") // Type is added manually
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
		{
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( getActivity() );

    	// HIBA ELLENŐRZÉS!!
    	Bundle args = getArguments();
		final Type type = (Type) args.getSerializable("TYPE");
		final String text = args.getString("TEXT");
    	
		switch ( type )
			{
			case CREATE_FOLDER:
				{
				alertDialogBuilder.setTitle( R.string.dialog_create_directory );

				final EditText name = new EditText( getActivity() );
				// Ezzel megőrzi elforgatásnál az értékét - alacsony ID értékek használhatóak
				// Ez érdekes: http://stackoverflow.com/questions/1714297/android-view-setidint-id-programmatically-how-to-avoid-id-conflicts
				// Gond: csak azokat a konfliktusokat ellenőrizzük, amik már benne vannak a View-Tree-ben, egyébként meg használhatunk alacsony értékeket
				// Vagy: használjuk a Res:id megadásának lehetőségét
				name.setId( 1 );
				name.setText( text );
				alertDialogBuilder.setView( name );
 				
				alertDialogBuilder.setPositiveButton( R.string.dialog_button_create, new DialogInterface.OnClickListener()
					{
					public void onClick(DialogInterface dialog, int which) 
						{
						selectFileActivity.onDialogPositiveResult( type, name.getText().toString() );
						}
		           });
		
				alertDialogBuilder.setNegativeButton( R.string.dialog_button_cancel, null );
		
				break;
				}
				
			case CREATE_FILE:
				{
				alertDialogBuilder.setTitle( R.string.dialog_create_file);

				final EditText name = new EditText( getActivity() );
				// Ezzel megőrzi elforgatásnál az értékét - alacsony ID értékek használhatóak
				name.setId( 2 );
				name.setText( text );
				alertDialogBuilder.setView( name );

				alertDialogBuilder.setPositiveButton( R.string.dialog_button_create, new DialogInterface.OnClickListener()
					{
					public void onClick(DialogInterface dialog, int which) 
						{	
						selectFileActivity.onDialogPositiveResult( type, name.getText().toString() );
						}
					});

				alertDialogBuilder.setNegativeButton( R.string.dialog_button_cancel, null );

				break;
				}
								
			}
		
		return alertDialogBuilder.create();
		}
   
	}
