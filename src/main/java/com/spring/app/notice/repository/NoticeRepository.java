package com.spring.app.notice.repository;

import com.spring.app.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {
    List<Notice> findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc(String noticeType, String isDeleted);
    Optional<Notice> findByNoticeIdAndIsDeleted(Long noticeId, String isDeleted);
    Page<Notice> findByIsDeletedOrderByImportanceDescCreatedAtDesc(String isDeleted, Pageable pageable);
    Page<Notice> findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc(String noticeType, String isDeleted, Pageable pageable);

    @Modifying
    @Query("UPDATE Notice n SET n.viewCount = n.viewCount + 1 WHERE n.noticeId = :id")
    void incrementViewCount(@Param("id") Long id);
}
