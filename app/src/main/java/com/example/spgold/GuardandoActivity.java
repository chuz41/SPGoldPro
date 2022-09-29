package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GuardandoActivity extends AppCompatActivity {

    private String mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guardando);

        mensaje = getIntent().getStringExtra("mensaje");
        guardando();

    }

    /*
    ################################################################################################
    Naegacion hacia atras personalizada!!!
    ################################################################################################
     */
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

    /*
    ################################################################################################
     */

    private void guardando () {
        View view1 = getLayoutInflater().inflate(R.layout.custom_toast_guardando, (ViewGroup) this.findViewById(R.id.layoutContainer));
        Toast toast = new Toast(this);
        toast.setGravity(Gravity.BOTTOM,0,150);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view1);
        toast.show();
        toast.show();
        toast.show();
        toast.show();
        toast.show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
    }

}