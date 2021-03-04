
package pickupcenter;

public class PickupcallCancelled extends AbstractEvent {

    private Long id;
    private String status; //호출취소
    private String tel;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status ) {
        this.status  = status ;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
