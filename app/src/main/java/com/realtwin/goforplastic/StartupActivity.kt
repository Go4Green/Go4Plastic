package com.realtwin.goforplastic;

import android.annotation.SuppressLint
import android.app.Activity
import android.app.SharedElementCallback
import android.content.Intent
import android.os.Bundle
//import android.support.v7.app.AppCompatActivity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_startup.*
//import com.realtwin.goforplastic.ui.AnimationUtils TODO:


class StartupActivity: AppCompatActivity() {

    private val REQUEST_SIGNIN = 0

    // Constructor
    init {

        // shared element transition and button animation
        handleEnterAnimation()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_startup)

        btn_login_fb.setOnClickListener{v: View? -> login() }
        btn_create_account.setOnClickListener{v: View? -> login() }
        btn_sign_in.setOnClickListener{v: View? -> signin() }
    }

    // Bring up buttons
    private fun showButtons() {

        // for each of the screen buttons, create slide-up animator
        val buttons = listOf(btn_login_fb, btn_create_account, btn_sign_in)
        for (btn in buttons)
            AnimationUtils.createSlideUpAnimation(btn, delay = buttons.indexOf(btn)*300L)
                .also{btn.startAnimation(it)}
    }

    private fun handleEnterAnimation(){
        setEnterSharedElementCallback(object: SharedElementCallback() {
            override fun onSharedElementEnd(
                sharedElementNames: MutableList<String>?,
                sharedElements: MutableList<View>?,
                sharedElementSnapshots: MutableList<View>?
            ) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots)
                showButtons()
            }
        })
    }

    fun login() {

    }

    fun signin() {
        val intent = Intent(this, SigninActivity::class.java).apply{}
        startActivityForResult(intent,REQUEST_SIGNIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_SIGNIN) {
            if (resultCode == Activity.RESULT_OK) {
                val intent = Intent(this, MapboxActivity::class.java).apply{}
                startActivity(intent)
                // By default we just finish the Activity and log them in automatically
                this.finish()
            }
        }
    }
}