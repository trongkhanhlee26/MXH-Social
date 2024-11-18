package fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social.Article;
import com.example.social.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.social.User;

import java.util.ArrayList;
import java.util.List;

import Adapter.ArticleAdapter;

public class CheckProfile extends AppCompatActivity {

    private ImageView userImageView;
    private TextView userNameTextView, userBirthdayTextView, userPostsTextView, userFollowersTextView, userFollowingTextView;
    private DatabaseReference userRef;
    private Button followButton;
    private FirebaseUser currentUser;
    private ImageButton backButton;
    private RecyclerView articlesRecyclerView;
    private ArticleAdapter articleAdapter;
    private List<Article> articleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_profile);

        userImageView = findViewById(R.id.userImage);
        userNameTextView = findViewById(R.id.textViewUserName);
        userBirthdayTextView = findViewById(R.id.textViewBirthday);
        userPostsTextView = findViewById(R.id.posts);
        userFollowersTextView = findViewById(R.id.followers);
        userFollowingTextView = findViewById(R.id.following);
        followButton = findViewById(R.id.followButton);
        backButton = findViewById(R.id.backButton);
        articlesRecyclerView = findViewById(R.id.Post);

        String userId = getIntent().getStringExtra("userId");

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        loadUserData();
        countFollow(userId);
        countPosts(userId);
        updateFollowStatus(userId);
        setupRecyclerView();
        loadUserArticles(userId);

        followButton.setOnClickListener(v -> {
            if (followButton.getText().toString().equals("Follow")) {
                followButton.setText("Following");
                FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUser.getUid())
                        .child("Following").child(userId).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(userId)
                        .child("Followers").child(currentUser.getUid()).setValue(true);
            } else {
                followButton.setText("Follow");
                FirebaseDatabase.getInstance().getReference().child("Follow").child(currentUser.getUid())
                        .child("Following").child(userId).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(userId)
                        .child("Followers").child(currentUser.getUid()).removeValue();
            }
        });
        backButton.setOnClickListener(v -> finish());
    }
    private void setupRecyclerView() {
        articleList = new ArrayList<>();
        articleAdapter = new ArticleAdapter(articleList, this);
        articlesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        articlesRecyclerView.setAdapter(articleAdapter);
    }
    private void loadUserArticles(String userId) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Articles");
        postsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                articleList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Article article = snapshot.getValue(Article.class);
                    articleList.add(article);
                }
                articleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void updateFollowStatus(String userId) {
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(currentUser.getUid()).child("Following");
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(userId).exists()) {
                    followButton.setText("Following");
                } else {
                    followButton.setText("Follow");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void loadUserData() {
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {

                        Glide.with(CheckProfile.this)
                                .load(user.getImage())
                                .placeholder(R.drawable.avatar)
                                .error(R.drawable.avatar)
                                .into(userImageView);

                        userNameTextView.setText(user.getFullName());

                        userBirthdayTextView.setText(user.getDateOfBirth());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database error
            }
        });
    }

    private void countFollow(String userId) {
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("Followers");
        followersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFollowersTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(userId).child("Following");
        followingRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userFollowingTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void countPosts(String userId) {
        DatabaseReference postsRef = FirebaseDatabase.getInstance().getReference().child("Articles");
        postsRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userPostsTextView.setText(String.valueOf(dataSnapshot.getChildrenCount()));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });
    }
}
