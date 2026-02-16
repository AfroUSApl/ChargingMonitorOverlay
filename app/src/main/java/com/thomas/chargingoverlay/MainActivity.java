package com.thomas.chargingoverlay;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.*;
import com.github.mikephil.charting.components.*;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextView voltage, current, power, temp, cpuTemp;
    private TextView cycleCount, healthPercent, designCapacity;
    private TextView directCurrent, chargingStep, timeToFull;

    private LineChart powerGraph;

    private Handler handler = new Handler();
    private Runnable runnable;

    private ArrayList<Entry> powerEntries = new ArrayList<>();
    private int graphIndex = 0;
    private double smoothedPower = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = findViewById(R.id.startButton);
        Button stop = findViewById(R.id.stopButton);

        cycleCount = findViewById(R.id.cycleCount);
        healthPercent = findViewById(R.id.healthPercent);
        designCapacity = findViewById(R.id.designCapacity);

        voltage = findViewById(R.id.voltage);
        current = findViewById(R.id.current);
        power = findViewById(R.id.power);
        temp = findViewById(R.id.temp);
        cpuTemp = findViewById(R.id.cpuTemp);
        directCurrent = findViewById(R.id.directCurrent);
        chargingStep = findViewById(R.id.chargingStep);
        timeToFull = findViewById(R.id.timeToFull);

        powerGraph = findViewById(R.id.powerGraph);
        setupGraph();

        start.setOnClickListener(v -> startMonitoring());
        stop.setOnClickListener(v -> stopMonitoring());

        loadStaticInfo();
    }

    private void setupGraph() {
        powerGraph.getDescription().setEnabled(false);
        powerGraph.getXAxis().setDrawLabels(false);
        powerGraph.getAxisRight().setEnabled(false);
        powerGraph.getAxisLeft().setAxisMinimum(-5f);
        powerGraph.getAxisLeft().setAxisMaximum(25f);
    }

    private void startMonitoring() {
        runnable = new Runnable() {
            @Override
            public void run() {
                updateLiveData();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(runnable);
    }

    private void stopMonitoring() {
        if (runnable != null) handler.removeCallbacks(runnable);
    }

    private void loadStaticInfo() {

        String cycles = MonitorService.readSys("/sys/class/power_supply/battery/battery_cycle");
        cycleCount.setText("Cycle Count: " + cycles);

        double design = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/charge_full_design"));

        double real = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/charge_full"));

        designCapacity.setText(String.format(Locale.US,
                "Design vs Real: %.0f / %.0f mAh", design / 1000, real / 1000));

        if (design > 0)
            healthPercent.setText(String.format(Locale.US,
                    "Health: %.1f %%", (real / design) * 100));
    }

    private void updateLiveData() {

        double rawVoltage = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/voltage_now"));

        double rawCurrent = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/current_now"));

        double voltageV = rawVoltage / 1_000_000.0;

        // auto detect unit
        double currentA;
        if (Math.abs(rawCurrent) > 100000)
            currentA = rawCurrent / 1_000_000.0; // microamps
        else
            currentA = rawCurrent / 1000.0;      // milliamps

        double powerW = voltageV * currentA;

        // smooth power
        smoothedPower = smoothedPower * 0.7 + powerW * 0.3;

        voltage.setText(String.format(Locale.US, "Voltage: %.2f V", voltageV));
        current.setText(String.format(Locale.US, "Current: %.2f A", currentA));
        power.setText(String.format(Locale.US, "Power: %.2f W", smoothedPower));

        addGraphPoint(smoothedPower);

        double battTemp = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/temp")) / 10.0;

        temp.setText(String.format(Locale.US,
                "Battery Temp: %.1f °C", battTemp));

        double cpu = parseDouble(
                MonitorService.readSys("/sys/class/thermal/thermal_zone0/temp"));

        if (cpu > 1000) cpu /= 1000.0;

        cpuTemp.setText(String.format(Locale.US,
                "CPU Temp: %.1f °C", cpu));

        double direct = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_iin"));

        directCurrent.setText(String.format(Locale.US,
                "Direct Input Current: %.2f A", direct / 1_000_000.0));

        chargingStep.setText("Charging Step: " +
                MonitorService.readSys("/sys/class/power_supply/battery/direct_charging_step"));

        double ttf = parseDouble(
                MonitorService.readSys("/sys/class/power_supply/battery/time_to_full_now"));

        timeToFull.setText(String.format(Locale.US,
                "Time To Full: %.0f min", ttf / 60.0));
    }

    private void addGraphPoint(double value) {

        powerEntries.add(new Entry(graphIndex++, (float) value));

        if (powerEntries.size() > 60)
            powerEntries.remove(0);

        LineDataSet set = new LineDataSet(powerEntries, "Power (W)");
        set.setDrawCircles(false);
        set.setLineWidth(2f);

        LineData data = new LineData(set);
        powerGraph.setData(data);
        powerGraph.invalidate();
    }

    private double parseDouble(String s) {
        try { return Double.parseDouble(s); }
        catch (Exception e) { return 0; }
    }
}