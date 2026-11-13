package com.u1mobis.dashboard_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.u1mobis.dashboard_backend.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUserName(String userName);

    // 인증용: 회사 정보까지 한 번에 읽어서 요청 처리 중 언제든 회사 이름/코드를 쓸 수 있게 한다
    @EntityGraph(attributePaths = "company")
    Optional<User> findWithCompanyByUserName(String userName);
}
