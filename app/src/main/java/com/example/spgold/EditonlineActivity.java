package com.example.spgold;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class EditonlineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editonline);
    }

    //////////////////Personalizacion de la navegacion hacia atras!//////////////////
    @Override
    public void onBackPressed(){
        boton_atras();
    }

    private void boton_atras() {

        Intent Admin = new Intent(this, AdministrarActivity.class);
        startActivity(Admin);
        finish();
        System.exit(0);
    }
    /////////////////////////////////////////////////////////////////////////////////

}