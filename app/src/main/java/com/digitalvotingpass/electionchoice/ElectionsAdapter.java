package com.digitalvotingpass.electionchoice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.digitalvotingpass.digitalvotingpass.R;

import java.util.ArrayList;


public class ElectionsAdapter extends BaseAdapter implements Filterable {
    private Context context;
    private ArrayList<Election> electionList;
    private ArrayList<Election> filteredList;
    private ElectionFilter electionFilter;


    public ElectionsAdapter(Context context, ArrayList<Election> elections) {
        this.context = context;
        this.electionList = elections;
        this.filteredList = elections;

        getFilter();
    }

    /**
     * Get size of the filtered electionlist
     * @return size
     */
    @Override
    public int getCount() {
        return filteredList.size();
    }

    /**
     * Get specific item from the filtered election list
     * @param i item index
     * @return list item
     */
    @Override
    public Election getItem(int i) {
        return filteredList.get(i);
    }

    /**
     * Must be overridden, normally returns the id of and item.
     * In our case the id will be the same as the position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Handle the conversion from the elections object to the list item textviews.
     * TODO: handle actual election choice data
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Election election = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_election, parent, false);
        }
        // Lookup view for data population
        TextView kind = (TextView) convertView.findViewById(R.id.kind);
        TextView place = (TextView) convertView.findViewById(R.id.place);
        // Populate the data into the template view using the data object
        kind.setText(election.kind);
        place.setText(election.place);

        // Return the completed view to render on screen
        return convertView;
    }

    /**
     * Get the custom election filter
     * @return filter
     */
    @Override
    public Filter getFilter() {
        if (electionFilter == null) {
            electionFilter = new ElectionFilter();
        }
        return electionFilter;
    }


    /**
     * Custom filter for election choice list
     * Filter content in election choice list according to the search text,
     * filter on both the place and the kind of election
     */
    private class ElectionFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<Election> tempList = new ArrayList<>();

                // Match the search input to the kind and place attributes of an election object
                for (Election election : electionList) {
                    if (election.getKind().toLowerCase().contains(constraint.toString().toLowerCase())
                            || election.getPlace().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        tempList.add(election);
                    }
                }

                filterResults.count = tempList.size();
                filterResults.values = tempList;
            } else {
                filterResults.count = electionList.size();
                filterResults.values = electionList;
            }

            return filterResults;
        }

        /**
         * Updates the filteredList and notifies the adapter that the dataset has been changed
         * so the view is updated.
         */
        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredList = (ArrayList<Election>) results.values;
            notifyDataSetChanged();
        }
    }
}
