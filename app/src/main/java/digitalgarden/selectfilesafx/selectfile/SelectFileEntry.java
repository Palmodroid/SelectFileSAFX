package digitalgarden.selectfilesafx.selectfile;


import android.content.Context;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import digitalgarden.selectfilesafx.R;

/**
 * SelectFileEntry represents a visible entry of the list of files. The list collects
 * folders, files and formatters.
 * It implements Comparable - so entries can be ordered by name.
 * Important! There are two different file systems to handle. Check DataFile
 */
class SelectFileEntry implements Comparable<SelectFileEntry>
	{
	// HEADER HOME BACK NEW_FOLDER NEW_FILE LINKED_FOLDER LINK_FOLDER UNLINK_FOLDER FOLDER FILE

	// Important! numeric values set order inside list!
	final static int HEADER = 0;	// First row of the list: name of current folder OR Main folder
		// file: NULL for main folder OR documentfile of current folder
		// HEADER has different view!
		// HEADER is not allowed to click - when selecting folder!
		// HEADER has no icon and no data - but it has selection icon on the right side

	final static int HOME = 1;			// Go back to main folder
	// file: main folder - could be null (as main folder)
	final static int BACK = 2;			// Go bact to parent folder
	// file: parent folder
	final static int LINKED_FOLDER = 3;	// Go to linked folder
	// file: linked folder
	final static int LINK_FOLDER= 4;	// Allow new folder inside main folder
	// file: NULL
	final static int FOLDER = 5;		// Folder entry
	// file: folder
	final static int NEW_FOLDER = 6;	// Create new folder
	// file: NULL
	final static int FILE = 7;			// File entry
	// file: file
	final static int NEW_FILE = 8;		// Create new file
	// file: NULL
	final static int UNLINK_FOLDER =9;	// Go back, and unlink this folder
	// file: current (linked) folder - folder who's parent folder is null


	// Type of the list entries - type values define sort order
	private final int type;

	private final DataFile dataFile;

	SelectFileEntry(DataFile dataFile, int type )
		{
		this.dataFile = dataFile;
		this.type = type;
		}

	int getType()
		{
		return type;
		}

	DataFile getDataFile()
		{
		return dataFile;
		}

	String getName( Context context )
		{
		switch (type)
			{
			case HEADER: // this will be the "title" string
				return ( dataFile == null ) ? // NULL means main folder
					context.getString( R.string.title_header) : dataFile.getName();
			case FOLDER:
			case FILE:
			case LINKED_FOLDER:
				return dataFile.getName();
			case NEW_FILE:
				return context.getString( R.string.create_new_file_short );
			case NEW_FOLDER:
				return context.getString( R.string.create_new_folder_short );
			case LINK_FOLDER:
				return context.getString( R.string.link_folder_short );

			case BACK:
				return context.getString( R.string.parent_folder_short);
			}
		return "ERROR!";
		}

	String getData( Context context )
		{
		SimpleDateFormat sdf=new SimpleDateFormat( context.getString(R.string.simple_date_format), Locale.US );

		switch (type)
			{
			// case HEADER: - HEADER has no data!
			case FOLDER:
			case LINKED_FOLDER:
				return sdf.format( new Date( dataFile.lastModified() )) + context.getString( R.string.separator ) + dataFile.length() + context.getString( R.string.items );
			case FILE:
				return sdf.format(new Date( dataFile.lastModified() )) + context.getString( R.string.separator ) + dataFile.length() + context.getString( R.string.bytes );
			case NEW_FILE:
				return context.getString( R.string.create_new_file );
			case NEW_FOLDER:
				return context.getString( R.string.create_new_folder );
			case LINK_FOLDER:
				return context.getString( R.string.link_folder );
			case BACK:
				return context.getString( R.string.parent_folder );
			}
		return "ERROR"; // This cannot happen!!
		}

	/* Icons are created by InkScape (ikonocskak3.svg)
	 * Dimensions: 64x64x32
	 * Used icons are copied from \SelectFileSAFX\Ikon64x64\ to
	 * SelectFileSAFX\app\src\main\sep\selectfile\drawable\ */
	int getImageResource()
		{
		switch ( type )
			{
			// case HEADER: - HEADER has no icon!
			case HOME:
				return R.drawable.icon_home;
			case BACK:
				return R.drawable.icon_back;
			case FOLDER:
				return R.drawable.icon_folder;
			case FILE:
				return R.drawable.icon_pencil;
			case NEW_FOLDER:
				return R.drawable.icon_folder_new;
			case NEW_FILE:
				return R.drawable.icon_file_new;
			case LINK_FOLDER:
				return R.drawable.icon_link;
			case LINKED_FOLDER:
				return R.drawable.icon_linked;
			case UNLINK_FOLDER:
				return R.drawable.icon_unlink;
			// itt lehet különböző ikonokat beállítani
			// http://stackoverflow.com/questions/4894885/how-to-check-file-extension-in-android
			// - vita az extension kikereséséről
			}
		return android.R.drawable.ic_dialog_alert; // This cannot happen!!
		}

	static int getViewTypeCount()
		{
		return 2; // 0. HEADER and 1. all other entries
		}

	int getItemViewType()
		{
		return ( getType() == SelectFileEntry.HEADER ) ? 0 : 1;
		}

	static boolean areAllItemsEnabled()
		{
		return true; // if HEADER is not selectable: false;
		}

	boolean isEnabled()
		{
		return true; // if HEADER is not selectable: getType() != SelectFileEntry.HEADER;
		}

	/*
	Mi legyen a sorend?

	Main-folder - maga a saját folder lesz
	1. HEADER - minden esetben a legelső
	2. Folderek:
		HOME_FOLDER??		Ilyen még nincs!!!
		PARENT_FOLDER   Második
		FOLDER			ABC sorrendben
		FOLDER_LINKED??

		ALLOW_FOLDER,	És ez hova?
		NEW_FOLDER,		Legvégére v legelejére?
		DISALLOW FOLDER??

	3. Fileok:
		FILE 			ABC sorrendben
		NEW_FILE		legvégére vagy legelejére?

	 */

	// Needed for ordering files
    // NULL is handled as empty string ("")
    @Override
    public int compareTo( SelectFileEntry thatFile )
		{
		if ( thatFile!= null )
			{
			if (type != thatFile.type)
				return type - thatFile.type;

			// types are identical, compare filenames!
			if (dataFile != null && thatFile.getDataFile() != null)
				{
				String thisString = dataFile.getName().toLowerCase(Locale.getDefault());
				String thatString =
						thatFile.getDataFile().getName().toLowerCase(Locale.getDefault());
				// normalize() could be an option instead of toLowerCase()
				// Name is obligatory, cannot be null

				return thisString.compareTo(thatString);
				}
			}

		// thatFile is missing OR types are identical BUT files are missing
		return 0;
    	}
	}
