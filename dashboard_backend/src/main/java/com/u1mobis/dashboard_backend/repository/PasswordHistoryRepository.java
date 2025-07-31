package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.PasswordHistory;
import com.u1mobis.dashboard_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 패스워드 히스토리 레포지토리
 */
@Repository
public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {
    
    /**
     * 사용자의 최근 패스워드 히스토리 조회 (개수 제한)
     */
    @Query("SELECT ph FROM PasswordHistory ph WHERE ph.user = :user ORDER BY ph.createdAt DESC")
    List<PasswordHistory> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * 사용자의 최근 N개 패스워드 히스토리 조회
     */
    @Query(value = "SELECT * FROM password_history WHERE user_id = :userId ORDER BY created_at DESC LIMIT :limit", 
           nativeQuery = true)
    List<PasswordHistory> findTopNByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, @Param("limit") int limit);
    
    /**
     * 사용자의 오래된 패스워드 히스토리 삭제 (지정된 개수만 유지)
     */
    @Modifying
    @Query(value = "DELETE FROM password_history WHERE user_id = :userId AND id NOT IN " +
                   "(SELECT id FROM (SELECT id FROM password_history WHERE user_id = :userId " +
                   "ORDER BY created_at DESC LIMIT :keepCount) AS t)", 
           nativeQuery = true)
    void deleteOldPasswordHistory(@Param("userId") Long userId, @Param("keepCount") int keepCount);
    
    /**
     * 특정 기간 이전의 패스워드 히스토리 삭제
     */
    @Modifying
    @Query("DELETE FROM PasswordHistory ph WHERE ph.createdAt < :cutoffDate")
    void deletePasswordHistoryBefore(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * 사용자의 패스워드 히스토리 개수 조회
     */
    long countByUser(User user);
}