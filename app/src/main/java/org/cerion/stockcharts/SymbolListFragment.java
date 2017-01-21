package org.cerion.stockcharts;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.cerion.stockcharts.common.GenericAsyncTask;
import org.cerion.stockcharts.common.SymbolLookupDialogFragment;
import org.cerion.stockcharts.common.Utils;
import org.cerion.stockcharts.database.StockDB;
import org.cerion.stockcharts.database.StockDataManager;
import org.cerion.stockcharts.database.StockDataStore;
import org.cerion.stockcharts.positions.PositionListAdapter;
import org.cerion.stocklist.model.Symbol;

import java.util.ArrayList;
import java.util.List;


public class SymbolListFragment extends ListFragment implements SymbolLookupDialogFragment.OnSymbolListener {

    private static final String TAG = SymbolListFragment.class.getSimpleName();
    //private ArrayAdapter<String> mAdapter;
    private SymbolListAdapter mAdapter;
    private List<Symbol> mSymbols = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.generic_list_fragment, container, false);

        //mAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, mSymbols);
        mAdapter = new SymbolListAdapter(getContext(), mSymbols);

        setListAdapter(mAdapter);

        setHasOptionsMenu(true);

        //TODO add empty list case

        refresh();

        return view;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        //String s = mSymbols.get(position); TODO Add back

        Intent intent = new Intent(getActivity(),PriceListActivity.class);
        //intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);

        //Intent intent = new Intent(getActivity(),ChartViewActivity.class);
        //intent.putExtra(PriceListActivity.SYMBOL_EXTRA,s);

        startActivity(intent);
    }

    public void refresh()
    {
        StockDataStore db = StockDB.getInstance(this.getContext());
        //db.log();
        mSymbols.clear();
        mSymbols.addAll( db.getSymbols() );

        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.symbol_list_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.add_symbol:
                onAddSymbol();
                break;
            default:
                return super.onContextItemSelected(item);
        }

        return true;
    }

    private static final int DIALOG_FRAGMENT = 1;
    private void onAddSymbol() {
        //DialogFragment newFragment = new SymbolLookupDialogFragment();
        //newFragment.show(getFragmentManager(), "addSymbol");

        // TODO simpler way of doing this?
        FragmentTransaction ft = getActivity().getFragmentManager().beginTransaction();
        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        DialogFragment dialog = new SymbolLookupDialogFragment();
        dialog.setTargetFragment(this, DIALOG_FRAGMENT);
        dialog.show(getFragmentManager().beginTransaction(), "dialog");
    }

    @Override
    public void onSymbolEntered(final String name) {

        GenericAsyncTask task = new GenericAsyncTask(new GenericAsyncTask.TaskHandler() {
            boolean result = false;

            @Override
            public void run() {
                StockDataManager dataManager = new StockDataManager(getContext());
                result = dataManager.insertSymbol(name);
            }

            @Override
            public void onFinish() {
                refresh();
                if(!result)
                    Toast.makeText(getContext(), "Could not find '" + name + "'", Toast.LENGTH_SHORT).show();
            }
        });

        task.execute();
    }

    private class ViewHolder {
        TextView symbol;
        TextView name;
        TextView exchange;
    }

    private class SymbolListAdapter extends ArrayAdapter<Symbol> {

        private static final int LAYOUT_ID = R.layout.list_item_symbol;

        public SymbolListAdapter(Context context, List<Symbol> items) {
            super(context, LAYOUT_ID, items);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Symbol symbol = getItem(position);
            ViewHolder viewHolder;

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(LAYOUT_ID, parent, false);

                viewHolder.symbol = (TextView) convertView.findViewById(R.id.symbol);
                viewHolder.name = (TextView) convertView.findViewById(R.id.name);
                viewHolder.exchange = (TextView) convertView.findViewById(R.id.exchange);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.symbol.setText(symbol.getSymbol());
            viewHolder.name.setText(symbol.getName());
            viewHolder.exchange.setText(symbol.getExchange());

            return convertView;
        }
    }
}
