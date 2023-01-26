package digitalgarden.selectfilesafx.selectfile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import digitalgarden.selectfilesafx.R;


public class SelectFileAdapter extends BaseAdapter implements Filterable
	{
	private final LayoutInflater layoutInflater;
	private final Context context;
	
	private List<SelectFileEntry> filteredEntries;
	private final List<SelectFileEntry> originalEntries;
	
	private entryFilter entryFilter;

	
    SelectFileAdapter(Context context, List<SelectFileEntry> entries)
		{
        super();
		
		this.layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		this.originalEntries = entries;
		this.filteredEntries = entries;
		this.context = context;
    	}

	@Override
	public int getCount()
		{
		return (filteredEntries == null) ? 0 : filteredEntries.size();
		}

	@Override
	public SelectFileEntry getItem(int position )
		{
		return filteredEntries.get( position );
		}

	@Override
	public long getItemId(int position)
		{
		return position;
		}
	
	// A következő két függvény biztosítja, hogy a HEADER/DIVIDER más view-t használhat
	@Override
	public int getViewTypeCount()
		{
		return SelectFileEntry.getViewTypeCount();
		}

	@Override
	public int getItemViewType(int position)
		{
		return getItem(position).getItemViewType();
		}
	
	// A következő két függvény biztosítja, hogy a HEADER/DIVIDER-t nem lehet kiválasztani
	@Override
	public boolean areAllItemsEnabled()
		{
		return SelectFileEntry.areAllItemsEnabled();
		}
	
	@Override
	public boolean isEnabled(int position)
		{
		return getItem(position).isEnabled();
		}
	
	
	@Override
    public View getView(int position, View convertView, ViewGroup parent)
		{
    	SelectFileEntry entry = getItem(position);
    	View rowView;
    	
    	if ( entry.getItemViewType() == 0 )
			{
			rowView = (convertView == null) ? layoutInflater.inflate(
					R.layout.file_entry_header_row_view, null) : convertView;
			
			TextView title = rowView.findViewById( R.id.divider_title );
			title.setText( entry.getName( context ));
			}
		else
    		{
    		rowView = (convertView == null) ? layoutInflater.inflate(
    				R.layout.file_entry_row_view, null) : convertView;
    	
			TextView name = rowView.findViewById( R.id.file_entry_name );
			TextView data = rowView.findViewById( R.id.file_entry_data );
			ImageView icon = rowView.findViewById( R.id.file_entry_icon );

			name.setText( entry.getName( context ));
			data.setText( entry.getData( context ));
			icon.setImageResource( entry.getImageResource() );

			icon.setOnClickListener(new View.OnClickListener()
				{
				@Override
				public void onClick(View v)
					{
					((ListView) parent).performItemClick(v, position, 0); // Let the event be handled in onItemClick()
					}
				});
    		}
    	return rowView;
    	}

	// http://stackoverflow.com/a/13514663 - Search and Filter List
	public Filter getFilter()
		{
		if (entryFilter == null)
			entryFilter = new entryFilter();
			
		return entryFilter;
		}	
		
	private class entryFilter extends Filter
		{
		protected FilterResults performFiltering(CharSequence constraint)
			{
			FilterResults filterResults = new FilterResults();
			if (constraint != null && constraint.length() > 0 )
				{
	            List<SelectFileEntry> filterList=new ArrayList<SelectFileEntry>();
				constraint = constraint.toString().toLowerCase( Locale.getDefault() );
				
				// dir es file tipust is szukiti
	            for ( int i=0; i < originalEntries.size(); i++ )
					{
					switch (originalEntries.get(i).getType())
						{
						case SelectFileEntry.FOLDER:
						case SelectFileEntry.FILE:
						case SelectFileEntry.LINKED_FOLDER:
							if (!originalEntries.get(i).getName(context).toLowerCase(Locale.getDefault()).contains(constraint))
								break;
						case SelectFileEntry.HEADER:
						case SelectFileEntry.HOME:
						case SelectFileEntry.BACK:
						case SelectFileEntry.NEW_FOLDER:
						case SelectFileEntry.NEW_FILE:
						case SelectFileEntry.LINK_FOLDER:
						case SelectFileEntry.UNLINK_FOLDER:
							filterList.add(originalEntries.get(i));
						}
					}
	            filterResults.count = filterList.size();
	            filterResults.values = filterList;
				}
			else
				{
	            filterResults.count = originalEntries.size();
	            filterResults.values = originalEntries;
				}
			return filterResults;
			}

		// http://stackoverflow.com/a/262416 - Type safety: Unchecked cast
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults filterResults)
			{
			filteredEntries = (List<SelectFileEntry>) filterResults.values;
			notifyDataSetChanged();
			}	
		}
	}
