package com.example.karthikeyan.fingerprintcheck;

import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.widget.TextView;



public class FingerprintHandler extends FingerprintManager.AuthenticationCallback {

    private TextView tv;


    public FingerprintHandler(TextView tv) {
        this.tv = tv;
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        super.onAuthenticationError(errorCode, errString);
        tv.setText("You are not supposed to Open");
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
        super.onAuthenticationHelp(helpCode, helpString);

    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        super.onAuthenticationSucceeded(result);
        tv.setText("opened");
        MainActivity.value=1;
        tv.setTextColor(tv.getContext().getResources().getColor(android.R.color.holo_green_light));
    }

    @Override
    public void onAuthenticationFailed() {
        super.onAuthenticationFailed();
    }

    public void doAuth(FingerprintManager manager, FingerprintManager.CryptoObject obj) {
        CancellationSignal signal = new CancellationSignal();

        try {
            manager.authenticate(obj, signal, 0, this, null);
        }
        catch(SecurityException sce) {}
    }
}