package com.tuzsa.nfc;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

public class ActivarNfc extends AppCompatActivity {

    private ControlarActividadActivarNfc controlarActividadActivarNfc;
    private NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activar_nfc);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        ejecutarControlarActividadActivarNfc();
        establecerDatos();
        establecerOnBackPressedCallback();


    }

    public void establecerDatos() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

    public void establecerOnBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {}
        });
    }

    public void ejecutarControlarActividadActivarNfc() {
        controlarActividadActivarNfc = new ControlarActividadActivarNfc();
        controlarActividadActivarNfc.setActivity(ActivarNfc.this);
        controlarActividadActivarNfc.setNfcAdapter(nfcAdapter);
        controlarActividadActivarNfc.start();
    }

    public void activarNfc(View view) {
        Intent nfcSettings;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            nfcSettings = new Intent(Settings.Panel.ACTION_NFC);
            startActivity(nfcSettings);
        }
        else {
            nfcSettings = new Intent(Settings.ACTION_NFC_SETTINGS);
            startActivity(nfcSettings);
        }
    }
}