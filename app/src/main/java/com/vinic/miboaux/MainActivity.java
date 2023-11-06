package com.vinic.miboaux;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onButtonClick(View view) {
        String IP = ((EditText) findViewById(R.id.inputIP)).getText().toString();
        String username = ((EditText) findViewById(R.id.inputUsername)).getText().toString();
        String password = ((EditText) findViewById(R.id.inputPassword)).getText().toString();
        String portaString = ((EditText) findViewById(R.id.inputPorta)).getText().toString();
        String urlPath = ((EditText) findViewById(R.id.inputUrl)).getText().toString();
        String metodo = ((EditText) findViewById(R.id.inputMetodo)).getText().toString();

        if (!IP.isEmpty() && !username.isEmpty() && !password.isEmpty() && !portaString.isEmpty() && !urlPath.isEmpty() && !metodo.isEmpty()) {
            try {
                int porta = Integer.parseInt(portaString);
                String url = "http://" + IP + ":" + porta + "/" + urlPath;

                executor.execute(() -> {
                    ApiRequester modeloDispositivo = new ApiRequester(IP, username, password, url, metodo);
                    String response = modeloDispositivo.makeRequest();

                    handler.post(() -> {
                        TextView responseText = findViewById(R.id.responseText);
                        responseText.setText(response);
                    });
                });
            } catch (NumberFormatException e) {
                TextView responseText = findViewById(R.id.responseText);
                responseText.setText("Número da porta inválido.");
            }
        } else {
            TextView responseText = findViewById(R.id.responseText);
            responseText.setText("Todos os campos devem ser preenchidos.");
        }
    }
}