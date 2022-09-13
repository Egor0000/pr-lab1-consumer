package md.utm.isa.pr.lab1.consumer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
@Slf4j
public class OrderController {
    private final KitchenService kitchenService;

    @PostMapping("/")
    public String post(@RequestBody OrderDto order) {
        log.info("Received order {}", order);

        kitchenService.postOrder(order);

        return String.format("Received new order {%s}", order.toString());
    }
}
