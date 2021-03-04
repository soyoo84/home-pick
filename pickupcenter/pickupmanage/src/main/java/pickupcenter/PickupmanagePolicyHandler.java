package pickupcenter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import pickupcenter.config.kafka.KafkaProcessor;

@Service
public class PickupmanagePolicyHandler {
	@Autowired
	PickupmanageRepository pickupmanageRepository;

	@StreamListener(KafkaProcessor.INPUT)
	public void onStringEventListener(@Payload String eventString) {

	}

	@StreamListener(KafkaProcessor.INPUT)
	public void wheneverPickupcallCancelled_(@Payload PickupcallCancelled pickupcallCancelled) {
		System.out.println("##### EVT TYPE[호출취소됨]  : " + pickupcallCancelled.getEventType());
		if (pickupcallCancelled.isMe()) {
			System.out.println("##### listener  : " + pickupcallCancelled.toJson());

			if (pickupcallCancelled.getId() != null)
				// Correlation id 는 '고객휴대폰번호' 임
				pickupmanageRepository.findById(Long.valueOf(pickupcallCancelled.getId())).ifPresent((pickupmanage) -> {
					pickupmanage.setStatus("호출요청취소됨");
					pickupmanageRepository.save(pickupmanage);
				});
		}
	}

	@StreamListener(KafkaProcessor.INPUT)
	public void wheneverPickupmanageAssigned_(@Payload PickupmanageAssigned pickupmanageAssigned) {
		System.out.println("##### EVT TYPE[택시할당요청됨]  : " + pickupmanageAssigned.getEventType());
		if (pickupmanageAssigned.isMe()) {
			System.out.println("##### listener[할당확인됨]  : " + pickupmanageAssigned.toJson());

			if (pickupmanageAssigned.getId() != null)
				// Correlation id 는 '고객휴대폰번호' 임
				pickupmanageRepository.findById(Long.valueOf(pickupmanageAssigned.getId())).ifPresent((pickupmanage) -> {
					pickupmanage.setStatus(pickupmanageAssigned.getStatus());
					pickupmanageRepository.save(pickupmanage);
				});

		}
	}

}
