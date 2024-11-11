package com.example.inus.Activity.Setting;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.inus.R;
import com.example.inus.databinding.ActivityPersonalInfoBinding;
import com.example.inus.util.Constants;
import com.example.inus.util.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Base64;

public class personal_info extends AppCompatActivity {

    private ActivityPersonalInfoBinding binding;
    private PreferenceManager preferenceManager;
    private String[] hobby1 ={"美妝保養","教育學習","居家婦幼","醫療保健","視聽娛樂","流行服飾","旅遊休閒","3C娛樂","人氣美食"};
    private boolean[] hobbyInChecked={false,false,false,false,false,false,false,false,false};
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String encodedImage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();

        db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID).get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        DocumentSnapshot doc = task.getResult();
                        binding.rname.setText("" +doc.get("name"));
                        binding.rphone.setText(""+ doc.get("phone"));
                        binding.rhobby.setText("" + doc.get("hobby"));
                    }
                });

        binding.rhobby.setOnClickListener(view -> {
            new AlertDialog.Builder(personal_info.this)
                    .setMultiChoiceItems(hobby1, hobbyInChecked, new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            hobbyInChecked[which] = isChecked;
                        }
                    })
                    .setPositiveButton("送出", (dialog , which) -> {
                        String myHobby ="";
                        for(int i =0; i<hobbyInChecked.length; i++){
                            if(hobbyInChecked[i]){
                                myHobby += " " + hobby1[i];
                            }
                            binding.rhobby.setText(myHobby);
                        }
                    })
                    .setNegativeButton("取消", null)
                    .show();
        });
        binding.fr.setOnClickListener(v -> {
            Intent it = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            it.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            pickImage.launch(it);
        });

        binding.btnReInfo.setOnClickListener(view -> {
            DocumentReference doc =  db.collection(Constants.KEY_COLLECTION_USERS).document(Constants.UID);
            doc.update(Constants.KEY_NAME, binding.rname.getText().toString());
            doc.update("phone",binding.rphone.getText().toString());
            doc.update("hobby",binding.rhobby.getText().toString());
            doc.update(Constants.KEY_IMAGE,encodedImage);
            Toast.makeText(getApplicationContext(), "已更新個資", Toast.LENGTH_SHORT).show();
            finish();
            });
    }

    private void init(){
        getSupportActionBar().hide();// 隱藏上方導覽列
        getWindow().setStatusBarColor(this.getResources().getColor(R.color.black));//狀態列顏色
        ImageView leftIcon = findViewById(R.id.lefticon);//自己做的導覽列
        leftIcon.setOnClickListener(view -> finish());  //上方返回鍵
        preferenceManager = new PreferenceManager(getApplicationContext());

        try {
            byte[] bytes = Base64.getDecoder().decode(preferenceManager.getString(Constants.KEY_IMAGE));
            if(bytes != null){
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                binding.imageProfile.setImageBitmap(bitmap);
            }else {
                binding.imageProfile.setImageDrawable(getDrawable(R.drawable.person_white));
            }
        }catch (NullPointerException e){
            Log.d("demoErr",e.getMessage());
        }
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
}