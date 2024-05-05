package no.uio.tmdetector;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
/**
 * This fragment is used to choose the current mode bu the user.
 */
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




        return builder.create();


    }


}