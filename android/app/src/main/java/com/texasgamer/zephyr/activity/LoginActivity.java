package com.texasgamer.zephyr.activity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.texasgamer.zephyr.R;
import com.texasgamer.zephyr.util.TokenUtils;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setZephyrTypeface();
        setupLoginBtn();
        setupPrivacyPolicyBtn();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TokenUtils.getInstance(this).doesTokenExist()) {
            Log.i(TAG, "Logged in, proceeding to MainActivity.");
            Intent i = new Intent(LoginActivity.this, MainActivity2.class);
            startActivity(i);
            finish();
        }
    }

    private void setZephyrTypeface() {
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/righteous.ttf");
        TextView myTextView = (TextView) findViewById(R.id.zephyr);
        myTextView.setTypeface(myTypeface);
    }

    private void setupLoginBtn() {
        findViewById(R.id.login_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, WebLoginActivity.class);
                startActivity(i);
            }
        });
    }

    private void setupPrivacyPolicyBtn() {
        findViewById(R.id.privacy_policy_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO: Open privacy policy page in browser
            }
        });
    }
}
