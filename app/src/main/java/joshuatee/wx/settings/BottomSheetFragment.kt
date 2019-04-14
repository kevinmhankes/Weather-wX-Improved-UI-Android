/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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

package joshuatee.wx.settings

import joshuatee.wx.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment() : BottomSheetDialogFragment() {

    lateinit  var label: TextView
    lateinit  var edit: TextView
    lateinit  var delete: TextView
    lateinit  var moveUp: TextView
    lateinit  var moveDown: TextView
    var position = -1
    lateinit var fn1: (pos: Int) -> Unit
    lateinit var fn2: (pos: Int) -> Unit
    lateinit var fn3: (pos: Int) -> Unit
    lateinit var fn4: (pos: Int) -> Unit

    private var fragmentView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        label = fragmentView!!.findViewById(R.id.label)
        edit = fragmentView!!.findViewById(R.id.edit)
        delete = fragmentView!!.findViewById(R.id.delete)
        moveUp = fragmentView!!.findViewById(R.id.moveUp)
        moveDown = fragmentView!!.findViewById(R.id.moveDown)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Location.numLocations == 1) {
            delete.visibility = View.INVISIBLE
            moveDown.visibility = View.INVISIBLE
            moveUp.visibility = View.INVISIBLE
        }
        initView()
    }

    fun initView() {
        label.text = Location.getName(position)
        edit.setOnClickListener{ fn1(position); dismiss()}
        delete.setOnClickListener{ fn2(position); dismiss()}
        moveUp.setOnClickListener{ fn3(position); dismiss()}
        moveDown.setOnClickListener{ fn4(position); dismiss()}
    }
}