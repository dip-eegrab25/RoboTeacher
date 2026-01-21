
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface

class CustomAlertDialogBuilder(var dialogButtonClickListener: DialogButtonClickListener) {

    var alertDialog:AlertDialog?=null

    fun buildAlertDialog(c:Context,title:String,message:String,iconId:Int):AlertDialog {

        alertDialog = AlertDialog.Builder(c)
            .setTitle(title)
            .setMessage(message)
            .setIcon(iconId)
            .setCancelable(false)
            .setPositiveButton("Ok",object : DialogInterface.OnClickListener{
                override fun onClick(dialog: DialogInterface?, which: Int) {

                    dialog?.let {

                        dialogButtonClickListener.onClickPositiveButton(dialog)

                    }




                }


            })
            .create()

        return alertDialog!!

    }

    interface DialogButtonClickListener{

        fun onClickPositiveButton(dialog: DialogInterface)
    }
}