package org.example.proj.util;

import jakarta.servlet.Filter;
import org.example.proj.security.JwtAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;

import java.util.List;

/**
 * @author rival
 * @since 2025-01-03
 */
public class FilterUtils {

    public static void reorderFilters(List<Filter> filters){
        LogoutFilter logoutFilter = null;

        for (int i = 0; i < filters.size(); i++) {
            if (filters.get(i) instanceof LogoutFilter) {
                logoutFilter = (LogoutFilter) filters.remove(i);
                break;
            }
        }

        if (logoutFilter != null) {
            for (int i = 0; i < filters.size(); i++) {
                if (filters.get(i) instanceof JwtAuthenticationFilter) {
                    filters.add(i + 1, logoutFilter);
                    break;
                }
            }
        }
    }
}
