package com.tuzsa.nfc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.provider.Settings;

public class VerificarNfcActivado extends Thread {
    private static boolean estaNfcActivado = false;
    private NfcAdapter nfcAdapter;
    private MainActivity activity;

    @Override
    public void run() {
        comprobarNfc();
        estaNfcActivado = true;

        while (!Thread.interrupted()) {
            try {
                Thread.sleep(100);
                crearActividadActivarNfc();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    //Si el dispositivo no soporta NFC, muestra un mensaje indicandolo
    public synchronized void comprobarNfc() {
        PackageManager packageManager = activity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            //El dispositivo no tiene NFC, maneja esta situación adecuadamente
            MainActivity.mostrarMensajeToast(activity, activity.getString(R.string.error_no_nfc));
        }
    }

    //El NFC no está habilitado, solicitar activación
    public synchronized void crearActividadActivarNfc() {
        if (nfcAdapter == null || !nfcAdapter.isEnabled()) {
            //El NFC no está habilitado, solicitar activación
            estaNfcActivado = false;

            activity.nuevaActividadActivarNfc();
            interrupt();
        }
    }

    public void setNfcAdapter(NfcAdapter nfcAdapter) {
        this.nfcAdapter = nfcAdapter;
    }

    public void setActivity(MainActivity activity) {
        this.activity = activity;
    }

    public static boolean isEstaNfcActivado() {
        return estaNfcActivado;
    }
}
