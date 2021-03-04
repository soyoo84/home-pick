
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
http localhost:8081/pickupcalls/ tel="01050958718" location="이수" status="호출" cost="20000"
http localhost:8081/pickupcalls/ tel="01089385708" location="사당" status="호출" cost="30000"
```

![화면 캡처 2021-03-05 015808](https://user-images.githubusercontent.com/30484527/110001758-0df9c980-7d58-11eb-9ef2-307bcfa676a4.jpg)


호출 결과는 모두 픽업할당(pickupassign)에서 택배기사의 할당으로 처리되어 호출 확정 상태가 되어 있음.

![화면 캡처 2021-03-05 020134](https://user-images.githubusercontent.com/30484527/110001767-118d5080-7d58-11eb-8c84-efa945c386ab.jpg)

- pickupcall 서비스 호출 취소 처리

호출 취소는 픽업호출에서 다음과 같이 호출 하나를 취소 함으로써 진행 함.

![화면 캡처 2021-03-05 020605](https://user-images.githubusercontent.com/30484527/110001771-118d5080-7d58-11eb-8204-e8becbf6b0ca.jpg)

```
http delete http://localhost:8081/pickupcalls/1
```
호출이 취소 되면 픽업호출이 하나가 삭제 되었고, 택시관리에서는 해당 호출에 대해서 호출취소로 상태가 변경 됨.
![화면 캡처 2021-03-05 020638](https://user-images.githubusercontent.com/30484527/110001775-1225e700-7d58-11eb-8497-77d9dc54c23b.jpg)

```
http localhost:8081/pickupmanages/
```

- 고객 메시지 서비스 처리
고객(customer)는 호출 확정과 할당 확정에 대한 메시지를 다음과 같이 받을 수 있으며,
할당 된 택시기사의 정보를 또한 확인 할 수 있다.
파이썬으로 구현 하였음.



## Gateway 적용

서비스에 대한 하나의 접점을 만들기 위한 게이트웨이의 설정은 8088로 설정 하였으며, 다음 마이크로서비스에 대한 설정 입니다.
```
픽업호출 서비스 : 8081
픽업관리 서비스 : 8082
픽업할당 서비스 : 8084
```

gateway > applitcation.yml 설정

![gateway_1](https://user-images.githubusercontent.com/30484527/110018221-6afe7b00-7d6a-11eb-9d66-2b80a5543447.png)
![gateway_2](https://user-images.githubusercontent.com/30484527/110018225-6c2fa800-7d6a-11eb-9a97-722dac8a342e.png)


gateway 테스트

```
http localhost:8080/택시호출s
-> gateway 를 호출하나 8081 로 호출됨
```
![gateway_3](https://user-images.githubusercontent.com/30484527/110018454-a5681800-7d6a-11eb-87f2-c318ad5c91ea.png)


## 동기식 호출 과 Fallback 처리

호출(pickupcall)->택시관리(pickupmanage) 간의 호출은 동기식 일관성을 유지하는 트랜잭션으로 처리함.
호출 프로토콜은 이미 앞서 Rest Repository 에 의해 노출되어있는 REST 서비스를 FeignClient 를 이용하여 호출하도록 한다. 

![동기1](https://user-images.githubusercontent.com/30484527/110020893-71dabd00-7d6d-11eb-8486-ea439dd311fd.png)

- 픽업호출 --> 픽업관리가 호출

![동기2](https://user-images.githubusercontent.com/30484527/110020899-730bea00-7d6d-11eb-9ae0-f9550a1546bd.png)

- 동기식 호출 적용으로 픽업관리 시스템이 비정상 경우  픽업도 호출될 수 없음을 확인 

![동기3](https://user-images.githubusercontent.com/30484527/110020902-73a48080-7d6d-11eb-8040-db79c526f062.png)


![동기4](https://user-images.githubusercontent.com/30484527/110020906-743d1700-7d6d-11eb-8267-93a5d820f338.png)

-fallback 

![풀백](https://user-images.githubusercontent.com/30484527/110021112-af3f4a80-7d6d-11eb-8b80-2ecc919b04e0.png)


## 비동기식 호출 / 시간적 디커플링 / 장애격리

픽업관리 (pickupmanage) 이후 픽업할당(pickupassign) 은 비동기식 처리이므로 , 픽업호출(pickupcall) 의 서비스 호출에는 영향이 없다
 
고객이 픽업호출(pickupcall) 후 상태가 [호출]->[호출중] 로 변경되고 할당이 완료되면 [픽업확정] 로 변경이 되지만 , 택시 할당(Taxi Assign)이 정상적이지 않으므로 [호출중]로 남아있음. 
--> (시간적 디커플링)

![비동기1](https://user-images.githubusercontent.com/30484527/110021617-51f7c900-7d6e-11eb-9746-b646d5ed9bcb.png)


# 운영

- 네임스페이스
```
kubectl create ns skuser14ns
kubectl config set-context skuser14-aks --namespace=skuser14ns
kubectl get ns

