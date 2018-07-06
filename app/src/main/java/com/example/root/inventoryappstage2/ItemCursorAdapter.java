package com.example.root.inventoryappstage2;

import java.text.NumberFormat;
import java.util.Locale;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.root.inventoryappstage2.data.ItemContract.ItemEntry;

public class ItemCursorAdapter extends CursorAdapter{
    public ItemCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvSummary1 = (TextView) view.findViewById(R.id.summary1);
        TextView tvSummary2 = (TextView) view.findViewById(R.id.summary2);

        String name = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_NAME));
        int price = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_PRICE));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(ItemEntry.COLUMN_ITEM_QUANTITY));

        // These will be used in sale method. They're final, as we are calling
        // MainActivity class directly
        final long id = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry._ID));
        final int qty = Integer.parseInt(quantity);

        String priceString = NumberFormat.getCurrencyInstance(new Locale("en", "US"))
                .format(((double)price)/100);

        quantity = quantity + " pcs";

        tvName.setText(name);
        tvSummary1.setText(quantity);
        tvSummary2.setText(priceString);

        // Handle sell button -
        // I use method similar to posted by Josifas A on Udacity Knowledge:
        // https://knowledge.udacity.com/questions/2466

        view.findViewById(R.id.sell_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity mainActivity = (MainActivity) context;
                mainActivity.saleProduct(id, qty);
            }
        });

    }


}

