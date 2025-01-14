package org.example.proj;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author rival
 * @since 2024-12-10
 */


@RestController
@RequestMapping("session")
public class SessionController {


    @GetMapping("/id")
    public Map<String,String> getSessionId(HttpSession httpSession){
        Map<String,String> body = new HashMap<>();
        body.put("sessionId", httpSession.getId());

        return body;
    }

    @GetMapping("/save")
    public void setDataToSession(HttpSession httpSession){
        httpSession.setAttribute("msg", "Hello World!");
    }

    @GetMapping("/read")
    public Object readAndRemoveFromSession(HttpSession httpSession){
        Object attribute = httpSession.getAttribute("msg");
        if(attribute == null){
            return "Cannot Read";
        }

        httpSession.removeAttribute("msg");
        return attribute;
    }


    // remove entire session
    @GetMapping("/delete")
    public String deleteSession(HttpSession httpSession){
        String sessionId = httpSession.getId();
        httpSession.invalidate();
        return "Delete Session : "+sessionId;
    }
}
