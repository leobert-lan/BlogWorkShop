package osp.leobert.android.blog.workshop

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

class BlankFragment : Fragment() {

    lateinit var demo: Demo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_blank, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        demo = Demo(Host.FragmentHost(this))
        demo.onStart()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            demo.setText(data?.getStringExtra(Demo.BUNDLE_STR_INPUT))
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        fun newInstance() = BlankFragment()
    }
}