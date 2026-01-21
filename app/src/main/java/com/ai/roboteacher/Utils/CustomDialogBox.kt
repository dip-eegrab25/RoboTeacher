
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface

class CustomDialogBox {

    var dialog:Dialog?=null

    fun buildDialog(c: Context,layoutId:Int): CustomDialogBox {

        dialog = Dialog(c)
        dialog?.setContentView(layoutId)
        return this

    }

    fun setSize(width:Int,height:Int):CustomDialogBox {

        dialog?.window?.setLayout(width,height)

        return this
    }

    fun createDialog():Dialog {

         dialog?.create()
        return dialog!!

    }

    fun setCancelable(cancelable:Boolean):CustomDialogBox {

        dialog?.setCancelable(cancelable)
        return this

    }
}