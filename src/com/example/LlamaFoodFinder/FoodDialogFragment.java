package com.example.LlamaFoodFinder;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.ArrayList;

public class FoodDialogFragment extends DialogFragment{

    public static final int NO_RESULTS = 0;
    public static final int RESULTS_FOUND= 1;
    public static final int ALL_RESULTS = 2;
    private int dialogtype;

    static FoodDialogFragment newInstance(int num, String restaurant) {
        FoodDialogFragment f = new FoodDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("type", num);
        args.putString("restaurant", restaurant);
        f.setArguments(args);

        return f;
    }
    static FoodDialogFragment newInstance(int num, ArrayList<String> results) {
        FoodDialogFragment f = new FoodDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("type", num);
        args.putStringArrayList("all", results);
        f.setArguments(args);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        dialogtype = getArguments().getInt("type");
        switch(dialogtype)
        {
            case(NO_RESULTS):
                return new AlertDialog.Builder(getActivity())
                        .setMessage("No results found")
                        .setCancelable( true )
                        .setPositiveButton( "OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick( DialogInterface dialog, int id )
                            {
                                dialog.cancel();
                            }
                        })
                        .create();
            case (RESULTS_FOUND):
                return new AlertDialog.Builder(getActivity())
                        .setMessage("Llama suggests " + getArguments().getString("restaurant"))
                        .setCancelable( false )
                        .setPositiveButton("Next result",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        ((LlamaFoodFinder)getActivity()).getRandomFood();
                                    }
                                }
                        )
                        .setNegativeButton("Done",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        dialog.cancel();
                                        ((LlamaFoodFinder)getActivity()).finishDB();
                                    }
                                }
                        )
                        .create();
            case (ALL_RESULTS):
                String txt = "All results:";
                ArrayList<String> temp = getArguments().getStringArrayList("all");
                for(String x : temp)
                {
                      txt += "\n" + x;
                }
                return new AlertDialog.Builder(getActivity())
                        .setMessage(txt)
                        .setCancelable(false)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .create();
            default:
                return null;
        }
    }
}