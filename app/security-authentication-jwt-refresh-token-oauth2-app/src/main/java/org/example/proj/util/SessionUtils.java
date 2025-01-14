package org.example.proj.util;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;

/**
 * @author rival
 * @since 2025-01-03
 */
@Slf4j
public class SessionUtils {

    public static void invalidateSession(@Nullable HttpSession session){
        if(session!=null){
            String sessionId = session.getId();
            session.invalidate();
            log.info("Session(id={}) invalidated",sessionId);
        }
    }
}
