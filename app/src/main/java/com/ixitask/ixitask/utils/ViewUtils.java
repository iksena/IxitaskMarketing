package com.ixitask.ixitask.utils;

import android.app.Activity;
import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.ixitask.ixitask.R;

public class ViewUtils {

    /**
     * a method to hide soft input keyboard
     * @param context context of activity or fragment
     * @param view currently focused view e.g. EditText or Spinner
     */
    public static void hideKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Activity.INPUT_METHOD_SERVICE);
        if (imm!=null && view!=null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * an error dialog that is usable anywhere
     * @param context context of an activity or fragment
     * @param status the status code (200, 100, 500, etc.)
     * @param message the status message
     * @return a dialog builder that is editable to add a button or any dialog elements
     */
    public static AlertDialog.Builder dialogError(Context context, String status, String message){
        return new AlertDialog.Builder(context)
                .setTitle(String.format(context.getString(R.string.dialog_error_title), status))
                .setMessage(message)
                .setNegativeButton(context.getString(R.string.dialog_error_negative),
                        (d, w)->d.dismiss());
    }
}
