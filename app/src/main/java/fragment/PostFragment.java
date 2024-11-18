package fragment;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.social.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

public class PostFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText editTextContent;
    private ImageView imageView;
    private Uri imageUri;
    private ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private FirebaseStorage storage;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Articles"); // Sửa đổi tên cơ sở dữ liệu thành "Articles"
        storage = FirebaseStorage.getInstance();

        editTextContent = view.findViewById(R.id.editTextContent);
        imageView = view.findViewById(R.id.imageView);
        Button buttonPost = view.findViewById(R.id.buttonPost);
        progressDialog = new ProgressDialog(getContext());

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == AppCompatActivity.RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        imageView.setImageURI(imageUri);
                    }
                }
        );

        imageView.setOnClickListener(v -> openFileChooser());
        buttonPost.setOnClickListener(v -> postArticle());

        return view;
    }

    private void openFileChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void postArticle() {
        String content = editTextContent.getText().toString().trim();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (content.isEmpty()) {
            Toast.makeText(getContext(), "Content cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Posting...");
        progressDialog.show();

        if (imageUri != null) {
            uploadImage(content, user.getUid());
        } else {
            savePostToDatabase(content, "", user.getUid());
        }
    }

    private void uploadImage(final String content, final String userId) {
        StorageReference storageRef = storage.getReference("images/" + System.currentTimeMillis() + ".jpg");
        storageRef.putFile(imageUri)
                .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            storageRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                @Override
                                public void onComplete(@NonNull Task<Uri> task) {
                                    if (task.isSuccessful()) {
                                        String imageUrl = task.getResult().toString();
                                        savePostToDatabase(content, imageUrl, userId);
                                    } else {
                                        progressDialog.dismiss();
                                        Toast.makeText(getContext(), "Failed to get download URL", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Image upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void savePostToDatabase(String content, String imageUrl, String userId) {
        String postId = databaseReference.push().getKey();
        Map<String, Object> articleData = new HashMap<>();
        articleData.put("articleId", postId); // Thêm articleId vào dữ liệu bài viết
        articleData.put("content", content);
        articleData.put("imageUrl", imageUrl);
        articleData.put("userId", userId);
        articleData.put("timestamp", System.currentTimeMillis());
        articleData.put("likedBy", new HashMap<String, Boolean>());

        if (postId != null) {
            databaseReference.child(postId).setValue(articleData)
                    .addOnCompleteListener(task -> {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Post published", Toast.LENGTH_SHORT).show();
                            editTextContent.setText("");
                            imageView.setImageResource(android.R.color.transparent);
                            imageUri = null;
                        } else {
                            Toast.makeText(getContext(), "Failed to publish post", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            progressDialog.dismiss();
            Toast.makeText(getContext(), "Failed to get post ID", Toast.LENGTH_SHORT).show();
        }
    }
}
