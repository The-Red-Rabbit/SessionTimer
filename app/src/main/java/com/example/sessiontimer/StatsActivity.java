package com.example.sessiontimer;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;

public class StatsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stats);

        TextView sumTotal = (TextView)findViewById(R.id.textView2);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabLayout);
        TextView tvRevenue = (TextView)findViewById(R.id.textView3);
        TextView tvFilter = (TextView)findViewById(R.id.textViewFilter);

        CardView cardRev = findViewById(R.id.cardRev);
        CardView cardFilter = findViewById(R.id.cardFilter);
        CardView cardTrends = findViewById(R.id.cardTrends);

        //Setup 3-Tab Structure & content
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i("felix", "Tab selected: "+tab.getText());
                String tabText = tab.getText().toString();
                if (tabText.contentEquals("Revenue")) {
                    cardRev.setVisibility(View.VISIBLE);
                    cardFilter.setVisibility(View.GONE);
                    cardTrends.setVisibility(View.GONE);

                } else if (tabText.contentEquals("Filter")) {
                    cardRev.setVisibility(View.GONE);
                    cardFilter.setVisibility(View.VISIBLE);
                    cardTrends.setVisibility(View.GONE);

                } else if (tabText.contentEquals("Trends")) {
                    cardRev.setVisibility(View.GONE);
                    cardFilter.setVisibility(View.GONE);
                    cardTrends.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        Intent intent = getIntent();
        double currentTimerBase = intent.getLongExtra("currentTimer",0);
        if (currentTimerBase != 0) {
            double currentDeltaT = SystemClock.elapsedRealtime() - currentTimerBase;
            double currIncome = (currentDeltaT / 3600000.0)*12.0;
            DecimalFormat df = new DecimalFormat("###.##");
            tvRevenue.setText("Already earned now: "+df.format(currIncome)+" €");
        }

        //Setup Total time display:
        EventGoogleCal dummyEvent = new EventGoogleCal(this);
        long totalMin = dummyEvent.getTotalMins("Work Session",12);
        int digitHours = (int)totalMin/60;
        int digitMins = (int)totalMin%60;
        sumTotal.setText("Total time invested:\n"+digitHours+"h "+digitMins+"m");





        final GraphView graph = (GraphView) findViewById(R.id.graph);
        graph.setVisibility(View.VISIBLE);
        graph.setTitle("Last week");



        // generate Dates
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -6);
        Date[] lastWeek = new Date[7];
        long[] minsWeek = dummyEvent.getLastWeekMins(calendar.getTime(),"Work Session", 12);
        for (int i=0; i < lastWeek.length; i++) {
            lastWeek[i] = calendar.getTime();
            calendar.add(Calendar.DATE, 1);
        }

        //Log.i("felix", " array: "+ Arrays.toString(lastWeek));

        DataPoint[] points = new DataPoint[7];
        //Log.i("felix", "schwanzvergleich: datapoints "+points.length+" - last week "+lastWeek.length+ " - minsweek "+minsWeek.length);
        for (int i = 0; i < points.length; i++) {
            points[i] = new DataPoint(lastWeek[i], ((double)minsWeek[i]/60));
            //Log.i("felix", "DataPoint: x = "+lastWeek[i]+", y = "+((double)minsWeek[c.get(Calendar.DAY_OF_WEEK)]/60));
        }
        LineGraphSeries<DataPoint> seriesLines = new LineGraphSeries<>(points);
        //seriesLines.setColor(Color.parseColor("#0097A7"));
        seriesLines.setColor(R.color.pearl_700);
        seriesLines.setAnimated(true);
        seriesLines.setDrawDataPoints(false);
        seriesLines.setDrawAsPath(true);
        seriesLines.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                // Calculate work duration of the weekday clicked on
                long totalMinsDay = Math.round(dataPoint.getY()*60);
                String timeLogged = "";
                if (totalMinsDay == 0) {
                    timeLogged = "nothing";
                } else if (totalMinsDay < 60) {
                    timeLogged = totalMinsDay+" minutes";
                } else {
                    int digitHours = (int)totalMinsDay/60;
                    int digitMins = (int)totalMinsDay%60;
                    timeLogged = digitHours+" hours "+digitMins+" minutes";
                }
                String timeLoggedTodayMsg = "On "+android.text.format.DateFormat.format("EEEE, dd.MM.", new java.util.Date((long) dataPoint.getX()))+" you logged\n"+timeLogged;
                Snackbar.make(findViewById(R.id.graph), timeLoggedTodayMsg, BaseTransientBottomBar.LENGTH_SHORT).show();
                tvFilter.setText(timeLoggedTodayMsg);
            }
        });
        graph.addSeries(seriesLines);

        // set date label formatter
        graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(this));
        graph.getGridLabelRenderer().setNumHorizontalLabels(7);
        graph.getGridLabelRenderer().setHorizontalLabelsAngle(45);
        graph.getGridLabelRenderer().setVerticalAxisTitle("hours");

        // set manual x bounds to have nice steps
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(lastWeek[0].getTime());
        graph.getViewport().setMaxX(lastWeek[lastWeek.length-1].getTime());

        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        double max = Double.NEGATIVE_INFINITY;
        for (long curr: minsWeek) {
            max = Math.max(max, curr);
        }
        graph.getViewport().setMaxY((int)(max/60)+1);
        graph.getGridLabelRenderer().setNumVerticalLabels((int)(max/60)+2);

        graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.BOTH);
        graph.getGridLabelRenderer().setHumanRounding(false);
    }
}