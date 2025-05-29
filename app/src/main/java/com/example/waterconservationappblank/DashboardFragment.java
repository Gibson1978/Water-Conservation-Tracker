package com.example.waterconservationappblank;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.graphics.Color;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.firestore.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;


public class DashboardFragment extends Fragment {

    public DashboardFragment() {}

    FirestoreRepo repo;
    BarChart barChart;

    TextView weekPercentagetv;
    TextView MoreLess;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View dashview = inflater.inflate(R.layout.fragment_dashboard, container, false);

        // initialize firestore repository and UI components
        repo = new FirestoreRepo();
        barChart = dashview.findViewById(R.id.barChart);
        weekPercentagetv = dashview.findViewById(R.id.this_week_percentage);
        MoreLess = dashview.findViewById(R.id.tv_percentage_diff);

        // set up the bar chart and load data
        setupBarChart();
        loadBarChartData(LocalDate.now());
        compareTwoWeeksUsage();
        return dashview;
    }

    private void setupBarChart() {
        // customize chart appearance
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setDrawBarShadow(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setFitBars(true); // helps with padding on bar edges

    }

    private void loadBarChartData(LocalDate baseDate) {

        // load usage data from firestore for the current week
        repo.readWeekByDate(baseDate, new FirestoreRepo.FirestoreCallback() {

            @Override
            public void onWeekDataReceived(List<DocumentSnapshot> weekDocs) {
                //Filtering the documents by the date
                List<DocumentSnapshot> filteredDocs = new ArrayList<>();
                for (DocumentSnapshot doc : weekDocs) {
                    if (doc.getString("date") != null && doc.getDouble("WaterUsage") != null) {
                        filteredDocs.add(doc);
                    }
                }

                // sort documents chronologically
                filteredDocs.sort(Comparator.comparing(doc -> doc.getString("date")));

                //Mapping the filtered data for reading later
                Map<LocalDate, Double> dateToReading = new HashMap<>();
                for (DocumentSnapshot doc : filteredDocs) {
                    LocalDate date = LocalDate.parse(doc.getString("date"));
                    Double reading = doc.getDouble("WaterUsage");
                    if (reading != null) {
                        dateToReading.put(date, reading);
                    }
                }

                //Setup the current monday and previous sunday to read
                LocalDate monday = baseDate.with(DayOfWeek.MONDAY);
                LocalDate prevSunday = monday.minusDays(1);
                ArrayList<BarEntry> entries = new ArrayList<>();
                float[] dailyUsages = new float[7];

                //Looping through each day to calculate water usage
                for (int i = 0; i < 7; i++) {
                    LocalDate day = monday.plusDays(i);
                    LocalDate prevDay = (i == 0) ? prevSunday : day.minusDays(1);

                    Double current = dateToReading.get(day);
                    Double previous = dateToReading.get(prevDay);

                    float usage = 0f;
                    if (current != null && previous != null) {
                        usage = (float) ((current - previous) * 1000);
                        if (usage < 0) usage = 0f;
                    }

                    // scale usage to fit 0-100% range
                    float scaled = Math.min((usage / 1200f) * 100f, 100f);
                    dailyUsages[i] = scaled;
                    entries.add(new BarEntry(i, scaled));

                    Log.d("WaterUsageDebug", "Date: " + day + " | Usage: " + usage + "L | Scaled: " + scaled + "%");
                }

                // render the chart with collected entries
                renderChart(entries);
            }

            @Override public void onUsageDataReceived(Map<String, Object> data) {}
            @Override public void onError(String errorMessage) {
                Log.e("WaterUsageDebug", "Chart Load Error: " + errorMessage);
            }
        });
    }

    private void renderChart(ArrayList<BarEntry> entries) {

        // create dataset and set appearance
        BarDataSet dataSet = new BarDataSet(entries, "Water Usage");
        dataSet.setColor(Color.CYAN);
        dataSet.setDrawValues(false);

        // assign data to chart
        BarData data = new BarData(dataSet);
        data.setBarWidth(0.45f);
        barChart.setData(data);

        // fix the Y-axis range to 0-100
        YAxis yAxis = barChart.getAxisLeft();
        yAxis.setAxisMinimum(0f);
        yAxis.setAxisMaximum(100f);

        // configure x-axis with weekday labels
        String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));
        xAxis.setTextSize(12f);
        xAxis.setGranularity(1f);
        xAxis.setPosition(XAxis.XAxisPosition.TOP);
        xAxis.setDrawGridLines(false);
        xAxis.setLabelCount(days.length);
        xAxis.setAxisMinimum(-0.2f);
        xAxis.setAxisMaximum(days.length - 0.815f);

        // disable grid lines and unnecessary elements
        barChart.getAxisLeft().setDrawGridLines(false);
        barChart.getAxisLeft().setDrawLabels(false);
        barChart.getAxisLeft().setDrawAxisLine(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.getDescription().setEnabled(false);

        // refresh chart
        barChart.invalidate();
    }

    private void compareTwoWeeksUsage() {

        // get this week and precious weeks monday
        LocalDate currentMonday = LocalDate.now().with(DayOfWeek.MONDAY);
        LocalDate previousMonday = currentMonday.minusWeeks(1);

        final float[][] usageData = new float[2][];

        // load last week's usage
        repo.readWeekByDate(previousMonday, new FirestoreRepo.FirestoreCallback() {
            @Override
            public void onWeekDataReceived(List<DocumentSnapshot> week1Docs) {
                usageData[0] = calculateUsagesInLiters(week1Docs, previousMonday);

                // load current weeks usage
                repo.readWeekByDate(currentMonday, new FirestoreRepo.FirestoreCallback() {
                    @Override
                    public void onWeekDataReceived(List<DocumentSnapshot> week2Docs) {
                        usageData[1] = calculateUsagesInLiters(week2Docs, currentMonday);

                        // sum and compare usage
                        float totalWeek1 = sumUsage(usageData[0]);
                        float totalWeek2 = sumUsage(usageData[1]);
                        float difference = totalWeek2 - totalWeek1;

                        float percentageDiff = 0f;
                        if (totalWeek1 > 0) {
                            percentageDiff = (difference / totalWeek1) * 100f;
                        }

                        // determine increase or decrease
                        String usageTrend;
                        if (percentageDiff < 0) {
                            usageTrend = "less";
                            percentageDiff = Math.abs(percentageDiff); // make it positive for display
                        } else {
                            usageTrend = "more";
                        }

                        // log and update UI
                        Log.d("WaterCompare", "Week 1 Total: " + totalWeek1 + "L");
                        Log.d("WaterCompare", "Week 2 Total: " + totalWeek2 + "L");
                        Log.d("WaterCompare", "Difference: " + difference + "L");
                        Log.d("WaterCompare", "Percentage Difference: " + percentageDiff + "%");

                        weekPercentagetv.setText(String.format("%.1f", percentageDiff) + "%");
                        MoreLess.setText(usageTrend);
                    }

                    @Override public void onUsageDataReceived(Map<String, Object> data) {}
                    @Override public void onError(String errorMessage) {}
                });
            }

            @Override public void onUsageDataReceived(Map<String, Object> data) {}
            @Override public void onError(String errorMessage) {}
        });
    }

    private float[] calculateUsagesInLiters(List<DocumentSnapshot> docs, LocalDate startMonday) {

        // parse and store readings by date
        Map<LocalDate, Double> readings = new HashMap<>();
        for (DocumentSnapshot doc : docs) {
            String dateStr = doc.getString("date");
            Double usage = doc.getDouble("WaterUsage");
            if (dateStr != null && usage != null) {
                readings.put(LocalDate.parse(dateStr), usage);
            }
        }

        // get the previous sunday as reference
        LocalDate previousSunday = startMonday.minusDays(1);
        float[] usages = new float[7];

        // calculate daily usage
        for (int i = 0; i < 7; i++) {
            LocalDate day = startMonday.plusDays(i);
            LocalDate prevDay = (i == 0) ? previousSunday : day.minusDays(1);

            Double current = readings.get(day);
            Double previous = readings.get(prevDay);

            if (current != null && previous != null) {
                float usage = (float) ((current - previous) * 1000);
                usages[i] = Math.max(usage, 0f);
            } else {
                usages[i] = 0f;
            }
        }
        return usages;
    }

    private float sumUsage(float[] weekData) {

        // utility to sum up total usage from an array
        float sum = 0f;
        for (float val : weekData) {
            sum += val;
        }
        return sum;
    }

}