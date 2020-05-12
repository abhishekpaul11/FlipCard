package com.example.flipcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Profile extends AppCompatActivity {

    private TextView id,name,date,rank,txt,temp;
    private ImageView img;
    SharedPreferences.Editor edit;
    SharedPreferences sp,sp1;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference tot = database.getReference("total");
    final ArrayList<String> arr = new ArrayList<>();
    private int num = 1;
    private Button delete,yes,no,res,ed,logout;
    private Dialog msg,d;
    private ImageView reset;
    CountDownTimer con;
    StorageReference ref = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_profile);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        sp = getSharedPreferences("id",MODE_PRIVATE);
        sp1 = getSharedPreferences("hs",MODE_PRIVATE);
        final int ID = sp.getInt("id",0);

        id = findViewById(R.id.textView15);
        name = findViewById(R.id.textView16);
        date = findViewById(R.id.textView17);
        rank = findViewById(R.id.textView20);
        txt = findViewById(R.id.textView21);
        temp = findViewById(R.id.textView22);
        delete = findViewById(R.id.del);
        msg = new Dialog(this);
        msg.setContentView(R.layout.message);
        ed = findViewById(R.id.edit);
        logout = findViewById(R.id.logout);
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);
        img = findViewById(R.id.imageView18);

        ref.child("images/"+ID).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                img.setImageBitmap(Bitmap.createScaledBitmap(bmp, img.getWidth(),
                        img.getHeight(), false));            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        Window win1 = d.getWindow();
        WindowManager.LayoutParams wlp = win1.getAttributes();
        win1.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp.gravity = Gravity.CENTER;
        win1.setBackgroundDrawable(new ColorDrawable(Color.rgb(255,255,255)));
        win1.setAttributes(wlp);

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

        Window win = msg.getWindow();
        WindowManager.LayoutParams wlp1 = win.getAttributes();
        win.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp1.gravity = Gravity.CENTER;
        win.setBackgroundDrawable(new ColorDrawable(Color.argb(200,229,255,204)));
        win.setAttributes(wlp1);

        ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Profile.this,Register.class);
                startActivity(i);
                con.cancel();
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.show();
                yes = msg.findViewById(R.id.button20);
                no = msg.findViewById(R.id.button21);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.dismiss();
                        myRef.child(ID+"").child("Name").setValue("");
                        myRef.child(ID+"").child("HS").setValue("");
                        myRef.child(ID+"").child("Date").setValue("");
                        myRef.child(ID+"").child("Password").setValue("");
                        edit = sp.edit();
                        edit.putInt("id",0);
                        edit.apply();
                        edit = sp1.edit();
                        edit.putString("hs","NA");
                        edit.apply();
                        Intent i = new Intent(Profile.this,Greet.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        con.cancel();
                        finish();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.dismiss();
                    }
                });
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                msg.show();
                yes = msg.findViewById(R.id.button20);
                no = msg.findViewById(R.id.button21);
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.dismiss();
                        edit = sp.edit();
                        edit.putInt("id",0);
                        edit.apply();
                        edit = sp1.edit();
                        edit.putString("hs","NA");
                        edit.apply();
                        Intent i = new Intent(Profile.this,Login.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        con.cancel();
                        finish();
                    }
                });
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        msg.dismiss();
                    }
                });
            }
        });

        myRef.child(ID+"").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                date.setText(dataSnapshot.child("Date").getValue(String.class));
                name.setText(dataSnapshot.child("Name").getValue(String.class));
                temp.setText(dataSnapshot.child("HS").getValue(String.class));
                id.setText(ID+"");
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        tot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int t = dataSnapshot.getValue(Integer.class);
                for (int i = 1; i <= t; i++) {
                    myRef.child(i + "").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bug(true);
                            String hs = dataSnapshot.child("HS").getValue(String.class);
                            arr.add(hs);
                            if(txt.getText().toString().equals(t+"")){
                                String key = temp.getText().toString();
                                if(key.equals("NA")){
                                    rank.setText("NA");
                                }
                                else {
                                    bug(false);
                                    String[] a = sort();
                                    for (int i = 0; i < a.length; i++) {
                                        if (key.equals(a[i])) {
                                            rank.setText(i+1 + " (" + a[i] + ")");
                                            break;
                                        }
                                    }
                                    res = findViewById(R.id.button22);
                                    reset = findViewById(R.id.imageView26);
                                    reset.setImageResource(R.drawable.reset);
                                    res.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            msg.show();
                                            yes = msg.findViewById(R.id.button20);
                                            no = msg.findViewById(R.id.button21);
                                            yes.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    msg.dismiss();
                                                    rank.setText("NA");
                                                    reset.setImageResource(R.drawable.bug);
                                                    myRef.child(ID+"").child("HS").setValue("NA");
                                                    edit = sp1.edit();
                                                    edit.putString("hs","NA");
                                                    edit.apply();
                                                }
                                            });
                                            no.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    msg.dismiss();
                                                }
                                            });
                                        }
                                    });
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
            public void onCancelled(DatabaseError error) {
            }
        });
    }
    public String[] sort(){
        int n = 0;
        for(int i = 0;i<arr.size();i++){
            if(arr.get(i).equals("NA") || arr.get(i).equals("")) n++;
        }
        String[] str = new String[arr.size()-n];
        for(int i=0,x=0;i<arr.size();i++){
            if(!arr.get(i).equals("NA") && !arr.get(i).equals("")) {
                str[x++] = arr.get(i);
            }
        }
        for (int i = 0; i < str.length; i++) {
            for (int j = 1; j < (str.length - i); j++) {
                if (compare(str[j-1],str[j])){
                    String temp = str[j-1];
                    str[j-1] = str[j];
                    str[j] = temp;
                }
            }
        }
        return str;
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
    public void bug(boolean flag){
        if(flag) {
            TextView txt = findViewById(R.id.textView21);
            txt.setText(num++ + "");
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
    protected void onResume() {
        super.onResume();
        con.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        con.cancel();
    }
}
