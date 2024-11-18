package fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.Comment;
import Adapter.CommentAdapter;
import com.example.social.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Comments extends AppCompatActivity {
    private RecyclerView recyclerView;
    private CommentAdapter adapter;
    private List<Comment> commentList;
    private EditText editTextComment;
    private Button buttonPostComment;
    private ImageButton backButton;
    private String articleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        if (getIntent().hasExtra("articleId")) {
            articleId = getIntent().getStringExtra("articleId");
        } else {
            Toast.makeText(this, "No article found", Toast.LENGTH_SHORT).show();
            finish();
        }
        backButton = findViewById(R.id.backButton);
        recyclerView = findViewById(R.id.recyclerViewComments);
        editTextComment = findViewById(R.id.editTextComment);
        buttonPostComment = findViewById(R.id.buttonPostComment);

        commentList = new ArrayList<>();
        adapter = new CommentAdapter(commentList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        buttonPostComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment();
            }
        });
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        loadComments();
    }

    private void loadComments() {
        DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Articles").child(articleId).child("Comments");
        commentsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Comment comment = dataSnapshot.getValue(Comment.class);
                if (comment != null) {
                    commentList.add(comment);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {}

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }

    private void postComment() {
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String content = editTextComment.getText().toString().trim().toLowerCase();
        if (!content.isEmpty()) {
            DatabaseReference commentsRef = FirebaseDatabase.getInstance().getReference().child("Articles").child(articleId).child("Comments");
            String commentId = commentsRef.push().getKey();
            long timestamp = System.currentTimeMillis();
            Comment comment = new Comment(commentId, userId, content, timestamp);
            commentsRef.child(commentId).setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        editTextComment.setText("");
                        Toast.makeText(Comments.this, "Comment posted successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(Comments.this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please enter your comment", Toast.LENGTH_SHORT).show();
        }
    }
}
