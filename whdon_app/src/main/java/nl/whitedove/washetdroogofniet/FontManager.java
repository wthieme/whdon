package nl.whitedove.washetdroogofniet;

import android.content.Context;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

class FontManager {
    static final String FONTAWESOME_BRANDS = "fa-brands-400.ttf";
    static final String FONTAWESOME_REGULAR = "fa-regular-400.ttf";
    static final String FONTAWESOME_SOLID = "fa-solid-900.ttf";

    static Typeface GetTypeface(Context context, String font) {
        return Typeface.createFromAsset(context.getAssets(), font);
    }

    static void MarkAsIconContainer(View v, Typeface typeface) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                MarkAsIconContainer(child, typeface);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(typeface);
        }
    }

    static void SetIconAndText(View v, Typeface typefaceIcon, String icon, int iconColor, Typeface typefaceText, String text, int textColor) {
        SpannableString ss = new SpannableString(icon + " " + text);
        ss.setSpan(new CustomTypefaceSpan(typefaceIcon), 0, icon.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(iconColor), 0, icon.length(), Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(new CustomTypefaceSpan(typefaceText), icon.length() + 1, text.length() + icon.length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(textColor), icon.length() + 1, text.length() + icon.length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        if (v instanceof TextView) {
            TextView tv = (TextView) v;
            tv.setText(ss);
        }
        if (v instanceof Button) {
            Button b = (Button) v;
            b.setText(ss);
        }
    }
}