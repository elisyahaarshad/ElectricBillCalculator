package com.example.electricbillcalculator;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {

    ListView lvBills;
    DatabaseHelper dbHelper;

    ArrayList<Integer> billIds = new ArrayList<>();
    ArrayList<String> billMonths = new ArrayList<>();
    ArrayList<Double> billCosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        lvBills = findViewById(R.id.lvBills);
        dbHelper = new DatabaseHelper(this);

        loadBills();

        // Click a bill to open DetailActivity
        lvBills.setOnItemClickListener((parent, view, position, id) -> {

            int billId = billIds.get(position);

            Intent intent = new Intent(
                    HistoryActivity.this,
                    DetailActivity.class
            );

            intent.putExtra("billId", billId);
            startActivity(intent);
        });
    }

    // Load bills from SQLite database
    private void loadBills() {

        billIds.clear();
        billMonths.clear();
        billCosts.clear();

        Cursor cursor = dbHelper.getAllBills();

        if (cursor != null && cursor.moveToFirst()) {

            do {

                int id = cursor.getInt(
                        cursor.getColumnIndexOrThrow("id")
                );

                String month = cursor.getString(
                        cursor.getColumnIndexOrThrow("month")
                );

                double finalCost = cursor.getDouble(
                        cursor.getColumnIndexOrThrow("finalCost")
                );

                billIds.add(id);
                billMonths.add(month);
                billCosts.add(finalCost);

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        // Set custom adapter
        BillAdapter adapter = new BillAdapter();
        lvBills.setAdapter(adapter);
    }

    // Custom adapter for Month and Final Cost alignment
    private class BillAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return billMonths.size();
        }

        @Override
        public Object getItem(int position) {
            return billMonths.get(position);
        }

        @Override
        public long getItemId(int position) {
            return billIds.get(position);
        }

        @Override
        public View getView(int position, View convertView,
                            ViewGroup parent) {

            if (convertView == null) {

                convertView = LayoutInflater.from(HistoryActivity.this)
                        .inflate(
                                R.layout.item_bill_history,
                                parent,
                                false
                        );
            }

            TextView tvMonth = convertView.findViewById(
                    R.id.tvHistoryMonth
            );

            TextView tvCost = convertView.findViewById(
                    R.id.tvHistoryCost
            );

            // Display month on the left
            tvMonth.setText(billMonths.get(position));

            // Display final cost on the right
            tvCost.setText(String.format(
                    Locale.getDefault(),
                    "RM %.2f",
                    billCosts.get(position)
            ));

            return convertView;
        }
    }

    // Refresh history when returning from DetailActivity
    @Override
    protected void onResume() {
        super.onResume();
        loadBills();
    }
}