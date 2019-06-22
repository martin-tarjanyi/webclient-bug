package com.example.webclientbug;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.TcpClient;

import java.util.function.Consumer;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@SpringBootApplication
@RestController
public class WebclientBugApplication
{
    @Autowired
    private WebClient webClient;

    public static void main(String[] args)
    {
        SpringApplication.run(WebclientBugApplication.class, args);
    }

    @Bean
    public WebClient webClient()
    {
        Consumer<Connection> doOnConnectedConsumer = connection ->
                connection
//                        .addHandlerLast(new ReadTimeoutHandler(5000, MILLISECONDS))
                          .addHandlerLast(new WriteTimeoutHandler(5000, MILLISECONDS));

        TcpClient tcpClient = TcpClient.create()
                                       .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                       .doOnConnected(doOnConnectedConsumer);

        return WebClient.builder().clientConnector(new ReactorClientHttpConnector(HttpClient.from(tcpClient))).build();
    }

    @GetMapping("/get")
    public Mono<String> get()
    {
        return webClient.get()
                        .uri("https://jsonplaceholder.typicode.com/todos/1/?param=value")
                        .retrieve()
                        .bodyToMono(String.class);
    }
}
