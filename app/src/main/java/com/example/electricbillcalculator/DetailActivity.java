package com.example.electricbillcalculator;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
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

    String[] monthOptions = {
            "January", "February", "March",
            "April", "May", "June",
            "July", "August", "September",
            "October", "November", "December"
    };

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

        if (cursor != null) {

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

        // Main layout
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);

        int padding = (int) (20 * getResources()
                .getDisplayMetrics().density);

        mainLayout.setPadding(padding, padding,
                padding, padding);

        // ScrollView supaya semua content boleh dilihat
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Month Spinner
        TextView monthLabel = new TextView(this);
        monthLabel.setText("Select Month");
        monthLabel.setTextSize(16);
        monthLabel.setPadding(0, 0, 0, 8);

        Spinner spinner = new Spinner(this);

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
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

        // Electricity Units
        TextView unitsLabel = new TextView(this);
        unitsLabel.setText("Electricity Usage (kWh)");
        unitsLabel.setTextSize(16);
        unitsLabel.setPadding(0, 16, 0, 8);

        EditText etEditUnits = new EditText(this);

        etEditUnits.setHint("Enter units (1 - 1000)");
        etEditUnits.setInputType(
                InputType.TYPE_CLASS_NUMBER);

        etEditUnits.setText(String.valueOf(units));

        // Rebate Label
        TextView rebateLabel = new TextView(this);
        rebateLabel.setText("Rebate (%)");
        rebateLabel.setTextSize(16);
        rebateLabel.setPadding(0, 16, 0, 8);

        // RadioGroup
        RadioGroup radioGroup = new RadioGroup(this);
        radioGroup.setOrientation(RadioGroup.VERTICAL);

        int[] radioIds = {
                101, 102, 103, 104, 105, 106
        };

        for (int i = 0; i <= 5; i++) {

            RadioButton radioButton =
                    new RadioButton(this);

            radioButton.setId(radioIds[i]);
            radioButton.setText(i + "%");
            radioButton.setTextSize(16);

            radioGroup.addView(radioButton);

            // Set rebate yang sedia ada
            if (i == (int) rebate) {
                radioButton.setChecked(true);
            }
        }

        // Add semua komponen ke layout
        layout.addView(monthLabel);
        layout.addView(spinner);

        layout.addView(unitsLabel);
        layout.addView(etEditUnits);

        layout.addView(rebateLabel);
        layout.addView(radioGroup);

        scrollView.addView(layout);
        mainLayout.addView(scrollView);

        // Dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Electricity Bill")
                .setView(mainLayout)
                .setPositiveButton("UPDATE", null)
                .setNegativeButton("CANCEL", null)
                .create();

        dialog.setOnShowListener(d -> {

            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> {

                        // Validate units
                        String newUnitsText =
                                etEditUnits.getText()
                                        .toString().trim();

                        if (newUnitsText.isEmpty()) {

                            etEditUnits.setError(
                                    "Please enter electricity units");

                            etEditUnits.requestFocus();
                            return;
                        }

                        int newUnits;

                        try {

                            newUnits = Integer.parseInt(
                                    newUnitsText);

                        } catch (NumberFormatException e) {

                            etEditUnits.setError(
                                    "Please enter a valid number");

                            etEditUnits.requestFocus();
                            return;
                        }

                        if (newUnits < 1 || newUnits > 1000) {

                            etEditUnits.setError(
                                    "Units must be between 1 and 1000");

                            etEditUnits.requestFocus();
                            return;
                        }

                        // Validate rebate selection
                        int selectedRadio =
                                radioGroup.getCheckedRadioButtonId();

                        if (selectedRadio == -1) {

                            Toast.makeText(
                                    DetailActivity.this,
                                    "Please select a rebate",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        // Get selected values
                        String newMonth =
                                spinner.getSelectedItem().toString();

                        int newRebate = selectedRadio - 101;

                        // Calculate charges
                        double newTotal =
                                calculateCharges(newUnits);

                        double rebateAmount =
                                newTotal * newRebate / 100.0;

                        double newFinal =
                                newTotal - rebateAmount;

                        // Update SQLite
                        boolean updated = dbHelper.updateBill(
                                billId,
                                newMonth,
                                newUnits,
                                newTotal,
                                newRebate,
                                newFinal
                        );

                        if (updated) {

                            Toast.makeText(
                                    DetailActivity.this,
                                    "Bill updated successfully!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadBillDetails();

                            dialog.dismiss();

                        } else {

                            Toast.makeText(
                                    DetailActivity.this,
                                    "Failed to update bill!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });
        });

        dialog.show();
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

                .setMessage(
                        "Are you sure you want to delete this bill?")

                .setPositiveButton("YES", (dialog, which) -> {

                    boolean deleted =
                            dbHelper.deleteBill(billId);

                    if (deleted) {

                        Toast.makeText(
                                this,
                                "Bill deleted successfully!",
                                Toast.LENGTH_SHORT
                        ).show();

                        finish();

                    } else {

                        Toast.makeText(
                                this,
                                "Failed to delete bill!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                })

                .setNegativeButton("NO", null)

                .show();
    }
}
