package pickupcenter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;

import org.springframework.beans.BeanUtils;

@Entity
@Table(name="Pickupmanage_table")
public class Pickupmanage {
	
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;
    
    private String orderId;
    private String tel;
    private String location;
    private String status; //호출,호출중,호출확정,호출취소
    private Integer cost;
    
    private String workerid;
    private String worker;
    private String workertel;
  

	@PrePersist
    public void onPrePersist(){
    	System.out.println("###############################=================================");

        System.out.println("휴대폰번호 " + tel);
        System.out.println("호출위치 " + location);
        System.out.println("호출상태 " + status);
        System.out.println("예상요금 " + cost);
    	
        System.out.println("orderId " + orderId);
        System.out.println("id " + getId());
        //System.out.println("호출위치 " + 호출위치);
        //System.out.println("호출상태 " + 호출상태);
        //System.out.println("예상요금 " + 예상요금);
    	
        
        if("호출취소".equals(status)){
			PickupmanageCancelled pickupmanageCancelled = new PickupmanageCancelled();
            BeanUtils.copyProperties(this, pickupmanageCancelled);
            pickupmanageCancelled.publish();

        }else{

        	status = "호출중";
        	PickupmanageAssigned pickupmanageAssigned = new PickupmanageAssigned();
        	pickupmanageAssigned.setId(Long.valueOf(orderId));
        	
        	pickupmanageAssigned.setLocation(location);
        	pickupmanageAssigned.setTel(tel);
        	pickupmanageAssigned.setCost(cost);
        	pickupmanageAssigned.setStatus(status);
            BeanUtils.copyProperties(this, pickupmanageAssigned);
            pickupmanageAssigned.publishAfterCommit();
            
            
            // 테스트 코드~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//            try {
//                Thread.currentThread().sleep((long) (400 + Math.random() * 220));
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
        }    
    }
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTel() {
		return tel;
	}


	public void setTel(String tel) {
		this.tel = tel;
	}


	public String getLocation() {
		return location;
	}


	public void setLocation(String location) {
		this.location = location;
	}


	public String getStatus() {
		return status;
	}


	public void setStatus(String status) {
		this.status = status;
	}


	public Integer getCost() {
		return cost;
	}


	public void setCost(Integer cost) {
		this.cost = cost;
	}


	public String getWorkerid() {
		return workerid;
	}


	public void setWorkerid(String workerid) {
		this.workerid = workerid;
	}


	public String getWorker() {
		return worker;
	}


	public void setWorker(String worker) {
		this.worker = worker;
	}


	public String getWorkertel() {
		return workertel;
	}


	public void setWorkertel(String workertel) {
		this.workertel = workertel;
	}


	public String getOrderId() {
		return orderId;
	}


	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}


}
