package com.realtwin.goforplastic;

//import com.google.android.gms.common.GooglePlayServicesNotAvailableException
//import com.realtwin.goforplastic.ui.AnimationUtils   TODO:
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_signin.*


class SigninActivity: AppCompatActivity() {

    private val TAG = "SigninActivity"

    @TargetApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signin)

        //input_email.setHintTextColor(resources.getColorStateList(R.color.edit_text_color_hint, null))
        input_email.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                sign_in_email_view.setTextColor(getColor(R.color.login_color))
            }
            else {
                sign_in_email_view.setTextColor(getColor(R.color.sign_in_color))
            }
        }

        input_password.setOnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
                sign_in_password_view.setTextColor(getColor(R.color.login_color))
            }
            else {
                sign_in_password_view.setTextColor(getColor(R.color.sign_in_color))
            }
        }

        btn_login.setOnClickListener{onSignInClick()}
    }


    private fun onSignInClick()
    {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }

        btn_login.isEnabled = false

        authentication_view.animate()?.alpha(1f)?.duration = 500
        blur.animate()?.alpha(.6f)?.duration = 500

        //val res = resources
        //val drawable = res.getDrawable(R.drawable.customprogressbar)
        //val mProgress = findViewById<View>(R.id.progressbar_id) as ProgressBar
        //mProgress.setProgressDrawable(drawable);

        Handler().postDelayed(Runnable {
            onLoginSuccess()
            authentication_view.visibility = View.GONE
        }, 3000)
    }


    


    fun validate(): Boolean {
        var valid = true

        val email = input_email.getText().toString()
        val password = input_password.getText().toString()

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            input_email.setError("enter a valid email address")
            valid = false
        } else {
            input_email.setError(null)
        }

        if (password.isEmpty() || password.length < 4 || password.length > 10) {
            input_password.setError("between 4 and 10 alphanumeric characters")
            valid = false
        } else {
            input_password.setError(null)
        }

        //return valid
        return true
    }


    fun onLoginFailed() {
        Toast.makeText(baseContext, "Login failed", Toast.LENGTH_LONG).show()

        btn_login.setEnabled(true)
    }

    fun onLoginSuccess() {
        btn_login.setEnabled(true)
        setResult(RESULT_OK, null);
        finish()
    }


    /*override fun onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true)
    }*/
}


