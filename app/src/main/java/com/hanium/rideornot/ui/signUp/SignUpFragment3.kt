package com.hanium.rideornot.ui.signUp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hanium.rideornot.R
import com.hanium.rideornot.databinding.FragmentSignUp3Binding
import com.hanium.rideornot.ui.signUp.SignUpViewModel.Gender


class SignUpFragment3 : Fragment() {
    private lateinit var binding: FragmentSignUp3Binding
    private lateinit var signUpViewModel: SignUpViewModel
    private lateinit var onBackPressedCallback: OnBackPressedCallback
    private fun setBackBtnHandling() {
        onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            onBackPressedCallback
        )
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        binding = FragmentSignUp3Binding.inflate(inflater, container, false)
        signUpViewModel = ViewModelProvider(requireActivity())[SignUpViewModel::class.java]

        setBackBtnHandling()

        val fadeInAnim = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        binding.llGenderSelection.startAnimation(fadeInAnim)

        val fillFromLeftAnim = AnimationUtils.loadAnimation(context, R.anim.fill_from_left)
        binding.ivProgressBarGaugeLeft.startAnimation(fillFromLeftAnim)

        val fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        fadeOutAnim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }
            override fun onAnimationEnd(p0: Animation?) {
                parentFragmentManager.beginTransaction().replace(R.id.frm_main, SignUpFragment4()).commit()
            }
            override fun onAnimationRepeat(p0: Animation?) {
            }
        })

        binding.btnMale.setOnClickListener {
            signUpViewModel.gender = Gender.MALE
            binding.llGenderSelection.startAnimation(fadeOutAnim)
            disableButtons()
        }
        binding.btnFemale.setOnClickListener {
            signUpViewModel.gender = Gender.FEMALE
            binding.llGenderSelection.startAnimation(fadeOutAnim)
            disableButtons()
        }

        return binding.root
    }

    private fun disableButtons() {
        binding.btnFemale.isClickable = false
        binding.btnMale.isClickable = false
    }

}