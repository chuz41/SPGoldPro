package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class EditarActivity extends AppCompatActivity {

    private String loteria;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar);

        loteria = getIntent().getStringExtra("Loteria");

    }

    @Override
    public void onBackPressed(){
        View view = null;
        boton_atras(view);
    }

    public void boton_atras(View view) {
        Intent Main = new Intent(this, MainActivity.class);
        startActivity(Main);
        finish();
        System.exit(0);
    }

}