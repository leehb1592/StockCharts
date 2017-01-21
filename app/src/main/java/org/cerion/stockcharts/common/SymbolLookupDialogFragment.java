package org.cerion.stockcharts.common;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.InputFilter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SymbolLookupDialogFragment extends DialogFragment {

    private OnSymbolListener mListener;

    public interface OnSymbolListener {
        void onSymbolEntered(String name);
    }

    /*
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        Fragment f = getParentFragment();
        // Can implement either of these
        if (activity instanceof SymbolLookupDialogFragment.OnSymbolListener)
            mListener = (SymbolLookupDialogFragment.OnSymbolListener)activity;
        else
            throw new RuntimeException("SymbolLookupDialogFragment.OnSymbolListener not implemented in activity");
    }
    */

    @Override
    public void setTargetFragment(Fragment fragment, int requestCode) {
        super.setTargetFragment(fragment, requestCode);

        try {
            mListener = (OnSymbolListener) fragment;
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Enter Symbol");

        final EditText input = new EditText(getContext());
        input.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(mListener != null)
                    mListener.onSymbolEntered(input.getText().toString());
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return builder.create();
    }
}