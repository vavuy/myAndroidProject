package com.example.inus.Activity.Setting;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inus.R;
import com.example.inus.databinding.ActivityRegisterBinding;
import com.example.inus.util.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class register extends AppCompatActivity {

    private String[] hobby1 ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","3C娛樂","人氣美食"};
    private boolean[] hobbyInChecked={false,false,false,false,false,false,false,false,false};
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private Map<String, Object> user = new HashMap<>();
    private String encodedImage;
    private Dialog dialog;
    private ActivityRegisterBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        //選擇嗜好
        binding.rhobby.setOnClickListener(view -> {
            new AlertDialog.Builder(register.this)
                    .setMultiChoiceItems(hobby1, hobbyInChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            hobbyInChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton("送出", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String myHobby ="";
                            for(int i =0; i<hobbyInChecked.length; i++){
                                if(hobbyInChecked[i]){
                                    myHobby += " " + hobby1[i];
                                }
                            binding.rhobby.setText(myHobby);
                            }
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });

        //註冊
        binding.userregister.setOnClickListener(view -> {
            if(isVaildSignUpDetails())
                signUp();
        });

        binding.fr.setOnClickListener(v -> {
            Intent it = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(it);
        });

    }

    private void init(){
        getSupportActionBar().hide();// 隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
        mAuth = FirebaseAuth.getInstance();
        ImageView leftIcon = findViewById(R.id.lefticon);//自己做的導覽列
        leftIcon.setOnClickListener(view -> finish());  //上方返回鍵
        dialog = new Dialog(this);
    }

    private void signUp() {
        String ac = binding.raccount.getText().toString();
        String pwd = binding.rpassword.getText().toString();
        mAuth.createUserWithEmailAndPassword(ac, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful() && mAuth.getCurrentUser()!=null){
                            String UID = mAuth.getCurrentUser().getUid();
                            user.put(Constants.KEY_EMAIL,ac);
                            user.put(Constants.KEY_PASSWORD,pwd);
                            user.put(Constants.KEY_NAME,binding.rname.getText().toString());
                            user.put("phone",binding.rphone.getText().toString());
                            user.put("hobby",binding.rhobby.getText().toString());
                            user.put(Constants.KEY_IMAGE,encodedImage);

                            try{  // 用UID 當doc field
                                db.collection(Constants.KEY_COLLECTION_USERS)
                                        .document(UID)
                                        .set(user)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                opensuccessDialog();
                                            }
                                        });
                            }catch (Exception e) {}
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        switch (e.getMessage()) {
                            case "The email address is badly formatted.":
                                Toast.makeText(register.this, "Email格式錯誤", Toast.LENGTH_LONG).show();
                                break;
                            case "The given password is invalid. [ Password should be at least 6 characters ]":
                                Toast.makeText(register.this, "密碼長度過短", Toast.LENGTH_LONG).show();
                                break;
                            case "The email address is already in use by another account.":
                                Toast.makeText(register.this, "已有此用戶", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }

    @NonNull
    private Boolean isVaildSignUpDetails(){
       if (encodedImage == null) {
           showToast("Select profile image");
           return false;
       }else if(binding.rname.getText().toString().isEmpty()){
           showToast("Enter name");
           return false;
       }else if(binding.rphone.getText().toString().isEmpty()){
           showToast("Enter phone");
           return false;
        }else if(!Patterns.EMAIL_ADDRESS.matcher(binding.raccount.getText().toString()).matches()){
            showToast("Enter valid email");
            return false;
       }else if(binding.rpassword.getText().toString().isEmpty()){
           showToast("Enter password");
           return false;
       }else if(binding.rhobby.getText().toString().isEmpty()){
           showToast("Enter hobby");
           return false;
       }else {return true;}
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            binding.imageProfile.setImageBitmap(bitmap);
                            binding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodedImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }
    //註冊成功圖示
    private void opensuccessDialog(){
        dialog.setContentView(R.layout.success_register);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button get =dialog.findViewById(R.id.get);
        get.setOnClickListener(view -> finish());
        dialog.show();
    }
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}