package org.cerion.stockcharts.ui.charts

import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.cerion.stockcharts.common.Constants
import org.cerion.stockcharts.repository.PreferenceRepository
import org.cerion.stocks.core.PriceList
import org.cerion.stocks.core.charts.*
import org.cerion.stocks.core.indicators.AccumulationDistributionLine
import org.cerion.stocks.core.indicators.MACD
import org.cerion.stocks.core.model.Interval
import org.cerion.stocks.core.model.Symbol
import org.cerion.stocks.core.overlays.BollingerBands
import org.cerion.stocks.core.overlays.ExpMovingAverage
import org.cerion.stocks.core.overlays.ParabolicSAR
import org.cerion.stocks.core.overlays.SimpleMovingAverage
import org.cerion.stocks.core.repository.CachedPriceListRepository

class ChartsViewModel(private val repo: CachedPriceListRepository, private val prefs: PreferenceRepository, private val colors: ChartColorScheme) : ViewModel() {

    private val DefaultSymbol = Symbol("^GSPC", "S&P 500")

    private val _symbol = MutableLiveData(DefaultSymbol)
    val symbol: LiveData<Symbol>
        get() = _symbol

    private val _interval = MutableLiveData(Interval.DAILY)
    val interval: LiveData<Interval>
        get() = _interval

    val prices = MediatorLiveData<PriceList>()

    private val DefaultCharts = mutableListOf(
            PriceChart(colors),
            VolumeChart(colors))

    private val DefaultChartsTest = mutableListOf(
            PriceChart(colors).apply {
                addOverlay(BollingerBands())
                addOverlay(SimpleMovingAverage())
                addOverlay(ExpMovingAverage())
            },
            VolumeChart(colors),
            IndicatorChart(MACD(), colors),
            PriceChart(colors).apply {
                addOverlay(ParabolicSAR())
            },
            VolumeChart(colors),
            IndicatorChart(AccumulationDistributionLine(), colors)
    )

    private var _charts = mutableListOf<StockChart>()
    val charts: MutableLiveData<List<StockChart>> = MutableLiveData(_charts)

    private val _busy = MutableLiveData(false)
    val busy: LiveData<Boolean>
        get() = _busy

    init {
        // Load saved charts
        _charts.addAll(prefs.getCharts(colors))
        if (_charts.isEmpty())
            _charts.addAll(DefaultCharts)

        charts.value = _charts
    }

    fun load() {
        val lastSymbol = prefs.getLastSymbol()
        if (lastSymbol != null)
            _symbol.value = lastSymbol

        refresh()
    }

    fun load(symbol: Symbol) {
        _symbol.value = symbol
        refresh()

        prefs.saveLastSymbol(symbol)
    }

    fun setInterval(interval: Interval) {
        _interval.value = interval
        refresh()
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                _busy.value = true
                prices.value = getPrices(_symbol.value!!.symbol)
            }
            finally {
                _busy.value = false
            }
        }
    }


    fun addPriceChart() {
        addChart(PriceChart(colors).apply {
            candleData = false
        })
    }

    fun addIndicatorChart() {
        addChart(IndicatorChart(MACD(), colors))
    }

    fun addVolumeChart() {
        addChart(VolumeChart(colors))
    }

    fun removeChart(chart: StockChart) {
        _charts.remove(chart)
        charts.value = _charts
        saveCharts()
    }

    fun replaceChart(old: StockChart, new: StockChart) {
        _charts = _charts.map { if(it == old) new else it }.toMutableList()
        charts.value = _charts
        saveCharts()
    }

    private fun saveCharts() {
        prefs.saveCharts(_charts)
    }

    private fun addChart(chart: StockChart) {
        _charts.add(chart)
        charts.value = _charts
        saveCharts()
    }

    private suspend fun getPrices(symbol: String): PriceList {
        return withContext(Dispatchers.IO) {
            // TODO repository should handle quarterly/monthly conversion
            val startDate = when(interval.value) {
                Interval.DAILY -> Constants.START_DATE_DAILY
                Interval.WEEKLY -> Constants.START_DATE_WEEKLY
                else -> Constants.START_DATE_MONTHLY
            }

            val intervalQuery = when(interval.value) {
                Interval.DAILY -> Interval.DAILY
                Interval.WEEKLY -> Interval.WEEKLY
                else -> Interval.MONTHLY
            }

            val list = repo.get(symbol, intervalQuery, startDate)

            when(interval.value) {
                Interval.QUARTERLY -> list.toQuarterly()
                Interval.YEARLY -> list.toYearly()
                else -> list
            }
        }
    }
}