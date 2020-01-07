package nl.whitedove.washetdroogofniet

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

internal class ContextMenuAdapter(var context: Context,
                                  private val listContextMenuItems: List<ContextMenuItem>) : BaseAdapter() {

    private class ViewHolder {
        internal var imageView: ImageView? = null
        internal var textView: TextView? = null
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var cv = convertView
        val viewHolder: ViewHolder
        if (cv == null) {
            val inflater = context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            viewHolder = ViewHolder()
            cv = inflater.inflate(R.layout.context_menu_item, parent,
                    false)
            viewHolder.imageView = cv!!
                    .findViewById(R.id.imageView_menu)
            viewHolder.textView = cv
                    .findViewById(R.id.textView_menu)
            cv.tag = viewHolder
        } else {
            viewHolder = cv.tag as ViewHolder
        }

        viewHolder.imageView!!.setImageDrawable(listContextMenuItems[position].drawable)
        viewHolder.textView!!.text = listContextMenuItems[position]
                .text
        return cv

    }

    override fun getCount(): Int {
        return listContextMenuItems.size
    }

    override fun getItem(position: Int): Any? {
        return null
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

}