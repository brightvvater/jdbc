package hello.jdbc.service;

import hello.jdbc.domain.Member;
import hello.jdbc.repository.MemberRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
/*
* 트랜잭션 - 파라미터 연동, 풀을 고려한 종료
* */
@Slf4j
@RequiredArgsConstructor
public class MemberServiceV2 {

    private final DataSource dataSource;
    private final MemberRepositoryV2 memberRepositoryV2;

    public void accountTransfer(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection();
        try {
            con.setAutoCommit(false);
            bisLogic(con, fromId, toId, money);
            con.commit(); //성공 시 커밋
        }catch (Exception e) {
            con.rollback(); //실패 시 롤백
            throw new IllegalStateException(e);
        }finally {
            if (con != null) {
                release(con);
            }
        }


    }

    private void bisLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        //비즈니스 로직
        Member fromMember = memberRepositoryV2.findById(con, fromId);
        Member toMember = memberRepositoryV2.findById(con, toId);

        memberRepositoryV2.update(con, fromId, fromMember.getMoney() - money);
        validtion(toMember);
        memberRepositoryV2.update(con, toId, toMember.getMoney() + money);
    }

    private static void release(Connection con) {
        try {
            con.setAutoCommit(true); //커넥션 풀 반납 전
            con.close();
        }catch (Exception e) {
            log.info("error", e);
        }
    }

    private void validtion(Member toMember) {
        if(toMember.getMemberId().equals("ex")) {
            throw new IllegalStateException("이체중 예외 발생");
        }
    }

}
