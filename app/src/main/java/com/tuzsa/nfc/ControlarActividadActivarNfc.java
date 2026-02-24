package com.tuzsa.nfc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;

public class ControlarActividadActivarNfc extends Thread {

    private NfcAdapter nfcAdapter;
    private ActivarNfc activity;

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(100);
                solicitarActivarNfc();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //El NFC no está habilitado, solicitar activación
    public void solicitarActivarNfc() {
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            activity.finish();
            interrupt();
        }
    }

    public NfcAdapter getNfcAdapter() {
        return nfcAdapter;
    }

    public void setNfcAdapter(NfcAdapter nfcAdapter) {
        this.nfcAdapter = nfcAdapter;
    }

    public ActivarNfc getActivity() {
        return activity;
    }

    public void setActivity(ActivarNfc activity) {
        this.activity = activity;
    }
}
