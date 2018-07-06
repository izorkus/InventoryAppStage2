package com.example.root.inventoryappstage2;

import java.text.DecimalFormat;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.root.inventoryappstage2.data.ItemContract.ItemEntry;

/**
 * Allows user to create a new item or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "EditorActivity";
    private static final int EXISTING_ITEM_LOADER = 0;

    private Uri currentItemUri;

    /**
     * EditText field to enter the items name
     */
    private EditText mNameEditText;

    /**
     * EditText field to enter the items quantity
     */
    private EditText mQuantityEditText;

    /**
     * EditText field to enter the items price
     */
    private EditText mPriceEditText;

    /**
     * EditText field to enter the items supplier name
     */
    private EditText mSuppNameEditText;

    /**
     * EditText field to enter the items supplier phone
     */
    private EditText mSuppPhoneEditText;

    private boolean mItemHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        currentItemUri = intent.getData();


        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_item_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mSuppNameEditText = (EditText) findViewById(R.id.edit_item_supp_name);
        mSuppPhoneEditText = (EditText) findViewById(R.id.edit_supp_phone);

        Button qtyPlusButton = (Button) findViewById(R.id.qty_plus_button);
        Button qtyMinusButton = (Button) findViewById(R.id.qty_minus_button);
        Button callSuppButton = (Button) findViewById(R.id.phone_to_supply_button);
        Button delItemButton = (Button) findViewById(R.id.delete_item_button);


        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mSuppPhoneEditText.setOnTouchListener(mTouchListener);
        mSuppNameEditText.setOnTouchListener(mTouchListener);

        if (currentItemUri == null) {
            setTitle(R.string.editor_activity_title_new_item);
            invalidateOptionsMenu();
            qtyPlusButton.setVisibility(View.INVISIBLE);
            qtyMinusButton.setVisibility(View.INVISIBLE);
            callSuppButton.setVisibility(View.INVISIBLE);
            delItemButton.setVisibility(View.INVISIBLE);
            TextView quantytiLabel = (TextView) findViewById(R.id.qty_label);
            quantytiLabel.setVisibility(View.INVISIBLE);

        } else {
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
            setTitle(R.string.editor_activity_title_edit_item);
        }

        callSuppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneString = mSuppPhoneEditText.getText().toString().trim();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneString, null));
                startActivity(intent);
            }
        });

        delItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        qtyPlusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());
                quantityInt++;
                mQuantityEditText.setText(Integer.toString(quantityInt));
            }
        });

        qtyMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());

                if (quantityInt > 0) {
                    quantityInt--;
                    mQuantityEditText.setText(Integer.toString(quantityInt));
                } else if (quantityInt == 0) {
                    Toast.makeText(EditorActivity.this, "Quantity is already zero.", Toast.LENGTH_SHORT).show();
                }


            }
        });


    }

    private void editItem() {

        // After click done in EditorActivity
        // getText from edit field
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString();
        String suppNameString = mSuppNameEditText.getText().toString().trim();
        String suppPhoneString = mSuppPhoneEditText.getText().toString().trim();

        //Little hack for , and . as decimal point, handling in polish Android

        priceString = priceString.replace(",", ".");

        if (nameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.name_required_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (quantityString.isEmpty()) {
            Toast.makeText(this, getString(R.string.quantity_required_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (priceString.isEmpty()) {
            Toast.makeText(this, getString(R.string.price_required_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (suppNameString.isEmpty()) {
            Toast.makeText(this, getString(R.string.supp_name_required_message), Toast.LENGTH_SHORT).show();
            return;
        }

        if (suppPhoneString.isEmpty()) {
            Toast.makeText(this, getString(R.string.supp_phone_required_message), Toast.LENGTH_SHORT).show();
            return;
        }

        float priceFloat = Float.parseFloat(priceString);

        priceFloat = priceFloat * 100;
        int priceInt = Math.round(priceFloat);

        int quantityInt = Integer.parseInt(mQuantityEditText.getText().toString());


        if (currentItemUri == null &&
                TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString)) {
            return;
        }

        // Create ContentValues object with data

        ContentValues values = new ContentValues();
        values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
        values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantityInt);
        values.put(ItemEntry.COLUMN_ITEM_PRICE, priceInt);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME, suppNameString);
        values.put(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE, suppPhoneString);

        if (currentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

            // Show a toast message depending on whether or not the insertion was successful
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int rowsAffected = getContentResolver().update(currentItemUri, values, null, null);
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        NavUtils.navigateUpFromSameTask(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (currentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                editItem();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        return new CursorLoader(this,
                currentItemUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {


        if (cursor.moveToFirst()) {
            // Find the columns of iten attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int suppNameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_NAME);
            int suppPhoneColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int priceInt = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String suppName = cursor.getString(suppNameColumnIndex);
            String suppPhone = cursor.getString(suppPhoneColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));

            //Conversion from int to string
            DecimalFormat twoPlaces = new DecimalFormat("0.00");

            Double doublePrice = ((double) priceInt) / 100;
            String priceString = twoPlaces.format(doublePrice);

            //Little hack for , and . handling in polish Android

            priceString = priceString.replace(",", ".");

            mPriceEditText.setText(priceString);
            mSuppNameEditText.setText(suppName);
            mSuppPhoneEditText.setText(suppPhone);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText(null);
        mQuantityEditText.setText(null);
        mPriceEditText.setText(null);
        mSuppNameEditText.setText(null);
        mSuppPhoneEditText.setText(null);

    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteItem() {

        if (currentItemUri != null) {

            int rowDeleted = getContentResolver().delete(currentItemUri, null, null);

            if (rowDeleted == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

        }
    }


}