package md.utm.isa.pr.lab1.consumer.entity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import md.utm.isa.pr.lab1.consumer.dto.TempFood;
import md.utm.isa.pr.lab1.consumer.enums.CookingApparatus;
import md.utm.isa.pr.lab1.consumer.service.KitchenService;

@RequiredArgsConstructor
@Slf4j
public class TCookingApparatus implements Runnable {
    private final CookingApparatus type;
    private final Long timeUnit;
    private final Long timeDuration;
    private final KitchenService kitchenService;
    private final Object lock;


    @Override
    public void run() {
        while (true) {

            TempFood tempFood = kitchenService.getNextUnpreparedFood(type);


            if (tempFood!=null) {
                    log.debug("[{}]READ food {} from queue", type, tempFood.getFood().getId());
                    prepareFood(tempFood);
                }

            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private void prepareFood(TempFood unpreparedFood) {
        try {
            Thread.sleep(1*timeUnit*timeDuration);
            if (unpreparedFood.getFood().getPreparationTime()>0) {
                unpreparedFood.getFood().setPreparationTime(unpreparedFood.getFood().getPreparationTime()-1);
            }

            if (unpreparedFood.getFood().getPreparationTime() <= 0) {
                kitchenService.prepareFood(unpreparedFood.getFood(), unpreparedFood.getCookId());
            } else  {
                kitchenService.addToUnpreparedQueue(unpreparedFood);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
