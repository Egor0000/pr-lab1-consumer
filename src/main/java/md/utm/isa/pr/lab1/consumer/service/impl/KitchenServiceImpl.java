package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.*;
import md.utm.isa.pr.lab1.consumer.entity.Food;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import md.utm.isa.pr.lab1.consumer.util.MenuUtil;
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
import java.util.*;
import java.util.concurrent.*;

@Service
@Slf4j
public class KitchenServiceImpl implements KitchenService {
    private ConcurrentLinkedQueue<OrderDto> orderList = new ConcurrentLinkedQueue<>();

    private ConcurrentMap<Long, TempOrder> workingList = new ConcurrentHashMap<>();

    private ConcurrentMap<Long, PriorityBlockingQueue<Food>> complexityQueues = new ConcurrentHashMap<>();

    public ConcurrentMap<CookingApparatus, BlockingQueue<TempFood>> unpreparedFoodQueue = new ConcurrentHashMap<>();

    public ConcurrentMap<Long, ConcurrentLinkedQueue<Food>> preparedFoodQueue = new ConcurrentHashMap<>();

    private Map<Long, Food> cachedMenu = new HashMap<>();
    @Value("${producer.address}")
    private String address;

    @Value("${producer.port}")
    private Integer port;

    private String path = "/distribution/";

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

            cachedMenu = MenuUtil.cacheMenu();

            unpreparedFoodQueue.put(CookingApparatus.oven, new LinkedBlockingQueue<>());
            unpreparedFoodQueue.put(CookingApparatus.stove, new LinkedBlockingQueue<>());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postOrder(OrderDto orderDto) {
        TempOrder tempOrder = new TempOrder();
        tempOrder.setOrderId(orderDto.getOrderId());
        tempOrder.setOrderDto(orderDto);

        log.debug("PUT order {}", orderDto.getOrderId());

        workingList.put(orderDto.getOrderId(), tempOrder);

        distributeOrder(orderDto);
    }


    @Override
    public TempOrder getNextOrder() {
        // todo remove or refactor
        return null;
    }

    @Override
    public TempOrder getTempOrder(Long id) {
        return workingList.get(id);
    }

    @Override
    public Food getNextFoodByComplexity(Long id) {
        PriorityBlockingQueue<Food> foods = complexityQueues.get(id);
        return (foods != null) ? foods.poll() : null;
    }

    @Override
    public void prepareFood(Food food, Long cookId) {
        TempOrder tempOrder = getTempOrder(food.getOrderId());

        if (tempOrder.prepareFood(food, cookId)) {
            PreparedOrderDto preparedOrderDto = new PreparedOrderDto();
            preparedOrderDto.setOrderId(tempOrder.getOrderId());

            // todo what should contain cooking time?
            preparedOrderDto.setCookingTime(System.currentTimeMillis());
            preparedOrderDto.setPriority(tempOrder.getOrderDto().getPriority());
            preparedOrderDto.setWaiterId(tempOrder.getOrderDto().getWaiterId());
            preparedOrderDto.setTableId(tempOrder.getOrderDto().getTableId());
            preparedOrderDto.setPickUpTime(tempOrder.getOrderDto().getPickUpTime());
            preparedOrderDto.setMaxWait(tempOrder.getOrderDto().getMaxWait());
            preparedOrderDto.setCookingDetails(tempOrder.getPreparedFoods());

            log.info("REMOVED TEMP ORDER ID = {}", tempOrder.getOrderId());

            workingList.remove(tempOrder.getOrderId());
            postPreparedOrder(preparedOrderDto);
        }
    }

    @Override
    public void addToUnpreparedQueue(Food food, Long cookId) {

        BlockingQueue<TempFood> foodByApparatus = unpreparedFoodQueue.get(food.getCookingApparatus());

        if (foodByApparatus == null) {
            unpreparedFoodQueue.put(food.getCookingApparatus(), new LinkedBlockingQueue<>());
        }

        unpreparedFoodQueue.get(food.getCookingApparatus()).add(new TempFood(food, cookId));
    }

    @Override
    public TempFood getNextUnpreparedFood(CookingApparatus type) {
        BlockingQueue<TempFood> foodByApparatus = unpreparedFoodQueue.get(type);

        if (foodByApparatus != null) {
            return unpreparedFoodQueue.get(type).poll();
        }

        return null;
    }


    @Override
    public String postPreparedOrder(PreparedOrderDto preparedOrderDto) {
        if (webClient != null) {
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

    @Override
    public void addToPreparedQueue(Food food, Long cookId) {
        ConcurrentLinkedQueue<Food> preparedFoodQueueByCook = preparedFoodQueue.get(cookId);

        if (preparedFoodQueueByCook == null) {
            preparedFoodQueue.put(cookId, new ConcurrentLinkedQueue<>());
        }

        preparedFoodQueue.get(cookId).add(food);
    }

    @Override
    public Food takePreparedFood(Long cookId) {
        ConcurrentLinkedQueue<Food> preparedFoodQueueByCook = preparedFoodQueue.get(cookId);
        if (preparedFoodQueueByCook != null) {
            return preparedFoodQueue.get(cookId).poll();
        }
        return null;
    }

    private void distributeOrder(OrderDto orderDto) {
        for (int i = 0; i < orderDto.getItems().size(); i++) {
            Food food = cachedMenu.get(orderDto.getItems().get(i));
            Food clonedFood = food.clone();
            clonedFood.setOrderId(orderDto.getOrderId());

            if (!complexityQueues.containsKey(clonedFood.getComplexity())) {
                complexityQueues.put(clonedFood.getComplexity(), new PriorityBlockingQueue<>(100, new FoodComparator()));
            }

            log.info("{}", complexityQueues.get(clonedFood.getComplexity()).peek());

            complexityQueues.get(clonedFood.getComplexity()).add(clonedFood);
        }
    }

    static class FoodComparator implements Comparator<Food> {

        @Override
        public int compare(Food food, Food food2) {
            return Long.compare(food.getPreparationTime(), food2.getPreparationTime());
        }
    }

}
