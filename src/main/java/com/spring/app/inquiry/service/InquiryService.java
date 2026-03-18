package com.spring.app.inquiry.service;

import com.spring.app.inquiry.domain.InquiryDTO;
import java.util.List;
import java.util.Map;

public interface InquiryService {
    void saveInquiry(InquiryDTO dto);
    List<InquiryDTO> getMyInquiries(String memberEmail);
    List<InquiryDTO> getAllInquiries();
    InquiryDTO getInquiry(Long inquiryId);
    void saveAnswer(Long inquiryId, String adminAnswer);
    Map<String, Object> getPagedInquiries(int page, int size, String status);
}
