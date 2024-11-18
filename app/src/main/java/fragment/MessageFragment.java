package fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.R;
import com.example.social.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.ChatAdapter;

public class MessageFragment extends Fragment {
    private RecyclerView rcvChatUsers;
    private DatabaseReference usersRef;
    private FirebaseAuth firebaseAuth;
    private ChatAdapter chatAdapter;
    private List<String> followingList;
    private List<String> followersList;

    public static MessageFragment newInstance() {
        return new MessageFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();
        DatabaseReference followingRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseAuth.getUid()).child("Following");
        DatabaseReference followersRef = FirebaseDatabase.getInstance().getReference("Follow")
                .child(firebaseAuth.getUid()).child("Followers");

        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        rcvChatUsers = view.findViewById(R.id.rcv_chat_users);
        rcvChatUsers.setLayoutManager(new LinearLayoutManager(getContext()));

        followingList = new ArrayList<>();
        followersList = new ArrayList<>();

        followingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followingList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followingList.add(snapshot.getKey());
                }
                fetchFollowersList(followersRef);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void fetchFollowersList(DatabaseReference followersRef) {
        followersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                followersList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    followersList.add(snapshot.getKey());
                }
                fetchMutualFollowingUsers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
    }

    private void fetchMutualFollowingUsers() {
        if (followingList.isEmpty() || followersList.isEmpty()) {
            return;
        }

        List<String> mutualFollowingList = new ArrayList<>();
        for (String userId : followingList) {
            if (followersList.contains(userId)) {
                mutualFollowingList.add(userId);
            }
        }

        if (mutualFollowingList.isEmpty()) {
            return;
        }

        fetchUsers(mutualFollowingList);
    }

    private void fetchUsers(List<String> mutualFollowingList) {
        List<User> mutualUsers = new ArrayList<>();

        for (String userId : mutualFollowingList) {
            usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        mutualUsers.add(user);
                    }
                    // Update the adapter once all mutual users are fetched
                    if (mutualUsers.size() == mutualFollowingList.size()) {
                        chatAdapter = new ChatAdapter(getContext(), mutualUsers);
                        rcvChatUsers.setAdapter(chatAdapter);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle possible errors.
                }
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
    }

}
