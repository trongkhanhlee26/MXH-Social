package Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.social.Article;
import com.example.social.EditArticleActivity;
import com.example.social.R;
import com.example.social.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.List;

import fragment.Comments;

public class MyPostAdapter extends RecyclerView.Adapter<MyPostAdapter.ArticleViewHolder> {
    private List<Article> articles;
    private Context mContext;

    public MyPostAdapter(List<Article> articles, Context context) {
        this.articles = articles;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_article, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ArticleViewHolder holder, int position) {
        Article article = articles.get(position);
        holder.content.setText(article.getContent());
        holder.timestamp.setText(new Date(article.getTimestamp()).toString());
        holder.likeCount.setText(String.valueOf(article.getLikedBy().size()));
        holder.likeButton.setImageResource(article.isLikedByCurrentUser() ? R.drawable.traitim_datim : R.drawable.trai_tim);
        if (article.getImageUrl() == null || article.getImageUrl().isEmpty()) {
            holder.image.setVisibility(View.GONE);
        } else {
            holder.image.setVisibility(View.VISIBLE);
            Glide.with(mContext).load(article.getImageUrl()).into(holder.image);
        }

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(article.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    holder.userName.setText(user.getFullName());
                    Glide.with(mContext).load(user.getImage()).placeholder(R.drawable.avatar)
                            .error(R.drawable.avatar).into(holder.avatar);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        holder.likeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Article article = articles.get(position);
                    String currentUserId = getCurrentUserId();

                    // Kiểm tra xem tài khoản hiện tại đã like bài viết này chưa
                    boolean isLikedByCurrentUser = article.isLikedByCurrentUser();

                    // Nếu tài khoản đã like bài viết này, chuyển trạng thái về unlike
                    if (isLikedByCurrentUser) {
                        article.removeLike(currentUserId);
                    } else {
                        // Ngược lại, thêm like của tài khoản hiện tại cho bài viết
                        article.addLike(currentUserId);
                    }

                    // Cập nhật giao diện người dùng
                    holder.likeButton.setImageResource(article.isLikedByCurrentUser() ? R.drawable.traitim_datim : R.drawable.trai_tim);
                    holder.likeCount.setText(String.valueOf(article.getLikedBy().size())); // Sử dụng số lượng userId trong likedBy để đếm số lượt thích

                    // Cập nhật trạng thái like của bài viết trong Firebase Realtime Database
                    DatabaseReference articleRef = FirebaseDatabase.getInstance().getReference("Articles").child(article.getArticleId());
                    articleRef.child("likedBy").setValue(article.getLikedBy()); // Cập nhật danh sách các người đã thích bài viết
                }
            }
        });

        holder.commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Article article = articles.get(position);
                    // Tạo intent để chuyển sang CommentsActivity
                    Intent intent = new Intent(mContext, Comments.class);
                    // Đưa articleId qua CommentsActivity
                    intent.putExtra("articleId", article.getArticleId());
                    mContext.startActivity(intent);
                }
            }
        });

        holder.optionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {
        TextView content, timestamp, userName, likeCount;
        ImageView image, avatar, likeButton, commentButton, optionButton;

        public ArticleViewHolder(@NonNull View itemView) {
            super(itemView);
            content = itemView.findViewById(R.id.article_content);
            timestamp = itemView.findViewById(R.id.article_timestamp);
            image = itemView.findViewById(R.id.article_image);
            avatar = itemView.findViewById(R.id.article_avatar);
            userName = itemView.findViewById(R.id.article_user_name);
            likeButton = itemView.findViewById(R.id.article_like);
            likeCount = itemView.findViewById(R.id.article_like_count);
            commentButton = itemView.findViewById(R.id.article_comment);
            optionButton = itemView.findViewById(R.id.article_options);
        }
    }

    private void showPopupMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(mContext, view);
        popupMenu.inflate(R.menu.article_option);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_edit) {
                    // Xử lý chức năng chỉnh sửa
                    Article article = articles.get(position);
                    Intent intent = new Intent(mContext, EditArticleActivity.class);
                    intent.putExtra("articleId", article.getArticleId());
                    mContext.startActivity(intent);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    // Xử lý chức năng xóa
                    Toast.makeText(mContext, "Post Deleted", Toast.LENGTH_SHORT).show();
                    Article article = articles.get(position);
                    articles.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, articles.size());
                    // Cập nhật trạng thái xóa của bài viết trong Firebase Realtime Database
                    DatabaseReference articleRef = FirebaseDatabase.getInstance().getReference("Articles").child(article.getArticleId());
                    articleRef.removeValue();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }


    private String getCurrentUserId() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
}
