package com.github.mohammedismaiel.websocket.app.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.github.mohammedismaiel.websocket.app.model.LoginDto;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;

@AllArgsConstructor
@Service
@Log4j2
public class RabbitListenerService {
    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = "test", messageConverter = "jsonConverter")
    public void receiveMessage(LoginDto payload) {
        if (payload != null) {
            System.out.println("Received message: " + payload.toString());
            messagingTemplate.convertAndSendToUser(payload.username(),
                    "/queue/messages", "Your pass is : %s".formatted(payload.password()));
        }
    }

}