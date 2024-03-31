package com.bright.TwitterAnalog.repository

import com.bright.TwitterAnalog.model.Post
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface PostRepository extends MongoRepository<Post, String>{

}