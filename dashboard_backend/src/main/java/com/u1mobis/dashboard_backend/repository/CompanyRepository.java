package com.u1mobis.dashboard_backend.repository;

import com.u1mobis.dashboard_backend.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // 회사 이름으로 조회
    Optional<Company> findByCompanyName(String companyName);

    // 회사 존재 여부 확인
    boolean existsByCompanyName(String companyName);

    // 회사 코드로 조회
    Optional<Company> findByCompanyCode(String companyCode);

    // 회사 코드 존재 여부 확인
    boolean existsByCompanyCode(String companyCode);

    // 회사 ID와 이름으로 조회
    @Query("SELECT c FROM Company c WHERE c.companyId = :companyId AND c.companyName = :companyName")
    Optional<Company> findByCompanyIdAndCompanyName(@Param("companyId") Long companyId, 
                                                   @Param("companyName") String companyName);
}