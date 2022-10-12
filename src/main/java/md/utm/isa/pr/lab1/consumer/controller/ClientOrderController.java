package md.utm.isa.pr.lab1.consumer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
@Slf4j
public class ClientOrderController {
    @PostMapping("/order")
    public String post(@RequestBody OrderDto order) {
        log.info("Received order {}. Timestamp: {}", order, System.currentTimeMillis() - order.getPickUpTime());

        order.setReceiveTime(System.currentTimeMillis());

        return String.format("Received new order {%s}", order.toString());
    }
}
