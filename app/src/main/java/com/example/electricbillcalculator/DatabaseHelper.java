package com.example.electricbillcalculator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ElectricBill.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_BILLS = "bills";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        String createTable = "CREATE TABLE " + TABLE_BILLS + " ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "month TEXT, "
                + "units INTEGER, "
                + "totalCharges REAL, "
                + "rebate REAL, "
                + "finalCost REAL)";

        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BILLS);
        onCreate(db);
    }

    // Insert bill
    public boolean insertBill(String month, int units,
                              double totalCharges,
                              double rebate,
                              double finalCost) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("units", units);
        values.put("totalCharges", totalCharges);
        values.put("rebate", rebate);
        values.put("finalCost", finalCost);

        long result = db.insert(TABLE_BILLS, null, values);

        return result != -1;
    }

    // Retrieve all bills
    public Cursor getAllBills() {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_BILLS,
                null
        );
    }

    // Retrieve bill by ID
    public Cursor getBillById(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT * FROM " + TABLE_BILLS
                        + " WHERE id = ?",
                new String[]{String.valueOf(id)}
        );
    }

    // Update bill
    public boolean updateBill(int id, String month,
                              int units, double totalCharges,
                              double rebate, double finalCost) {

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("month", month);
        values.put("units", units);
        values.put("totalCharges", totalCharges);
        values.put("rebate", rebate);
        values.put("finalCost", finalCost);

        int result = db.update(
                TABLE_BILLS,
                values,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }

    // Delete bill
    public boolean deleteBill(int id) {

        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_BILLS,
                "id = ?",
                new String[]{String.valueOf(id)}
        );

        return result > 0;
    }
}
