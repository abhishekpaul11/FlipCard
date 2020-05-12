package com.example.flipcard;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.media.MediaRecorder.VideoSource.CAMERA;

public class Register extends AppCompatActivity {

    private EditText name,pwd;
    private TextView bug;
    private ImageView img;
    private Button register,login;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference("players");
    DatabaseReference total = database.getReference("total");
    SharedPreferences sp;
    SharedPreferences.Editor edit;
    private Dialog d,d1,pic;
    private Button capture;
    CountDownTimer con;
    private StorageReference mStorageRef;
    private boolean perm = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        name = findViewById(R.id.textView3);
        register = findViewById(R.id.button18);
        login = findViewById(R.id.login);
        pwd = findViewById(R.id.textView29);
        bug = findViewById(R.id.textView6);
        sp = getSharedPreferences("id",MODE_PRIVATE);
        final int ID = sp.getInt("id", 0);
        d = new Dialog(this);
        d.setContentView(R.layout.invalid);
        d1 = new Dialog(this);
        d1.setContentView(R.layout.invalid);
        capture = findViewById(R.id.button17);
        pic = new Dialog(this);
        pic.setContentView(R.layout.cam);
        img = findViewById(R.id.imageView17);
        mStorageRef = FirebaseStorage.getInstance().getReferenceFromUrl("gs://flipcard-5b41d.appspot.com");

