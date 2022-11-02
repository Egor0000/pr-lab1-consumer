package md.utm.isa.pr.lab1.consumer.controller;

import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.dto.ResponseOrderDto;
import md.utm.isa.pr.lab1.consumer.dto.ResponseRating;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v2")
@Slf4j
public class ClientOrderController {
    private final KitchenService kitchenService;
    private final String restAddress;
    private final int restPort;

    @Autowired
    public ClientOrderController(KitchenService kitchenService, @Value("${server.address}") String restAddress, @Value("${server.port}") int restPort ) {
        this.kitchenService = kitchenService;
        this.restAddress = restAddress;
        this.restPort = restPort;
    }

    @PostMapping(value = "/order", consumes="application/json")
    public ResponseOrderDto postOrder(@RequestBody OrderDto order) {
        order.setExternal(true);
        log.info("Received order {}. Timestamp: {}", order, System.currentTimeMillis() - order.getPickUpTime());

        kitchenService.postOrder(order);

        ResponseOrderDto responseOrderDto = new ResponseOrderDto();
        responseOrderDto.setOrderId(order.getOrderId());
        responseOrderDto.setRestaurantId(order.getRestaurantId());
        responseOrderDto.setCreatedTime(order.getPickUpTime());
        responseOrderDto.setRegisteredTime(System.currentTimeMillis());
        responseOrderDto.setEstimateWaitingTime(Math.round(kitchenService.getEstimatedTime(order)));
        responseOrderDto.setRestaurantAddress(String.format("%s:%s", restAddress, restPort));

        return responseOrderDto;
    }

    @GetMapping("/order")
    public PreparedOrderDto getPreparedOrder(@RequestParam Long id) {
        return kitchenService.getPreparedExternalOrder(id);
    }

    @PostMapping(value = "/rating", consumes="application/json")
    public ResponseRating postRating(@RequestBody PreparedOrderDto preparedOrderDto) {
        return kitchenService.getAverageRating(preparedOrderDto);
    }
}
