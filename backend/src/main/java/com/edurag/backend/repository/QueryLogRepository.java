package com.edurag.backend.repository;

import com.edurag.backend.entity.QueryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 查询日志数据库操作接口
 */
@Repository
public interface QueryLogRepository extends JpaRepository<QueryLog, Long> {

    // 分页查询某用户的历史记录（Page 是 JPA 内置分页对象）
    Page<QueryLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // 统计某知识库被查询的总次数（用于 Dashboard 数据）
    long countByKbId(Long kbId);

    /**
     * 统计某用户消耗的总 token 数
     * @Query 注解：当方法名无法表达复杂查询时，手写 JPQL（类似 SQL 但操作的是 Java 对象）
     */
    @Query("SELECT COALESCE(SUM(q.tokensUsed), 0) FROM QueryLog q WHERE q.userId = :userId")
    Long sumTokensUsedByUserId(Long userId);
}