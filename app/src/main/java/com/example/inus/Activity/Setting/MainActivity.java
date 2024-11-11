package com.example.inus.Activity.Setting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inus.Activity.Home_screen;
import com.example.inus.R;
import com.example.inus.databinding.ActivityMainBinding;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private Dialog dialog;
    private FirebaseAuth mAuth;
    private ActivityMainBinding binding;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        getSupportActionBar().hide();// 隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
        preferenceManager = new PreferenceManager(getApplicationContext());
        dialog = new Dialog(this);
        mAuth = FirebaseAuth.getInstance();
        setListeners();

        binding.login.setOnClickListener(v -> {
            String ac = binding.account.getText().toString();//取得帳號
            String pwd = binding.password.getText().toString();//取得密碼
            if (isValidInput(ac, pwd)) {
                mAuth.signInWithEmailAndPassword(ac, pwd) // 會員登入
                        .addOnCompleteListener(task -> {
                            signIn();
                        })
                        .addOnFailureListener(e ->{
                            switch (e.getMessage()) {
                                case "The email address is badly formatted.":
                                    Toast.makeText(MainActivity.this, "Email格式錯誤", Toast.LENGTH_LONG).show();
                                    break;
                                case "The password is invalid or the user does not have a password.":
                                    Toast.makeText(MainActivity.this, "密碼錯誤", Toast.LENGTH_LONG).show();
                                    break;
                                case "There is no user record corresponding to this identifier. The user may have been deleted.":
                                    Toast.makeText(MainActivity.this, "無此用戶", Toast.LENGTH_LONG).show();
                                    break;
                            }
                });
            }
        });
        }

    private void setListeners(){
        binding.register.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, register.class)));
        binding.passwordforgot.setOnClickListener(view -> openpwforgotDialog());
    }

    private Boolean isValidInput(String ac , String pwd) {
        if (TextUtils.isEmpty(ac) && TextUtils.isEmpty(pwd)) {
            binding.erroraccount.setText("輸入帳號");
            binding.errorpassword.setText("輸入密碼");
            return false;
        } else if (TextUtils.isEmpty(pwd)) {
            binding.erroraccount.setText(" ");
            binding.errorpassword.setText("輸入密碼");
            return false;
        } else if (TextUtils.isEmpty(ac)) {
            binding.erroraccount.setText("輸入帳號");
            binding.errorpassword.setText(" ");
            return false;
        } else {
            return true;
        }
    }

    private void signIn(){
        try {
            db.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_EMAIL, binding.account.getText().toString())
                    .whereEqualTo(Constants.KEY_PASSWORD, binding.password.getText().toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null
                                && task.getResult().getDocuments().size() > 0) {
                            DocumentSnapshot doc = task.getResult().getDocuments().get(0);
                            preferenceManager.putString(Constants.KEY_USER_ID, doc.getId());
                            preferenceManager.putString(Constants.KEY_NAME, doc.getString(Constants.KEY_NAME));
                            preferenceManager.putString(Constants.KEY_IMAGE, doc.getString(Constants.KEY_IMAGE));
                            Intent it = new Intent(getApplicationContext(), Home_screen.class);
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(it);
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, e.getMessage() , Toast.LENGTH_SHORT).show();
        }

    }
    private void openpwforgotDialog(){
        dialog.setContentView(R.layout.forgot_password);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        ImageView imageView = dialog.findViewById(R.id.imageView);
        Button get =dialog.findViewById(R.id.get);
        EditText fpassword =dialog.findViewById(R.id.fpassword);

        imageView.setOnClickListener(view -> dialog.dismiss());

        get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(fpassword.getText().toString())!=true) {
                    String password = fpassword.getText().toString();
                    mAuth.sendPasswordResetEmail(password)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this,"請至您的信箱修改密碼",Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d("Demo",e.getMessage());
                                    switch (e.getMessage()){
                                        case "There is no user record corresponding to this identifier. The user may have been deleted.":
                                            Toast.makeText(MainActivity.this,"無此用戶",Toast.LENGTH_LONG).show();
                                            break;
                                        case "The email address is badly formatted.":
                                            Toast.makeText(MainActivity.this, "Email格式錯誤", Toast.LENGTH_LONG).show();
                                            break;
                                    }
                                }
                            });
                }else{
                    Toast.makeText(MainActivity.this,"請輸入帳號(信箱)",Toast.LENGTH_LONG).show();
                }
            }
        });
        dialog.show();
    }
}
