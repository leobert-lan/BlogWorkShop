package osp.leobert.android.blog.workshop

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentResultListener
import osp.leobert.android.blog.workshop.Demo.Companion.BUNDLE_BOOL_FROM_FG


sealed class Host {
    abstract val context: Context

    abstract fun <T : View> view(@IdRes id: Int): T
    abstract fun launchFragment(fragment: DialogFragment)
    abstract fun launchFragment2(fragment: DialogFragment, listener: FragmentResultListener)


    class ActivityHost(private val activity: AppCompatActivity) : Host() {
        override val context: Context
            get() = activity

        override fun <T : View> view(id: Int): T {
            return activity.findViewById(id)
        }

        override fun launchFragment(fragment: DialogFragment) {
            val arg = fragment.arguments ?: Bundle()
            arg.putBoolean(BUNDLE_BOOL_FROM_FG, false)
            fragment.arguments = arg
            fragment.show(activity.supportFragmentManager, fragment.javaClass.name)
        }

        override fun launchFragment2(fragment: DialogFragment, listener: FragmentResultListener) {
            activity.supportFragmentManager.setFragmentResultListener(Demo.KEY_INPUT, activity, listener)
            fragment.show(activity.supportFragmentManager, fragment.javaClass.name)
        }
    }

    class FragmentHost(private val fragment: Fragment) : Host() {
        override val context: Context
            get() = fragment.requireContext()

        override fun <T : View> view(id: Int): T {
            return fragment.requireView().findViewById(id)
        }

        override fun launchFragment(fragment: DialogFragment) {
            val arg = fragment.arguments ?: Bundle()
            arg.putBoolean(BUNDLE_BOOL_FROM_FG, true)
            fragment.arguments = arg
            fragment.setTargetFragment(this.fragment, 2)
            //使用 fragment.requireActivity().supportFragmentManager 亦可
            fragment.show(this.fragment.requireFragmentManager(), fragment.javaClass.name)
        }

        override fun launchFragment2(fragment: DialogFragment, listener: FragmentResultListener) {
            this.fragment.parentFragmentManager.setFragmentResultListener(Demo.KEY_INPUT, this.fragment, listener)
            fragment.show(this.fragment.parentFragmentManager, fragment.javaClass.name)
        }
    }
}

/**
 * Created by leobert on 2021/12/21.
 */
class Demo(val host: Host) {

    interface OnResultListener {
        fun onResult(text: String?)
    }

    companion object {
        const val BUNDLE_STR_INPUT = "BUNDLE_STR_INPUT"
        const val BUNDLE_BOOL_FROM_FG = "BUNDLE_BOOL_FROM_FG"
        const val KEY_INPUT = "KEY_INPUT"
    }


    fun onStart() {
        val btnDemo1: Button = host.view(R.id.btn1)
        val btnDemo2: Button = host.view(R.id.btn2)
        val btnDemo3: Button = host.view(R.id.btn3)
        val btnDemo4: Button = host.view(R.id.btn4)

        btnDemo1.setOnClickListener { demo1() }
        btnDemo2.setOnClickListener { demo2() }
        btnDemo3.setOnClickListener { demo3() }
        btnDemo4.setOnClickListener { demo4() }
    }

    fun setText(text: String?) {
        host.view<TextView>(R.id.tv_result).text = text
    }

    private fun demo1() {
        val view = LayoutInflater.from(host.context).inflate(R.layout.view_input, null)
        val dialog = AlertDialog.Builder(host.context)
            .setView(view)
            .setPositiveButton("OK") { dialog, _ ->
                dialog?.dismiss()
                setText(view.findViewById<EditText>(R.id.et_input)?.text?.toString())
            }
            .setNegativeButton("cancel") { dialog, _ -> dialog?.dismiss() }
            .create()
        dialog.show()
    }

    sealed class DemoDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val view = LayoutInflater.from(requireContext()).inflate(R.layout.view_input, null)
            return AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("OK") { _, _ ->
                    dismissAllowingStateLoss()
                    onPositive(view.findViewById<EditText>(R.id.et_input)?.text?.toString())
                }
                .setNegativeButton("Cancel") { _, _ -> dismissAllowingStateLoss() }
                .create()
        }

        protected abstract fun onPositive(text: String?)

        class Demo2 : DemoDialogFragment() {
            private var fromFg = false

            override fun onCreate(savedInstanceState: Bundle?) {
                super.onCreate(savedInstanceState)
                fromFg = arguments?.getBoolean(BUNDLE_BOOL_FROM_FG) ?: false
            }

            override fun onPositive(text: String?) {
                val intent = Intent()
                intent.putExtras(Bundle().apply {
                    putString(BUNDLE_STR_INPUT, text)
                })

                if (fromFg) {
                    targetFragment?.onActivityResult(targetRequestCode, Activity.RESULT_OK, intent)
                } else {
                    (requireActivity() as OnResultListener).onResult(text)
                }
            }
        }

        class Demo3 : DemoDialogFragment() {
            var listener: OnResultListener? = null
            override fun onPositive(text: String?) {
                listener?.onResult(text) ?: Log.e("DEMO3", "listener is null!")
            }
        }

        class Demo4 : DemoDialogFragment() {
            override fun onPositive(text: String?) {
                val bundle = Bundle().apply {
                    putString(BUNDLE_STR_INPUT, text)
                }
                this.parentFragmentManager.setFragmentResult(KEY_INPUT, bundle)
            }
        }
    }

    private fun demo2() {
        host.launchFragment(DemoDialogFragment.Demo2())
    }

    private fun demo3() {
        host.launchFragment(DemoDialogFragment.Demo3().apply {
            this.listener = object : OnResultListener {
                override fun onResult(text: String?) {
                    setText(text)
                }
            }
        })
    }

    private fun demo4() {
        host.launchFragment2(fragment = DemoDialogFragment.Demo4()) { requestKey, result ->
            if (requestKey != KEY_INPUT) return@launchFragment2
            setText(result.getString(BUNDLE_STR_INPUT))
        }
    }
}