package com.example.flipcard;

import androidx.annotation.NonNull;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Login extends AppCompatActivity {

    private EditText id,pwd;
    private Button login;
    private TextView txt;
    private int num =1;
    private Dialog d,d1;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference tot = database.getReference("total");
    SharedPreferences sp,sp1;
    SharedPreferences.Editor edit;
    final ArrayList<String> arr = new ArrayList<>();
    final ArrayList<String> pass = new ArrayList<>();
    final ArrayList<String> hs = new ArrayList<>();
    CountDownTimer con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_login);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        login = findViewById(R.id.login2);
        id = findViewById(R.id.textView30);
        pwd = findViewById(R.id.textView33);
        txt = findViewById(R.id.textView27);
        sp = getSharedPreferences("id",MODE_PRIVATE);
        sp1 = getSharedPreferences("hs",MODE_PRIVATE);
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);
        d1 = new Dialog(this);
        d1.setContentView(R.layout.invalid);
        txt.setText("-1");

        Window win = d.getWindow();
        WindowManager.LayoutParams wlp = win.getAttributes();
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.CENTER;
        win.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win.setAttributes(wlp);

        Window win1 = d1.getWindow();
        WindowManager.LayoutParams wlp1 = win.getAttributes();
        win1.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp1.gravity = Gravity.CENTER;
        win1.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win1.setAttributes(wlp1);

        con = new CountDownTimer(360000,100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(!haveNetworkConnection()){
                    TextView t = d1.findViewById(R.id.textView24);
                    t.setText("No Network !!!");
                    d1.show();
                    d1.setCancelable(false);
                    ImageView bg = d1.findViewById(R.id.imageView28);
                    bg.setImageResource(R.drawable.cancel);
                    Button b = d1.findViewById(R.id.cancel);
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
                    d1.dismiss();
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if(!id.getText().toString().equals("") && !(pwd.getText().toString().equals(""))){
                int n = Integer.parseInt(id.getText().toString());
                int flag = Integer.parseInt(txt.getText().toString());
                if(n<=0 || n>flag){
                    d.show();
                }
                else{
                    if(arr.get(n-1).equals("")){
                        d.show();
                    }
                    else{
                        if(pass.get(n-1).equals(pwd.getText().toString())) {
                            edit = sp.edit();
                            edit.putInt("id", n);
                            edit.apply();
                            edit = sp1.edit();
                            edit.putString("hs",hs.get(n-1));
                            edit.apply();
                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            con.cancel();
                            finish();
                        }
                        else{
                            TextView t = d.findViewById(R.id.textView24);
                            t.setText("Wrong Password");
                            d.show();
                        }
                    }
                }
            }
            }
        });

        tot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int t = (dataSnapshot.getValue(Integer.class));
                txt.setText(t+"");
                for (int i = 1; i <= t; i++) {
                    myRef.child(i + "").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bug(true);
                            String name = dataSnapshot.child("Name").getValue(String.class);
                            arr.add(name);
                            pass.add(dataSnapshot.child("Password").getValue(String.class));
                            hs.add(dataSnapshot.child("HS").getValue(String.class));
                            TextView tv = findViewById(R.id.textView28);
                            if (tv.getText().toString().equals(t + "")) {
                                bug(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }

            }
            @Override
            public void onCancelled(DatabaseError error) {
            }
        });
    }
    public void bug(boolean flag){
        if(flag) {
            TextView tx = findViewById(R.id.textView28);
            tx.setText(num++ + "");
        }
        else{
            num = -100;
        }
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        con.cancel();
    }
}
