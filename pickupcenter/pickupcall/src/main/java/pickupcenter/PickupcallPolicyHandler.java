package pickupcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import pickupcenter.config.kafka.KafkaProcessor;

@Service
public class PickupcallPolicyHandler {
	@Autowired
	PickupcallRepository pickupcallRepository;

	@StreamListener(KafkaProcessor.INPUT)
	public void onStringEventListener(@Payload String eventString) {

	}

	@StreamListener(KafkaProcessor.INPUT)
	public void wheneverPickupassignCompleted_(@Payload PickupassignCompleted pickupassignCompleted) {
		System.out.println("##### EVT TYPE[할당확인됨]  : " + pickupassignCompleted.getEventType());
		if (pickupassignCompleted.isMe() && pickupassignCompleted.get고객휴대폰번호() != null) {

//           try {
//               // 원래 데이터가 트랜잭션 커밋되기도 전에 이벤트가 너무 빨리 도달하는 경우를 막기 위함
//               Thread.currentThread().sleep(3000); //  no good. --> pay 가 TX 를 마친 후에만 실행되도록 수정함
//           } catch (InterruptedException e) {
//               e.printStackTrace();
//           }
			System.out.println("##### listener[할당확인됨]  : " + pickupassignCompleted.toJson());
			

			// Correlation id 는 '고객휴대폰번호' 임
			if(pickupassignCompleted.getId() != null)
				pickupcallRepository.findById(Long.valueOf(pickupassignCompleted.getId())).ifPresent((pickupcall) -> {
					pickupcall.setStatus("호출확정");
					pickupcallRepository.save(pickupcall);
				});
		}

	}

	@StreamListener(KafkaProcessor.INPUT)
	public void wheneverPickupmanageCancelled_(@Payload PickupmanageCancelled pickupmanageCancelled) {
		System.out.println("##### EVT TYPE[할당취소됨]  : " + pickupmanageCancelled.getEventType());
		if (pickupmanageCancelled.isMe()) {
			System.out.println("##### listener[할당취소됨]  : " + pickupmanageCancelled.toJson());
			pickupcallRepository.findById(Long.valueOf(pickupmanageCancelled.getId())).ifPresent((pickupcall) -> {
				pickupcall.setStatus("호출취소");
				pickupcallRepository.save(pickupcall);
			});
		}
	}

}
