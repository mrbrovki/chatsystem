package com.example.chatsystem.repository;

import com.example.chatsystem.model.Info;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InfoRepository extends MongoRepository<Info, ObjectId> {
    List<Info> findAllBySenderNameEquals(String infoType);
}
