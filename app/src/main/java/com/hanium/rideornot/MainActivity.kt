package com.hanium.rideornot


import android.content.Intent
import android.content.IntentSender
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import com.hanium.rideornot.databinding.ActivityMainBinding
import com.hanium.rideornot.gps.GpsManager
import com.hanium.rideornot.ui.FavoriteFragment
import com.hanium.rideornot.ui.HomeFragment
import com.hanium.rideornot.ui.SearchFragment
import com.hanium.rideornot.ui.SettingFragment

private const val REQ_ONE_TAP = 2

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private var showOneTapUi: Boolean = true
    private val loginResultHandler = registerForActivityResult<IntentSenderRequest, ActivityResult>(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        // handle intent result here
        if (result.resultCode == RESULT_OK) Log.d("loginResultHandler", "RESULT_OK.")
        if (result.resultCode == RESULT_CANCELED) Log.d("loginResultHandler", "RESULT_CANCELED.")
        if (result.resultCode == RESULT_FIRST_USER) Log.d("loginResultHandler", "RESULT_FIRST_USER.")
        try {
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            val idToken = credential.googleIdToken
            val username = credential.id
            val password = credential.password
            if (idToken != null) {
                Log.d("loginResultHandler", "Got ID token, $idToken")
            }
            if (password != null) {
                Log.d("loginResultHandler", "Got password., $password")
            }
            if (username != null) {
                Log.d("loginResultHandler", "Got username, $username")
            }


        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    Log.d("loginResultHandler", "One-tap dialog was closed.")
                    // Don't re-prompt the user.
                    showOneTapUi = false
                }
                CommonStatusCodes.NETWORK_ERROR -> Log.d("loginResultHandler", "One-tap encountered a network error.")
                else -> Log.d(
                    "loginResultHandler", "Couldn't get credential from result."
                            + e.localizedMessage
                )
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initBottomNavigation()

        GpsManager.initGpsManager(this)

        // 실시간 위치 업데이트 시작
        //GpsManager.startLocationUpdates()
        // 지오펜스 생성 예시
        //GpsManager.addGeofence("myStation", 37.540455,126.9700533 ,1000f, 1000000)


        val googleWebClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Your server's client ID, not your Android client ID.
                    .setServerClientId(googleWebClientId)
                    // Only show accounts previously used to sign in.
                    // 첫 로그인 시 false로, 로그인 정보가 있을 땐 true로 설정하기
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            // Automatically sign in when exactly one credential is retrieved.
            .setAutoSelectEnabled(true)
            .build()

        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result ->
                try {
                    startIntentSenderForResult(
                        result.pendingIntent.intentSender, REQ_ONE_TAP,
                        null, 0, 0, 0, null
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("oneTapUiFailure", "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No saved credentials found. Launch the One Tap sign-up flow, or
                // do nothing and continue presenting the signed-out UI.
                Log.d("beginSignInFailure", e.localizedMessage)
            }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_ONE_TAP -> {
                try {
                    val credential = oneTapClient.getSignInCredentialFromIntent(data)
                    val idToken = credential.googleIdToken
                    val username = credential.id
                    val password = credential.password
                    Log.d("loginResultHandler", "method operated")
                    if (idToken != null) {
                        Log.d("loginResultHandler", "Got ID token, $idToken")
                    }
                    if (password != null) {
                        Log.d("loginResultHandler", "Got password., $password")
                    }
                    if (username != null) {
                        Log.d("loginResultHandler", "Got username, $username")
                    }

                } catch (e: ApiException) {
                    Log.e("loginResultHandler", e.toString())
                    // ...
                }
            }
        }
    }
    // ...

    private fun initBottomNavigation() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.frm_main, HomeFragment())
            .commitAllowingStateLoss()
        binding.bnvMain.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, HomeFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.searchFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, SearchFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.favoriteFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, FavoriteFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
                R.id.settingFragment -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frm_main, SettingFragment())
                        .commitAllowingStateLoss()
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

}
