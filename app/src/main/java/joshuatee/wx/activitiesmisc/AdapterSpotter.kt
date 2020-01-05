/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

*/

package joshuatee.wx.activitiesmisc

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import joshuatee.wx.MyApplication
import joshuatee.wx.R
import joshuatee.wx.UIPreferences
import joshuatee.wx.radar.Spotter
import joshuatee.wx.ui.ObjectCard
import joshuatee.wx.ui.ObjectTextView

internal class AdapterSpotter(private val dataSet: MutableList<Spotter>) :
    RecyclerView.Adapter<AdapterSpotter.DataObjectHolder>() {

    internal class DataObjectHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        val name = ObjectTextView(itemView, R.id.name, UIPreferences.textHighlightColor)
        val email = ObjectTextView(itemView, R.id.email)
        val time = ObjectTextView(itemView, R.id.time)
        val phone = ObjectTextView(itemView, R.id.phone)
        val objCard = ObjectCard(itemView, R.id.cv1)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(v: View) {
            myClickListener!!.onItemClick(adapterPosition)
        }
    }

    fun setListener(fn: (Int) -> Unit) {
        myClickListener = object : MyClickListener {
            override fun onItemClick(position: Int) {
                fn(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataObjectHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_spotter, parent, false)
        return DataObjectHolder(view)
    }

    override fun onBindViewHolder(holder: DataObjectHolder, position: Int) {
        holder.name.text = dataSet[position].lastName + ", " + dataSet[position].firstName
        holder.time.text = dataSet[position].reportAt
        holder.email.text = dataSet[position].email.replace(MyApplication.newline, " ")
        holder.email.setOnClickListener(View.OnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(holder.email.text))
            intent.putExtra(Intent.EXTRA_SUBJECT, "")
            holder.email.context.startActivity(Intent.createChooser(intent, "Send Email"))
        })
        holder.phone.text = dataSet[position].phone.replace(MyApplication.newline, " ")
        listOf(holder.time, holder.email, holder.phone).forEach {
            it.setAsBackgroundText()
        }
        holder.phone.setOnClickListener(View.OnClickListener {
            val telephonyManager = holder.phone.context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (telephonyManager.phoneType != TelephonyManager.PHONE_TYPE_NONE) {
                val intent = Intent(Intent.ACTION_DIAL)
                intent.data = Uri.parse("tel:" + dataSet[position].phone)
                holder.phone.context.startActivity(intent)
            }
        })
    }

    override fun getItemCount() = dataSet.size

    fun getItem(index: Int): Spotter = dataSet[index]

    interface MyClickListener {
        fun onItemClick(position: Int)
    }

    private fun removeItem(position: Int) {
        dataSet.removeAt(position)
        notifyItemRemoved(position)
    }

    private fun addItem(position: Int, model: Spotter) {
        dataSet.add(position, model)
        notifyItemInserted(position)
    }

    private fun moveItem(fromPosition: Int, toPosition: Int) {
        val model = dataSet.removeAt(fromPosition)
        dataSet.add(toPosition, model)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun animateTo(models: List<Spotter>) {
        applyAndAnimateRemovals(models)
        applyAndAnimateAdditions(models)
        applyAndAnimateMovedItems(models)
    }

    private fun applyAndAnimateRemovals(newModels: List<Spotter>) {
        dataSet.indices.reversed().forEach {
            val model = dataSet[it]
            if (!newModels.contains(model)) {
                removeItem(it)
            }
        }
    }

    private fun applyAndAnimateAdditions(newModels: List<Spotter>) {
        var i = 0
        val count = newModels.size
        while (i < count) {
            val model = newModels[i]
            if (!dataSet.contains(model)) {
                addItem(i, model)
            }
            i += 1
        }
    }

    private fun applyAndAnimateMovedItems(newModels: List<Spotter>) {
        newModels.indices.reversed().forEach {
            val model = newModels[it]
            val fromPosition = dataSet.indexOf(model)
            if (fromPosition >= 0 && fromPosition != it) {
                moveItem(fromPosition, it)
            }
        }
    }

    companion object {
        private var myClickListener: MyClickListener? = null
    }
}
