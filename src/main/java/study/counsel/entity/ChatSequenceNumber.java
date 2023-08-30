package study.counsel.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatSequenceNumber {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chatSequenceNumber_id")
    private Long id;

    // 엔티티에 접근하기 위한 팩토리 메서드(직접 객체를 생성하는 대신 객체를 반환하는 역할)
    public static ChatSequenceNumber createNew() {
        return new ChatSequenceNumber();
    }

}
