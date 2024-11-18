
package com.example.social;

import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Map;

public class Article {
    private String articleId;
    private String content;
    private String imageUrl;
    private long timestamp;
    private String userId;
    private Map<String, Boolean> likedBy;

    public Article() {
        likedBy = new HashMap<>();
    }
    public void addLike(String userId) {
        likedBy.put(userId, true);
    }

    public void removeLike(String userId) {
        likedBy.remove(userId);
    }

    public boolean isLikedByCurrentUser() {
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        return likedBy.containsKey(currentUserId);
    }

    public Article(String articleId, String content, String imageUrl, long timestamp, String userId) {
        this.articleId = articleId;
        this.content = content;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String, Boolean> getLikedBy() {
        return likedBy;
    }
    public void setLikedBy(Map<String, Boolean> likedBy) {
        this.likedBy = likedBy;
    }
    public int getLikeCount() {
        return likedBy.size();
    }
}
