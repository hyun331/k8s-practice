package com.beyond.order_system.ordering.controller;

import com.beyond.order_system.ordering.dto.OrderListResDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.support.ConcurrentExecutorAdapter;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class SseController implements MessageListener {
    //SseEmitter : 연결된 사용자 정보를 의미
    //ConcurrentHashMap : Thread-safe한 map = 멀티 스레드 상황에서 안전 => 동시성 이슈 발생 안함
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    //여러번 구독을 방지하기 위한 ConcurrentHashSet 변수 생성
    private Set<String> subscribeList = ConcurrentHashMap.newKeySet();


    @Qualifier("4")
    private final RedisTemplate<String, Object >sseRedisTemplate;

    private final RedisMessageListenerContainer redisMessageListenerContainer;

    public SseController(@Qualifier("4") RedisTemplate<String, Object> sseRedisTemplate, RedisMessageListenerContainer redisMessageListenerContainer) {
        this.sseRedisTemplate = sseRedisTemplate;
        this.redisMessageListenerContainer = redisMessageListenerContainer;
    }

    //email에 해당되는 메세지를 listen하는 listener를 추가
    public void subscribeChannel(String email){
        if(!subscribeList.contains(email)) {    //이미 구독한 email 일 경우 더이상 구독하지 않는 분기처리
            MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(this);
            redisMessageListenerContainer.addMessageListener(listenerAdapter, new PatternTopic(email));
            subscribeList.add(email);
        }
    }

    private MessageListenerAdapter createListenerAdapter(SseController sseController){
        return new MessageListenerAdapter(sseController, "onMessage");
    }

    //연결이 들어올 수 있도록 api 생성
    @GetMapping("/subscribe")
    public SseEmitter subscribe(){
        SseEmitter emitter = new SseEmitter(14400*60*1000L); // 정도로 emitter유효시간 설정
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        emitters.put(email, emitter);   //사용자에 대한 정보가 emiiters에 저장되어 있어야 사용자는 알림을 받을 수 있다.
        emitter.onCompletion(()->emitters.remove(email));   //할거 다하면 emitters에서 제거
        emitter.onTimeout(()->emitters.remove(email));      //시간 지나면 emitters에서 제거

        try{
//            연결을 요청한 emitter에게 connect라는 event 연결되었다고 보내기
            emitter.send(SseEmitter.event().name("connect").data("connected!!!!"));
        }catch(IOException e){
            e.printStackTrace();
        }

        //redis에 해당 email을 listen하겠다고 선언
        //email에 대해 구독한다.
        subscribeChannel(email);
        return emitter;
    }


    //0809
    //주문하면 메세지 보내는 메서드 - 멀티 서버에서는 send하지 않고 redis에 넣기
    public void publishMessage(OrderListResDto dto, String email){
        SseEmitter emitter = emitters.get(email);
//        if(emitter != null){
//            try{
//                emitter.send(SseEmitter.event().name("ordered").data(dto));
//
//            }catch (IOException e){
//                throw new RuntimeException(e);
//            }
//        }else{
            //멀티서버
            sseRedisTemplate.convertAndSend(email, dto);
//        }

    }


    //listen하게되면 사용자에게 전파
    //A의 서버거 onMessage메서드 실행하여 A에게 알림 전달
    @Override
    public void onMessage(Message message, byte[] pattern) {    //message = dto, pattern = email
        //message내용 파싱 - objectMapper 이용해서
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OrderListResDto dto = objectMapper.readValue(message.getBody(), OrderListResDto.class);
            String email = new String(pattern, StandardCharsets.UTF_8);
            SseEmitter emitter = emitters.get(email);
            if(emitter!=null){
                emitter.send(SseEmitter.event().name("ordered").data(dto));
            }

            System.out.println("listening!!!");
            System.out.println(dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }
}
