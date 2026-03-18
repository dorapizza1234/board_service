package com.spring.app.inquiry.service;

import com.spring.app.entity.Inquiry;
import com.spring.app.inquiry.domain.InquiryDTO;
import com.spring.app.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InquiryService_imple implements InquiryService {

    private final InquiryRepository inquiryRepository;

    @Override
    public void saveInquiry(InquiryDTO dto) {
        Inquiry inquiry = Inquiry.builder()
            .memberEmail(dto.getMemberEmail())
            .title(dto.getTitle())
            .content(dto.getContent())
            .inquiryStatus("대기")
            .build();
        inquiryRepository.save(inquiry);
    }

    @Override
    public List<InquiryDTO> getMyInquiries(String memberEmail) {
        return inquiryRepository.findByMemberEmailOrderByCreatedAtDesc(memberEmail)
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<InquiryDTO> getAllInquiries() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc()
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public InquiryDTO getInquiry(Long inquiryId) {
        return toDTO(inquiryRepository.findById(inquiryId)
            .orElseThrow(() -> new RuntimeException("문의가 없습니다.")));
    }

    @Override
    @Transactional
    public void saveAnswer(Long inquiryId, String adminAnswer) {
        inquiryRepository.findById(inquiryId).ifPresent(i -> {
            i.setAdminAnswer(adminAnswer);
            i.setAnsweredAt(LocalDate.now());
            i.setInquiryStatus("답변완료");
            inquiryRepository.save(i);
        });
    }

    @Override
    public Map<String, Object> getPagedInquiries(int page, int size, String status) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Inquiry> pageResult;
        if (status == null || status.isEmpty() || "ALL".equals(status)) {
            pageResult = inquiryRepository.findAllByOrderByCreatedAtDesc(pageable);
        } else {
            pageResult = inquiryRepository.findByInquiryStatusOrderByCreatedAtDesc(status, pageable);
        }
        List<InquiryDTO> list = pageResult.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages() == 0 ? 1 : pageResult.getTotalPages());
        result.put("currentPage", page);
        return result;
    }

    private InquiryDTO toDTO(Inquiry i) {
        return InquiryDTO.builder()
            .inquiryId(i.getInquiryId())
            .memberEmail(i.getMemberEmail())
            .title(i.getTitle())
            .content(i.getContent())
            .createdAt(i.getCreatedAt())
            .inquiryStatus(i.getInquiryStatus())
            .adminAnswer(i.getAdminAnswer())
            .answeredAt(i.getAnsweredAt())
            .build();
    }
}
