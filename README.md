
![image](https://user-images.githubusercontent.com/30484527/109915198-bd9d5000-7cf4-11eb-94c4-bf0101621a34.png)




# 서비스 시나리오

기능적 요구사항
1. 고객이 택배유형(10kg이상, 10kg 이하)을 선택하고 픽업 요청한다.
2. 고객 위치 근처의 택배기사를 조회 후 택배기사를 할당 요청한다.
3. 할당요청된 택배기사중 하나를 자동할당 한다.
4. 할당 즉시, 고객에게 할당완료 정보를 전달 한다.
5. 고객은 택배기사호출을 취소 할 수 있다.
6. 호출이 취소 되면 해당 할당을 취소한다.
7. 고객은 상태를 중간중간 조회하고 카톡으로 받는다.

비기능적 요구사항
1. 트랜잭션
- 택배기사가 할당확인 되지 않으면 고객은 호출요청을 할 수 없다. Sync 호출
2. 장애격리
- 택배기사 할당요청은 할당확인 기능이 동작하지 않더라도, 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency
- 고객 호출요청이 과중되면 택배기사 할당확인 요청을 잠시동안 받지 않고 잠시후에 하도록 유도한다 Circuit breaker, fallback
3. 성능
- 고객은 호출상태를 조회하고 할당/할당취소 여부를 카톡으로 확인 할 수 있어야 한다. CQRS, Event driven



# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)


# 분석/설계



## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과: 


### 이벤트 도출
![이벤트도출](https://user-images.githubusercontent.com/78134019/109769477-98013f80-7c3d-11eb-9914-e7737a67ed01.jpg)


### 부적격 이벤트 탈락
![부적격이벤트도출](https://user-images.githubusercontent.com/78134019/109769488-9b94c680-7c3d-11eb-9bc8-8c5a8282837e.jpg)


- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 택배유형(10kg이상/10kg이하)선택됨:  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외
- 가용 택배기사 조회됨 :  계획된 사업 범위 및 프로젝트에서 벗어서난다고 판단하여 제외


	

### 액터, 커맨드 부착하여 읽기 좋게
![액터커멘드](https://user-images.githubusercontent.com/78134019/109769523-ab140f80-7c3d-11eb-82d0-f0fef09ad53c.jpg)


### 어그리게잇으로 묶기
![어그리게잇](https://user-images.githubusercontent.com/78134019/109769535-af402d00-7c3d-11eb-8a0a-d4980b1fba0e.jpg)


- 호출, 택배기사관리, 택배기사 할당 어그리게잇을 생성하고 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌 
 


### 바운디드 컨텍스트로 묶기

![바운디드컨텍스트](https://user-images.githubusercontent.com/78134019/109769565-bbc48580-7c3d-11eb-864b-4704fd9eb8c8.jpg)


### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

![폴리시부착](https://user-images.githubusercontent.com/78134019/109769581-c1ba6680-7c3d-11eb-9610-42140cb4869f.jpg)


### 폴리시의 이동

![폴리시이동](https://user-images.githubusercontent.com/78134019/109769594-c54ded80-7c3d-11eb-938e-76c30ee501cd.jpg)


### 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![컨텍스트매핑](https://user-images.githubusercontent.com/78134019/109769605-ca12a180-7c3d-11eb-8090-75fc659d5eba.jpg)




### 완성된 모형

![최종본](https://user-images.githubusercontent.com/78134019/109769624-d0088280-7c3d-11eb-99c5-a9fc3b02b2da.jpg)




### 기능적 요구사항 검증

![image](https://user-images.githubusercontent.com/78134019/109769654-d860bd80-7c3d-11eb-8d95-e3fdffcef243.png)


- 고객이 택배기사를 호출요청한다.(ok)
- 택배기사 관리 시스템이 택배기사 할당을 요청한다.(ok)
- 택배기사 자동 할당이 완료된다.(ok)
- 호출상태 및 할당상태를 갱신한다.(ok)
- 고객에게 카톡 알림을 한다.(ok)



![image](https://user-images.githubusercontent.com/78134019/109769704-e9a9ca00-7c3d-11eb-8270-ee2d9b59f02b.png)


- 고객이 택배기사를 호출취소요청한다.(ok)
- 택배기사 관리 시스템이 택배기사 할당 취소를 요청한다.(ok)
- 택배기사 할당이 취소된다.(ok)
- 취소상태로 갱신한다.(ok)
- 고객에게 카톡 알림을 한다.(ok)


![image](https://user-images.githubusercontent.com/78134019/109769741-f7f7e600-7c3d-11eb-94b9-c32f587cd14f.png)


  
	- 고객이 호출진행내역을 볼 수 있어야 한다. (ok)


### 비기능 요구사항 검증

![image](https://user-images.githubusercontent.com/78134019/109769773-047c3e80-7c3e-11eb-91fc-45d0a20fdfca.png)


1) 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리 
   택배기사 할당요청이 완료되지 않은 호출요청 완료처리는 최종 할당이 되지 않는 경우 무한정 대기 등 대고객 서비스 및 신뢰도에 치명적 문제점이 있어 ACID 트랜잭션 적용. 
   호출요청 시 택배기사 할당요청에 대해서는 Request-Response 방식 처리 
2) 호출요청 완료시 할당확인 및 결과 전송: pickup manage service 에서pickup Assign 마이크로서비스로 택시할당 요청이 전달되는 과정에 있어서 
  pickup Assig 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함. 
3) 나머지 모든 inter-microservice 트랜잭션: 호출상태, 할당/할당취소 여부 등 이벤트에 대해 카톡을 처리하는 등 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, 
Eventual Consistency 를 기본으로 채택함. 



## 헥사고날 아키텍처 다이어그램 도출 (Polyglot)

![image](https://user-images.githubusercontent.com/78134019/109769864-24136700-7c3e-11eb-9abf-abc8415155ae.png)






# 구현:

서비스를 로컬에서 실행하는 방법은 아래와 같다 
각 서비스별로 bat 파일로 실행한다. 

```
-setnv.bat
SET JAVA_HOME=C:\dev\Tools\jdk1.8.0_131
SET MVN_HOME=C:\dev\Tools\apache-maven-3.6.3
SET KAFKA_HOME=C:\dev\Tools\kafka_2.13-2.7.0
SET ANACONDA_HOME=C:\dev\Tools\Anaconda3
SET MONGO_HOME=C:\dev\Tools\mongodb
SET MARIA_HOME=C:\dev\Tools\mariadb-10.3.13-winx64
SET MARIA_DATA=C:\dev\DATA\mariadb
SET PATH=%MARIA_HOME%\BIN;%MONGO_HOME%\BIN;%NODE_HOME%;%NODE_HOME%\node_modules\npm;%NODE_HOME%\node_modules;%KAFKA_HOME%\BIN\WINDOWS;%JAVA_HOME%\BIN;%MVN_HOME%\BIN;%PATH%;

- run_zookeeper
zookeeper-server-start.bat %KAFKA_HOME%\config\zookeeper.properties 

- run_kafka
kafka-server-start.bat %KAFKA_HOME%\config\server.properties

- run_pickupcall.bat
call setenv.bat
cd ..\pickupcenter\pickupcall
mvn clean spring-boot:run
pause ..

- run_pickupmanage.bat
call setenv.bat
cd ..\pickupcenter\pickupmanage
mvn clean spring-boot:run
pause ..

- run_pickupassign.bat
call setenv.bat
cd ..\pickupcenter\pickupassign
mvn clean spring-boot:run
pause ..

- run_customer.bat
call setenv.bat
SET CONDA_PATH=%ANACONDA_HOME%;%ANACONDA_HOME%\BIN;%ANACONDA_HOME%\condabin;%ANACONDA_HOME%\Library\bin;%ANACONDA_HOME%\Scripts;
SET PATH=%CONDA_PATH%;%PATH%;
cd ..\pickupcenter\customer_py\
python policy-handler.py 
pause ..

```

## DDD 의 적용
총 3개의 Domain 으로 관리되고 있으며, 픽업요청(pickupcall) , 픽업관리(pickupManage), 픽업할당(pickupAssign) 으로 구성된다. 


![DDD](https://user-images.githubusercontent.com/30484527/109992823-fec24e00-7d4e-11eb-98a1-48a08a886871.png)


## 폴리글랏 퍼시스턴스

```
위치 : /pickupcenter>pickupmanage>pom.xml
```
![폴리그랏](https://user-images.githubusercontent.com/30484527/109993612-d555f200-7d4f-11eb-97a3-83ab4255cad4.png)



## 폴리글랏 프로그래밍 - 파이썬
```
위치 : /pickupcenter>cutomer>policy-handler.py
```
![폴리그랏2](https://user-images.githubusercontent.com/30484527/109993997-38e01f80-7d50-11eb-9ae0-bfaf76854864.png)


## 마이크로 서비스 호출 흐름

- pickupcall 서비스 호출처리
픽업호출(pickupcall)->픽업관리(pickupmanage) 간의 호출처리 되고, 픽업할당에서 택배기사를 할당하여 호출확정 상태가 됨.
두 개의 호출 상태
를 만듬.
```
http localhost:8081/pickupcalls 휴대폰번호="01050958718" status=호출 location="이수" cost=10000
http localhost:8081/pickupcalls 휴대폰번호="01089385708" status=호출 location="사당" cost=20000
```
![image](screenshots/taxicall1.png "taxicall 서비스 호출")
![image](screenshots/taxicall2.png "taxicall 서비스 호출")

호출 결과는 모두 택시 할당(taxiassign)에서 택시기사의 할당으로 처리되어 호출 확정 상태가 되어 있음.

![image](screenshots/taxicall_result1.png "taxicall 서비스 호출 결과")
![image](screenshots/taxicall_result2.png "taxicall 서비스 호출 결과")
![image](screenshots/taximanage_result1.png "taxicall 서비스 호출 결과 - 택시관리")


- taxicall 서비스 호출 취소 처리

호출 취소는 택시호출에서 다음과 같이 호출 하나를 취소 함으로써 진행 함.

```
http delete http://localhost:8081/택시호출s/1
HTTP/1.1 204
Date: Tue, 02 Mar 2021 16:59:12 GMT
```
호출이 취소 되면 택시 호출이 하나가 삭제 되었고, 

```
http localhost:8081/택시호출s/
```
![image](screenshots/taxicancel_result.png "taxicall 서비스 호출취소 결과")


택시관리에서는 해당 호출에 대해서 호출취소로 상태가 변경 됨.

```
http localhost:8082/택시관리s/
```
![image](screenshots/taximanage_result.png "taxicall 서비스 호출취소 결과")

- 고객 메시지 서비스 처리
고객(customer)는 호출 확정과 할당 확정에 대한 메시지를 다음과 같이 받을 수 있으며,
할당 된 택시기사의 정보를 또한 확인 할 수 있다.
파이썬으로 구현 하였음.

![image](screenshots/customer.png "호출 결과에 대한 고객 메시지")


## Gateway 적용

서비스에 대한 하나의 접점을 만들기 위한 게이트웨이의 설정은 8088로 설정 하였으며, 다음 마이크로서비스에 대한 설정 입니다.
```
택시호출 서비스 : 8081
택시관리 서비스 : 8082
택시호출 서비스 : 8083
```

gateway > applitcation.yml 설정

![gateway_1](https://user-images.githubusercontent.com/78134019/109480363-c73d7280-7abe-11eb-9904-0c18e79072eb.png)

![gateway_2](https://user-images.githubusercontent.com/78134019/109480386-d02e4400-7abe-11eb-9251-a813ac911e0d.png)


gateway 테스트

```
http localhost:8080/택시호출s
-> gateway 를 호출하나 8081 로 호출됨
```
![gateway_3](https://user-images.githubusercontent.com/78134019/109480424-da504280-7abe-11eb-988e-2a6d7a1f7cea.png)



## 동기식 호출 과 Fallback 처리

호출(taxicall)->택시관리(taximanage) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리함.
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 


```
# external > 택시관리Service.java


package taxiguider.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(name="taximanage", url="http://localhost:8082")
@FeignClient(name="taximanage", url="http://localhost:8082", fallback = 택시관리ServiceFallback.class)
public interface 택시관리Service {

    @RequestMapping(method= RequestMethod.POST, path="/택시관리s")
    public void 택시할당요청(@RequestBody 택시관리 택시관리);

}

```

```
# external > 택시관리ServiceFallback.java


package taxiguider.external;

import org.springframework.stereotype.Component;

@Component
public class 택시관리ServiceFallback implements 택시관리Service {
	 
	//@Override
	//public void 택시할당요청(택시관리 택시관리) 
	//{	
	//	System.out.println("Circuit breaker has been opened. Fallback returned instead.");
	//}
	
	
	@Override
	public void 택시할당요청(택시관리 택시관리) {
		// TODO Auto-generated method stub
		System.out.println("Circuit breaker has been opened. Fallback returned instead. " + 택시관리.getId());
	}

}

```

![동기식](https://user-images.githubusercontent.com/78134019/109463569-97837000-7aa8-11eb-83c4-6f6eff1594aa.jpg)


- 택시호출을 하면 택시관리가 호출되도록..
```
# 택시호출.java

 @PostPersist
    public void onPostPersist(){    	
    	System.out.println("휴대폰번호 " + get휴대폰번호());
        System.out.println("호출위치 " + get호출위치());
        System.out.println("호출상태 " + get호출상태());
        System.out.println("예상요금 " + get예상요금());
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.   	
    	if(get휴대폰번호() != null)
		{
    		System.out.println("SEND###############################" + getId());
			택시관리 택시관리 = new 택시관리();
	        
			택시관리.setOrderId(String.valueOf(getId()));
	        택시관리.set고객휴대폰번호(get휴대폰번호());
	        if(get호출위치()!=null) 
	        	택시관리.set호출위치(get호출위치());
	        if(get호출상태()!=null) 
	        	택시관리.set호출상태(get호출상태());
	        if(get예상요금()!=null) 
	        	택시관리.set예상요금(get예상요금());
	        
	        // mappings goes here
	        TaxicallApplication.applicationContext.getBean(택시관리Service.class).택시할당요청(택시관리);
		}
```

![동기식2](https://user-images.githubusercontent.com/78134019/109463985-47f17400-7aa9-11eb-8603-c1f83e17951d.jpg)

- 동기식 호출 적용으로 택시 관리 시스템이 정상적이지 않으면 , 택시콜도 접수될 수 없음을 확인 
```
# 택시 관리 시스템 down 후 taxicall 호출 

#taxicall

C:\Users\Administrator>http localhost:8081/택시호출s 휴대폰번호="01012345678" 호출상태="호출"
```

![택시관리죽으면택시콜놉](https://user-images.githubusercontent.com/78134019/109464780-905d6180-7aaa-11eb-9c90-e7d1326deea1.jpg)

```
# 택시 관리 (taximanage) 재기동 후 주문하기

#주문하기(order)
http localhost:8081/택시호출s 휴대폰번호="01012345678" 호출상태="호출"
```

![택시관리재시작](https://user-images.githubusercontent.com/78134019/109464984-e5997300-7aaa-11eb-9363-b7bfe15de105.jpg)

-fallback 

![fallback캡쳐](https://user-images.githubusercontent.com/78134019/109480299-b5f46600-7abe-11eb-906e-9e1e6da245b2.png)


## 비동기식 호출 / 장애격리  / 성능

택시 관리 (Taxi manage) 이후 택시 할당(Taxi Assign) 은 비동기식 처리이므로 , 택시 호출(Taxi call) 의 서비스 호출에는 영향이 없다
 
고객이 택시 호출(Taxi call) 후 상태가 [호출]->[호출중] 로 변경되고 할당이 완료되면 [호출확정] 로 변경이 되지만 , 택시 할당(Taxi Assign)이 정상적이지 않으므로 [호출중]로 남아있음. 
--> (시간적 디커플링)
<고객 택시 호출 Taxi call>
![비동기_호출2](https://user-images.githubusercontent.com/78134019/109468467-f4365900-7aaf-11eb-877a-049637b5ee6a.png)

<택시 할당이 정상적이지 않아 호출중으로 남아있음>
![택시호출_택시할당없이_조회](https://user-images.githubusercontent.com/78134019/109471791-99ebc700-7ab4-11eb-924f-03715de42eba.png)



## 성능 조회 / View 조회
고객이 호출한 모든 정보는 조회가 가능하다. 

![고객View](https://user-images.githubusercontent.com/78134019/109483385-80ea1280-7ac2-11eb-9419-bf3ff5a0dbbc.png)


---mvn MSA Service
<gateway>
	
![mvn_gateway](https://user-images.githubusercontent.com/78134019/109744124-244b3c80-7c15-11eb-80a9-bed42413aa58.png)
	
<taxicall>
	
![mvn_taxicall](https://user-images.githubusercontent.com/78134019/109744165-31682b80-7c15-11eb-9d94-7bc23efca6b6.png)

<taximanage>
	
![mvn_taximanage](https://user-images.githubusercontent.com/78134019/109744195-3b8a2a00-7c15-11eb-9554-1c3ba088af52.png)

<taxiassign>
	
![mvn_taxiassign](https://user-images.githubusercontent.com/78134019/109744226-46dd5580-7c15-11eb-8b47-5100ed01e3ae.png)


# 운영

## Deploy / Pipeline

- az login
```
{
    "cloudName": "AzureCloud",
    "homeTenantId": "6011e3f8-2818-42ea-9a63-66e6acc13e33",
    "id": "718b6bd0-fb75-4ec9-9f6e-08ae501f92ca",
    "isDefault": true,
    "managedByTenants": [],
    "name": "2",
    "state": "Enabled",
    "tenantId": "6011e3f8-2818-42ea-9a63-66e6acc13e33",
    "user": {
      "name": "skTeam03@gkn2021hotmail.onmicrosoft.com",
      "type": "user"
    }
  }
```


- account set 
```
az account set --subscription "종량제2"
```


- 리소스그룹생성
```
그룹명 : skccteam03-rsrcgrp
```


- 클러스터 생성
```
클러스터 명 : skccteam03-aks
```

- 토큰 가져오기
```
az aks get-credentials --resource-group skccteam03-rsrcgrp --name skccteam03-aks
```

- aks에 acr 붙이기
```
az aks update -n skccteam03-aks -g skccteam03-rsrcgrp --attach-acr skccteam03
```

![aks붙이기](https://user-images.githubusercontent.com/78134019/109653395-540e2c00-7ba4-11eb-97dd-2dcfdf5dc539.jpg)



-deployment.yml을 사용하여 배포 
--> 도커 이미지 만들기 붙이기 
- deployment.yml 편집
```
namespace, image 설정
env 설정 (config Map) 
readiness 설정 (무정지 배포)
liveness 설정 (self-healing)
resource 설정 (autoscaling)
```
![deployment_yml](https://user-images.githubusercontent.com/78134019/109652001-9171ba00-7ba2-11eb-8c29-7128ceb4ec97.jpg)

- deployment.yml로 서비스 배포
```
cd app
kubectl apply -f kubernetes/deployment.yml
```
<Deploy cutomer>
![deploy_customer](https://user-images.githubusercontent.com/78134019/109744443-a471a200-7c15-11eb-94c9-a0c0a7999d04.png)

<Deploy gateway>
![deploy_gateway](https://user-images.githubusercontent.com/78134019/109744457-acc9dd00-7c15-11eb-8502-ff65e779e9d2.png)

<Deploy taxiassign>
![deploy_taxiassign](https://user-images.githubusercontent.com/78134019/109744471-b3585480-7c15-11eb-8d68-bba9c3d8ce01.png)

<Deploy taxicall>
![deploy_taxicall](https://user-images.githubusercontent.com/78134019/109744487-bb17f900-7c15-11eb-8bd0-ff0a9fc9b2e3.png)

<Deploy_taximanage>
![deploy_taximanage](https://user-images.githubusercontent.com/78134019/109744591-e69ae380-7c15-11eb-834a-44befae55092.png)

## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

시나리오는 단말앱(app)-->결제(pay) 시의 연결을 RESTful Request/Response 로 연동하여 구현이 되어있고, 결제 요청이 과도할 경우 CB 를 통하여 장애격리.

- Hystrix 를 설정:  요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true

# To set thread isolation to SEMAPHORE
#hystrix:
#  command:
#    default:
#      execution:
#        isolation:
#          strategy: SEMAPHORE

hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```
![hystrix](https://user-images.githubusercontent.com/78134019/109652345-0218d680-7ba3-11eb-847b-708ba071c119.jpg)


-----------------------------------------
* siege 툴 사용법:
```
 siege가 생성되어 있지 않으면:
 kubectl run siege --image=apexacme/siege-nginx -n phone82
 siege 들어가기:
 kubectl exec -it pod/siege-5c7c46b788-4rn4r -c siege -n phone82 -- /bin/bash
 siege 종료:
 Ctrl + C -> exit
```
* 부하테스터 siege 툴을 통한 서킷 브레이커 동작 확인:
- 동시사용자 100명
- 60초 동안 실시

```
siege -c100 -t60S -r10 -v --content-type "application/json" 'http://app:8080/orders POST {"item": "abc123", "qty":3}'
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 pay에서 처리되면서 다시 order를 받기 시작 

![image](https://user-images.githubusercontent.com/73699193/98098702-07eefb80-1ed2-11eb-94bf-316df4bf682b.png)

- report

![image](https://user-images.githubusercontent.com/73699193/98099047-6e741980-1ed2-11eb-9c55-6fe603e52f8b.png)

- CB 잘 적용됨을 확인


### 오토스케일 아웃

- 대리점 시스템에 대한 replica 를 동적으로 늘려주도록 HPA 를 설정한다. 설정은 CPU 사용량이 15프로를 넘어서면 replica 를 10개까지 늘려준다:

```
# autocale out 설정
store > deployment.yml 설정
```
![image](https://user-images.githubusercontent.com/73699193/98187434-44fbd200-1f54-11eb-9859-daf26f812788.png)

```
kubectl autoscale deploy store --min=1 --max=10 --cpu-percent=15 -n phone82
```
![image](https://user-images.githubusercontent.com/73699193/98100149-ce1ef480-1ed3-11eb-908e-a75b669d611d.png)


-
- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.
```
kubectl exec -it pod/siege-5c7c46b788-4rn4r -c siege -n phone82 -- /bin/bash
siege -c100 -t120S -r10 -v --content-type "application/json" 'http://store:8080/storeManages POST {"orderId":"456", "process":"Payed"}'
```
![image](https://user-images.githubusercontent.com/73699193/98102543-0d9b1000-1ed7-11eb-9cb6-91d7996fc1fd.png)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy store -w -n phone82
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.

![image](https://user-images.githubusercontent.com/73699193/98102926-92862980-1ed7-11eb-8f19-a673d72da580.png)

- 다시 부하를 주고 확인하니 Availability가 높아진 것을 확인 할 수 있었다.

![image](https://user-images.githubusercontent.com/73699193/98103249-14765280-1ed8-11eb-8c7c-9ea1c67e03cf.png)


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscale 이나 CB 설정을 제거함


- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
kubectl apply -f kubernetes/deployment_readiness.yml
```
- readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/73699193/98105334-2a394700-1edb-11eb-9633-f5c33c5dee9f.png)


- deployment.yml에 readiness 옵션을 추가 

![image](https://user-images.githubusercontent.com/73699193/98107176-75ecf000-1edd-11eb-88df-617c870b49fb.png)

- readiness적용된 deployment.yml 적용

```
kubectl apply -f kubernetes/deployment.yml
```
- 새로운 버전의 이미지로 교체
```
cd acr
az acr build --registry admin02 --image admin02.azurecr.io/store:v4 .
kubectl set image deploy store store=admin02.azurecr.io/store:v4 -n phone82
```
- 기존 버전과 새 버전의 store pod 공존 중

![image](https://user-images.githubusercontent.com/73699193/98106161-65884580-1edc-11eb-9540-17a3c9bdebf3.png)

- Availability: 100.00 % 확인

![image](https://user-images.githubusercontent.com/73699193/98106524-c152ce80-1edc-11eb-8e0f-3731ca2f709d.png)



## Config Map

- apllication.yml 설정

* default쪽

![image](https://user-images.githubusercontent.com/73699193/98108335-1c85c080-1edf-11eb-9d0f-1f69e592bb1d.png)

* docker 쪽

![image](https://user-images.githubusercontent.com/73699193/98108645-ad5c9c00-1edf-11eb-8d54-487d2262e8af.png)

- Deployment.yml 설정

![image](https://user-images.githubusercontent.com/73699193/98108902-12b08d00-1ee0-11eb-8f8a-3a3ea82a635c.png)

- config map 생성 후 조회
```
kubectl create configmap apiurl --from-literal=url=http://pay:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n phone82
```
![image](https://user-images.githubusercontent.com/73699193/98107784-5bffdd00-1ede-11eb-8da6-82dbead0d64f.png)

- 설정한 url로 주문 호출
```
http POST http://app:8080/orders item=dfdf1 qty=21
```

![image](https://user-images.githubusercontent.com/73699193/98109319-b732cf00-1ee0-11eb-9e92-ad0e26e398ec.png)

- configmap 삭제 후 app 서비스 재시작
```
kubectl delete configmap apiurl -n phone82
kubectl get pod/app-56f677d458-5gqf2 -n phone82 -o yaml | kubectl replace --force -f-
```
![image](https://user-images.githubusercontent.com/73699193/98110005-cf571e00-1ee1-11eb-973f-2f4922f8833c.png)

- configmap 삭제된 상태에서 주문 호출   
```
http POST http://app:8080/orders item=dfdf2 qty=22
```
![image](https://user-images.githubusercontent.com/73699193/98110323-42f92b00-1ee2-11eb-90f3-fe8044085e9d.png)

![image](https://user-images.githubusercontent.com/73699193/98110445-720f9c80-1ee2-11eb-851e-adcd1f2f7851.png)

![image](https://user-images.githubusercontent.com/73699193/98110782-f4985c00-1ee2-11eb-97a7-1fed3c6b042c.png)



## Self-healing (Liveness Probe)

- store 서비스 정상 확인

![image](https://user-images.githubusercontent.com/27958588/98096336-fb1cd880-1ece-11eb-9b99-3d704cd55fd2.jpg)


- deployment.yml 에 Liveness Probe 옵션 추가
```
cd ~/phone82/store/kubernetes
vi deployment.yml

(아래 설정 변경)
livenessProbe:
	tcpSocket:
	  port: 8081
	initialDelaySeconds: 5
	periodSeconds: 5
```
![image](https://user-images.githubusercontent.com/27958588/98096375-0839c780-1ecf-11eb-85fb-00e8252aa84a.jpg)

- store pod에 liveness가 적용된 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096393-0a9c2180-1ecf-11eb-8ac5-f6048160961d.jpg)

- store 서비스의 liveness가 발동되어 13번 retry 시도 한 부분 확인

![image](https://user-images.githubusercontent.com/27958588/98096461-20a9e200-1ecf-11eb-8b02-364162baa355.jpg)
















# 서비스 시나리오

기능적 요구사항
1. 고객이 필요한 자율운행 택시등급을 선택하고 호출 요청한다.
2. 고객 위치에서 가용 택시를 조회 후 택시를 할당 요청한다.
3. 할당요청된 택시중 하나를 자동할당 한다.
4. 할당 즉시, 고객에게 호출완료 정보를 전달 한다.
5. 고객은 택시호출을 취소 할 수 있다.
6. 호출이 취소 되면 해당 할당을 취소한다.
7. 고객은 호출상태를 중간중간 조회하고 카톡으로 받는다.

비기능적 요구사항
1. 트랜잭션
- 택시 할당확인 되지 않으면 고객 호출요청을 할 수 없다. Sync 호출
2. 장애격리
- 택시 할당요청은 할당확인 기능이 동작하지 않더라도, 365일 24시간 받을 수 있어야 한다 Async (event-driven), Eventual Consistency
- 고객 호출요청이 과중되면 택시 할당확인 요청을 잠시동안 받지 않고 잠시후에 하도록 유도한다 Circuit breaker, fallback
3. 성능
- 고객은 호출상태를 조회하고 할당/할당취소 여부를 카톡으로 확인 할 수 있어야 한다. CQRS, Event driven



# 체크포인트

1. Saga
1. CQRS
1. Correlation
1. Req/Resp
1. Gateway
1. Deploy/ Pipeline
1. Circuit Breaker
1. Autoscale (HPA)
1. Zero-downtime deploy (Readiness Probe)
1. Config Map/ Persistence Volume
1. Polyglot
1. Self-healing (Liveness Probe)


# 분석/설계


## AS-IS 조직 (Horizontally-Aligned)
  ![image](https://user-images.githubusercontent.com/487999/79684144-2a893200-826a-11ea-9a01-79927d3a0107.png)

## TO-BE 조직 (Vertically-Aligned)
  ![조직구조](https://user-images.githubusercontent.com/78134019/109453964-977a7480-7a96-11eb-83cb-5445c363a9e8.jpg)


## Event Storming 결과
* MSAEz 로 모델링한 이벤트스토밍 결과:  http://www.msaez.io/#/storming/AYDToXSmsXY4cguxhLA9jTpgvGc2/every/663e27e6ef252865a609e90c5e01f243


### 이벤트 도출
![이벤트도출](https://user-images.githubusercontent.com/78134019/109454199-0d7edb80-7a97-11eb-81f0-4c7d3f581636.jpg)

### 부적격 이벤트 탈락
![부적격이벤트](https://user-images.githubusercontent.com/78134019/109454229-18d20700-7a97-11eb-82a9-e1046b78682a.jpg)

- 과정중 도출된 잘못된 도메인 이벤트들을 걸러내는 작업을 수행함
- 택시 등급 선택됨,  :  UI 의 이벤트이지, 업무적인 의미의 이벤트가 아니라서 제외
- 가용 택시 조회됨 :  계획된 사업 범위 및 프로젝트에서 벗어서난다고 판단하여 제외

	

### 액터, 커맨드 부착하여 읽기 좋게
![커멘드부착](https://user-images.githubusercontent.com/78134019/109456931-2f7b5c80-7a9d-11eb-94fd-9552795783b1.jpg)

### 어그리게잇으로 묶기
![어그리게잇](https://user-images.githubusercontent.com/78134019/109456954-399d5b00-7a9d-11eb-8815-f5d2c0dc06f5.jpg)

    - 호출, 택시관리, 택시 할당 어그리게잇을 생성하고 그와 연결된 command 와 event 들에 의하여 트랜잭션이 유지되어야 하는 단위로 그들 끼리 묶어줌 


### 바운디드 컨텍스트로 묶기

![바운디드](https://user-images.githubusercontent.com/78134019/109457090-8123e700-7a9d-11eb-82c1-8567db428b25.jpg)


### 폴리시 부착 (괄호는 수행주체, 폴리시 부착을 둘째단계에서 해놔도 상관 없음. 전체 연계가 초기에 드러남)

![폴리시부착](https://user-images.githubusercontent.com/78134019/109457118-8e40d600-7a9d-11eb-9562-f03a83b336d4.jpg)

### 폴리시의 이동

![폴리시이동](https://user-images.githubusercontent.com/78134019/109457134-96991100-7a9d-11eb-9ca7-6f22063795c2.jpg)

### 컨텍스트 매핑 (점선은 Pub/Sub, 실선은 Req/Resp)

![컨택스트매핑](https://user-images.githubusercontent.com/78134019/109457150-9f89e280-7a9d-11eb-9564-5e91755cfca5.jpg)



### 완성된 모형

![완성된모형2](https://user-images.githubusercontent.com/78134019/109457187-b16b8580-7a9d-11eb-835d-5c0c61c6dae9.jpg)



### 기능적 요구사항 검증

![기능적요구사항검증](https://user-images.githubusercontent.com/78134019/109457210-c1836500-7a9d-11eb-8b74-f8971cc6e1b0.jpg)

- 고객이 택시를 호출요청한다.(ok)
- 택시 관리 시스템이 택시 할당을 요청한다.(ok)
- 택시 자동 할당이 완료된다.(ok)
- 호출상태 및 할당상태를 갱신한다.(ok)
- 고객에게 카톡 알림을 한다.(ok)


![기능적요구사항검증_취소](https://user-images.githubusercontent.com/78134019/109457259-d9f37f80-7a9d-11eb-9ef5-d18faeb8cdaf.jpg)

- 고객이 택시를 호출취소요청한다.(ok)
- 택시 관리 시스템이 택시 할당 취소를 요청한다.(ok)
- 택시 할당이 취소된다.(ok)
- 취소상태로 갱신한다.(ok)
- 고객에게 카톡 알림을 한다.(ok)


![기능적요구사항_VIEW](https://user-images.githubusercontent.com/78134019/109457311-f5f72100-7a9d-11eb-8190-8e571eb95d7b.jpg)

  
	- 고객이 호출진행내역을 볼 수 있어야 한다. (ok)


### 비기능 요구사항 검증

![비기능적요구사항](https://user-images.githubusercontent.com/78134019/109457342-0b6c4b00-7a9e-11eb-8ab9-8b26e93d4cf0.jpg)

1) 마이크로 서비스를 넘나드는 시나리오에 대한 트랜잭션 처리 
   택시 할당요청이 완료되지 않은 호출요청 완료처리는 최종 할당이 되지 않는 경우 무한정 대기 등 대고객 서비스 및 신뢰도에 치명적 문제점이 있어 ACID 트랜잭션 적용. 
   호출요청 시 택시 할당요청에 대해서는 Request-Response 방식 처리 
2) 호출요청 완료시 할당확인 및 결과 전송: Taxi manage service 에서taxi Assign 마이크로서비스로 택시할당 요청이 전달되는 과정에 있어서 
  taxi Assig 마이크로 서비스가 별도의 배포주기를 가지기 때문에 Eventual Consistency 방식으로 트랜잭션 처리함. 
3) 나머지 모든 inter-microservice 트랜잭션: 호출상태, 할당/할당취소 여부 등 이벤트에 대해 카톡을 처리하는 등 데이터 일관성의 시점이 크리티컬하지 않은 모든 경우가 대부분이라 판단, 
Eventual Consistency 를 기본으로 채택함. 



## 헥사고날 아키텍처 다이어그램 도출 (Polyglot)

![핵사고날_최종](https://user-images.githubusercontent.com/78134019/109744745-29f55200-7c16-11eb-8981-88924ad28cb3.jpg)





# 구현:

서비스를 로컬에서 실행하는 방법은 아래와 같으며, 실행의 편의성을 위해서
각 서비스별로 bat 파일로 묶어서 실행 합니다. 

```
- run_taxicall.bat
call setenv.bat
cd ..\taxiguider\taxicall
mvn clean spring-boot:run
pause ..

- run_taximanage.bat
call setenv.bat
cd ..\taxiguider\taximanage
mvn clean spring-boot:run
pause ..

- run_taxiassign.bat
call setenv.bat
cd ..\taxiguider\taxiassign
mvn clean spring-boot:run
pause ..

- run_customer.bat
call setenv.bat
SET CONDA_PATH=%ANACONDA_HOME%;%ANACONDA_HOME%\BIN;%ANACONDA_HOME%\condabin;%ANACONDA_HOME%\Library\bin;%ANACONDA_HOME%\Scripts;
SET PATH=%CONDA_PATH%;%PATH%;
cd ..\taxiguider_py\customer\
python policy-handler.py 
pause ..

```

## DDD 의 적용
총 3개의 Domain 으로 관리되고 있으며, 택시요청(Taxicall) , 택시관리(TaxiManage), 택시할당(TaxiAssign) 으로 구성하였습니다. 



![DDD](https://user-images.githubusercontent.com/78134019/109460756-74ef5800-7aa4-11eb-8140-ec3ebb47a63f.jpg)


![DDD_2](https://user-images.githubusercontent.com/78134019/109460847-9ea87f00-7aa4-11eb-8fe4-94dd57009cd4.jpg)



## 폴리글랏 퍼시스턴스

```
위치 : /taxiguider>taximanage>pom.xml
```
![폴리그랏DB_최종](https://user-images.githubusercontent.com/78134019/109745194-d800fc00-7c16-11eb-87bd-2f65884a5f71.jpg)



## 폴리글랏 프로그래밍 - 파이썬

- 로컬 용 소스 
```
위치 : /taxiguider_py>cutomer>policy-handler-local.py
```
![폴리그랏프로그래밍](https://user-images.githubusercontent.com/78134019/109745241-ebac6280-7c16-11eb-8839-6c974340839b.jpg)

- 클라우드 용 소스 
```
위치 : /taxiguider_py>cutomer>policy-handler.py

#-*- coding: euc-kr -*-

from flask import Flask
from redis import Redis, RedisError
from kafka import KafkaConsumer
import os
import socket


# To consume latest messages and auto-commit offsets
consumer = KafkaConsumer('taxiguider',
                         group_id='customer',
                         bootstrap_servers=['my-kafka.kafka.svc.cluster.local:9092'])
for message in consumer:
  print ("%s:%d:%d: key=%s value=%s" % (message.topic, message.partition, message.offset, message.key, message.value.decode('utf-8')))
#	msg = message.value.decode('utf-8')
#	print( '' )

#sys.exit()
```

## 마이크로 서비스 호출 흐름

- taxicall 서비스 호출처리 

호출(taxicall)->택시관리(taximanage) 우선 호출처리(req/res) 되며,
택시할당(taxiassign)에서 택시기사를 할당하게 되면 호출 상태가 호출에서 호출확정 상태가 됩니다

우선, 로컬에서는 다음과 같이 두 개의 호출 상태를 만듭니다.
```
http localhost:8081/택시호출s 휴대폰번호="01012345678" 호출상태=호출 호출위치="마포" 예상요금=25000
http localhost:8081/택시호출s 휴대폰번호="01056789012" 호출상태=호출 호출위치="서대문구" 예상요금=30000
```
![taxicall1](https://user-images.githubusercontent.com/78134019/109771576-51611480-7c40-11eb-8754-94d35a5703ec.png)

![taxicall2](https://user-images.githubusercontent.com/78134019/109771589-545c0500-7c40-11eb-997a-90249ea8f912.png)

우선, 클라우드 상에서 호출은 다음과 같이 합니다. External-IP는 20.194.36.201 입니다.
```
http 20.194.36.201:8080/taxicalls tel="01023456789" status="호출" cost=25500
http 20.194.36.201:8080/taxicalls tel="01023456789" status="호출" cost=25500
```

![1](https://user-images.githubusercontent.com/7607807/109840083-16380300-7c8b-11eb-80d1-5eb6815ac53a.png)


아래 호출 결과는 모두 택시 할당(taxiassign)에서 택시기사의 할당되어 호출 서비스를 확인하연 호출 상태는 호출 확정가 되어 있습니다.

![3](https://user-images.githubusercontent.com/78134019/109771602-58882280-7c40-11eb-93c4-a3831156c151.png)

![4](https://user-images.githubusercontent.com/78134019/109771654-69d12f00-7c40-11eb-9d2c-4807f0c3d726.png)

![5](https://user-images.githubusercontent.com/78134019/109771661-6c338900-7c40-11eb-8a4a-9a758a8d1613.png)

클라우드 상에서도 마찬가지 입니다.

![3](https://user-images.githubusercontent.com/7607807/109840275-50090980-7c8b-11eb-9f37-0ca07115308e.png)

![1](https://user-images.githubusercontent.com/7607807/109841386-564bb580-7c8c-11eb-8262-bf28c1a2bd70.png)
- taxicall 서비스 호출 취소 처리

호출 취소는 택시호출에서 다음과 같이 호출 하나를 취소 함으로써 진행 합니다.

```
http delete http://localhost:8081/택시호출s/1
HTTP/1.1 204
Date: Tue, 02 Mar 2021 16:59:12 GMT
```

클라우드 상에서 호출 취소는 다음과 같습니다.
```
http delete http://20.194.36.201:8080/taxicalls/1
HTTP/1.1 204
Date: Tue, 02 Mar 2021 16:59:12 GMT
```


호출이 취소 되면 아래와 같이 택시 호출이 하나가 삭제 되었고, 

```
http localhost:8081/택시호출s/
http 20.194.36.201:8080/taxicalls
```

![6](https://user-images.githubusercontent.com/78134019/109771698-7a81a500-7c40-11eb-964e-a07e989f997c.png)

![1](https://user-images.githubusercontent.com/7607807/109840796-cb6abb00-7c8b-11eb-8cb9-0d623fe11043.png)

택시관리에서는 해당 호출의 호출 상태가 호출취소로 상태가 변경 됩니다.

```
http localhost:8082/택시관리s/
http 20.194.36.201:8080/taximanags
```

![7](https://user-images.githubusercontent.com/78134019/109771726-83727680-7c40-11eb-88bd-169a8d6184fe.png)

![2](https://user-images.githubusercontent.com/7607807/109840982-f3f2b500-7c8b-11eb-9373-00844726bb04.png)

- 고객 메시지 서비스 처리 

고객(customer)는 호출 확정과 할당 확정에 대한 메시지를 다음과 같이 받을 수 있으며,
할당 된 택시기사의 정보를 또한 확인 할 수 있습니다.
파이썬으로 구현 하였습니다.

![8](https://user-images.githubusercontent.com/78134019/109771811-9ab16400-7c40-11eb-8a49-57156a4d0c8e.png)


## Gateway 적용

서비스에 대한 하나의 접점을 만들기 위한 게이트웨이의 설정은 8088이며, 
택시호출,택시관리 및 택시할당 마이크로서비스에 대한 일원화 된 접점을 제공하기 위한 설정 입니다.
```
택시호출 서비스 : 8081
택시관리 서비스 : 8082
택시할당 서비스 : 8083
```

gateway > applitcation.yml 설정

![gateway_1](https://user-images.githubusercontent.com/78134019/109480363-c73d7280-7abe-11eb-9904-0c18e79072eb.png)

아래 설정은 DDD를 통해서 구현 된 한글화 된 서비스를 클라우드에서 호출 할 경우, 문제가 발생하여 모든 도메인 명 및 서비스를 
영어로 재 구현 하였으며, 이에 대한 게이트웨이 처리를 보여주고 있습니다.

![gateway_2](https://user-images.githubusercontent.com/78134019/109480386-d02e4400-7abe-11eb-9251-a813ac911e0d.png)


- gateway 로컬 테스트

로컬 테스트는 다음과 같이 한글 서비스 호출로 테스트 되었습니다.

```
http localhost:8080/택시호출s
-> gateway 를 호출하나 8081 로 호출됨
```
![gateway_3](https://user-images.githubusercontent.com/78134019/109480424-da504280-7abe-11eb-988e-2a6d7a1f7cea.png)



## 동기식 호출 과 Fallback 처리

호출(taxicall)->택시관리(taximanage) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리하였습니다.
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

로컬 테스트를 위한 파일은 다음과 같이 구현 하였으며,
```
# external > 택시관리Service.java


package taxiguider.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(name="taximanage", url="http://localhost:8082")
@FeignClient(name="taximanage", url="http://localhost:8082", fallback = 택시관리ServiceFallback.class)
public interface 택시관리Service {

    @RequestMapping(method= RequestMethod.POST, path="/택시관리s")
    public void 택시할당요청(@RequestBody 택시관리 택시관리);

}

```

클라우드 배포시 구현은 영문 클래스 해당 URL 호출은 다음과 같이 구현 하였습니다.

```
# external > TaximanageService.java


package taxiguider.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

//@FeignClient(name="taximanage", url="http://localhost:8082")
//@FeignClient(name="taximanage", url="http://localhost:8082", fallback = TaximanageServiceFallback.class)
@FeignClient(name="taximanage", url="http://taximanage:8080", fallback = TaximanageServiceFallback.class)
public interface TaximanageService {

    @RequestMapping(method= RequestMethod.POST, path="/taximanages")
    public void requestTaxiAssign(@RequestBody Taximanage txMange);

}

```

다음은 택시관리Service 인터페이스를 구현한 택시관리ServiceFallback 클래스이며, 클라우드 배포용 영문과 따로 구현 되었습니다.

```
# external > 택시관리ServiceFallback.java


package taxiguider.external;

import org.springframework.stereotype.Component;

@Component
public class 택시관리ServiceFallback implements 택시관리Service {
	 
	
	@Override
	public void 택시할당요청(택시관리 택시관리) {
		// TODO Auto-generated method stub
		System.out.println("Circuit breaker has been opened. Fallback returned instead. " + 택시관리.getId());
	}

}

```

![동기식](https://user-images.githubusercontent.com/78134019/109463569-97837000-7aa8-11eb-83c4-6f6eff1594aa.jpg)


![2021-03-04_004922](https://user-images.githubusercontent.com/7607807/109832226-80e54080-7c83-11eb-9526-e1820a60c938.png)


- 로컬 택시 할당요청

택시호출을 하면 택시관리에 택시 할당 요청을 다음과 같이 동기적으로 진행 합니다.
```
# 택시호출.java

 @PostPersist
    public void onPostPersist(){    	
    	System.out.println("휴대폰번호 " + get휴대폰번호());
        System.out.println("호출위치 " + get호출위치());
        System.out.println("호출상태 " + get호출상태());
        System.out.println("예상요금 " + get예상요금());
        //Following code causes dependency to external APIs
        // it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.   	
    	if(get휴대폰번호() != null)
		{
    		System.out.println("SEND###############################" + getId());
			택시관리 택시관리 = new 택시관리();
	        
			택시관리.setOrderId(String.valueOf(getId()));
	        택시관리.set고객휴대폰번호(get휴대폰번호());
	        if(get호출위치()!=null) 
	        	택시관리.set호출위치(get호출위치());
	        if(get호출상태()!=null) 
	        	택시관리.set호출상태(get호출상태());
	        if(get예상요금()!=null) 
	        	택시관리.set예상요금(get예상요금());
	        
	        // mappings goes here
	        TaxicallApplication.applicationContext.getBean(택시관리Service.class).택시할당요청(택시관리);
		}
```

- 클라우드 배포시 택시 할당요청(영문)

택시호출을 하면 택시관리에 택시 할당 요청을 다음과 같이 동기적으로 진행 합니다.
```
# 택시호출.java

@PostPersist
public void onPostPersist(){
	System.out.println("휴대폰번호 " + getTel());
	System.out.println("호출위치 " + getLocation());
	System.out.println("호출상태 " + getStatus());
	System.out.println("예상요금 " + getCost());
	//Following code causes dependency to external APIs
	// it is NOT A GOOD PRACTICE. instead, Event-Policy mapping is recommended.   	
	if(getTel() != null)
	{
		System.out.println("SEND###############################" + getId());
		Taximanage txMgr = new Taximanage();
		txMgr.setId(getId());
		txMgr.setOrderId(String.valueOf(getId()));
		txMgr.setTel(getTel());
		if(getLocation()!=null) 
			txMgr.setLocation(getLocation());
		if(getStatus()!=null) 
			txMgr.setStatus(getStatus());
		if(getCost()!=null) 
			txMgr.setCost(getCost());
		
		// mappings goes here
		TaxicallApplication.applicationContext.getBean(TaximanageService.class)
			.requestTaxiAssign(txMgr);;
	}

}
```

![동기식2](https://user-images.githubusercontent.com/78134019/109463985-47f17400-7aa9-11eb-8603-c1f83e17951d.jpg)


![2021-03-04_005205](https://user-images.githubusercontent.com/7607807/109832649-e6393180-7c83-11eb-822f-bd41957e7a65.png)

- 동기식 호출 적용으로 택시 관리 시스템이 정상적이지 않으면 , 택시콜도 접수될 수 없음을 다음과 같이 확인 할 수 있습니다.

```
- 택시 관리 시스템 down 후 taxicall 호출 
#taxicall

C:\Users\Administrator>http localhost:8081/택시호출s 휴대폰번호="01012345678" 호출상태="호출"
```

![택시관리죽으면택시콜놉](https://user-images.githubusercontent.com/78134019/109464780-905d6180-7aaa-11eb-9c90-e7d1326deea1.jpg)


```
# 택시 관리 (taximanage) 재기동 후 호출

http localhost:8081/택시호출s 휴대폰번호="01012345678" 호출상태="호출"
```

![택시관리재시작](https://user-images.githubusercontent.com/78134019/109464984-e5997300-7aaa-11eb-9363-b7bfe15de105.jpg)

-fallback 

![fallback캡쳐](https://user-images.githubusercontent.com/78134019/109480299-b5f46600-7abe-11eb-906e-9e1e6da245b2.png)


## 비동기식 호출 / 장애격리  / 성능

택시 관리 (Taxi manage) 이후 택시 할당(Taxi Assign) 은 비동기식 처리이므로 , 
택시 호출(Taxi call) 의 서비스 호출에는 영향이 없도록 구성 합니다.
 
고객이 택시 호출(Taxicall) 후 상태가 [호출]->[호출중] 로 변경되고 할당이 완료되면 [호출확정] 로 변경이 되지만 , 
택시 할당(TaxiAssign)이 정상적이지 않으므로 [호출중]로 남게 됩니다. 

<고객 택시 호출 Taxi call>
![비동기_호출2](https://user-images.githubusercontent.com/78134019/109468467-f4365900-7aaf-11eb-877a-049637b5ee6a.png)

<택시 할당이 정상적이지 않아 호출중으로 남아있음>
![택시호출_택시할당없이_조회](https://user-images.githubusercontent.com/78134019/109471791-99ebc700-7ab4-11eb-924f-03715de42eba.png)



## 정보 조회 / View 조회
고객은 택시가 할당되는 동안의 내용을 조회 할 수 있습니다.

![고객View](https://user-images.githubusercontent.com/78134019/109483385-80ea1280-7ac2-11eb-9419-bf3ff5a0dbbc.png)


## 소스 패키징

- 클라우드 배포를 위해서 다음과 같이 패키징 작업을 하였습니다.
```
cd gateway
mvn clean && mvn package
cd ..
cd taxicall
mvn clean && mvn package
cd ..
cd taximanage
mvn clean && mvn package
cd ..
cd taxiassign
mvn clean && mvn package
cd ..
```

<gateway>
![mvn_gateway](https://user-images.githubusercontent.com/78134019/109744124-244b3c80-7c15-11eb-80a9-bed42413aa58.png)
	
<taxicall>
	
![mvn_taxicall](https://user-images.githubusercontent.com/78134019/109744165-31682b80-7c15-11eb-9d94-7bc23efca6b6.png)

<taximanage>
	
![mvn_taximanage](https://user-images.githubusercontent.com/78134019/109744195-3b8a2a00-7c15-11eb-9554-1c3ba088af52.png)

<taxiassign>
	
![mvn_taxiassign](https://user-images.githubusercontent.com/78134019/109744226-46dd5580-7c15-11eb-8b47-5100ed01e3ae.png)


# 클라우드 배포/운영 파이프라인

- 애저 클라우드에 배포하기 위해서 다음과 같이 주요 정보를 설정 하였습니다.

```
리소스 그룹명 : skccteam03-rsrcgrp
클러스터 명 : skccteam03-aks
레지스트리 명 : skccteam03
```

- az login
우선 애저에 로그인 합니다.
```
{
    "cloudName": "AzureCloud",
    "homeTenantId": "6011e3f8-2818-42ea-9a63-66e6acc13e33",
    "id": "718b6bd0-fb75-4ec9-9f6e-08ae501f92ca",
    "isDefault": true,
    "managedByTenants": [],
    "name": "2",
    "state": "Enabled",
    "tenantId": "6011e3f8-2818-42ea-9a63-66e6acc13e33",
    "user": {
      "name": "skTeam03@gkn2021hotmail.onmicrosoft.com",
      "type": "user"
    }
  }
```

- 토큰 가져오기
```
az aks get-credentials --resource-group skccteam03-rsrcgrp --name skccteam03-aks
```

- aks에 acr 붙이기
```
az aks update -n skccteam03-aks -g skccteam03-rsrcgrp --attach-acr skccteam03
```

![aks붙이기](https://user-images.githubusercontent.com/78134019/109653395-540e2c00-7ba4-11eb-97dd-2dcfdf5dc539.jpg)

- 네임스페이스 만들기

```
kubectl create ns team03
kubectl get ns
```
![image](https://user-images.githubusercontent.com/78134019/109776836-5cb73e80-7c46-11eb-9562-d462525d6dab.png)

* 도커 이미지 만들고 레지스트리에 등록하기
```
cd taxicall_eng
az acr build --registry skccteam03 --image skccteam03.azurecr.io/taxicalleng:v1 .
az acr build --registry skccteam03 --image skccteam03.azurecr.io/taxicalleng:v2 .
cd ..
cd taximanage_eng
az acr build --registry skccteam03 --image skccteam03.azurecr.io/taximanageeng:v1 .
cd ..
cd taxiassign_eng
az acr build --registry skccteam03 --image skccteam03.azurecr.io/taxiassigneng:v1 .
cd ..
cd gateway_eng
az acr build --registry skccteam03 --image skccteam03.azurecr.io/gatewayeng:v1 .
cd ..
cd customer_py
az acr build --registry skccteam03 --image skccteam03.azurecr.io/customer-policy-handler:v1 .
```

![docker_gateway](https://user-images.githubusercontent.com/78134019/109777813-76a55100-7c47-11eb-8d8d-59eaabefab54.png)

![docker_taxiassign](https://user-images.githubusercontent.com/78134019/109777820-77d67e00-7c47-11eb-9d77-85403dcf2da4.png)

![docker_taxicall](https://user-images.githubusercontent.com/78134019/109777826-786f1480-7c47-11eb-9992-41f75907d16f.png)

![docker_taximanage](https://user-images.githubusercontent.com/78134019/109777827-786f1480-7c47-11eb-9c9b-d3357eda0bd5.png)

![docker_customer](https://user-images.githubusercontent.com/78134019/109777829-7907ab00-7c47-11eb-936f-723396cb272a.png)


-각 마이크로 서비스를 yml 파일을 사용하여 배포 합니다.


![deployment_yml](https://user-images.githubusercontent.com/78134019/109652001-9171ba00-7ba2-11eb-8c29-7128ceb4ec97.jpg)

- deployment.yml로 서비스 배포
```
cd ../../
cd customer_py/kubernetes
kubectl apply -f deployment.yml --namespace=team03
kubectl apply -f service.yaml --namespace=team03
cd ../../
cd taxicall_eng/kubernetes
kubectl apply -f deployment.yml --namespace=team03
kubectl apply -f service.yaml --namespace=team03

cd ../../
cd taximanage_eng/kubernetes
kubectl apply -f deployment.yml --namespace=team03
kubectl apply -f service.yaml --namespace=team03

cd ../../
cd taxiassign_eng/kubernetes
kubectl apply -f deployment.yml --namespace=team03
kubectl apply -f service.yaml --namespace=team03

cd ../../
cd gateway_eng/kubernetes
kubectl apply -f deployment.yml --namespace=team03
kubectl apply -f service.yaml --namespace=team03
```
<Deploy cutomer>
	
![deploy_customer](https://user-images.githubusercontent.com/78134019/109744443-a471a200-7c15-11eb-94c9-a0c0a7999d04.png)

<Deploy gateway>
	
![deploy_gateway](https://user-images.githubusercontent.com/78134019/109744457-acc9dd00-7c15-11eb-8502-ff65e779e9d2.png)

<Deploy taxiassign>
	
![deploy_taxiassign](https://user-images.githubusercontent.com/78134019/109744471-b3585480-7c15-11eb-8d68-bba9c3d8ce01.png)

<Deploy taxicall>
	
![deploy_taxicall](https://user-images.githubusercontent.com/78134019/109744487-bb17f900-7c15-11eb-8bd0-ff0a9fc9b2e3.png)


![deploy_taximanage](https://user-images.githubusercontent.com/78134019/109744591-e69ae380-7c15-11eb-834a-44befae55092.png)



- 서비스확인
```
kubectl get all -n team03
```
![image](https://user-images.githubusercontent.com/78134019/109777026-9be58f80-7c46-11eb-9eac-a55ebcf91989.png)



## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현하였습니다.

- Hystrix 를 설정:  

요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정
```
# application.yml
feign:
  hystrix:
    enabled: true

# To set thread isolation to SEMAPHORE
#hystrix:
#  command:
#    default:
#      execution:
#        isolation:
#          strategy: SEMAPHORE

hystrix:
  command:
    # 전역설정
    default:
      execution.isolation.thread.timeoutInMilliseconds: 610

```
![hystrix](https://user-images.githubusercontent.com/78134019/109652345-0218d680-7ba3-11eb-847b-708ba071c119.jpg)


부하테스트


* Siege 리소스 생성

```
kubectl run siege --image=apexacme/siege-nginx -n team03
```

* 실행

```
kubectl exec -it pod/siege-5459b87f86-hlfm9 -c siege -n team03 -- /bin/bash
```

*부하 실행

```
siege -c200 -t60S -r10 -v --content-type "application/json" 'http://20.194.36.201:8080/taxicalls POST {"tel": "0101231234"}'
```

- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 택시호출(taxicall) 서비스에서 처리되면서 
다시 taxicall에서 서비스를 받기 시작 합니다

![secs1](https://user-images.githubusercontent.com/78134019/109786899-01d71480-7c51-11eb-9e6c-0a819e85b020.png)


- report

![secs2](https://user-images.githubusercontent.com/78134019/109786922-07345f00-7c51-11eb-900a-315f7d0d6484.png)





### 오토스케일 아웃



```
# autocale out 설정
 deployment.yml 설정
```


![auto1](https://user-images.githubusercontent.com/78134019/109794479-3ea70980-7c59-11eb-8d32-fbc039106c8c.jpg)


```
kubectl autoscale deploy taxicall --min=1 --max=10 --cpu-percent=15 -n team03
```


```
root@labs--279084598:/home/project# kubectl exec -it pod/siege-5459b87f86-hlfm9 -c siege -n team03 -- /bin/bash
root@siege-5459b87f86-hlfm9:/# siege -c100 -t120S -r10 -v --content-type "application/json" 'http://20.194.36.201:8080/taxicalls POST {"tel": "0101231234"}'
```
![auto4](https://user-images.githubusercontent.com/78134019/109794919-b70dca80-7c59-11eb-9710-8ff6b4dd5f54.jpg)



- 오토스케일링에 대한 모니터링:
```
kubectl get deploy taxicall -w -n team03
```
![auto_final](https://user-images.githubusercontent.com/78134019/109796515-98a8ce80-7c5b-11eb-9512-a0a927217a38.jpg)



## 무정지 재배포

- deployment.yml에 readiness 옵션을 추가 


![무정지 배포1](https://user-images.githubusercontent.com/78134019/109809110-45d71300-7c6b-11eb-955c-9b8a3b3db698.png)


- seige 실행
```
siege -c100 -t120S -r10 -v --content-type "application/json" 'http://20.194.36.201:8080/taxicalls POST {"tel": "0101231234"}'
```


- Availability: 100.00 % 확인


![무정지 배포2](https://user-images.githubusercontent.com/78134019/109810318-bd597200-7c6c-11eb-88e4-197386b1e338.png)


![무정지 배포3](https://user-images.githubusercontent.com/78134019/109810688-2fca5200-7c6d-11eb-9c67-d252d703064a.png)



## Config Map

- apllication.yml 설정

* default 프로파일

![configmap1](https://user-images.githubusercontent.com/31096538/109798636-5df46580-7c5e-11eb-982d-16482f98b13f.JPG)

* docker 프로파일

![configmap2](https://user-images.githubusercontent.com/31096538/109798699-6e0c4500-7c5e-11eb-9d0d-47b90d637ae9.JPG)

- Deployment.yml 설정

![configmap3](https://user-images.githubusercontent.com/31096538/109798713-72d0f900-7c5e-11eb-8458-8fb9d6225c49.JPG)

- config map 생성 후 조회
```
kubectl create configmap apiurl --from-literal=url=http://taxicall:8080 --from-literal=fluentd-server-ip=10.xxx.xxx.xxx -n team03
```
![configmap4](https://user-images.githubusercontent.com/31096538/109798727-76fd1680-7c5e-11eb-9818-327870ea2e4d.JPG)

- 설정한 url로 주문 호출
```
http 20.194.36.201:8080/taxicalls tel="01012345678" status="call" location="mapo" cost=25000
```

![configmap5](https://user-images.githubusercontent.com/31096538/109798744-7c5a6100-7c5e-11eb-8aaa-03fa8277cee6.JPG)

- configmap 삭제 후 app 서비스 재시작
```
kubectl delete configmap apiurl -n team03
kubectl get pod/taxicall-74f7dbc967-mtbmq -n team03 -o yaml | kubectl replace --force -f-
```
![configmap6](https://user-images.githubusercontent.com/31096538/109798766-811f1500-7c5e-11eb-8008-1b9073cb6722.JPG)

- configmap 삭제된 상태에서 주문 호출   
```
http 20.194.36.201:8080/taxicalls tel="01012345678" status="call" location="mapo" cost=25000
kubectl get all -n team03
```
![configmap7](https://user-images.githubusercontent.com/31096538/109798785-85e3c900-7c5e-11eb-8769-ab416b1e17b2.JPG)


![configmap8](https://user-images.githubusercontent.com/31096538/109798805-8bd9aa00-7c5e-11eb-8d05-1db2457d3611.JPG)


![configmap9](https://user-images.githubusercontent.com/31096538/109798824-9005c780-7c5e-11eb-9d5b-6f14f9b6bba9.JPG)


## Self-healing (Liveness Probe)


- deployment.yml 에 Liveness Probe 옵션 추가
```
livenessProbe:
	tcpSocket:
	  port: 8081
	initialDelaySeconds: 5
	periodSeconds: 5
```
![selfhealing](https://user-images.githubusercontent.com/78134019/109805068-589b1900-7c66-11eb-9565-d44adde4ffc5.jpg)

