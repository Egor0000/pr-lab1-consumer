package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.RequiredArgsConstructor;
import md.utm.isa.pr.lab1.consumer.entity.Cook;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
public class CookServiceImpl {

    private final KitchenService kitchenService;
    private ConcurrentMap<String, Thread> cooks = new ConcurrentHashMap<>();

    @Value("${kitchen.time.unit}")
    private Long timeUnit;

    @Value("${kitchen.time.duration}")
    private Long timeDuration;

    @Value("${kitchen.cooks}")
    private Long cookCnt;

    @PostConstruct
    private void onInit() {
        startCooks();
    }

    private void startCooks() {
        for (int i = 0; i < cookCnt; i++) {
            String cookName = "Cook_" + i;
            Thread r = new Thread(new Cook(kitchenService, (long) i, timeUnit, timeDuration));
            cooks.put(cookName, r);
            r.start();
        }
    }

}
