package nl.whitedove.washetdroogofniet

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView

internal object FontManager {
    val FONTAWESOME_BRANDS = "fa-brands-400.ttf"
    val FONTAWESOME_REGULAR = "fa-regular-400.ttf"
    val FONTAWESOME_SOLID = "fa-solid-900.ttf"

    fun GetTypeface(context: Context, font: String): Typeface {
        return Typeface.createFromAsset(context.assets, font)
    }

    fun MarkAsIconContainer(v: View, typeface: Typeface) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                val child = v.getChildAt(i)
                MarkAsIconContainer(child, typeface)
            }
        } else if (v is TextView) {
            v.typeface = typeface
        }
    }

    fun SetIconAndText(v: View, typefaceIcon: Typeface, icon: String, iconColor: Int, typefaceText: Typeface, text: String, textColor: Int) {
        val ss = SpannableString("$icon $text")
        ss.setSpan(CustomTypefaceSpan(typefaceIcon), 0, icon.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        ss.setSpan(ForegroundColorSpan(iconColor), 0, icon.length, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        ss.setSpan(CustomTypefaceSpan(typefaceText), icon.length + 1, text.length + icon.length + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        ss.setSpan(ForegroundColorSpan(textColor), icon.length + 1, text.length + icon.length + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE)
        if (v is TextView) {
            v.text = ss
        }
        if (v is Button) {
            v.text = ss
        }
    }
}