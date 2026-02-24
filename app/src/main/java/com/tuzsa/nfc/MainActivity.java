package com.tuzsa.nfc;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private VerificarNfcActivado verificarNfcActivado;
    private NfcAdapter nfcAdapter;
    private MifareClassic mfc;
    private byte[] KeyB = hexStringToByteArray("XXXXXXXXXXXX");
    private boolean auth = false;
    private float saldo = 0.0f;
    private float saldoaRecargar = 0.0f;
    private TextView saldoActualTexto;
    private TextView retiraFunda;
    private TextView saldoActual;
    private ImageView imagenTarjetaBus;
    private Button recargar5;
    private Button recargar10;
    private Button recargar15;
    private Button recargar20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        establecerOnBackPressedCallback();
        establecerVistas();
        crearIntent();
    }

    public void establecerVistas() {
        this.recargar5 = findViewById(R.id.recargar_5_euros);
        this.recargar10 = findViewById(R.id.recargar_10_euros);
        this.recargar15 = findViewById(R.id.recargar_15_euros);
        this.recargar20 = findViewById(R.id.recargar_20_euros);
        this.saldoActualTexto = findViewById(R.id.saldo_actual_texto);
        this.retiraFunda = findViewById(R.id.saldo_actual_texto_retira_funda);
        this.saldoActual = findViewById(R.id.saldo_actual);
        this.imagenTarjetaBus = findViewById(R.id.imageView_tarjeta_bus);
    }

    public void establecerOnBackPressedCallback() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    public void ejecutarHiloVerificarNfcActivado() {
        verificarNfcActivado = new VerificarNfcActivado();
        verificarNfcActivado.setNfcAdapter(nfcAdapter);
        verificarNfcActivado.setActivity(this);
        verificarNfcActivado.start();
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        mfc = MifareClassic.get(tag);
        leerSaldoTarjeta(mfc);
    }

    public void leerSaldoTarjeta(MifareClassic mfc) {
        try {
            mfc.connect();
            auth = mfc.authenticateSectorWithKeyB(2, this.KeyB);
            if (auth) {
                byte[] data = mfc.readBlock(8);
                try {
                    saldo = getsaldo(data);
                    runOnUiThread(() -> {
                        mostrarSaldo();
                    });
                } catch (Exception e) {
                    MainActivity.mostrarMensajeToast(this, this.getString(R.string.error));
                    e.printStackTrace();
                }
            }
        } catch (TagLostException e2) {
            MainActivity.mostrarMensajeToast(this, this.getString(R.string.error_tarjeta_retirada));
            e2.printStackTrace();
        } catch (IOException e3) {
            MainActivity.mostrarMensajeToast(this, this.getString(R.string.error_no_posible_auntentificar));
            e3.printStackTrace();
        }
    }

    public void mostrarSaldo() {
        saldoActualTexto.setText(R.string.saldo_actual);
        retiraFunda.setVisibility(View.INVISIBLE);
        saldoActual.setText(String.format("%.2f",this.saldo) + "€");
        imagenTarjetaBus.setVisibility(View.INVISIBLE);
    }


    public void recargar(View view) {
        if (view == recargar5) {
            saldoaRecargar = 5f;
        }
        else if (view == recargar10) {
            saldoaRecargar = 10f;
        }
        else if (view == recargar15) {
            saldoaRecargar = 15f;
        }
        else if (view == recargar20) {
            saldoaRecargar = 20f;
        }

        System.out.println("saldoaRecargar: " + saldoaRecargar);

        if (saldo + saldoaRecargar >= 60.0f) {
            mostrarMensajeToast(this, this.getString(R.string.max_saldo_alcanzado));
        }
        else {
            nuevaActividadRecargar();
        }
    }


    public void crearIntent() {
        IntentFilter ndef = new IntentFilter("android.nfc.action.TECH_DISCOVERED");

        try {
            ndef.addDataType("*/*");
            resolveIntent(getIntent());

        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("fail", e);
        }
    }

    /* access modifiers changed from: package-private */
    public void resolveIntent(Intent intent) {
        if ("android.nfc.action.TECH_DISCOVERED".equals(intent.getAction())) {
            mfc = MifareClassic.get(intent.getParcelableExtra("android.nfc.extra.TAG"));
            leerSaldoTarjeta(mfc);
        }
    }

    public static float getsaldo(byte[] arr) {
        ByteBuffer buffete = ByteBuffer.wrap(arr);
        buffete.order(ByteOrder.LITTLE_ENDIAN);
        float temp = (float) buffete.getInt(0);
        return temp / 1000.0f;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    //Se llama cuando la actividad se vuelve visible para el usuario y se coloca en primer plano.
    //Se debe iniciar cualquier recurso o servicio que deba estar activo mientras la actividad esté en primer plano.
    //En este caso, se activa el ReaderModej para varias teconologías NFC.
    @Override
    protected void onResume() {
        super.onResume();

        if(nfcAdapter!= null) {
            Bundle options = new Bundle();
            //Solución para algunas implementaciones de firmware Nfc defectuosas que sondean la tarjeta demasiado rápido.
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250);

            //Activar ReaderMode para todos los tipos de tarjeta y desactivar los sonidos de la plataforma
            nfcAdapter.enableReaderMode(this,
                    this,
                    NfcAdapter.FLAG_READER_NFC_A |
                            NfcAdapter.FLAG_READER_NFC_B |
                            NfcAdapter.FLAG_READER_NFC_F |
                            NfcAdapter.FLAG_READER_NFC_V |
                            NfcAdapter.FLAG_READER_NFC_BARCODE |
                            NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                    options);
        }


        if (!VerificarNfcActivado.isEstaNfcActivado()) {
            ejecutarHiloVerificarNfcActivado();
        }

    }

    //Se llama cuando la actividad está a punto de pasar al segundo plano y pierde el foco del usuario.
    //Se deben realizar operaciones que deben ser guardadas o suspendidas, como guardar los cambios
    //de datos en una base de datos o liberar recursos que no son necesarios mientras la actividad no está visible.
    //En este caso, se desactiva el ReaderMode.
    @Override
    protected void onPause() {
        super.onPause();
        if(nfcAdapter!= null)
            nfcAdapter.disableReaderMode(this);
    }

    //Inicia una nueva actividad de la actvidad recargar
    public void nuevaActividadRecargar() {
        Intent intencion = new Intent(this, Recargar.class);
        Bundle bundle = new Bundle();
        bundle.putFloat("saldoaRecargar", saldoaRecargar);
        intencion.putExtras(bundle);
        startActivity(intencion);
    }

    //Inicia una nueva actividad de la actvidad recargar
    public void nuevaActividadActivarNfc() {
        Intent intencion = new Intent(this, ActivarNfc.class);
        startActivity(intencion);
    }

    //Llama al método finish() cuando el usuario presiona el botón de retroceso
    @Override
    public void onBackPressed() {
        finish();
    }

    //Muestra un mensaje personalizado en forma de Toast en una actividad
    //Crea un Toast personalizado utilizando un diseño definido en el archivo "toast_personalizado.xml".
    //El mensaje pasado como parámetro se muestra en el TextView dentro del Toast.
    //El Toast se muestra en la esquina superior derecha de la pantalla.
    public static void mostrarMensajeToast(Activity actividad, String mensaje) {
        actividad.runOnUiThread(() -> {
            LayoutInflater inflater = actividad.getLayoutInflater();
            View layout = inflater.inflate(R.layout.toast_personalizado, actividad.findViewById(R.id.toastPersonalizadoLayout));

            Toast toast = new Toast(actividad);
            toast.setGravity(Gravity.END | Gravity.TOP, 0, 50);
            toast.setDuration(Toast.LENGTH_LONG);
            toast.setView(layout);
            TextView mensajeToast = layout.findViewById(R.id.toastPersonalizadoTextViewMensaje);
            mensajeToast.setText(mensaje);
            toast.show();
        });
    }
}