```
![운영1](https://user-images.githubusercontent.com/30484527/110022973-ed3d6e00-7d6f-11eb-8013-bd8d7b6ec59b.png)


-deployment.yml을 사용하여 배포 
```
cd gateway
mvn clean && mvn package
cd ..
cd pickupcall
mvn clean && mvn package
cd ..
cd pickupmanage
mvn clean && mvn package
cd ..
cd pickupassign
mvn clean && mvn package

cd ..
cd gateway
az acr build --registry skuser14 --image skuser14.azurecr.io/gateway:v1 .
//az acr build --registry skuser14 --image skuser14.azurecr.io/gateway:v2 .
cd ..
cd pickupcall
az acr build --registry skuser14 --image skuser14.azurecr.io/pickupcall:v1 .
az acr build --registry skuser14 --image skuser14.azurecr.io/pickupcall:v2 .
cd ..
cd pickupmanage
az acr build --registry skuser14 --image skuser14.azurecr.io/pickupmanage:v1 .
cd ..
cd pickupassign
az acr build --registry skuser14 --image skuser14.azurecr.io/pickupassign:v1 .
cd ..
cd customer
az acr build --registry skuser14 --image skuser14.azurecr.io/customer-policy-handler:v1 .


cd gateway/kubernetes
kubectl apply -f deployment.yml --namespace=skuser14ns
kubectl apply -f service.yaml --namespace=skuser14ns

cd ../../
cd pickupcall/kubernetes
kubectl apply -f deployment.yml,service.yaml --namespace=skuser14ns
kubectl apply -f service.yaml --namespace=skuser14ns

cd ../../
cd pickupmanage/kubernetes
kubectl apply -f deployment.yml,service.yaml --namespace=skuser14ns
kubectl apply -f service.yaml --namespace=skuser14ns

cd ../../
cd pickupassign/kubernetes
kubectl apply -f deployment.yml,service.yaml --namespace=skuser14ns
kubectl apply -f service.yaml --namespace=skuser14ns

cd ../../
cd customer/kubernetes
kubectl apply -f deployment.yml,service.yaml --namespace=skuser14ns
kubectl apply -f service.yaml --namespace=skuser14ns

```
![운영2](https://user-images.githubusercontent.com/30484527/110023953-1c081400-7d71-11eb-9113-bd50d0f5be72.png)


## 동기식 호출 / 서킷 브레이킹 / 장애격리

* 서킷 브레이킹 프레임워크의 선택: Spring FeignClient + Hystrix 옵션을 사용하여 구현함

- Hystrix 를 설정:  
- 요청처리 쓰레드에서 처리시간이 610 밀리가 넘어서기 시작하여 어느정도 유지되면 CB 회로가 닫히도록 (요청을 빠르게 실패처리, 차단) 설정


![전역설정](https://user-images.githubusercontent.com/30484527/110024615-b0727680-7d71-11eb-9ed2-33f5908c7f21.png)


부하테스트
-----------------------------------------
* siege 툴 사용법:
```
 siege가 생성되어 있지 않으면:
 kubectl run siege --image=apexacme/siege-nginx -n skuser14
 siege 들어가기:
 kubectl exec -it pod/siege-5459b87f86-ht7ng -c siege -n skuser14ns -- /bin/bash
 동시사용자 100명, 60초 동안 실시
 siege -c100 -t60S -r10 -v --content-type "application/json" 'http://pickupcall:8080/pickupcalls POST {"tel": "01050958718"}'
 siege 종료: Ctrl + C -> exit
