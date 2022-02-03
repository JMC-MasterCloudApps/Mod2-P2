package es.urjc.code.daw.library;

import javax.net.ssl.SSLException;

import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.web.reactive.server.WebTestClient;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.netty.http.client.HttpClient;

@Configuration
@Lazy(true)
public class TestConfiguration {

    @LocalServerPort
    int port;

    @Bean
    public WebTestClient createWebClient() throws SSLException {

        SslContext sslContext = SslContextBuilder
                .forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE)
                .build();

        HttpClient httpClient = HttpClient.create()
                .secure(sslSpec -> sslSpec.sslContext(sslContext))
                .baseUrl("https://localhost:" + port);

        ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);

        return WebTestClient
            .bindToServer(connector)
            .build();
    }
    
}
