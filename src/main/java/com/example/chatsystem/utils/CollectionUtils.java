package com.example.chatsystem.utils;

import com.example.chatsystem.model.ChatType;
import org.bson.types.ObjectId;

public class CollectionUtils {
    public static String buildCollectionName(ObjectId id1, ObjectId id2, ChatType chatType){
        String collectionName = chatType + "_";
        if(id2 == null){
            return collectionName + id1.toHexString();
        }
        if(id1.getTimestamp() < id2.getTimestamp()){
            collectionName += id1 + "&" + id2;
        }else if(id1.getTimestamp() > id2.getTimestamp()){
            collectionName += id2 + "&" + id1;
        }else{
            int result = id1.compareTo(id2);
            if(result < 0){
                collectionName += id1 + "&" + id2;
            }else if(result > 0){
                collectionName += id2 + "&" + id1;
            }else{
                collectionName += id1;
            }
        }
        return collectionName;
    }
}
