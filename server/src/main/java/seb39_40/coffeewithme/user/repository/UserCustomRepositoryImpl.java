package seb39_40.coffeewithme.user.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import seb39_40.coffeewithme.user.domain.UserStatus;

import javax.transaction.Transactional;

import static seb39_40.coffeewithme.user.domain.QUser.user;

@Repository
@RequiredArgsConstructor
public class UserCustomRepositoryImpl implements UserCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Transactional
    @Override
    public void updateUserStatus(String email, UserStatus status) {
        queryFactory.update(user).set(user.status,status).where(user.email.eq(email)).execute();
    }
}
