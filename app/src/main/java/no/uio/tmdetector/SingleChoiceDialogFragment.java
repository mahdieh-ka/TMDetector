package no.uio.tmdetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SingleChoiceDialogFragment extends DialogFragment {

    static  int position =0; //default selected mode
    String selectiveItem;
    String tripId;

    public SingleChoiceDialogFragment(String item) {
        selectiveItem = item;

    }

    public interface SingleChoiceListener {
        void onPositiveButtonClicked(String[] list, int position, String selectiveItem, String id);

        void onNegativeButtonClicked(String selectiveItem);
    }


    SingleChoiceListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (SingleChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(getActivity().toString() + "SingleChoiceListener must be implemented!");

        }


    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        if (selectiveItem == "mode") {
            final String[] modeList = getActivity().getResources().getStringArray(R.array.modes);
            builder.setTitle("Select your current mode").setSingleChoiceItems(modeList, position, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    position = which;
                }
            })
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onPositiveButtonClicked(modeList, position, "mode", tripId);


                        }
                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onNegativeButtonClicked("mode");
                        }
                    });
        }

        else if (selectiveItem =="city") {
            final String[] cityList = getActivity().getResources().getStringArray(R.array.cities);
            builder.setTitle("Select the city:").setSingleChoiceItems(cityList, position, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    position = which;
                }
            })
                    .setPositiveButton("Submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onPositiveButtonClicked(cityList, position , "city",tripId);


                        }
                    })

                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mListener.onNegativeButtonClicked("city");
                        }
                    });
        }


        return builder.create();


    }

    /*public int setPosition(String lastUsedMode) {
        int position = 0;

        switch (lastUsedMode) {
            case "still":
                position = 0;
                break;
            case "bike":
                position = 1;
                break;
            case "walk":
                position = 2;
                break;
            case "run":
                position = 3;
                break;
            case "car":
                position = 4;
                break;
            case "train":
                position = 5;
                break;
            case "tram":
                position = 6;
                break;
            case "subway":
                position = 7;
                break;
            case "ferry":
                position = 8;
                break;
            case "plain":
                position = 9;
                break;
            case "bus":
                position = 10;
                break;
            case "other":
                position = 11;
                break;
            default:
                break;
        }
        return position;
    }*/
}