package com.protel.yesterday.util;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v7.app.AlertDialog;


/**
 * Created by eolkun on 10.12.2014.
 */
public class DialogUtils {

    public static Dialog showDefaultDialog(@NonNull Context context, String title, String message, String infoText) {
        return showDefaultDialog(context, title, message, infoText, null, null, null);
    }

    public static Dialog showDefaultDialog(@NonNull Context context, String title, String message, String infoText, Dialog.OnClickListener dialogClickListener) {
        return showDefaultDialog(context, title, message, infoText, null, dialogClickListener, null);
    }

    public static Dialog showDefaultDialog(@NonNull Context context, String title, String message, String infoText, Dialog.OnClickListener dialogClickListener, DialogInterface.OnCancelListener onCancelListener) {
        return showDefaultDialog(context, title, message, infoText, null, dialogClickListener, onCancelListener);
    }

    public static Dialog showDefaultDialog(@NonNull Context context, String title, String message, String positiveText, String negativeText, Dialog.OnClickListener dialogClickListener, DialogInterface.OnCancelListener onCancelListener) {

        DialogBuilder dialogBuilder = new DialogBuilder(context);
        if (title != null) {
            dialogBuilder.title(title);
        }
        if (message != null) {
            dialogBuilder.message(message);
        }
        if (positiveText != null) {
            dialogBuilder.positive(positiveText, dialogClickListener);
        }
        if (negativeText != null) {
            dialogBuilder.negative(negativeText, dialogClickListener);
        }
        dialogBuilder.setCancelListener(onCancelListener);
        return dialogBuilder.show();
    }

    public static DialogBuilder getDialogBuilder(Context context) {
        DialogBuilder dialogBuilder = new DialogBuilder(context);
        return dialogBuilder;
    }


    /**
     * Custom dialog builder to create non standard views.
     */
    public static class DialogBuilder {
        private String title;
        private String message;
        private String positiveText, negativeText, neutralText;
        private Dialog.OnClickListener dialogClickListener;
        private Dialog.OnCancelListener dialogCancelListener;
        private DialogInterface.OnDismissListener dismissListener;
        private
        @DrawableRes
        Integer iconResource;
        private Drawable drawable;
        private Boolean closeOnTouchOutside;
        private
        @StyleRes
        Integer animationRes;

        private Context context;

        private DialogBuilder(@NonNull Context context) {
            this.context = context;
        }

        public DialogBuilder title(String title) {
            this.title = title;
            return this;
        }

        public DialogBuilder title(@StringRes Integer titleRes) {
            return title(context.getString(titleRes));
        }

        public DialogBuilder message(String message) {
            this.message = message;
            return this;
        }

        public DialogBuilder message(@StringRes Integer messageRes) {
            return message(context.getString(messageRes));
        }

        public DialogBuilder positive(String positiveText) {
            this.positiveText = positiveText;
            return this;
        }

        public DialogBuilder positive(@StringRes Integer positiveRes) {
            return positive(context.getString(positiveRes));
        }

        public DialogBuilder negative(String negativeText) {
            this.negativeText = negativeText;
            return this;
        }

        public DialogBuilder negative(@StringRes Integer negativeRes) {
            return negative(context.getString(negativeRes));
        }

        /*
        * Should be a dialog animation with
           <item name="android:windowEnterAnimation">@anim/enter_sample1</item>
            <item name="android:windowExitAnimation">@anim/exit_sample1</item>
            values
        * */
        public DialogBuilder anim(@StyleRes Integer animRes) {
            this.animationRes = animRes;
            return this;
        }

        public DialogBuilder neutral(String neutralText) {
            this.neutralText = neutralText;
            return this;
        }

        public DialogBuilder neutral(@StringRes Integer neutralRes) {
            return neutral(context.getString(neutralRes));
        }

        public DialogBuilder negative(String negativeText, Dialog.OnClickListener dialogClickListener) {
            this.negativeText = negativeText;
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder positive(String positiveText, Dialog.OnClickListener dialogClickListener) {
            this.positiveText = positiveText;
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder listener(Dialog.OnClickListener dialogClickListener) {
            this.dialogClickListener = dialogClickListener;
            return this;
        }

        public DialogBuilder dismissListener(DialogInterface.OnDismissListener dismissListener) {
            this.dismissListener = dismissListener;
            return this;
        }

        public DialogBuilder setCancelListener(DialogInterface.OnCancelListener cancelListener) {
            this.dialogCancelListener = cancelListener;
            return this;
        }

        public DialogBuilder icon(@DrawableRes Integer iconResource) {
            this.drawable = null;
            this.iconResource = iconResource;
            return this;
        }

        public DialogBuilder icon(Drawable icon) {
            this.iconResource = null;
            this.drawable = icon;
            return this;
        }

        public DialogBuilder closeOnTouchOutside(Boolean closeOnTouchOutside) {
            this.closeOnTouchOutside = closeOnTouchOutside;
            return this;
        }

        public Dialog build() {
            return buildDialog();
        }

        public Dialog show() {
            Dialog d = buildDialog();

            try {
                d.show();
                /*
                To change button colors.
                * */
//                if(d instanceof AlertDialog) {
//                    Button nbutton = ((AlertDialog)d).getButton(DialogInterface.BUTTON_NEGATIVE);
//                    nbutton.setTextColor(Color.MAGENTA);
//                    Button pbutton = ((AlertDialog)d).getButton(DialogInterface.BUTTON_POSITIVE);
//                    pbutton.setTextColor(Color.YELLOW);
//                }
                if (dismissListener != null) {
                    d.setOnDismissListener(dismissListener);
                }
            } catch (Exception ex) {
                L.ex(ex);
            }
            return d;
        }

        private Dialog buildDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);//R.style.DialogStyleTest

            if (title != null) {
                builder.setTitle(title);
            }
            if (message != null) {
                builder.setMessage(message);
            }
            if (positiveText != null) {
                builder.setPositiveButton(positiveText, dialogClickListener);
            }
            if (negativeText != null) {
                builder.setNegativeButton(negativeText, dialogClickListener);
            }
            if (neutralText != null) {
                builder.setNeutralButton(neutralText, dialogClickListener);
            }
            if (dialogCancelListener != null) {
                builder.setOnCancelListener(dialogCancelListener);
            }
            if (iconResource != null) {
                builder.setIcon(iconResource);
            }
            if (drawable != null) {
                builder.setIcon(drawable);
            }

            AlertDialog dialog = builder.create();
            if (closeOnTouchOutside != null) {
                dialog.setCanceledOnTouchOutside(closeOnTouchOutside);
            }
            // todo no animation
//            dialog.getWindow().setWindowAnimations(animationRes == null ? R.style.DialogAnimationDefault : animationRes);
            return dialog;
        }
    }
}
