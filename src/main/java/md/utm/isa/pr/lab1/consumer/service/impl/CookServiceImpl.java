package md.utm.isa.pr.lab1.consumer.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.ApplicationProperties;
import md.utm.isa.pr.lab1.consumer.dto.CookDto;
import md.utm.isa.pr.lab1.consumer.entity.Cook;
import md.utm.isa.pr.lab1.consumer.entity.TCookingApparatus;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.context.Theme;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookServiceImpl {

    private final KitchenService kitchenService;
    private final ApplicationProperties applicationProperties;
    private ConcurrentMap<String, Thread> cooks = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Thread> cookApparatus = new ConcurrentHashMap<>();

    private Object ovenLock = new Object();
    private Object stoveLock = new Object();

    @Value("${kitchen.time.unit}")
    private Long timeUnit;
    @Value("${kitchen.time.duration}")
    private Long timeDuration;
    @Value("${kitchen.apparatus.oven}")
    private Long ovenCount;
    @Value("${kitchen.apparatus.stove}")
    private Long stoveCount;


    @PostConstruct
    private void onInit() {
        System.out.println(applicationProperties.getCooksList());
        startCookingApparatus();
        startCooks();
    }

    private void startCooks() {
        long cookId = 0;
        for (CookDto cookDto: applicationProperties.getCooksList()) {
            cookDto.setCookId(cookId);
            for (int i = 0; i < cookDto.getProficiency(); i++) {
                String cookName = cookDto.getName() + "_" + i;
                Thread r = new Thread(new Cook(kitchenService, cookDto, timeUnit, timeDuration, i, ovenLock, stoveLock));
                log.info("Cook thread (id={}) of cook (id={}) was started", i, cookDto.getCookId());
                cooks.put(cookName, r);
                r.setName(cookName);
                r.start();
            }
            cookId++;
        }
    }

    private void startCookingApparatus() {
        for (int i = 0; i < ovenCount; i++) {
            Thread t = new Thread(new TCookingApparatus(CookingApparatus.oven, timeUnit, timeDuration, kitchenService, ovenLock));
            String name = "OVEN_" + i;
            cookApparatus.put(name, t);
            t.setName(name);
            t.start();
            log.info("Oven (id={}} was started", i);
        }

        for (int i = 0; i < stoveCount; i++) {
            Thread t = new Thread(new TCookingApparatus(CookingApparatus.stove, timeUnit, timeDuration, kitchenService, stoveLock));
            String name = "STOVE_" + i;
            cookApparatus.put(name, t);
            t.setName(name);
            t.start();
            log.info("Stove (id={}} was started", i);
        }
    }



}
