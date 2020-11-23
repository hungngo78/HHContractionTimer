package com.hh.contractiontimer.ui.profile

import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.widget.EditText
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputLayout
import com.hh.contractiontimer.MainActivity
import com.hh.contractiontimer.R
import com.hh.contractiontimer.databinding.FragmentProfileBinding
import com.hh.contractiontimer.model.User
import java.util.regex.Pattern


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var viewModel : ProfileViewModel
    lateinit var databinding : FragmentProfileBinding
    lateinit var emailDoctor : TextInputLayout
    lateinit var emailSpouse : TextInputLayout
    lateinit var phoneNumberDoctor: TextInputLayout
    lateinit var phoneNumberSpouse: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        setHasOptionsMenu(true)
    }

    private fun isInValidation() : Boolean {
        return emailDoctor.isErrorEnabled || emailSpouse.isErrorEnabled || phoneNumberDoctor.isErrorEnabled || phoneNumberSpouse.isErrorEnabled
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view =  inflater.inflate(R.layout.fragment_profile, container, false)

        databinding = FragmentProfileBinding.bind(view)
        databinding.executePendingBindings()
        emailDoctor = view.findViewById(R.id.doctorEmailError)
        emailSpouse = view.findViewById(R.id.spouseEmailError)
        phoneNumberDoctor = view.findViewById(R.id.doctorPhoneNumberError)
        phoneNumberSpouse = view.findViewById(R.id.spousePhoneNumberError)
        emailDoctor.editText?.addTextChangedListener(onTextChanged = { s: CharSequence?,start: Int,before: Int, count: Int ->onTextChangedForEmail(s, emailDoctor) })
        emailSpouse.editText?.addTextChangedListener(onTextChanged = { s: CharSequence?,start: Int,before: Int, count: Int ->onTextChangedForEmail(s, emailSpouse) })
        phoneNumberDoctor.editText?.addTextChangedListener(onTextChanged = { s: CharSequence?,start: Int,before: Int, count: Int ->onTextChangedForPhone(s, phoneNumberDoctor) })
        phoneNumberSpouse.editText?.addTextChangedListener(onTextChanged = { s: CharSequence?,start: Int,before: Int, count: Int ->onTextChangedForPhone(s, phoneNumberSpouse) })

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = activity?.let { ViewModelProviders.of(it).get(ProfileViewModel::class.java) } ?: ProfileViewModel()
        viewModel.getUser()
        viewModel.user.observe(viewLifecycleOwner, object: Observer<User>{
            override fun onChanged(t: User) {
                databinding.user = t
            }
        })
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile, menu);
        super.onCreateOptionsMenu(menu, inflater)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.nav_saveProfile) {
            println("save profile")
            if (!isInValidation()) {
                viewModel.saveProfile()
                val navController =
                    Navigation.findNavController(context as MainActivity, R.id.nav_host_fragment)
                return navController.navigateUp()
            } else {
                Toast.makeText(context,this.getText(R.string.invalid_data_input), Toast.LENGTH_SHORT).show()
            }
        }
        return super.onOptionsItemSelected(item)

    }

    private fun String.isValidMobile(): Boolean {
        return Patterns.PHONE.matcher(this).matches()
    }
    private fun String.isValidEmail(): Boolean =
        this.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

    private fun onTextChangedForEmail(
        s: CharSequence?,
        emailTextInputLayout: TextInputLayout
    ) {
        if (!s.isNullOrEmpty() && !s.toString().isValidEmail() ) {
            emailTextInputLayout.setError(getString(R.string.email_invalid))
            emailTextInputLayout.setErrorEnabled(true)
        } else {
            emailTextInputLayout.setErrorEnabled(false)
        }
    }
    private fun onTextChangedForPhone(s: CharSequence?, phoneTextInputLayout: TextInputLayout) {
        if (!s.isNullOrEmpty() && !s.toString().isValidMobile()) {
            phoneTextInputLayout.setError(getString(R.string.phone_invalid))
            phoneTextInputLayout.setErrorEnabled(true)
        } else {
            phoneTextInputLayout.setErrorEnabled(false)
        }
    }

}
