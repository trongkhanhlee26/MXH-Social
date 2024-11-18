package fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.social.HomePageActivity;
import com.example.social.MainActivity;
import com.example.social.RegisterActivity;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.social.R;
import com.example.social.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.bumptech.glide.Glide;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public class MyprofileFragment extends Fragment {
    // Constants
    private static final int PICK_IMAGE_REQUEST = 71;
    // Views
    private TextView textViewUserName, textViewEmail, textViewBirthday, posts, followers, following;
    FirebaseUser firebaseUser;
    private Button changeAvtBtn, changeInfoBtn, myPostBtn;
    private ImageView userImage;


    public MyprofileFragment() {
        // Required empty public constructor
    }

    public static MyprofileFragment newInstance() {
        return new MyprofileFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_myprofile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        userImage = view.findViewById(R.id.userImage);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        textViewUserName = view.findViewById(R.id.textViewUserName);
        textViewEmail = view.findViewById(R.id.textViewEmail);
        textViewBirthday = view.findViewById(R.id.textViewBirthday);
        changeAvtBtn = view.findViewById(R.id.changeAvtBtn);
        changeInfoBtn = view.findViewById(R.id.changeInfoBtn);
        myPostBtn = view.findViewById(R.id.myPostBtn);

        changeAvtBtn.setOnClickListener(v -> openGallery());

        loadUserProfile();
        countFollow();
        countPosts();

        changeInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ChangeUserInfo.class);
                startActivity(intent);
            }
        });

        myPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), MyPost.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
        countFollow();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");
        if (galleryIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST);
        } else {
            Toast.makeText(requireContext(), "No application found to open photo gallery", Toast.LENGTH_SHORT).show(); //Không tìm thấy ứng dụng để mở thư viện ảnh
        }
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
            userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String fullName = dataSnapshot.child("fullName").getValue(String.class);
                        String email = dataSnapshot.child("email").getValue(String.class);
                        String dateOfBirth = dataSnapshot.child("dateOfBirth").getValue(String.class);
                        textViewUserName.setText(fullName);
                        textViewEmail.setText(email);
                        textViewBirthday.setText(dateOfBirth);

                        String image = dataSnapshot.child("image").getValue(String.class);
                        if (image != null && !image.isEmpty()) {
                            // Check if the image is a URL or a Base64 string
                            if (image.startsWith("http")) {
                                // Load image from URL
                                Glide.with(requireContext()).load(image).into(userImage);
                            } else {
                                // Decode the Base64 string to a bitmap and set it to the ImageView
                                try {
                                    byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                    userImage.setImageBitmap(decodedByte);
                                } catch (IllegalArgumentException e) {
                                    Toast.makeText(requireContext(), "Invalid image format", Toast.LENGTH_SHORT).show();
                                    userImage.setImageResource(R.drawable.avatar); // Set default avatar in case of error
                                }
                            }
                        } else {
                            // If there is no image, set the default avatar
                            userImage.setImageResource(R.drawable.avatar);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle the error if any
                    Toast.makeText(requireContext(), "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("avatars/" + UUID.randomUUID().toString());
        storageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userId = currentUser.getUid();
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
                        userRef.child("image").setValue(imageUrl);
                    }
                }))
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Lỗi khi tải ảnh lên: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            Glide.with(requireContext()).load(imageUri).into(userImage);
            uploadImageToFirebaseStorage(imageUri);
        }
    }
    private void countFollow() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("Followers");
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followers.setText(""+dataSnapshot.getChildrenCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("Following");
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                following.setText(""+dataSnapshot.getChildrenCount());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void countPosts() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userId = currentUser.getUid();
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Articles");
        postsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int count = (int) dataSnapshot.getChildrenCount();
                posts.setText("" + count);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

}
