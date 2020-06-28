package org.cerion.stockcharts.ui.charts

import android.graphics.Matrix
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import org.cerion.stockcharts.R
import org.cerion.stockcharts.common.SymbolSearchView
import org.cerion.stockcharts.databinding.FragmentChartsBinding
import org.cerion.stocks.core.charts.StockChart
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChartsFragment : Fragment() {

    private val viewModel: ChartsViewModel by viewModel()
    private lateinit var binding: FragmentChartsBinding
    private lateinit var adapter: ChartListAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentChartsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewmodel = viewModel

        setHasOptionsMenu(true)

        val chartListener = object : StockChartListener {
            override fun onClick(chart: StockChart) {
                val fm = requireActivity().supportFragmentManager
                val dialog = EditChartDialog.newInstance(chart, viewModel)
                dialog.show(fm, "editDialog")
            }

            override fun onViewPortChange(matrix: Matrix) {
                syncCharts(matrix)
            }
        }

        adapter = ChartListAdapter(requireContext(), chartListener)

        binding.recyclerView.adapter = adapter

        val chartsChangedObserver = Observer<Any> {
            adapter.setCharts(viewModel.charts.value!!, viewModel.prices.value)
        }

        viewModel.symbol.observe(viewLifecycleOwner, Observer{
            (requireActivity() as AppCompatActivity).supportActionBar?.title = it
        })

        viewModel.charts.observe(viewLifecycleOwner, chartsChangedObserver)
        viewModel.prices.observe(viewLifecycleOwner, chartsChangedObserver)

        if (viewModel.symbol.value.isNullOrEmpty() || savedInstanceState == null) {
            val args = if (arguments != null) ChartsFragmentArgs.fromBundle(arguments!!) else null
            val symbol = args?.symbol ?: "XLE"
            viewModel.load(symbol)
        }

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.charts_menu, menu)

        val menuItem = menu.findItem(R.id.action_search)
        val searchView = menuItem.actionView as SymbolSearchView

        searchView.setOnSymbolClickListener(object : SymbolSearchView.OnSymbolClickListener {
            override fun onClick(symbol: String) {
                viewModel.load(symbol)
                menuItem.collapseActionView()
            }
        })

        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_indicator -> viewModel.addIndicatorChart()
            R.id.add_price -> viewModel.addPriceChart()
            R.id.add_volume -> viewModel.addVolumeChart()
            else -> return super.onContextItemSelected(item)
        }

        return true
    }

    private val _mainVals = FloatArray(9)
    private fun syncCharts(matrix: Matrix) {
        matrix.getValues(_mainVals)
        for(view in binding.recyclerView.children) {
            adapter.syncMatrix(matrix, _mainVals, binding.recyclerView.getChildViewHolder(view) as ChartListAdapter.ViewHolder)
        }
    }
}