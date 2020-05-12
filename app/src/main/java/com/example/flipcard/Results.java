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
import android.util.Log;
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

import java.util.ArrayList;

public class Results extends AppCompatActivity {

    private TextView msg, score, load;
    private ImageView symbol;
    private int num = 1;
    Dialog d;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference tot = database.getReference("total");
    SharedPreferences sp,sp1;
    SharedPreferences.Editor edit;
    final ArrayList<String> arr = new ArrayList<>();
    CountDownTimer con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_results);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        msg = findViewById(R.id.textView2);
        score = findViewById(R.id.textView31);
        symbol = findViewById(R.id.imageView27);
        load = findViewById(R.id.textView32);
        sp = getSharedPreferences("hs",MODE_PRIVATE);
        sp1 = getSharedPreferences("id",MODE_PRIVATE);
        final int id = sp1.getInt("id",0);
        edit = sp.edit();
        final String local = sp.getString("hs","NA");
        arr.add("1");
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);

        Window win = d.getWindow();
        WindowManager.LayoutParams wlp = win.getAttributes();
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.CENTER;
        win.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win.setAttributes(wlp);

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

        String s;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                s= null;
            } else {
                s= extras.getString("score");
            }
        } else {
            s= (String) savedInstanceState.getSerializable("score");
        }
        final String str = s;
        tot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int t = (dataSnapshot.getValue(Integer.class));
                for (int i = 1; i <= t; i++) {
                    myRef.child(i + "").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bug(true);
                            String hs = dataSnapshot.child("HS").getValue(String.class);
                            if(!hs.equals("NA") && !hs.equals("")) {
                                if (arr.get(0).equals("1")) {
                                    arr.remove(0);
                                    arr.add(hs);
                                } else {
                                    if (compare(arr.get(0), hs)) {
                                        arr.remove(0);
                                        arr.add(hs);
                                    }
                                }
                            }
                            TextView tv = findViewById(R.id.textView25);
                            if (tv.getText().toString().equals(t + "")) {
                                bug(false);
                                if(arr.get(0).equals("1") || compare(arr.get(0),str)){
                                    symbol.setImageResource(R.drawable.ghs);
                                    msg.setText("Woohoo !!!\nYou're The Best");
                                    myRef.child(id+"").child("HS").setValue(str);
                                    edit.putString("hs",str);
                                    edit.apply();
                                }
                                else if(local.equals("NA") || compare(local,str)){
                                    symbol.setImageResource(R.drawable.hs);
                                    msg.setText("Congrats !!!\nNew High Score");
                                    myRef.child(id+"").child("HS").setValue(str);
                                    edit.putString("hs",str);
                                    edit.apply();
                                }
                                else{
                                    symbol.setImageResource(R.drawable.score);
                                    msg.setText("Your Score");
                                }
                                load.setText("");
                                score.setText(str);
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
            TextView tx = findViewById(R.id.textView25);
            tx.setText(num++ + "");
        }
        else{
            num = -100;
        }
    }
    public boolean compare(String s1,String s2){
        int j1 = s1.indexOf('m');
        int m1 = Integer.parseInt(s1.substring(0,j1-1));
        int j2 = s2.indexOf('m');
        int m2 = Integer.parseInt(s2.substring(0,j2-1));
        if(m1<m2) return false;
        else if(m1>m2) return true;
        else{
            int k1 = s1.indexOf('s');
            int sec1 = Integer.parseInt(s1.substring(j1+2,k1-1));
            int k2 = s2.indexOf('s');
            int sec2 = Integer.parseInt(s2.substring(j2+2,k2-1));
            if(sec1<sec2) return false;
            else if(sec1>sec2) return true;
            else return false;
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
