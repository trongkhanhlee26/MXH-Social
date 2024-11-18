package com.example.social;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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

public class EditArticleActivity extends AppCompatActivity {
    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText editTextContent;
    private ImageView imageView, backButton;
    private Button buttonSave, buttonChooseImage;
    private String articleId;
    private DatabaseReference articleRef;
    private Uri imageUri;
    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_article);

        editTextContent = findViewById(R.id.edit_article_content);
        imageView = findViewById(R.id.edit_article_image);
        buttonSave = findViewById(R.id.button_save);
        buttonChooseImage = findViewById(R.id.button_choose_image);
        backButton = findViewById(R.id.backButton);

        // Kiểm tra Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("articleId")) {
            articleId = intent.getStringExtra("articleId");
        } else {
            Toast.makeText(this, "Article ID not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Kiểm tra articleId
        if (articleId == null || articleId.isEmpty()) {
            Toast.makeText(this, "Invalid Article ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        articleRef = FirebaseDatabase.getInstance().getReference("Articles").child(articleId);

        loadArticleData();

        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveArticle();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this).load(imageUri).into(imageView);
        }
    }

    private void loadArticleData() {
        articleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Article article = dataSnapshot.getValue(Article.class);
                if (article != null) {
                    editTextContent.setText(article.getContent());
                    imageUrl = article.getImageUrl();
                    if (!TextUtils.isEmpty(imageUrl)) {
                        Glide.with(EditArticleActivity.this).load(imageUrl).into(imageView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(EditArticleActivity.this, "Failed to load article data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveArticle() {
        String content = editTextContent.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            editTextContent.setError("Content cannot be empty");
            return;
        }

        if (imageUri != null) {
            uploadImageAndSaveArticle(content);
        } else {
            updateArticle(content, imageUrl);
        }
    }

    private void uploadImageAndSaveArticle(String content) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("article_images").child(articleId);
        storageReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        updateArticle(content, uri.toString());
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EditArticleActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateArticle(String content, String imageUrl) {
        articleRef.child("content").setValue(content);
        articleRef.child("imageUrl").setValue(imageUrl);
        Toast.makeText(this, "Article updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
