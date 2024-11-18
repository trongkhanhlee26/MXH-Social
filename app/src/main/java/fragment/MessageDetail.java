package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social.Messages;
import com.example.social.R;
import com.example.social.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import Adapter.MessagesAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

public class MessageDetail extends AppCompatActivity {
    private ImageButton backButton;
    private EditText typeMessage;
    private Button sendMessageButton;
    private RecyclerView rcvMessage;
    private CircleImageView userImage;
    private TextView textUserName, textUserStatus;

    String enterMessage;
    String senderId, senderName, receiverId, receiverName;
    String sender, receiver;
    private FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    String currentTime;
    Calendar calendar;
    SimpleDateFormat simpleDateFormat;
    Intent intent;
    MessagesAdapter messagesAdapter;
    ArrayList<Messages> messagesArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_message_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize views
        backButton = findViewById(R.id.backButton);
        rcvMessage = findViewById(R.id.rcvMessage);
        typeMessage = findViewById(R.id.typeMessage);
        sendMessageButton = findViewById(R.id.sendMessageButton);
        textUserName = findViewById(R.id.textUserName);
        textUserStatus = findViewById(R.id.textUserStatus);
        userImage = findViewById(R.id.userImage);

        // Initialize ArrayList and Adapter
        messagesArrayList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messagesArrayList);

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        rcvMessage.setLayoutManager(linearLayoutManager);
        rcvMessage.setAdapter(messagesAdapter);

        // Get Intent data
        intent = getIntent();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("hh:mm a");

        backButton.setOnClickListener(v -> {setResult(RESULT_OK);
        finish();});

        if (intent != null) {
            senderId = firebaseAuth.getUid();
            receiverId = intent.getStringExtra("userId");
            receiverName = intent.getStringExtra("fullName");

            sender = senderId + receiverId;
            receiver = receiverId + senderId;

            String imageUri = intent.getStringExtra("image");
            boolean onlineStatus = intent.getBooleanExtra("onlineStatus", false);
            textUserName.setText(receiverName);
            textUserStatus.setText(onlineStatus ? "Online" : "Offline");
            Glide.with(this)
                    .load(imageUri)
                    .placeholder(R.drawable.avatar)
                    .error(R.drawable.avatar)
                    .into(userImage);

        }

        DatabaseReference databaseReference = firebaseDatabase.getReference().child("Chats").child(sender).child("Messages");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesArrayList.clear();
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Messages messages = snapshot1.getValue(Messages.class);
                    messagesArrayList.add(messages);
                }
                messagesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enterMessage = typeMessage.getText().toString().trim();
                if (enterMessage.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Please Type Message", Toast.LENGTH_SHORT).show();
                } else {
                    // Di chuyển phần lấy thời gian vào trong đây
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm a");
                    String currentTime = simpleDateFormat.format(calendar.getTime());

                    Date date = new Date();
                    Messages messages = new Messages(enterMessage, firebaseAuth.getUid(), date.getTime(), currentTime);

                    firebaseDatabase = FirebaseDatabase.getInstance();
                    DatabaseReference senderRef = firebaseDatabase.getReference().child("Chats").child(sender).child("Messages");
                    DatabaseReference receiverRef = firebaseDatabase.getReference().child("Chats").child(receiver).child("Messages");

                    senderRef.push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                receiverRef.push().setValue(messages).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            // Clear the input field
                                            typeMessage.setText(null);
                                            rcvMessage.scrollToPosition(messagesArrayList.size() - 1);
                                        } else {
                                            // Handle failure to send to receiver's chat
                                        }
                                    }
                                });
                            } else {
                                // Handle failure to send to sender's chat
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
        public void onStart(){
            super.onStart();
            messagesAdapter.notifyDataSetChanged();
        }

        @Override
        public void onStop(){
            super.onStop();
            if(messagesAdapter != null){
                messagesAdapter.notifyDataSetChanged();
            }
        }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Gửi kết quả trả về cho MessageFragment
        setResult(RESULT_OK);
        finish();
    }
    }