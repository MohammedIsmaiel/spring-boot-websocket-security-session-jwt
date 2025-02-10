package com.github.mohammedismaiel.websocket.app.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Controller
@AllArgsConstructor
@Log4j2
public class AppController {
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping("/user/{username}")
    @ResponseBody
    public ResponseEntity<Void> sendMessageToUser(@PathVariable String username,
            @RequestBody String message, Principal principal) {
        log.info("sendin: {} to: {}", message, username);
        messagingTemplate.convertAndSendToUser(username, "/queue/messages", message + "for :" + username);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public String test() {
        return "home";
    }

    @MessageExceptionHandler
    @SendToUser(destinations = "/queue/errors", broadcast = false)
    public String handleException(Exception exception) {
        log.error(exception.getMessage());
        return exception.getMessage();
    }
}
