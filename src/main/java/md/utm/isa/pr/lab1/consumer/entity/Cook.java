package md.utm.isa.pr.lab1.consumer.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.CookingDetailDto;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import md.utm.isa.pr.lab1.consumer.util.OrderUtil;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class Cook implements Runnable{
    private final KitchenService kitchenService;

    private final Long cookId;

    private final Long timeUnit;

    private final Long timeDuration;


    @Override
    public void run() {
        while (true) {
            try {
                OrderDto orderDto = kitchenService.getNextOrder();
                if (orderDto != null) {
                    PreparedOrderDto preparedOrder = prepareOrder(orderDto, cookId);
                    kitchenService.postPreparedOrder(preparedOrder);
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private PreparedOrderDto prepareOrder(OrderDto orderDto, Long cookId) {
        PreparedOrderDto preparedOrder;
        try {
            Long cookingTime = orderDto.getMaxWait();

            if (cookingTime == null) {
                cookingTime = 1L;
            }
            preparedOrder = OrderUtil.mapOrderToPreparedOrder(orderDto);
            preparedOrder.setCookingTime(cookingTime);
            preparedOrder.setCookingDetails(orderDto.getItems().stream()
                    .map(food -> fillCookingDetail(food, cookId))
                    .collect(Collectors.toList()));

            log.info("Cook [id={}] started preparing order [id={}]. Estimated time {} ms", cookId, orderDto.getOrderId(), cookingTime*timeUnit*timeDuration);

            Thread.sleep(cookingTime*timeUnit*timeDuration);

            return preparedOrder;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private CookingDetailDto fillCookingDetail(Long foodId, Long cookId) {
        CookingDetailDto cookingDetailDto = new CookingDetailDto();
        cookingDetailDto.setCookId(cookId);
        cookingDetailDto.setFoodId(foodId);
        return cookingDetailDto;
    }
}
