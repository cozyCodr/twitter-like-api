package com.bright.TwitterAnalog.model

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.builder.Builder
import lombok.AllArgsConstructor
import lombok.Getter
import lombok.NoArgsConstructor
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "users")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
class User {
    @Id
    String id
    String username
    String firstname
    String lastname

    @JsonIgnore
    String password

    // First Permission is that users role
    @JsonIgnore
    Set<String> permissions = []

    Date createdAt
    Date updatedAt

    @DBRef
    @JsonIgnore
    Set<Post> posts = []

    @DBRef
    @JsonIgnore
    Set<Post> favoritePosts = []

    @DBRef
    @JsonIgnore
    Set<User> following = []

    @DBRef
    @JsonIgnore
    Set<User> followers = []

    void addPost(Post post){
        if(posts == null){
            posts = new HashSet<>()
        }
        posts.add(post)
    }

    void removePost(Post post){
        posts.removeIf {it.id == post.getId() }
    }

    void addNewFollower(User user){
        followers.add(user)
    }

    void removeFollower(User user){
        followers.removeIf {it.id == user.getId() }
    }

    void addFollowing(User user){
        following.add(user)
    }

    void removeFollowing(User user){
        following.removeIf {it.id == user.getId() }
    }

    void addFavorite(Post post){
        favoritePosts.add(post)
    }

    void removeFavorite(Post post){
        favoritePosts.removeIf { it.id == post.getId() }
    }

    void addPermission(String permission){
        if (permissions == null){
            permissions = new HashSet<>()
        }
        permissions.add(permission)
    }

}