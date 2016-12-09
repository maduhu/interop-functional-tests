package com.l1.interop.business_processes.full_payment_with_notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.*;
import java.io.IOException;

@ClientEndpoint
public class WebsocketClientEndpoint {
	
  private static final Logger log =  LoggerFactory.getLogger(WebsocketClientEndpoint.class);
  private String outMessage;
  private String account;
  
  public WebsocketClientEndpoint(String outMessage, String account) {
	  this.outMessage = outMessage;
	  this.account = account;
  }

  @OnOpen
  public void onOpen(Session session) {
    System.out.println("Connected to endpoint: " + session.getBasicRemote());
    try {
      String subscriptionRequest = "{   \"jsonrpc\": \"2.0\",   \"method\": \"subscribe_account\",   \"params\": {     \"accounts\": [       \"" + account + "\"     ]   },   \"id\": 1 }";
      log.info("Sending message to endpoint: {}", subscriptionRequest);
      session.getBasicRemote().sendText(subscriptionRequest);
    } catch (IOException ex) {
      log.error("error", ex);
    }
  }

  @OnMessage
  public void processMessage(String message) {
    System.out.println("Received message in client: " + message);
    this.outMessage = message;
  }

  @OnError
  public void processError(Throwable t) {
    t.printStackTrace();
  }
  
  
}