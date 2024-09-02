package com.beyond.order_system.common.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class StockInventoryService {

    @Qualifier("3")
    private final RedisTemplate<String, Object> redisTemplate;


    public StockInventoryService(@Qualifier("3") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //상품 등록시 increaseStock() 호출
    public Long increaseStock(Long itemId, int quantity){
        //redis가 음수까지 내려가 경우 추후 재고 update상황에서 increase값이 정확하지 않을 수 있으므로, 음수면 0으로 setting 로직이 필요
        Object remains = redisTemplate.opsForValue().get(String.valueOf(itemId));
        if(remains == null){
            redisTemplate.opsForValue().set(itemId.toString(), quantity);
            return itemId;
        }else{
            int longRemains = Integer.parseInt(remains.toString());
            if(longRemains<0){
                int longRemains2 = Math.abs(longRemains);
                redisTemplate.opsForValue().increment(String.valueOf(itemId), longRemains2);
            }

            //원래는 increaseStock엔 이 return값만 있으면 됨. 위의 코드는 재고가 최소 0인데 음수로 표시되기 때문ㅇ ㅔ처리 해준것
            //아래 메서드의 리턴값은 잔량값을 리턴해줌
            return redisTemplate.opsForValue().increment(String.valueOf(itemId), quantity);
        }

    }

    //주문 등록시 decreaseStock()호출
    public Long decreaseStock(Long itemId, int quantity){
        Object remains = redisTemplate.opsForValue().get(String.valueOf(itemId));
        int intRemains = Integer.parseInt(remains.toString());
        if(intRemains < quantity){
            return -1L;
        }else{
            //quantity만큼 감소시킨 후 남아있는 잔량을 리턴
            return redisTemplate.opsForValue().decrement(String.valueOf(itemId), quantity);
        }
    }
}
