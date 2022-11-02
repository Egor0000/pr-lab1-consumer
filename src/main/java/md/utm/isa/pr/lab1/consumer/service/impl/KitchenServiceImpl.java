package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.RequiredArgsConstructor;
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
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class KitchenServiceImpl implements KitchenService {
    private final ApplicationProperties applicationProperties;

    private ConcurrentLinkedQueue<OrderDto> orderList = new ConcurrentLinkedQueue<>();
    private List<Double> ratings = new CopyOnWriteArrayList<>();
    private List<Integer> stars = new ArrayList<>();

    private ConcurrentMap<Key, TempOrder> workingList = new ConcurrentHashMap<>();

    private ConcurrentMap<Long, PreparedOrderDto> preparedExternalOrders = new ConcurrentHashMap<>();

    private ConcurrentMap<Long, PriorityBlockingQueue<Food>> complexityQueues = new ConcurrentHashMap<>();

    public ConcurrentMap<CookingApparatus, BlockingQueue<TempFood>> unpreparedFoodQueue = new ConcurrentHashMap<>();

    public ConcurrentMap<Long, ConcurrentLinkedQueue<Food>> preparedFoodQueue = new ConcurrentHashMap<>();

    private Map<Long, Food> cachedMenu = new HashMap<>();
    @Value("${producer.address}")
    private String address;

    @Value("${producer.port}")
    private Integer port;

    @Value("${server.address}")
    private String serverAddress;

    @Value("${server.port}")
    private Integer serverPort;

    private String path = "/distribution/";

    private WebClient webClient;

    @Value("${food-ordering.address}")
    private String foodOrderingAddress;

    @Value("${food-ordering.port}")
    private Integer foodOrderingPort;

    @Value("${server.id}")
    private Long restaurantId;

    @Value("${kitchen.apparatus.oven}")
    private Long ovenCount;

    @Value("${kitchen.apparatus.stove}")
    private Long stoveCount;

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

            register();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void postOrder(OrderDto orderDto) {
        TempOrder tempOrder = new TempOrder();
        tempOrder.setOrderId(orderDto.getOrderId());
        tempOrder.setOrderDto(orderDto);
        tempOrder.setExternal(orderDto.isExternal());


        log.info("PUT order {}", orderDto.getOrderId());

        workingList.put(new Key(orderDto.getOrderId(), orderDto.isExternal()), tempOrder);

        distributeOrder(orderDto);
    }


    @Override
    public TempOrder getNextOrder() {
        // todo remove or refactor
        return null;
    }

    @Override
    public TempOrder getTempOrder(Key key) {
        return workingList.get(key);
    }

    @Override
    public Food getNextFoodByComplexity(Long id) {
        PriorityBlockingQueue<Food> foods = complexityQueues.get(id);
        return (foods != null) ? foods.poll() : null;
    }

    @Override
    public void prepareFood(Food food, Long cookId) {
        TempOrder tempOrder = getTempOrder(new Key(food.getOrderId(), food.isExternal()));

        log.info("Prepared food {}", food);

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
            preparedOrderDto.setExternal(tempOrder.isExternal());

            log.info("REMOVED TEMP ORDER ID = {}", tempOrder.getOrderId());

            workingList.remove(new Key(tempOrder.getOrderId(), tempOrder.isExternal()));
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
    public void addToUnpreparedQueue(TempFood tempFood) {
        BlockingQueue<TempFood> foodByApparatus = unpreparedFoodQueue.get(tempFood.getFood().getCookingApparatus());

        if (foodByApparatus == null) {
            unpreparedFoodQueue.put(tempFood.getFood().getCookingApparatus(), new LinkedBlockingQueue<>());
        }

        unpreparedFoodQueue.get(tempFood.getFood().getCookingApparatus()).add(tempFood);
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
            if (!preparedOrderDto.isExternal()) {
                Mono<String> response = webClient.post()
                        .uri(String.format("%s:%s%s", address, port, path))
                        .body(BodyInserters.fromValue(preparedOrderDto))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class);

                return response.block();
            } else {
                preparedExternalOrders.put(preparedOrderDto.getOrderId(), preparedOrderDto);
            }
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

    @Override
    public void register() {
        try {
            Thread.sleep(100);

            List<Food> foods = new ArrayList<>(MenuUtil.cacheMenu().values());
            RestaurantDto restaurantDto = new RestaurantDto();
            restaurantDto.setRestaurantId(restaurantId);
            restaurantDto.setName("Restaurant_"+restaurantId);
            restaurantDto.setAddress(String.format("%s:%s", serverAddress, serverPort));
            restaurantDto.setMenu(foods);
            restaurantDto.setMenuItems((long)foods.size());

            log.info("Registering the restaurant ... ");

            WebClient foodOrderingClient = WebClient.create(String.format("http://%s:%s", foodOrderingAddress, foodOrderingPort));

             foodOrderingClient.post()
                    .uri(String.format("%s:%s%s", foodOrderingAddress, foodOrderingPort, "/register/"))
                    .body(BodyInserters.fromValue(restaurantDto))
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class).subscribe(resp -> log.info("Response: {}", resp));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public double getEstimatedTime(OrderDto order) {
        Map<Long, Food> menu = MenuUtil.cacheMenu();

        long noApparatusTime = order.getItems().stream()
                .map(menu::get)
                .filter(food -> food.getCookingApparatus() == null)
                .map(Food::getPreparationTime)
                .mapToLong(Long::longValue)
                .sum();

        List<CookDto> cookDtoList = applicationProperties.getCooksList();
        int cookProef = cookDtoList.stream()
                .map(CookDto::getProficiency)
                .mapToInt(Integer::intValue)
                .sum();

        long apparatusTime = order.getItems().stream()
                .map(menu::get)
                .filter(food -> food.getCookingApparatus() != null)
                .map(Food::getPreparationTime)
                .mapToLong(Long::longValue)
                .sum();

        long apparatusCount = ovenCount + stoveCount;

        int waitingTime = unpreparedFoodQueue.values().stream()
                .map(Collection::size)
                .mapToInt(Integer::intValue)
                .sum();

        int currentFoods = order.getItems().size();
        return (noApparatusTime / (double)cookProef + apparatusTime / (double)apparatusCount)
                * (waitingTime + currentFoods) / currentFoods;
    }

    @Override
    public PreparedOrderDto getPreparedExternalOrder(Long id) {
        PreparedOrderDto preparedOrderDto = preparedExternalOrders.remove(id);
        log.info ("Client requested order {}. STATUS: {} ", id, preparedOrderDto);
        return preparedOrderDto;
    }

    @Override
    public ResponseRating getAverageRating(PreparedOrderDto preparedOrderDto) {
        stars.add(preparedOrderDto.getRating());
        ResponseRating responseRating = new ResponseRating();
        responseRating.setRestaurantId(preparedOrderDto.getRestaurantId());
        responseRating.setPreparedOrders((long) stars.size());
        Double avg = stars.stream().mapToDouble(Integer::doubleValue).average().orElse(Double.NaN);
        responseRating.setRestaurantAverageRating(avg);
        return responseRating;
    }

    private void distributeOrder(OrderDto orderDto) {
        for (int i = 0; i < orderDto.getItems().size(); i++) {
            Food food = cachedMenu.get(orderDto.getItems().get(i));
            Food clonedFood = food.clone();
            clonedFood.setOrderId(orderDto.getOrderId());
            clonedFood.setExternal(orderDto.isExternal());

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
