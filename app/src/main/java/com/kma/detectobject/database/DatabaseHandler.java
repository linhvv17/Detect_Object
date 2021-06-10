package com.kma.detectobject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "objectManager";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "items";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_MEAN = "mean";
    private static final String KEY_PATH = "path";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String create_items_table = String.format("CREATE TABLE %s(%s INTEGER PRIMARY KEY, %s TEXT, %s TEXT, %s TEXT)", TABLE_NAME, KEY_ID, KEY_NAME, KEY_MEAN, KEY_PATH);
        db.execSQL(create_items_table);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String drop_items_table = String.format("DROP TABLE IF EXISTS %s", TABLE_NAME);
        db.execSQL(drop_items_table);

        onCreate(db);
    }

    public void addItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_MEAN, item.getMean());
        values.put(KEY_PATH, item.getPath());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }
    public Item getItem(int itemId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, null, KEY_ID + " = ?", new String[] { String.valueOf(itemId) },null, null, null);
        if(cursor != null)
            cursor.moveToFirst();
        Item item = new Item(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
        return item;
    }

    public List<Item> getAllItems() {
        List<Item> itemList = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToFirst();

        while(cursor.isAfterLast() == false) {
            Item item = new Item(cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getString(3));
            itemList.add(item);
            cursor.moveToNext();
        }
        return itemList;
    }

    public void updateItem(Item item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, item.getName());
        values.put(KEY_MEAN, item.getMean());
        values.put(KEY_PATH, item.getPath());

        db.update(TABLE_NAME, values, KEY_ID + " = ?", new String[] { String.valueOf(item.getId()) });
        db.close();
    }

    public void deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, KEY_ID + " = ?", new String[] { String.valueOf(itemId) });
        db.close();
    }

    public void deleteAllItems(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_NAME);
    }
}