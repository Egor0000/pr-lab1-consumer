package md.utm.isa.pr.lab1.consumer.util;

import lombok.RequiredArgsConstructor;
import md.utm.isa.pr.lab1.consumer.dto.OrderDto;
import md.utm.isa.pr.lab1.consumer.dto.PreparedOrderDto;

@RequiredArgsConstructor
public class OrderUtil {
    public static PreparedOrderDto mapOrderToPreparedOrder(OrderDto orderDto) {
        PreparedOrderDto preparedOrder = new PreparedOrderDto();
        preparedOrder.setOrderId(orderDto.getOrderId());
        preparedOrder.setTableId(orderDto.getTableId());
        preparedOrder.setWaiterId(orderDto.getWaiterId());
        preparedOrder.setItems(orderDto.getItems());
        preparedOrder.setPriority(orderDto.getPriority());
        preparedOrder.setMaxWait(orderDto.getMaxWait());
        preparedOrder.setPickUpTime(orderDto.getPickUpTime());

        return preparedOrder;
    }
}
