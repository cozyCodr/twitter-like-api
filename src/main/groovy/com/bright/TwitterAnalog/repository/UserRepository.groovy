package com.bright.TwitterAnalog.repository

import com.bright.TwitterAnalog.model.User
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository extends MongoRepository<User, String>{
    Optional<User> findByUsername(String username);
}