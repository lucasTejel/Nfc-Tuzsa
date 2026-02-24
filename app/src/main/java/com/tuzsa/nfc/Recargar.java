package com.tuzsa.nfc;

import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.TagLostException;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class Recargar extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private NfcAdapter nfcAdapter;
    private MifareClassic mfc;
    private boolean auth = false;
    private byte[] KeyB = MainActivity.hexStringToByteArray("XXXXXXXXXXXX");
    private float saldoaRecargar;
    private TextView cantidadRecargar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recargar);

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        cantidadRecargar = findViewById(R.id.cantidad_a_recargar);
        establecerDatos();
    }

    public void establecerDatos() {
        saldoaRecargar = recogerSaldoaRecargarBundle();
        cantidadRecargar.setText(String.format("%.0f", saldoaRecargar) + "€");
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        mfc = MifareClassic.get(tag);

        try {
            mfc.close();
            mfc.connect();
            auth = mfc.authenticateSectorWithKeyB(2, KeyB);

            auth = mfc.authenticateSectorWithKeyB(2, KeyB);
            if (auth) {
                mfc.increment(8, (int) (saldoaRecargar * 1000.0f));
                mfc.transfer(8);
                mfc.increment(9, (int) (saldoaRecargar * 1000.0f));
                mfc.transfer(9);
            }

            MainActivity.mostrarMensajeToast(this, this.getString(R.string.cargado_correctamente) + saldoaRecargar + " €");
            finish();

        } catch (TagLostException e) {
            MainActivity.mostrarMensajeToast(this, this.getString(R.string.error_tarjeta_retirada));
            e.printStackTrace();
        } catch (IOException e2) {
            MainActivity.mostrarMensajeToast(this, this.getString(R.string.error_no_posible_auntentificar));
            e2.printStackTrace();
        }
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

        finish();
    }


    public float recogerSaldoaRecargarBundle() {
        Bundle datos = this.getIntent().getExtras();
        return datos.getFloat("saldoaRecargar", 0.0f);
    }

}
