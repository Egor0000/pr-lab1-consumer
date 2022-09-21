package md.utm.isa.pr.lab1.consumer.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.CookDto;
import md.utm.isa.pr.lab1.consumer.dto.CookingDetailDto;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import md.utm.isa.pr.lab1.consumer.util.OrderUtil;
import org.springframework.beans.factory.annotation.Value;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public class Cook implements Runnable{
    private final KitchenService kitchenService;
    private final CookDto cookDto;
    private final Long timeUnit;
    private final Long timeDuration;

    private final long threadId;

    @Override
    public void run() {
        while (true) {
            long start = System.currentTimeMillis();
            OrderDto orderDto = new OrderDto();
            Food food = null;

            for (long l = cookDto.getRank(); l>=0; l--) {
                food = kitchenService.getNextFoodByComplexity(l);

                if (food!=null) {
                    break;
                }
            }

            if (food != null) {
//                PreparedOrderDto preparedOrder = prepareOrder(orderDto, cookDto.getCookId());
//                Long end = System.currentTimeMillis();
//                log.info("MaxWait: {}. Receive {} Start {} Timestamp:{}", orderDto.getMaxWait()*100, orderDto.getReceiveTime()-orderDto.getPickUpTime(), start-orderDto.getPickUpTime(), end-orderDto.getPickUpTime());
//                kitchenService.postPreparedOrder(preparedOrder);

                prepareFood(food);
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private PreparedOrderDto prepareOrder(OrderDto orderDto, Long cookId) {
        PreparedOrderDto preparedOrder;
        try {
            Double cookingTime = orderDto.getMaxWait();
            Long startTime = System.currentTimeMillis();

            if (cookingTime == null) {
                cookingTime = 1.0;
            }
            preparedOrder = OrderUtil.mapOrderToPreparedOrder(orderDto);
            preparedOrder.setPickUpTime(orderDto.getPickUpTime());
            preparedOrder.setCookingTime(cookingTime.longValue());
            preparedOrder.setCookingDetails(orderDto.getItems().stream()
                    .map(food -> fillCookingDetail(food, cookId))
                    .collect(Collectors.toList()));

            log.info("Cook [id={}] started preparing order [id={}]. Estimated time {} ms", cookId, orderDto.getOrderId(), cookingTime*timeUnit*timeDuration);

            Thread.sleep(cookingTime.longValue()*timeUnit*timeDuration);

            cookingTime =  (double) System.currentTimeMillis() - startTime;
            preparedOrder.setCookingTime(cookingTime.longValue());
            preparedOrder.setSendTime(System.currentTimeMillis());
            return preparedOrder;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void prepareFood(Food food) {
        try {
            Thread.sleep(food.getPreparationTime()*timeUnit*timeDuration);

            kitchenService.addToPrepared(food, cookDto.getCookId());
        } catch (Exception e) {
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
