package com.spring.app.inquiry.repository;

import com.spring.app.entity.Inquiry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface InquiryRepository extends JpaRepository<Inquiry, Long> {
    List<Inquiry> findByMemberEmailOrderByCreatedAtDesc(String memberEmail);
    List<Inquiry> findAllByOrderByCreatedAtDesc();
    Page<Inquiry> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Inquiry> findByInquiryStatusOrderByCreatedAtDesc(String inquiryStatus, Pageable pageable);
}
