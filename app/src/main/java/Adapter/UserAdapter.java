package Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> implements Filterable {
    private Context mContext;
    private List<User> mListUsers;
    private List<User> mListUsersOld;
    private FirebaseUser firebaseUser;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(User user);
    }

    public UserAdapter(Context mContext, List<User> mListUsers, OnItemClickListener listener) {
        this.mContext = mContext;
        this.mListUsers = mListUsers;
        this.mListUsersOld = new ArrayList<>(mListUsers);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        User user = mListUsers.get(position);
        if (user == null) {
            return;
        }
        holder.btn_follow.setVisibility(View.VISIBLE);

        Glide.with(holder.itemView.getContext())
                .load(user.getImage())
                .placeholder(R.drawable.avatar)
                .error(R.drawable.avatar)
                .into(holder.imgUser);

        holder.tvName.setText(user.getFullName());
        isFollowing(user.getUserId(), holder.btn_follow);

        if (holder.tvStatus != null) {
            if (user.isOnlineStatus()) {
                holder.tvStatus.setText("Online");
            } else {
                holder.tvStatus.setText("Offline");
            }
        }

        if (user.getUserId().equals(firebaseUser.getUid())) {
            holder.btn_follow.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onItemClick(user);
            }
        });

        holder.btn_follow.setOnClickListener(v -> {
            if (holder.btn_follow.getText().toString().equals("Follow")) {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("Following").child(user.getUserId()).setValue(true);
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                        .child("Followers").child(firebaseUser.getUid()).setValue(true);
            } else {
                FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                        .child("Following").child(user.getUserId()).removeValue();
                FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getUserId())
                        .child("Followers").child(firebaseUser.getUid()).removeValue();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mListUsers != null ? mListUsers.size() : 0;
    }

    public void updateData(List<User> userList) {
        this.mListUsers = userList;
        this.mListUsersOld = new ArrayList<>(userList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String strSearch = constraint.toString();
                if (strSearch.isEmpty()) {
                    mListUsers = new ArrayList<>(mListUsersOld);
                } else {
                    List<User> list = new ArrayList<>();
                    for (User user : mListUsersOld) {
                        if (user.getFullName().toLowerCase().contains(strSearch.toLowerCase())) {
                            list.add(user);
                        }
                    }
                    mListUsers = list;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = mListUsers;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                mListUsers = (List<User>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    private void isFollowing(final String userId, final Button button) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Follow").child(firebaseUser.getUid()).child("Following");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(userId).exists()) {
                    button.setText("Following");
                } else {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class UserViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView imgUser;
        private TextView tvName;
        public TextView tvStatus;
        public Button btn_follow;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgUser = itemView.findViewById(R.id.userImage);
            tvName = itemView.findViewById(R.id.textUserName);
            tvStatus = itemView.findViewById(R.id.textUserStatus);
            btn_follow = itemView.findViewById(R.id.followButton);
        }
    }
}
