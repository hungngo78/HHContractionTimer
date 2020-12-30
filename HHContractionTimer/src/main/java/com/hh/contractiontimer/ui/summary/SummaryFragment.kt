package com.hh.contractiontimer.ui.summary

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.*
import android.widget.ExpandableListView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.snackbar.Snackbar
import com.hh.contractiontimer.BuildConfig
import com.hh.contractiontimer.R
import com.hh.contractiontimer.databinding.SummaryFragmentBinding
import com.hh.contractiontimer.model.User
import com.hh.contractiontimer.ui.measure.MeasurementViewModel
import com.hh.contractiontimer.ui.profile.ProfileViewModel
import java.io.File

class SummaryFragment : Fragment() {

    private lateinit var chart: LineChart

    private lateinit var adapter: SummaryExpandableListAdapter
    private lateinit var expandableListView: ExpandableListView
    private var user: User? = null
    private var alertDialog: AlertDialog? = null

    companion object {
        fun newInstance() = SummaryFragment()
    }

    lateinit var databinding: SummaryFragmentBinding
    private lateinit var viewModel: MeasurementViewModel
    private lateinit var userProfileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.summary_fragment, container, false)
        databinding = SummaryFragmentBinding.bind(view)

        expandableListView = view.findViewById(R.id.expandableListView)

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(requireActivity()).get(MeasurementViewModel::class.java)
        userProfileViewModel =
            ViewModelProviders.of(requireActivity()).get(ProfileViewModel::class.java)
        userProfileViewModel.getUser()
        userProfileViewModel.user.observe(viewLifecycleOwner, object : Observer<User> {
            override fun onChanged(t: User) {
                user = t
            }
        })
        databinding.viewModel = viewModel

        val titleList = ArrayList<String>()
        titleList.add("Contraction Durations")
        titleList.add("Interval Durations")
        titleList.add("Contraction Timeline (30 min)")
        titleList.add("Intensity Distribution")
        adapter = SummaryExpandableListAdapter(this, viewModel, titleList)

        expandableListView.setAdapter(adapter)

        expandableListView.expandGroup(0)
        expandableListView.expandGroup(1)
        expandableListView.expandGroup(2)
        expandableListView.expandGroup(3)


    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_summary, menu);
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.nav_saveSummary) {
            println("Hung, onOptionsItemSelected")
            // save
            if (ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireActivity(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //saveToGallery()
                println("Hung, save summary")
                adapter.pieChart.saveToGallery("summary2")
                adapter.chart.saveToGallery("summary1")
                showDialogShareEmailOrMessage("summary1", "summary2")
                Toast.makeText(
                    context,
                    this.getText(R.string.save_summary_success),
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                println("Hung, save summary, requestStoragePermission")
                requestStoragePermission()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun showDialogShareEmailOrMessage(
        fileNameOfBarChar: String,
        fileNameOfPieChart: String
    ) {

        if (alertDialog == null) {
            alertDialog = AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.share_data_title_dialog))
                .setMessage(getString(R.string.share_data_description_dialog)).setPositiveButton(
                    getString(R.string.share_data_via_SMS)
                ) { dialogInterface: DialogInterface, i: Int ->
                    checkPermissionSendBySMS()

                }.setNegativeButton(
                    getString(R.string.share_data_via_email)
                ) { dialogInterface: DialogInterface, i: Int ->

                    var attachedFile = getListAttachmentFile(fileNameOfBarChar, fileNameOfPieChart)
                    shareDataViaEmail(attachedFile)
                }.create()

            alertDialog?.setButton(
                AlertDialog.BUTTON_NEUTRAL, getString(R.string.share_data_dialog_close)
            ) { dialog, id ->
                dialog?.dismiss()
            }
            alertDialog?.show()
        } else if (alertDialog?.isShowing == false) {
            alertDialog?.show()
        }
    }

    private fun shareDataViaSMS(attachedFile: List<File>) {
        var phoneList = mutableListOf<String>()

        if (user?.phoneNumberOfDoctor?.isNullOrBlank() == false) { phoneList.add(user?.phoneNumberOfDoctor!!) }
        if (user?.phoneNumberOfSpouse?.isNullOrBlank() == false) { phoneList.add(user?.phoneNumberOfSpouse!!) }
        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        sendIntent.putExtra("sms_body", getString(R.string.email_content))
           // sendIntent.putExtra("address", phoneList.joinToString(separator = ","))
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        sendIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
     //   sendIntent.setClassName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity");
        //sendIntent.type = "vnd.android-dir/mms-sms"
        sendIntent.setType("image/png");
        val attachments =
            attachedFile.map { it }.filter { it.exists() && !it.isDirectory }.map {
                FileProvider
                    .getUriForFile(
                        requireContext(),
                        "${BuildConfig.APPLICATION_ID}.provider",
                        it
                    )
            }.toList()

        if (attachedFile.isNotEmpty())
            sendIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachments))

        //sendIntent.putStringArrayListExtra("address", ArrayList(phoneList))
        if (sendIntent.resolveActivity(requireContext().packageManager) != null)
            startActivity(Intent.createChooser(sendIntent, getString(R.string.choose_sms_app)))
    }

    private fun sendEmail(
        subject: String,
        content: String,
        fileAttachments: List<File> = emptyList()
    ) {
        var arrayEmail = mutableListOf<String>()
        user?.emailAddressOfDoctor?.run { arrayEmail.add(this) }
        user?.emailAddressOfSpouse?.run { arrayEmail.add(this) }
        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "text/html"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            putExtra(Intent.EXTRA_TEXT, content)
            if (!arrayEmail.isNullOrEmpty())
                putExtra(Intent.EXTRA_EMAIL, arrayEmail.toTypedArray())
            // Configure attachments
            val attachments =
                fileAttachments.map { it }.filter { it.exists() && !it.isDirectory }.map {
                    FileProvider
                        .getUriForFile(
                            requireContext(),
                            "${BuildConfig.APPLICATION_ID}.provider",
                            it
                        )
                }.toList()

            if (fileAttachments.isNotEmpty())
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(attachments))
        }

        if (emailIntent.resolveActivity(requireContext().packageManager) != null)
            startActivity(Intent.createChooser(emailIntent, getString(R.string.choose_email_app)))
    }


    private fun getListAttachmentFile(
        fileNameOfBarChar: String,
        fileNameOfPieChart: String
    ): List<File> {
        val extBaseDir = Environment.getExternalStorageDirectory()
        val file = File(extBaseDir.absolutePath + "/DCIM/" + fileNameOfBarChar + ".png")
        val filePieChart = File(extBaseDir.absolutePath + "/DCIM/" + fileNameOfPieChart + ".png")
        var attachedFile = mutableListOf<File>()
        if (!file.exists() && !filePieChart.exists()) {
            return attachedFile
        } else {
            if (file.exists()) attachedFile.add(file)
            if (filePieChart.exists()) attachedFile.add(filePieChart)
        }
        return attachedFile
    }

    private fun shareDataViaEmail(attachedFile: List<File>) {
        sendEmail(
            requireActivity()?.getString(R.string.email_subject),
            requireActivity()?.getString(R.string.email_content),
            attachedFile.toList()
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //saveToGallery()
                println("Hung, save to Gallery")
                adapter.pieChart.saveToGallery("summary2")
                adapter.chart.saveToGallery("summary1")
                Toast.makeText(
                    context,
                    this.getText(R.string.save_summary_success),
                    Toast.LENGTH_SHORT
                ).show()
                showDialogShareEmailOrMessage("summary1", "summary2")

            } else {
                println("Hung, Request Save Permission FAILED!")
                Toast.makeText(
                    requireActivity()?.applicationContext,
                    "Request Save Permission FAILED!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else if (requestCode == PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val attachedFile = getListAttachmentFile("summary1", "summary2")
                shareDataViaSMS(attachedFile)
            } else {
                Toast.makeText(
                    requireActivity()?.applicationContext,
                    "Request Send SMS Permission FAILED!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun checkPermissionSendBySMS() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.SEND_SMS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {

            val attachedFile = getListAttachmentFile("summary1", "summary2")
            shareDataViaSMS(attachedFile)
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                PERMISSIONS_REQUEST_SEND_SMS
            )

        }
    }

    private val PERMISSION_STORAGE = 100
    private val PERMISSIONS_REQUEST_SEND_SMS = 101
    private fun requestStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE) && ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE)
        ) {
            println("Hung, requestStoragePermission 1")

            Snackbar.make(
                requireView(),
                "Write permission is required to save image to gallery",
                Snackbar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                        PERMISSION_STORAGE
                    )
                }.show()
        } else {
            println("Hung, requestStoragePermission 2")

            Toast.makeText(
                requireActivity()?.applicationContext,
                "Permission Required!",
                Toast.LENGTH_SHORT
            ).show()
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_STORAGE
            )
        }
    }

}
