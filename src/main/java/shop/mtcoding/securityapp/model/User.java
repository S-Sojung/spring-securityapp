package shop.mtcoding.securityapp.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor // hibernate가 ORM시에 new 하려고 필요
@Getter
@Table(name = "user_tb")
@Entity // hibernate가 관리 (영속, 비영속, 준영속)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private String role; // USER, MANAGER, ADMIN
    private Boolean status; // 활성계정 비활성계정

    // 자동으로 LocalDateTime -> Timestamp로 인서트된다.
    private LocalDateTime createAt; // 회원가입 날짜
    private LocalDateTime updateAt; // 회원수정 날짜

    @PrePersist // insert 시 동작
    public void onCreate() {
        this.createAt = LocalDateTime.now();
    }

    @PreUpdate // update 시 동작
    public void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }

    @Builder
    public User(Long id, String username, String password, String email, String role, Boolean status,
            LocalDateTime createAt, LocalDateTime updateAt) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createAt = createAt;
        this.updateAt = updateAt;
    }

}
