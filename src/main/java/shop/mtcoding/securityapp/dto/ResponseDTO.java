package shop.mtcoding.securityapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseDTO<T> {
    private Integer status;
    private String msg; // 제목
    private T data; // 상세내용

    // 성공
    public ResponseDTO() { // 일반적으로 얘만 return 해주면 되고
        this.status = 200;
        this.msg = "성공";
    }

    public ResponseDTO<?> data(T data) { // 데이터를 넣고 싶을 경우
        this.data = data;
        return this;
    }

    // 실패
    public ResponseDTO<?> fail(Integer status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
        return this;
    }
}