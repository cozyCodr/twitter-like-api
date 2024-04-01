package com.bright.TwitterAnalog.model

import com.fasterxml.jackson.annotation.JsonIgnore
import groovy.transform.builder.Builder
import lombok.Getter
import lombok.Setter
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.DBRef
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "posts")
@Builder
@Getter
@Setter
class Post {

    @Id
    String id
    String content
    Set<Comment> comments = []
    Date createdAt;
    Date updatedAt;

    @DBRef
    @JsonIgnore
    Set<User> likes = []

    @DBRef
    User owner

    void addLike(User user){
        likes.add(user)
    }

    void removeLike(User user){
        likes.removeIf { it.id == user.getId() }
    }

    void addComment(Comment comment){
        comments.add(comment)
    }

    void removeComment(Comment comment){
        comments.removeIf {it.id == comment.getId()}
    }
}
