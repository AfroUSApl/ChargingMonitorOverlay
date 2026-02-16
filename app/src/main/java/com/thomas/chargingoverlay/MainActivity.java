package com.thomas.chargingoverlay;

import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    TextView voltage,current,power,battTemp,cpuTemp,directCurrent,chargingStep,timeToFull;
    TextView cycleCount,healthPercent,designCapacity;
    PowerGraphView graph;

    Handler handler = new Handler();
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bindViews();

        Button start = findViewById(R.id.startBtn);
        Button stop = findViewById(R.id.stopBtn);

        start.setOnClickListener(v -> startMonitoring());
        stop.setOnClickListener(v -> stopMonitoring());
    }

    private void bindViews(){
        voltage=findViewById(R.id.voltage);
        current=findViewById(R.id.current);
        power=findViewById(R.id.power);
        battTemp=findViewById(R.id.battTemp);
        cpuTemp=findViewById(R.id.cpuTemp);
        directCurrent=findViewById(R.id.directCurrent);
        chargingStep=findViewById(R.id.chargingStep);
        timeToFull=findViewById(R.id.timeToFull);

        cycleCount=findViewById(R.id.cycleCount);
        healthPercent=findViewById(R.id.healthPercent);
        designCapacity=findViewById(R.id.designCapacity);

        graph=findViewById(R.id.powerGraph);
    }

    private void startMonitoring(){
        runnable=new Runnable(){
            @Override
            public void run(){
                updateData();
                handler.postDelayed(this,1000);
            }
        };
        handler.post(runnable);
    }

    private void stopMonitoring(){
        handler.removeCallbacks(runnable);
    }

    private void updateData(){
        double v=readDouble("/sys/class/power_supply/battery/voltage_now")/1000000;
        double c=readDouble("/sys/class/power_supply/battery/current_now")/1000;
        double p=v*(c/1000);
        double bt=readDouble("/sys/class/power_supply/battery/temp")/10;
        double ct=readDouble("/sys/class/thermal/thermal_zone0/temp")/1000;

        voltage.setText("Voltage: "+String.format("%.2f V",v));
        current.setText("Current: "+String.format("%.0f mA",c));
        power.setText("Power: "+String.format("%.2f W",p));
        battTemp.setText("Battery Temp: "+bt+" °C");
        cpuTemp.setText("CPU Temp: "+ct+" °C");

        graph.addValue((float)p);

        logCSV(v,c,p,bt,ct);
    }

    private double readDouble(String path){
        try{
            Process p=Runtime.getRuntime().exec(new String[]{"su","-c","cat "+path});
            java.io.BufferedReader r=new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()));
            return Double.parseDouble(r.readLine());
        }catch(Exception e){
            return 0;
        }
    }

    private void logCSV(double v,double c,double p,double bt,double ct){
        try{
            String time=new SimpleDateFormat("HH:mm:ss",Locale.US).format(new Date());
            FileWriter fw=new FileWriter(getExternalFilesDir(null)+"/charging_log.csv",true);
            fw.append(time+","+v+","+c+","+p+","+bt+","+ct+"\n");
            fw.close();
        }catch(IOException ignored){}
    }
}