        Window win2 = pic.getWindow();
        WindowManager.LayoutParams wlp2 = win2.getAttributes();
        win2.setLayout(WindowManager.LayoutParams.WRAP_CONTENT,WindowManager.LayoutParams.WRAP_CONTENT);
        wlp2.gravity = Gravity.CENTER;
        win2.setBackgroundDrawable(new ColorDrawable(Color.argb(255,255,255,255)));
        win2.setAttributes(wlp2);

        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission(Manifest.permission.CAMERA,100);
                Button cam = pic.findViewById(R.id.button20);
                Button gallery = pic.findViewById(R.id.button21);
                if(perm)pic.show();
                cam.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pic.dismiss();
                        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, CAMERA);

                    }
                });
                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pic.dismiss();
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, 2);

                    }
                });
            }
        });
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

        total.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int count = dataSnapshot.getValue(Integer.class);
                bug.setText(count + "");
            }

            @Override
            public void onCancelled(DatabaseError error) {
            }
        });

        if(ID==0){
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (!name.getText().toString().equals("") && !(pwd.getText().toString().equals(""))) {
                    if(!isValid(name.getText().toString())){
                        TextView txt = d.findViewById(R.id.textView24);
                        txt.setText("Invalid Name !!!");
                        d.show();
                    }
                    else {
                        int c = Integer.parseInt(bug.getText().toString());
                        c++;
                        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = mStorageRef.child("images/" + c + "").putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                            }
                        });
                        String n = name.getText().toString();
                        myRef.child(c + "").child("Name").setValue(n);
                        myRef.child(c + "").child("HS").setValue("NA");
                        String date = new SimpleDateFormat("dd MMM ''yy", Locale.getDefault()).format(new Date());
                        myRef.child(c + "").child("Date").setValue(date);
                        myRef.child(c+ "").child("Password").setValue(pwd.getText().toString());
                        total.setValue(c);
                        sp = getSharedPreferences("id", MODE_PRIVATE);
                        edit = sp.edit();
                        edit.putInt("id", c);
                        edit.apply();
                        Intent i = new Intent(Register.this, MainActivity.class);
                        startActivity(i);
                        con.cancel();
                        finish();
                    }
                }
                }
            });

            login.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    name.setText("");
                    pwd.setText("");
                    Intent i = new Intent(getApplicationContext(),Login.class);
                    startActivity(i);
                    con.cancel();
                }
            });
        }
        else{
            Button rotate = findViewById(R.id.button19);
            Button delete = findViewById(R.id.button23);
            final ImageView rot = findViewById(R.id.imageView29);
            final ImageView del = findViewById(R.id.imageView30);
            rot.setImageResource(R.drawable.rotate);
            del.setImageResource(R.drawable.remove);
            final TextView t = findViewById(R.id.textView5);
            rotate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!t.getText().toString().equals("Add image")) {
                        Bitmap bmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
                        img.setImageBitmap(rotateImage(bmp, 90));
                    }
                }
            });
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(!t.getText().toString().equals("Add image")){
                        img.setImageResource(R.drawable.avatar);
                        t.setText("Add image");
                        del.setImageResource(R.drawable.bug);
                        rot.setImageResource(R.drawable.bug);
                    }
                }
            });
            mStorageRef.child("images/"+ID).getBytes(Long.MAX_VALUE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
            TextView tvt = findViewById(R.id.textView5);
            tvt.setText("Edit Image");
            register.setText("SAVE");
            login.setText("");
            login.setBackgroundColor(Color.TRANSPARENT);
            name.setHint("Loading");
            pwd.setHint("Loading");
            pwd.setFocusable(false);
            name.setFocusable(false);
            myRef.child(ID+"").child("Name").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    name.setText(dataSnapshot.getValue(String.class));
                    name.setFocusableInTouchMode(true);
                    name.setHint("Enter new name");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
            myRef.child(ID+"").child("Password").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    pwd.setText(dataSnapshot.getValue(String.class));
                    pwd.setFocusableInTouchMode(true);
                    pwd.setHint("Enter new password");
                }

                @Override
                public void onCancelled(DatabaseError error) {
                }
            });
            register.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if(!name.getText().toString().equals("") && !(pwd.getText().toString().equals(""))){
                    if(!isValid(name.getText().toString())){
                        TextView txt = d.findViewById(R.id.textView24);
                        txt.setText("Invalid Name !!!");
                        d.show();
                    }
                    else {
                        Bitmap bitmap = ((BitmapDrawable) img.getDrawable()).getBitmap();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask uploadTask = mStorageRef.child("images/" + ID + "").putBytes(data);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                            }
                        });
                        myRef.child(ID + "").child("Name").setValue(name.getText().toString());
                        myRef.child(ID + "").child("Password").setValue(pwd.getText().toString());
                        Intent i = new Intent(Register.this, MainActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(i);
                        con.cancel();
                        finish();
                    }
                }
                }
            });
        }
    }

    public boolean isValid(String s){
        for(int i =0;i<s.length();i++){
            if(Character.isLetter(s.charAt(i))){
                return true;
            }
        }
        return false;
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        Bitmap bitmap;

        if (resultCode == this.RESULT_CANCELED) {
            return;
        }
        if (requestCode == 2) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    img.setImageResource(R.drawable.bug);
                    img.setImageBitmap(bitmap);
                    TextView tv = findViewById(R.id.textView5);
                    tv.setText("");

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        }
        else if(requestCode == 1) {
            bitmap = (Bitmap) data.getExtras().get("data");
            img.setImageResource(R.drawable.bug);
            img.setImageBitmap(bitmap);
            TextView tv = findViewById(R.id.textView5);
            tv.setText("");
        }
        Button rotate = findViewById(R.id.button19);
        Button delete = findViewById(R.id.button23);
        final ImageView rot = findViewById(R.id.imageView29);
        final ImageView del = findViewById(R.id.imageView30);
        rot.setImageResource(R.drawable.rotate);
        del.setImageResource(R.drawable.remove);
        final TextView t = findViewById(R.id.textView5);
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!t.getText().toString().equals("Add image")) {
                    Bitmap bmp = ((BitmapDrawable) img.getDrawable()).getBitmap();
                    img.setImageBitmap(rotateImage(bmp, 90));
                }
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!t.getText().toString().equals("Add image")){
                    img.setImageResource(R.drawable.avatar);
                    t.setText("Add image");
                    del.setImageResource(R.drawable.bug);
                    rot.setImageResource(R.drawable.bug);
                }
            }
        });
    }

    public static Bitmap rotateImage(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

    // Function to check and request permission.
    public void checkPermission(String permission, int requestCode)
    {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), permission)
                == PackageManager.PERMISSION_DENIED) {

            // Requesting the permission
            ActivityCompat.requestPermissions(Register.this,
                    new String[] { permission },
                    requestCode);
        }
        else {
            perm = true;
        }
    }

    // This function is called when the user accepts or decline the permission.
    // Request Code is used to check which permission called this function.
    // This request code is provided when the user is prompt for permission.

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super
                .onRequestPermissionsResult(requestCode,
                        permissions,
                        grantResults);

        if (requestCode == 100) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                perm = true;
                pic.show();
            }
            else {
                Toast.makeText(getApplicationContext(),"Access Denied",Toast.LENGTH_SHORT).show();
                perm = false;
            }
        }
    }
}
