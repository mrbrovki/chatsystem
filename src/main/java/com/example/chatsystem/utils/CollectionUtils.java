package com.example.chatsystem.utils;

import java.util.UUID;

public class CollectionUtils {
    public static String buildCollectionName(UUID id1, UUID id2){
        if(id2 == null){
            return id1.toString();
        }

        int result = id1.compareTo(id2);
        if(result < 0){
            return id1 + "&" + id2;
        }else if(result > 0){
            return id2 + "&" + id1;
        }else {
            return id1.toString();
        }
    }
}
