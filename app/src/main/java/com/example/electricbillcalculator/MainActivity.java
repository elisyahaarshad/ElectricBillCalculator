package com.example.electricbillcalculator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Spinner spMonth;
    EditText etUnit;
    RadioGroup rgRebate;
    Button btnCalculate, btnHistory, btnAbout;
    TextView tvTotal, tvFinal;

    DatabaseHelper dbHelper;

    String[] months = {
            "January", "February", "March", "April",
            "May", "June", "July", "August",
            "September", "October", "November", "December"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect UI components
        spMonth = findViewById(R.id.spMonth);
        etUnit = findViewById(R.id.etUnit);
        rgRebate = findViewById(R.id.rgRebate);

        btnCalculate = findViewById(R.id.btnCalculate);
        btnHistory = findViewById(R.id.btnHistory);
        btnAbout = findViewById(R.id.btnAbout);

        tvTotal = findViewById(R.id.tvTotal);
        tvFinal = findViewById(R.id.tvFinal);

        // Initialize Month Spinner
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(
                MainActivity.this,
                android.R.layout.simple_spinner_item,
                months
        );

        monthAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spMonth.setAdapter(monthAdapter);

        // Initialize database
        dbHelper = new DatabaseHelper(this);

        // Calculate button
        btnCalculate.setOnClickListener(v -> calculateBill());

        // History button
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    HistoryActivity.class
            );
            startActivity(intent);
        });

        // About button
        btnAbout.setOnClickListener(v -> {
            Intent intent = new Intent(
                    MainActivity.this,
                    AboutActivity.class
            );
            startActivity(intent);
        });
    }

    private void calculateBill() {

        // Validate electricity unit input
        String unitText = etUnit.getText().toString().trim();

        if (unitText.isEmpty()) {
            etUnit.setError("Please enter electricity units");
            etUnit.requestFocus();
            return;
        }

        int units;

        try {
            units = Integer.parseInt(unitText);
        } catch (NumberFormatException e) {
            etUnit.setError("Please enter a valid number");
            etUnit.requestFocus();
            return;
        }

        if (units < 1 || units > 1000) {
            etUnit.setError("Units must be between 1 and 1000 kWh");
            etUnit.requestFocus();
            return;
        }

        // Get selected month
        String month = spMonth.getSelectedItem().toString();

        // Get selected rebate percentage
        int selectedRebateId = rgRebate.getCheckedRadioButtonId();

        if (selectedRebateId == -1) {
            Toast.makeText(
                    this,
                    "Please select a rebate percentage",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        RadioButton selectedRadioButton =
                findViewById(selectedRebateId);

        String rebateText =
                selectedRadioButton.getText().toString();

        int rebatePercent = Integer.parseInt(
                rebateText.replace("%", "").trim()
        );

        // Calculate progressive electricity charges
        double totalCharges = 0;

        if (units <= 200) {

            totalCharges = units * 0.218;

        } else if (units <= 300) {

            totalCharges = (200 * 0.218)
                    + ((units - 200) * 0.334);

        } else if (units <= 600) {

            totalCharges = (200 * 0.218)
                    + (100 * 0.334)
                    + ((units - 300) * 0.516);

        } else {

            totalCharges = (200 * 0.218)
                    + (100 * 0.334)
                    + (300 * 0.516)
                    + ((units - 600) * 0.546);
        }

        // Calculate rebate and final cost
        double rebateAmount =
                totalCharges * rebatePercent / 100.0;

        double finalCost = totalCharges - rebateAmount;

        // Display results
        tvTotal.setText(String.format(
                Locale.getDefault(),
                "Total Charges: RM %.2f",
                totalCharges
        ));

        tvFinal.setText(String.format(
                Locale.getDefault(),
                "Final Cost: RM %.2f",
                finalCost
        ));

        // Save bill into SQLite database
        boolean inserted = dbHelper.insertBill(
                month,
                units,
                totalCharges,
                rebatePercent,
                finalCost
        );

        if (inserted) {

            Toast.makeText(
                    this,
                    "Bill calculated and saved successfully!",
                    Toast.LENGTH_SHORT
            ).show();

        } else {

            Toast.makeText(
                    this,
                    "Error saving bill!",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }
}
