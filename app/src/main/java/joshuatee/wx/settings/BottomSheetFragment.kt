package joshuatee.wx.settings

import joshuatee.wx.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import joshuatee.wx.util.UtilityLog

class BottomSheetFragment() : BottomSheetDialogFragment() {

    lateinit  var edit: TextView
    lateinit  var delete: TextView
    lateinit  var moveUp: TextView
    lateinit  var moveDown: TextView
    var position = -1

    private var fragmentView: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentView = inflater.inflate(R.layout.bottom_sheet_layout, container, false)
        edit = fragmentView!!.findViewById(R.id.edit)
        delete = fragmentView!!.findViewById(R.id.delete)
        moveUp = fragmentView!!.findViewById(R.id.moveUp)
        moveDown = fragmentView!!.findViewById(R.id.moveDown)
        return fragmentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        edit.setOnClickListener {
            UtilityLog.d("wx", "edit " + position.toString())
            dismiss()
        }
        delete.setOnClickListener {
            UtilityLog.d("wx", "delete")
            dismiss()
        }
        moveUp.setOnClickListener {
            UtilityLog.d("wx", "move up")
            dismiss()
        }
        moveDown.setOnClickListener {
            UtilityLog.d("wx", "move down")
            dismiss()
        }
    }
}