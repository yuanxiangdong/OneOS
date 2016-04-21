package com.eli.lib.magicdialog;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/4/21.
 */
public class MagicDialogSample {

    public static void notice(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("Notify Title").notice().content("Notify Content Body.").positive("OK").bold(null).warning().check("Show List Dialog").checked(true)
                .listener(new MagicDialog.OnMagicDialogClickListener() {
                    @Override
                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                        Toast.makeText(activity, "On Dialog Button Click: " + button, Toast.LENGTH_SHORT).show();
                        if (checked) {
                            MagicDialogSample.list(activity);
                        }
                    }
                }).show();
    }

    public static void confirm(final Activity activity) {
        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("Confirm Title").confirm().content("Confirm Content Body").warning().positive("OK").negative("Cancel")
                .check("Show Notice Dialog?").checked(true).bold(MagicDialog.MagicDialogButton.POSITIVE).right(MagicDialog.MagicDialogButton.POSITIVE)
                .listener(new MagicDialog.OnMagicDialogClickListener() {
                    @Override
                    public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                        Toast.makeText(activity, "On Dialog Button Click: " + button, Toast.LENGTH_SHORT).show();
                        if (button == MagicDialog.MagicDialogButton.POSITIVE && checked) {
                            MagicDialogSample.notice(activity);
                        }
                    }
                }).show();
    }

    public static void list(final Activity activity) {
        ArrayList<MagicDialog.MagicDialogListItem> itemList = new ArrayList<>();
        MagicDialog.MagicDialogListItem item3 = new MagicDialog.MagicDialogListItem();
        item3.title = "List:";
        item3.content = "Magic List Dialog";
        item3.color = Color.BLACK;
        itemList.add(item3);
        MagicDialog.MagicDialogListItem item2 = new MagicDialog.MagicDialogListItem();
        item2.title = "Confirm:";
        item2.content = "Magic Confirm Dialog";
        item2.color = Color.GREEN;
        itemList.add(item2);
        MagicDialog.MagicDialogListItem item1 = new MagicDialog.MagicDialogListItem();
        item1.title = "Notice:";
        item1.content = "Magic Notify Dialog";
        item1.color = Color.RED;
        itemList.add(item1);

        MagicDialog dialog = new MagicDialog(activity);
        dialog.title("List Title").list(itemList).content("This is MagicDialog Tester").warning().positive("ConfirmDialog").negative("Cancel").neutral("NotifyDialog")
                .bold(MagicDialog.MagicDialogButton.NEGATIVE).listener(new MagicDialog.OnMagicDialogClickListener() {
            @Override
            public void onClick(View view, MagicDialog.MagicDialogButton button, boolean checked) {
                Toast.makeText(activity, "On Dialog Button Click: " + button, Toast.LENGTH_SHORT).show();
                if (button == MagicDialog.MagicDialogButton.POSITIVE) {
                    MagicDialogSample.confirm(activity);
                } else if (button == MagicDialog.MagicDialogButton.NEUTRAL) {
                    MagicDialogSample.notice(activity);
                }
            }
        }).cancelable(true).show();
    }
}
