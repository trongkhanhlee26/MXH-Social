package fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.social.R;
import com.example.social.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import Adapter.UserAdapter;

public class SearchFragment extends Fragment {
    private RecyclerView rcvUsers;
    private EditText etSearch;
    private UserAdapter userAdapter;
    private List<User> userList;
    private UserAdapter.OnItemClickListener listener;

    private DatabaseReference databaseReference;
    private DatabaseReference onlineStatusRef;

    public static SearchFragment newInstance() {
        return new SearchFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        rcvUsers = view.findViewById(R.id.rcv_users);
        etSearch = view.findViewById(R.id.et_search);
        userList = new ArrayList<>();

        listener = user -> {
            Intent intent = new Intent(getContext(), CheckProfile.class);
            intent.putExtra("userId", user.getUserId());
            startActivity(intent);
        };

        userAdapter = new UserAdapter(getContext(), userList, listener);
        rcvUsers.setAdapter(userAdapter);
        rcvUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvUsers.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        onlineStatusRef = FirebaseDatabase.getInstance().getReference("OnlineStatus");

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });

        getListUsers();
//        userAdapter = new UserAdapter(getContext(), userList, user -> {
//            Intent intent = new Intent(getContext(), CheckProfile.class);
//            intent.putExtra("userId", user.getUserId());
//            startActivity(intent);
//        });
    }
    private void getListUsers() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        String currentUserId = currentUser.getUid();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null && user.getUserId() != null && !user.getUserId().equals(currentUserId)) {
                        userList.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        onlineStatusRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (User user : userList) {
                    Boolean onlineStatus = dataSnapshot.child(user.getUserId()).getValue(Boolean.class);
                    if (onlineStatus != null) {
                        user.setOnlineStatus(onlineStatus);
                    }
                }
                userAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void filter(String text) {
        List<User> filteredList = new ArrayList<>();
        for (User user : userList) {
            if (user.getFullName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(user);
            }
        }
        userAdapter.updateData(filteredList);
    }
}