```
- 부하 발생하여 CB가 발동하여 요청 실패처리하였고, 밀린 부하가 픽업호출에서 처리되면서 다시 받기 시작 

![오토3](https://user-images.githubusercontent.com/30484527/110033745-23351f00-7d7d-11eb-81e9-5233a45ba4c0.png)

![부하](https://user-images.githubusercontent.com/30484527/110025611-efed9280-7d72-11eb-91aa-76859aa61f8e.png)

- CB 잘 적용됨을 확인

![부하3](https://user-images.githubusercontent.com/30484527/110031687-91c4ad80-7d7a-11eb-8db1-6092a687f3b4.png)

### 오토스케일 아웃

```
# autocale out 설정
pickupmanage > deployment.yml 설정
```
![오토2](https://user-images.githubusercontent.com/30484527/110032085-18798a80-7d7b-11eb-9b0a-708959d35c8c.png)

```
kubectl autoscale deploy store --min=1 --max=10 --cpu-percent=15 -n skuser14ns
```
![오토2](https://user-images.githubusercontent.com/30484527/110033149-5aef9700-7d7c-11eb-9b2b-2f68184e5b05.png)


- CB 에서 했던 방식대로 워크로드를 2분 동안 걸어준다.

![image](https://user-images.githubusercontent.com/30484527/110030753-73aa7d80-7d79-11eb-8b76-5438f09d1c3e.png)

- 오토스케일이 어떻게 되고 있는지 모니터링을 걸어둔다:
```
kubectl get deploy pickupmanage -w -n skuser14ns
```
- 어느정도 시간이 흐른 후 스케일 아웃이 벌어지는 것을 확인할 수 있다. max=10 
- 부하를 줄이니 늘어난 스케일이 점점 줄어들었다.

![오토4](https://user-images.githubusercontent.com/30484527/110039215-42837a80-7d84-11eb-96df-081e49093718.png)

- 다시 부하를 주고 확인하니 Availability가 높아진 것을 확인 할 수 있었다.


## 무정지 재배포

* 먼저 무정지 재배포가 100% 되는 것인지 확인하기 위해서 Autoscale 이나 CB 설정을 제거함

- seige 로 배포작업 직전에 워크로드를 모니터링 함.
```
kubectl apply -f pickupmanage/deployment.yml
```
- readiness 옵션이 없는 경우 배포 중 서비스 요청처리 실패

![image](https://user-images.githubusercontent.com/73699193/98105334-2a394700-1edb-11eb-9633-f5c33c5dee9f.png)


- deployment.yml에 readiness 옵션을 추가 

![re](https://user-images.githubusercontent.com/30484527/110039614-d6554680-7d84-11eb-8bcf-ed9797e29451.png)

- readiness적용된 deployment.yml 적용

```
kubectl apply -f kubernetes/deployment.yml
```
- 새로운 버전의 이미지로 교체
```
az acr build --registry skuser14 --image skuser14.azurecr.io/pickupmanage:v3 .
kubectl set image deploy pickupmanage pickupmanage=skuser14.azurecr.io/pickupmanage:v3 -n skuser14ns
```
![re2](https://user-images.githubusercontent.com/30484527/110040487-41534d00-7d86-11eb-8ec1-a2a874cc75fb.png)

- 기존 버전과 새 버전의 store pod 공존 중

![re3](https://user-images.githubusercontent.com/30484527/110041112-2503e000-7d87-11eb-80af-9b40fdb30a6c.png)

- 부하시 pod 증가
 
![re4](https://user-images.githubusercontent.com/30484527/110041488-7ca24b80-7d87-11eb-8b2b-6cb46168be18.png)


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

