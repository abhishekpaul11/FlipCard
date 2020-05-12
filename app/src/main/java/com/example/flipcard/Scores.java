package com.example.flipcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Scores extends AppCompatActivity {

    private RecyclerView view;
    private Dialog d;
    private NameAdapter nameAdapter;
    private TextView last;
    private int count = 1;
    ArrayList<Card> profile;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference tot = database.getReference("total");
    private int num = 1,l = 1;
    CountDownTimer con,timer;
    final ArrayList<Player> players = new ArrayList<>();
    final ArrayList<byte[]> imgs = new ArrayList<>();
    SharedPreferences sp;
    StorageReference ref = FirebaseStorage.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_scores);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        profile = new ArrayList<>();
        final TextView txt = findViewById(R.id.textView8);
        final TextView load = findViewById(R.id.textView13);
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);
        sp = getSharedPreferences("id",MODE_PRIVATE);
        final int id = sp.getInt("id",0);
        last = findViewById(R.id.textView35);
        last.setText("0");
        for(int i=0;i<100;i++){
            imgs.add(new byte[0]);
        }

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

        timer = new CountDownTimer(360000,100){
            @Override
            public void onTick(long millisUntilFinished) {
                TextView tv = findViewById(R.id.textView36);
                if(Integer.parseInt(last.getText().toString())==1){
                    view = findViewById(R.id.recycler);
                    view.setHasFixedSize(true);
                    view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
                    nameAdapter = new NameAdapter(getApplicationContext(), profile);
                    view.setAdapter(nameAdapter);
                    Player[] arr = sort();
                    if(tv.getText().toString().equals(players.size()+"")) {
                        load.setText("");
                        for (int i = 0; i < arr.length; i++) {
                            int n = arr[i].getId();
                            Bitmap bmp = BitmapFactory.decodeByteArray(imgs.get(n), 0, imgs.get(n).length);
                            if (arr[i].getSelf()) {
                                profile.add(new Card(bmp, arr[i].getName(), "Joined on: " + arr[i].getDate(), i + 1 + "", arr[i].getHs(), Color.argb(220, 229, 255, 204)));
                            } else {
                                profile.add(new Card(bmp, arr[i].getName(), "Joined on: " + arr[i].getDate(), i + 1 + "", arr[i].getHs(), Color.argb(191, 255, 255, 255)));
                            }
                        }
                        if (arr.length > 5) {
                            Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.bug);
                            profile.add(new Card(icon, "", "", "", "", Color.argb(0, 0, 0, 0)));
                        }
                        this.cancel();
                    }
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();

        tot.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final int t = dataSnapshot.getValue(Integer.class);
                for (int i = 1; i <= t; i++) {
                    myRef.child(i + "").addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            bug(true);
                            String name = dataSnapshot.child("Name").getValue(String.class);
                            String hs = dataSnapshot.child("HS").getValue(String.class);
                            String date = dataSnapshot.child("Date").getValue(String.class);
                            boolean q = (id==(Integer.parseInt(txt.getText().toString())));
                            players.add(new Player(name, date, hs,q,Integer.parseInt(txt.getText().toString())));
                            if(Integer.parseInt(txt.getText().toString())>0){
                                ref.child("images/"+Integer.parseInt(txt.getText().toString())).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    int n = Integer.parseInt(txt.getText().toString());
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        imgs.set(n,bytes);
                                        count();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle any errors
                                    }
                                });
                            }
                            if(txt.getText().toString().equals(t+"")) {
                                bug(false);
                                last.setText(l++ + "");
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
    public void count(){
        TextView t = findViewById(R.id.textView36);
        t.setText(count++ + "");
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
    final public Player[] sort() {
        int n = 0,num = 0;
        for(int i = 0;i<players.size();i++){
            if(players.get(i).getName().equals("")) n++ ;
            if(players.get(i).getHs().equals("NA")){
                num++;
            }
        }
        int[] arr = new int[num];
        for(int i = 0,q=0;i<players.size();i++){
            if(players.get(i).getHs().equals("NA")) arr[q++]=i;
        }
        Player[] pl =  new Player[players.size()-n];
        for(int i = 0,x=0;i<players.size();i++){
            if(!players.get(i).getName().equals("") && !players.get(i).getHs().equals("NA")){
                pl[x++] = players.get(i);
            }
        }

        for (int i = 0; i < pl.length-num; i++) {
            for (int j = 1; j < (pl.length-num - i); j++) {
                if (compare(pl[j-1].getHs(),pl[j].getHs())){
                    Player temp = new Player(pl[j - 1].getName(),pl[j-1].getDate(),pl[j-1].getHs(),pl[j-1].getSelf(),pl[j-1].getId());
                    pl[j-1] = pl[j];
                    pl[j] = temp;
                }
            }
        }
        for(int i=pl.length-num,x=0;i<pl.length;i++,x++){
            pl[i] = players.get(arr[x]);
        }
        return pl;
    }
    final public void bug(boolean flag){
        if(flag) {
            TextView txt = findViewById(R.id.textView8);
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
    protected void onDestroy() {
        super.onDestroy();
        timer.cancel();
        con.cancel();

    }
}
class NameAdapter extends RecyclerView.Adapter<NameAdapter.NameHolder>{

    Context context;

    List<Card> list;
    public NameAdapter(Context context,List<Card> list) {
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public NameHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.card,parent,false);
        return new NameHolder(view);
    }

    @Override
    public void onBindViewHolder(NameHolder holder, int position) {
        Card nameDetails = list.get(position);
        holder.img.setImageBitmap(Bitmap.createScaledBitmap(nameDetails.getImg(), 80,
                80, false));
        holder.nam.setText(nameDetails.getName());
        holder.dt.setText(nameDetails.getDate());
        holder.rnk.setText(nameDetails.getRank());
        holder.scr.setText(nameDetails.getScr());
        holder.cv.setCardBackgroundColor(nameDetails.getColor());
    }

    class NameHolder extends RecyclerView.ViewHolder {
         private ImageView img;
         private TextView nam,dt,rnk,scr;
         private CardView cv;

         NameHolder(View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imageView);
            nam = itemView.findViewById(R.id.textView);
            dt = itemView.findViewById(R.id.textView10);
            rnk = itemView.findViewById(R.id.textView11);
            scr = itemView.findViewById(R.id.textView12);
            cv = itemView.findViewById(R.id.cardView);
        }
    }
}

