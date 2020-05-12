package com.example.flipcard;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Greet extends AppCompatActivity {

    SharedPreferences sp;
    SharedPreferences.Editor edit;
    private TextView txt;
    private Dialog d;
    int id;
    CountDownTimer con;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_greet);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        txt = findViewById(R.id.textView);
        sp = getSharedPreferences("id",MODE_PRIVATE);
        id = sp.getInt("id",0);
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);

        Window win = d.getWindow();
        WindowManager.LayoutParams wlp = win.getAttributes();
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.CENTER;
        win.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win.setAttributes(wlp);

        final CountDownTimer ct = new CountDownTimer(1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                Intent i = new Intent(Greet.this, MainActivity.class);
                startActivity(i);
                con.cancel();
                finish();
            }
        };

        if(id == 0){
            txt.setText("Please register to continue...");
            new CountDownTimer(1000,1000) {
                @Override
                public void onTick(long millisUntilFinished) {

                }

                @Override
                public void onFinish() {
                    Intent i = new Intent(Greet.this, Register.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    finish();
                }
            }.start();
        }
        else{
            con = new CountDownTimer(360000,100) {
                @Override
                public void onTick(long millisUntilFinished) {
                    if(!haveNetworkConnection()){
                        TextView t = d.findViewById(R.id.textView24);
                        t.setText("No Network !!!");
                        d.show();
                        d.setCancelable(false);
                        ImageView bg = d.findViewById(R.id.imageView28);
                        bg.setImageResource(R.drawable.cancel);
                        Button b = d.findViewById(R.id.cancel);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                con.cancel();
                                d.dismiss();
                                finish();
                            }
                        });
                    }
                    else {
                        d.dismiss();
                    }
                }

                @Override
                public void onFinish() {
                }
            }.start();
            txt.setText("Launching...");
            myRef.child(id+"").child("Name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String name = dataSnapshot.getValue(String.class);
                    txt.setText("Hello "+name+" !!!");
                    ct.start();
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(id!=0) con.cancel();
    }

    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected()) {
                    haveConnectedWifi = true;
                }
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }
}
