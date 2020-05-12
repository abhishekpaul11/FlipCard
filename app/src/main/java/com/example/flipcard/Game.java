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
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Game extends AppCompatActivity {

    private Integer[] numArr = new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 0, 1, 2, 3, 4, 5, 6, 7};
    private ImageView[] imgs = new ImageView[16];
    private int[] debug = new int[16];
    private Button[] btn = new Button[16];
    private int[] btn_id = new int[]{R.id.button, R.id.button2, R.id.button3, R.id.button4, R.id.button5, R.id.button6, R.id.button7, R.id.button8, R.id.button9, R.id.button10, R.id.button11, R.id.button12, R.id.button13, R.id.button14, R.id.button15, R.id.button16};
    private int[] ids = new int[]{R.id.imageView1, R.id.imageView2, R.id.imageView3, R.id.imageView4, R.id.imageView5, R.id.imageView6, R.id.imageView7, R.id.imageView8, R.id.imageView9, R.id.imageView10, R.id.imageView11, R.id.imageView12, R.id.imageView13, R.id.imageView14, R.id.imageView15, R.id.imageView16};
    private int[] arr = new int[]{R.drawable.ball, R.drawable.cloud, R.drawable.drop, R.drawable.heart, R.drawable.house, R.drawable.key, R.drawable.leaf, R.drawable.smiley};
    private boolean state = false, bug = true;
    private int sum = 0;
    private int number = 1;
    private int card = -1, num = -1;
    private TextView timer, high, ghs;
    private int sec = 0, min = 0;
    SharedPreferences sp;
    CountDownTimer ct,con;
    private boolean val;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference tot = database.getReference("total");
    final ArrayList<String> ar = new ArrayList<>();
    private Dialog d;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_game);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        val = true;

        sp = getSharedPreferences("hs", MODE_PRIVATE);
        String hs = sp.getString("hs", "NA");
        ar.add("1");
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);

        Window win = d.getWindow();
        WindowManager.LayoutParams wlp = win.getAttributes();
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.CENTER;
        win.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win.setAttributes(wlp);

        con = new CountDownTimer(360000,100) {
            boolean flag = false;
            @Override
            public void onTick(long millisUntilFinished) {
                if(!haveNetworkConnection()){
                    TextView t = d.findViewById(R.id.textView24);
                    t.setText("No Network !!!");
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
                    d.show();
                    d.setCancelable(false);
                    ct.cancel();
                    flag = true;
                }
                else {
                    d.dismiss();
                    if(flag){
                        ct.start();
                        flag = false;
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();

        high = findViewById(R.id.score);
        ghs = findViewById(R.id.score2);
        high.setText(hs);
        timer = findViewById(R.id.counter);
        shuffle();
        for (int i = 0; i < 16; i++) {
            btn[i] = findViewById(btn_id[i]);
            final int j = i;
            final String h = hs;
            btn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (j != num && bug && debug[j] != 1) {
                    if (state) {
                        state = false;
                        btn[j].setBackgroundColor(Color.TRANSPARENT);
                        new CountDownTimer(500, 1) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                bug = false;
                            }

                            @Override
                            public void onFinish() {
                                if (card == numArr[j]) {
                                    sum++;
                                    debug[j] = debug[num] = 1;
                                    if (sum == 8) {
                                        ct.cancel();
                                        Intent i = new Intent(Game.this, Results.class);
                                        i.putExtra("score", timer.getText().toString());
                                        startActivity(i);
                                        con.cancel();
                                        finish();
                                    }
                                } else {
                                    if (num == 0 || num == 2 || num == 5 || num == 7 || num == 8 || num == 10 || num == 13 || num == 15) {
                                        btn[num].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    } else {
                                        btn[num].setBackgroundColor(Color.WHITE);
                                    }
                                    if (j == 0 || j == 2 || j == 5 || j == 7 || j == 8 || j == 10 || j == 13 || j == 15) {
                                        btn[j].setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                                    } else {
                                        btn[j].setBackgroundColor(Color.WHITE);
                                    }
                                }
                                bug = true;
                                num = -1;
                            }
                        }.start();
                    } else {
                        btn[j].setBackgroundColor(Color.TRANSPARENT);
                        state = true;
                        card = numArr[j];
                        num = j;
                    }
                }
                }
            });
        }
        for (int i = 0; i < 16; i++) {
            imgs[i] = findViewById(ids[i]);
            imgs[i].setImageResource(arr[numArr[i]]);
        }

        ct = new CountDownTimer(3600000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                sec++;
                if (sec == 60) {
                    sec = 0;
                    min++;
                }
                String str = min + " m " + sec + " s";
                timer.setText(str);
            }

            @Override
            public void onFinish() {

            }
        };
        ct.start();

        tot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (DataSnapshot dataSnapshot){
                final int t = (dataSnapshot.getValue(Integer.class));
                for (int i = 1; i <= t; i++) {
                    myRef.child(i + "").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bug(true);
                            String hs = dataSnapshot.child("HS").getValue(String.class);
                            if (!hs.equals("NA") && !(hs.equals(""))) {
                                if (ar.get(0).equals("1")) {
                                    ar.remove(0);
                                    ar.add(hs);
                                }
                                else{
                                    if (compare(ar.get(0), hs)) {
                                        ar.remove(0);
                                        ar.add(hs);
                                    }
                                }
                            }
                            TextView tv = findViewById(R.id.textView34);
                            if (tv.getText().toString().equals(t + "")) {
                                bug(false);
                                if (ar.get(0).equals("1")) {
                                    ghs.setText("NA");
                                }
                                else{
                                    ghs.setText(ar.get(0));
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }
            @Override
            public void onCancelled (DatabaseError error){
            }
        });
    }

    public void bug(boolean flag){
        if(flag) {
            TextView tx = findViewById(R.id.textView34);
            tx.setText(number++ + "");
        }
        else{
            number = -100;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!val) ct.start();
        val = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        val = false;
        ct.cancel();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        con.cancel();
    }

    public boolean compare(String s1, String s2) {
        int j1 = s1.indexOf('m');
        int m1 = Integer.parseInt(s1.substring(0, j1 - 1));
        int j2 = s2.indexOf('m');
        int m2 = Integer.parseInt(s2.substring(0, j2 - 1));
        if (m1 < m2) return false;
        else if (m1 > m2) return true;
        else {
            int k1 = s1.indexOf('s');
            int sec1 = Integer.parseInt(s1.substring(j1 + 2, k1 - 1));
            int k2 = s2.indexOf('s');
            int sec2 = Integer.parseInt(s2.substring(j2 + 2, k2 - 1));
            if (sec1 < sec2) return false;
            else if (sec1 > sec2) return true;
            else return false;
        }
    }

    public void shuffle() {
        List<Integer> intList = Arrays.asList(numArr);
        Collections.shuffle(intList);
        intList.toArray(numArr);
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
