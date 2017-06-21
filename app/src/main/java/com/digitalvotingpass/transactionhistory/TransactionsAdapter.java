package com.digitalvotingpass.transactionhistory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.digitalvotingpass.digitalvotingpass.R;

import java.util.List;

public class TransactionsAdapter extends ArrayAdapter<TransactionHistoryItem> {
    public TransactionsAdapter(Context context, List<TransactionHistoryItem> transactionHistoryItems) {
        super(context, 0, transactionHistoryItems);
    }

    /**
     * Handle the conversion from the transaction object to the list item textviews.
     */
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        TransactionHistoryItem transactionHistoryItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        }
        if (transactionHistoryItem != null) {
            // Lookup view for data population
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView time = (TextView) convertView.findViewById(R.id.time);
            TextView details = (TextView) convertView.findViewById(R.id.details);
            // Populate the data into the template view using the data object
            title.setText(transactionHistoryItem.title);
            time.setText(transactionHistoryItem.time.toString());
            details.setText(transactionHistoryItem.transactionDetails);
        }

        // Return the completed view to render on screen
        return convertView;
    }
}