package com.digitalvotingpass.transactionhistory;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.digitalvotingpass.digitalvotingpass.R;

import java.util.List;

/**
 * Created by wkmeijer on 26-5-17.
 */

public class TransactionsAdapter extends ArrayAdapter<Transaction> {
    public TransactionsAdapter(Context context, List<Transaction> transactions) {
        super(context, 0, transactions);
    }

    /**
     * Handle the conversion from the transaction object to the list item textviews.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Transaction transaction = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_transaction, parent, false);
        }
        // Lookup view for data population
        TextView title = (TextView) convertView.findViewById(R.id.title);
        TextView time = (TextView) convertView.findViewById(R.id.time);
        TextView details = (TextView) convertView.findViewById(R.id.details);
        // Populate the data into the template view using the data object
        title.setText(transaction.title);
        time.setText(transaction.time.toString());
        details.setText(transaction.transactionDetails);

        // Return the completed view to render on screen
        return convertView;
    }
}