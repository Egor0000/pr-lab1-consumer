package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.entity.Cook;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
@Slf4j
public class KitchenServiceImpl implements KitchenService {
    private ConcurrentLinkedQueue<OrderDto> orderList = new ConcurrentLinkedQueue<>();
    @Value("${producer.address}")
    private String address;

    @Value("${producer.port}")
    private Integer port;

    private String path  = "/distribution/";

    private WebClient webClient;

    public KitchenServiceImpl() {

    }


    @PostConstruct
    private void onInit() {
        try {
            URI uri = new URI("http", null, address, port, null, null, null);
            URL url = uri.toURL();

            webClient = WebClient.builder()
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .defaultUriVariables(Collections.singletonMap("url", url))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postOrder(OrderDto orderDto) {
        orderList.add(orderDto);
    }

    @Override
    public OrderDto getNextOrder() {
        return orderList.poll();
    }

    @Override
    public String postPreparedOrder(PreparedOrderDto preparedOrderDto) {
        if (webClient!=null) {
            log.info("Send prepared order {}", preparedOrderDto);
            Mono<String> response = webClient.post()
                    .uri(String.format("%s:%s%s", address, port, path))
                    .body(BodyInserters.fromValue(preparedOrderDto))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class);

            return response.block();
        }
        return null;
    }

}
