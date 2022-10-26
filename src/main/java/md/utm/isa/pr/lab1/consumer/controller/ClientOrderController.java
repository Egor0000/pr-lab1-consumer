package md.utm.isa.pr.lab1.consumer.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.ResponseOrderDto;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v2")
@RequiredArgsConstructor
@Slf4j
public class ClientOrderController {
    private final KitchenService kitchenService;

    @PostMapping(value = "/order", consumes="application/json")
    public ResponseOrderDto post(@RequestBody OrderDto order) {
        order.setExternal(true);
        log.info("Received order {}. Timestamp: {}", order, System.currentTimeMillis() - order.getPickUpTime());

        kitchenService.postOrder(order);

        ResponseOrderDto responseOrderDto = new ResponseOrderDto();
        responseOrderDto.setOrderId(order.getOrderId());
        responseOrderDto.setRestaurantId(order.getRestaurantId());
        responseOrderDto.setCreatedTime(order.getPickUpTime());
        responseOrderDto.setRegisteredTime(System.currentTimeMillis());

        return responseOrderDto;
    }
}
