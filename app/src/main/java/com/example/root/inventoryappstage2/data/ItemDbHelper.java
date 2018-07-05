package com.example.root.inventoryappstage2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.root.inventoryappstage2.data.ItemContract.ItemEntry;

public class ItemDbHelper extends SQLiteOpenHelper{

        // If you change the database schema, you must increment the database version.
        public static final int DATABASE_VERSION = 1;
        public static final String DATABASE_NAME = "ItemDatabase.db";

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                        ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        ItemEntry.COLUMN_ITEM_NAME + " TEXT NON NULL, " +
                        ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NON NULL, " +
                        ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NON NULL, " +
                        ItemEntry.COLUMN_ITEM_SUPPLIER_NAME + " TEXT, " +
                        ItemEntry.COLUMN_ITEM_SUPPLIER_PHONE + " TEXT )";

        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME;
    }