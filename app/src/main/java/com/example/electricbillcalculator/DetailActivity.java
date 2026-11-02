package com.example.electricbillcalculator;

import android.app.AlertDialog;
import android.os.Bundle;
import android.database.Cursor;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class DetailActivity extends AppCompatActivity {

    TextView tvMonth, tvUnits, tvTotalCharges,
            tvRebate, tvFinalCost;

    Button btnEdit, btnDelete;

    DatabaseHelper dbHelper;

    int billId;
    String month;
    int units;
    double totalCharges;
    double rebate;
    double finalCost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        tvMonth = findViewById(R.id.tvMonth);
        tvUnits = findViewById(R.id.tvUnits);
        tvTotalCharges = findViewById(R.id.tvTotalCharges);
        tvRebate = findViewById(R.id.tvRebate);
        tvFinalCost = findViewById(R.id.tvFinalCost);

        btnEdit = findViewById(R.id.btnEdit);
        btnDelete = findViewById(R.id.btnDelete);

        dbHelper = new DatabaseHelper(this);

        billId = getIntent().getIntExtra("billId", -1);

        loadBillDetails();

        btnEdit.setOnClickListener(v -> showEditDialog());

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    private void loadBillDetails() {

        Cursor cursor = dbHelper.getBillById(billId);

        if (cursor.moveToFirst()) {

            month = cursor.getString(
                    cursor.getColumnIndexOrThrow("month"));

            units = cursor.getInt(
                    cursor.getColumnIndexOrThrow("units"));

            totalCharges = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("totalCharges"));

            rebate = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("rebate"));

            finalCost = cursor.getDouble(
                    cursor.getColumnIndexOrThrow("finalCost"));

            displayDetails();
        }

        cursor.close();
    }

    private void displayDetails() {

        tvMonth.setText("Month: " + month);

        tvUnits.setText("Electricity Usage: "
                + units + " kWh");

        tvTotalCharges.setText(String.format(
                Locale.US,
                "Total Charges: RM %.2f",
                totalCharges));

        tvRebate.setText(String.format(
                Locale.US,
                "Rebate: %.0f%%",
                rebate));

        tvFinalCost.setText(String.format(
                Locale.US,
                "Final Cost: RM %.2f",
                finalCost));
    }

    private void showEditDialog() {

        String[] monthOptions = {
                "January", "February", "March",
                "April", "May", "June",
                "July", "August", "September",
                "October", "November", "December"
        };

        android.widget.LinearLayout layout =
                new android.widget.LinearLayout(this);

        layout.setOrientation(
                android.widget.LinearLayout.VERTICAL);

        layout.setPadding(50, 20, 50, 10);

        android.widget.Spinner spinner =
                new android.widget.Spinner(this);

        android.widget.ArrayAdapter<String> adapter =
                new android.widget.ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        monthOptions);

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        int monthPosition = 0;

        for (int i = 0; i < monthOptions.length; i++) {
            if (monthOptions[i].equals(month)) {
                monthPosition = i;
                break;
            }
        }

        spinner.setSelection(monthPosition);

        android.widget.EditText etEditUnits =
                new android.widget.EditText(this);

        etEditUnits.setHint("Electricity units (1 - 1000)");
        etEditUnits.setInputType(
                android.text.InputType.TYPE_CLASS_NUMBER);

        etEditUnits.setText(String.valueOf(units));

        android.widget.RadioGroup radioGroup =
                new android.widget.RadioGroup(this);

        radioGroup.setOrientation(
                android.widget.RadioGroup.HORIZONTAL);

        int[] radioIds = {101, 102, 103, 104, 105, 106};

        for (int i = 0; i <= 5; i++) {

            android.widget.RadioButton radioButton =
                    new android.widget.RadioButton(this);

            radioButton.setId(radioIds[i]);
            radioButton.setText(i + "%");

            radioGroup.addView(radioButton);

            if (i == (int) rebate) {
                radioButton.setChecked(true);
            }
        }

        layout.addView(spinner);
        layout.addView(etEditUnits);
        layout.addView(radioGroup);

        new AlertDialog.Builder(this)
                .setTitle("Edit Electricity Bill")
                .setView(layout)
                .setPositiveButton("UPDATE", (dialog, which) -> {

                    String newUnitsText =
                            etEditUnits.getText().toString().trim();

                    if (newUnitsText.isEmpty()) {
                        Toast.makeText(this,
                                "Please enter electricity units",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int newUnits =
                            Integer.parseInt(newUnitsText);

                    if (newUnits < 1 || newUnits > 1000) {
                        Toast.makeText(this,
                                "Units must be between 1 and 1000",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String newMonth =
                            spinner.getSelectedItem().toString();

                    int selectedRadio =
                            radioGroup.getCheckedRadioButtonId();

                    double newRebate = selectedRadio - 101;

                    double newTotal = calculateCharges(newUnits);

                    double newFinal = newTotal
                            - (newTotal * newRebate / 100);

                    boolean updated = dbHelper.updateBill(
                            billId,
                            newMonth,
                            newUnits,
                            newTotal,
                            newRebate,
                            newFinal);

                    if (updated) {

                        Toast.makeText(this,
                                "Bill updated successfully!",
                                Toast.LENGTH_SHORT).show();

                        loadBillDetails();

                    } else {

                        Toast.makeText(this,
                                "Failed to update bill!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private double calculateCharges(int units) {

        if (units <= 200) {
            return units * 0.218;
        }

        if (units <= 300) {
            return (200 * 0.218)
                    + ((units - 200) * 0.334);
        }

        if (units <= 600) {
            return (200 * 0.218)
                    + (100 * 0.334)
                    + ((units - 300) * 0.516);
        }

        return (200 * 0.218)
                + (100 * 0.334)
                + (300 * 0.516)
                + ((units - 600) * 0.546);
    }

    private void confirmDelete() {

        new AlertDialog.Builder(this)
                .setTitle("Delete Bill")
                .setMessage("Are you sure you want to delete this bill?")
                .setPositiveButton("YES", (dialog, which) -> {

                    boolean deleted =
                            dbHelper.deleteBill(billId);

                    if (deleted) {

                        Toast.makeText(this,
                                "Bill deleted successfully!",
                                Toast.LENGTH_SHORT).show();

                        finish();

                    } else {

                        Toast.makeText(this,
                                "Failed to delete bill!",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("NO", null)
                .show();
    }
}
