package com.pes.fibness;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ViewProfileActivity extends AppCompatActivity {

    private ImageView ivUser, backButton;
    private TextView username, age, country, description;
    BarChart barChart;
    User u = User.getInstance();

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        ivUser = findViewById(R.id.iv_user);
        backButton = (ImageView) findViewById(R.id.backImgButton);
        username = findViewById(R.id.username);
        age = findViewById(R.id.age);
        country = findViewById(R.id.country);
        description = findViewById(R.id.description);
        barChart = (BarChart) findViewById(R.id.chart);

        showUserInfo();
        showChart();


        /*to go back*/
        backButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View v) {
                onBackPressed();
                finish();

            }
        });

    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void showUserInfo() {
        Resources res = getResources();
        String[] planets = res.getStringArray(R.array.countries);

        boolean validImage = false;
        byte[] userImage = null;
        if (u.getImage() != null) {
            validImage = true;
            userImage = u.getImage();
        }

        username.setText(u.getName());

        if((u.getBirthDate().equals("null")|| u.getBirthDate().equals("")))
            age.setText("");
        else age.setText(howManyYears(u.getBirthDate()));

        if((u.getCountry().equals("null")|| u.getCountry().equals("")))
            country.setText("");
        else country.setText(planets[Integer.parseInt(u.getCountry())]);

        System.out.println("segundo");
        if((u.getDescription().equals("null")|| u.getDescription().equals("")))
            description.setText("");
        else description.setText(u.getDescription());

        if (validImage) {
            Glide.with(ViewProfileActivity.this)
                    .load(userImage)
                    .centerCrop()
                    .circleCrop()
                    .skipMemoryCache(true)
                    .into(ivUser);
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String howManyYears(String birthDate){
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate fechaNac = LocalDate.parse(birthDate, fmt);
        LocalDate ahora = LocalDate.now();

        Period periodo = Period.between(fechaNac, ahora);
        System.out.printf("Tu edad es: %s años, %s meses y %s días",
                periodo.getYears(), periodo.getMonths(), periodo.getDays());
        return String.valueOf(periodo.getYears());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String calculateAge(String birth){

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate Datebirth = LocalDate.parse(birth, fmt);
        LocalDate now = LocalDate.now();

        return String.valueOf(Period.between(Datebirth, now).getYears());
    }



    private void showChart() {

        Description d = new Description();
        d.setEnabled(false);
        barChart.setDescription(d);


        final String[] week = new String[] {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new MyValueFormatter(week));
        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.getAxisRight().setAxisMinimum(0);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setCenterAxisLabels(true);
        xAxis.setGranularityEnabled(true);

        List<BarEntry> distances = new ArrayList<>();
        List<BarEntry> calories = new ArrayList<>();

        Date now = new Date();
        SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE"); // the day of the week spelled out completely
        String day = simpleDateformat.format(now);
        System.out.println("thar day: " + day);
        ArrayList<Statistic> statistics = u.getStatistics();
        int loop = statistics.size();
        /*
        switch (day){
            case "Monday":
                loop = 1;
                break;
            case "Tuesday":
                loop = 2;
                break;
            case "Wednesday":
                loop = 3;
                break;
            case "Thursday":
                loop = 4;
                break;
            case "Friday":
                loop = 5;
                break;
            case "Saturday":
                loop = 6;
                break;
            case "Sunday":
                loop = 7;
                break;
        }

         */
        System.out.println("loop: " + loop);
        int y = 0;
        for(int i=0; i<14; i+=2){
            if(y == loop){
                distances.add(new BarEntry(i, 0));
                calories.add(new BarEntry(i+1, 0));
            }
            else {
                distances.add(new BarEntry(i, statistics.get(y).dst));
                calories.add(new BarEntry(i + 1, (statistics.get(y).dst * 60) / 1000)); //(dst * 60)/ 1000
                System.out.println("------it:" + y);
                ++y;
            }


        }


        /*Create 2 datasets*/
        BarDataSet setDistance = new BarDataSet(distances, "Distance");
        setDistance.setColor(Color.rgb(78,186,221));
        BarDataSet setCalorie = new BarDataSet(calories, "Calorie");
        setCalorie.setColor(Color.rgb(140, 221, 159));
        //setCalorie.setColors(ColorTemplate.COLORFUL_COLORS);

        BarData data = new BarData(setDistance, setCalorie);
        //data.setBarWidth(0.75f); set custom bar width
        barChart.setData(data);
        barChart.animateXY(2000, 2000);
        barChart.invalidate(); //refresh


    }







}
