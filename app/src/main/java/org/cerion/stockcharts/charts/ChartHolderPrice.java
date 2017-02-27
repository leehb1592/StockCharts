package org.cerion.stockcharts.charts;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.github.mikephil.charting.charts.Chart;

import org.cerion.stockcharts.R;
import org.cerion.stocklist.charts.PriceChart;
import org.cerion.stocklist.functions.IPriceOverlay;
import org.cerion.stocklist.model.Interval;

class ChartHolderPrice extends ChartHolderBase {

    private static final String TAG = ChartHolderIndicator.class.getSimpleName();

    public ChartHolderPrice(Context context, String symbol, Interval interval) {
        super(context, symbol, interval);

        mChartParams = mChartParams.toPrice();

        findViewById(R.id.function).setVisibility(View.GONE);
        findViewById(R.id.check_linechart).setVisibility(View.VISIBLE);

        init();
        reload();
    }

    private void init() {

        findViewById(R.id.save_edit_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View controls = findViewById(R.id.edit_layout);

                if(controls.getVisibility() == View.VISIBLE) { // SAVE

                    mChartParams.overlays.clear();

                    // Get overlay parameters
                    for(int i = 0; i < mOverlays.getChildCount(); i++) {
                        OverlayEditControl editControl = (OverlayEditControl)mOverlays.getChildAt(i);
                        mChartParams.overlays.add(editControl.getOverlayFunction());
                    }

                    mChartParams.logscale = mCheckLogScale.isChecked();
                    params().lineChart = ((CheckBox)findViewById(R.id.check_linechart)).isChecked();

                    reload();
                    setInEditMode(false);
                } else {
                    setInEditMode(true);
                }
            }
        });

    }

    private ChartParams.Price params() {
        return (ChartParams.Price)mChartParams;
    }

    @Override
    public Chart getChart() {
        PriceChart chart = new PriceChart();
        ChartParams.Price params = params();
        chart.logScale = params.logscale;
        chart.candleData = !params.lineChart;
        chart.interval = params.interval;

        for(IPriceOverlay ol : params.overlays) {
            chart.addOverlay(ol);
        }

        //return mChartFactory.getPriceChart(params());
        return mChartFactory.getChart(chart, params.symbol);
    }

}
