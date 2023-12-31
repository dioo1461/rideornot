package com.hanium.rideornot.ui.signUp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hanium.rideornot.R
import com.hanium.rideornot.data.request.ProfilePostRequestBody
import com.hanium.rideornot.databinding.FragmentSignUp4Binding
import com.hanium.rideornot.ui.setting.SettingViewModel
import com.hanium.rideornot.utils.NetworkModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val FIRST_ANIM_DELAY: Long = 0
private const val SECOND_ANIM_DELAY: Long = 800
private const val MAX_NICKNAME_BYTES: Int = 24

class SignUpFragment4 : Fragment() {
    private lateinit var binding: FragmentSignUp4Binding
    private lateinit var signUpViewModel: SignUpViewModel
    private lateinit var settingViewModel: SettingViewModel
    private lateinit var onBackPressedCallback: OnBackPressedCallback

    private var currentBytes = 0
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
        binding = FragmentSignUp4Binding.inflate(inflater, container, false)
        signUpViewModel = ViewModelProvider(requireActivity())[SignUpViewModel::class.java]

        setBackBtnHandling()

        val fadeInAnim1 = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        fadeInAnim1.startOffset = FIRST_ANIM_DELAY
        binding.clNicknameInput.startAnimation(fadeInAnim1)
        val fadeInAnim2 = AnimationUtils.loadAnimation(context, R.anim.fade_in)
        fadeInAnim2.startOffset = SECOND_ANIM_DELAY
        binding.tvOkBtn.startAnimation(fadeInAnim2)

        val fillFromLeftAnim = AnimationUtils.loadAnimation(context, R.anim.fill_from_left)
        binding.ivProgressBarGaugeRight.startAnimation(fillFromLeftAnim)

        val fadeOutAnim = AnimationUtils.loadAnimation(context, R.anim.fade_out)
        fadeOutAnim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(p0: Animation?) {
            }

            override fun onAnimationEnd(p0: Animation?) {
                parentFragmentManager.beginTransaction().remove(this@SignUpFragment4).commit()
            }

            override fun onAnimationRepeat(p0: Animation?) {
            }
        })

        binding.tvNicknameCurrentBytes.text = signUpViewModel.nickName.toByteArray(Charsets.UTF_8).size.toString()
        binding.tvNicknameMaxBytes.text = MAX_NICKNAME_BYTES.toString()
        binding.editTextNicknameInput.hint = signUpViewModel.nickName

        binding.editTextNicknameInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val text = s.toString()

                currentBytes = if (text == "") signUpViewModel.nickName.toByteArray(Charsets.UTF_8).size
                else text.toByteArray(Charsets.UTF_8).size

                binding.tvNicknameCurrentBytes.text = currentBytes.toString()

                if (currentBytes > MAX_NICKNAME_BYTES) {
                    binding.tvNicknameCurrentBytes.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
                } else {
                    binding.tvNicknameCurrentBytes.setTextColor(ContextCompat.getColor(requireContext(), R.color.gray_700))
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        // TODO: 글자수 제한 넘으면 재설정 플로팅 출력
        binding.tvOkBtn.setOnClickListener {
            if (currentBytes > MAX_NICKNAME_BYTES) {
                //TODO: 추후 dataBinding으로 리팩토링
                Toast.makeText(requireContext(), "닉네임의 길이가 최대 글자 수를 초과하였습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("okBtn", "clicked")
            val profilePostRequestBody: ProfilePostRequestBody = if (
                binding.editTextNicknameInput.text.toString() == ""
            ) {
                ProfilePostRequestBody(
                    ageRange = signUpViewModel.ageRange.id,
                    gender = signUpViewModel.gender.id,
                    nickName = signUpViewModel.nickName
                )
            } else {
                ProfilePostRequestBody(
                    ageRange = signUpViewModel.ageRange.id,
                    gender = signUpViewModel.gender.id,
                    nickName = binding.editTextNicknameInput.text.toString()
                )
            }
            CoroutineScope(Dispatchers.Main).launch {
                val response = withContext(Dispatchers.Default) {
                    NetworkModule.getProfileService().postProfile(profilePostRequestBody)
                }
                Log.d("response-signUp", response.toString())
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.toast_signUp_success), Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                    binding.root.startAnimation(fadeOutAnim)
                }
            }
            disableButtons()
        }

        return binding.root
    }

    private fun disableButtons() {
        binding.tvOkBtn.isClickable = false
    }

}