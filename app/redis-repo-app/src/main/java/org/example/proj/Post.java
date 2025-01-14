package org.example.proj;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

/**
 * @author rival
 * @since 2025-01-13
 */



@RedisHash(value="Post", timeToLive = 60*5)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {

    @Id
    private String id;


    private String title;
    private String content;

    private long count;

}
