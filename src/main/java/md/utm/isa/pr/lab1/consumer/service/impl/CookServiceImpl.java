package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.ApplicationProperties;
import md.utm.isa.pr.lab1.consumer.dto.CookDto;
import md.utm.isa.pr.lab1.consumer.entity.Cook;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookServiceImpl {

    private final KitchenService kitchenService;
    private final ApplicationProperties applicationProperties;
    private ConcurrentMap<String, Thread> cooks = new ConcurrentHashMap<>();

    @Value("${kitchen.time.unit}")
    private Long timeUnit;

    @Value("${kitchen.time.duration}")
    private Long timeDuration;

    @Value("${kitchen.cooks.count}")
    private Long cookCnt;

    @PostConstruct
    private void onInit() {
        System.out.println(applicationProperties.getCooksList());
        startCooks();
    }

    private void startCooks() {


        long cookId = 0;
        for (CookDto cookDto: applicationProperties.getCooksList()) {
            cookDto.setCookId(cookId);
            for (int i = 0; i < cookDto.getProficiency(); i++) {
                String cookName = cookDto.getName() + "_" + i;
                Thread r = new Thread(new Cook(kitchenService, cookDto, timeUnit, timeDuration, i));
                log.info("Cook thread (id={}) of cook (id={}) was started", i, cookDto.getCookId());
                cooks.put(cookName, r);
                r.start();
            }

            cookId++;
        }

    }



}
