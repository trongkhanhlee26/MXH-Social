    package Adapter;

    import android.content.Context;
    import android.content.Intent;
    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import androidx.annotation.NonNull;
    import androidx.recyclerview.widget.RecyclerView;

    import com.bumptech.glide.Glide;
    import com.example.social.R;
    import com.example.social.User;

    import java.util.List;

    import de.hdodenhof.circleimageview.CircleImageView;
    import fragment.MessageDetail;

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private Context mContext;
        private List<User> mListUsers;

        public ChatAdapter(Context mContext, List<User> mListUsers) {
            this.mContext = mContext;
            this.mListUsers = mListUsers;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            User user = mListUsers.get(position);
            if (user == null) {
                return;
            }
            holder.tvName.setText(user.getFullName());
            if (user.isOnlineStatus()) {
                holder.tvStatus.setText("Online");
            } else {
                holder.tvStatus.setText("Offline");
            }
            // Hiển thị hình ảnh người dùng
            Glide.with(mContext)
                    .load(user.getImage())
                    .placeholder(R.drawable.avatar) // Hình mặc định khi tải ảnh
                    .error(R.drawable.avatar) // Hình mặc định khi có lỗi tải ảnh
                    .into(holder.imgUser);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext, MessageDetail.class);
                    intent.putExtra("userId", user.getUserId());
                    intent.putExtra("fullName", user.getFullName());
                    intent.putExtra("image", user.getImage());
                    intent.putExtra("onlineStatus", user.isOnlineStatus());
                    mContext.startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mListUsers.size();
        }

        public void updateData(List<User> userList) {
            mListUsers = userList;
            notifyDataSetChanged();
        }

        public class ChatViewHolder extends RecyclerView.ViewHolder {
            private CircleImageView imgUser;
            private TextView tvName;
            private TextView tvStatus;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                imgUser = itemView.findViewById(R.id.userImage);
                tvName = itemView.findViewById(R.id.textUserName);
                tvStatus = itemView.findViewById(R.id.textUserStatus);
            }
        }
    }
