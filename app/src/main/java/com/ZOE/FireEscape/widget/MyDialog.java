package com.ZOE.FireEscape.widget;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class MyDialog extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder AD = new AlertDialog.Builder(getActivity());
        AD.setTitle("系统提示");
        AD.setMessage("您不在此区域，是否退出");
        AD.setPositiveButton("是", new DialogInterface.OnClickListener()
        {

            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                getActivity().onBackPressed();
            }
        });
        AD.setNegativeButton("否", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                //cancelled
            }
        });
        return AD.create();

    }
}