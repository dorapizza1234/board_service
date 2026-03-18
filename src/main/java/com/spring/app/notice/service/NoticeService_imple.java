package com.spring.app.notice.service;

import com.spring.app.entity.Notice;
import com.spring.app.notice.domain.NoticeDTO;
import com.spring.app.notice.repository.NoticeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeService_imple implements NoticeService {

    private final NoticeRepository noticeRepository;

    @Override
    public List<NoticeDTO> getNoticeList() {
        return noticeRepository
            .findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc("NOTICE", "N")
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    public List<NoticeDTO> getFaqList() {
        return noticeRepository
            .findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc("FAQ", "N")
            .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public NoticeDTO getNotice(Long noticeId) {
        noticeRepository.incrementViewCount(noticeId);
        Notice notice = noticeRepository.findByNoticeIdAndIsDeleted(noticeId, "N")
            .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));
        return toDTO(notice);
    }

    @Override
    public void saveNotice(NoticeDTO dto) {
        Notice notice = Notice.builder()
            .noticeId(dto.getNoticeId())
            .adminEmail(dto.getAdminEmail())
            .title(dto.getTitle())
            .content(dto.getContent())
            .importance(dto.getImportance() != null ? dto.getImportance() : 0)
            .status("PUBLISHED")
            .isDeleted("N")
            .noticeType(dto.getNoticeType() != null ? dto.getNoticeType() : "NOTICE")
            .build();
        noticeRepository.save(notice);
    }

    @Override
    @Transactional
    public void deleteNotice(Long noticeId) {
        noticeRepository.findById(noticeId).ifPresent(n -> {
            n.setIsDeleted("Y");
            noticeRepository.save(n);
        });
    }

    @Override
    public Map<String, Object> getPagedNoticeList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notice> pageResult = noticeRepository
            .findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc("NOTICE", "N", pageable);
        return buildPageResult(pageResult, page);
    }

    @Override
    public Map<String, Object> getPagedFaqList(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notice> pageResult = noticeRepository
            .findByNoticeTypeAndIsDeletedOrderByImportanceDescCreatedAtDesc("FAQ", "N", pageable);
        return buildPageResult(pageResult, page);
    }

    @Override
    public Map<String, Object> getPagedAllNotices(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Notice> pageResult = noticeRepository
            .findByIsDeletedOrderByImportanceDescCreatedAtDesc("N", pageable);
        return buildPageResult(pageResult, page);
    }

    @Override
    public NoticeDTO getNoticeForEdit(Long noticeId) {
        return noticeRepository.findByNoticeIdAndIsDeleted(noticeId, "N")
            .map(this::toDTO)
            .orElseThrow(() -> new RuntimeException("공지사항이 없습니다."));
    }

    private Map<String, Object> buildPageResult(Page<Notice> pageResult, int page) {
        List<NoticeDTO> list = pageResult.getContent().stream().map(this::toDTO).collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("total", pageResult.getTotalElements());
        result.put("totalPages", pageResult.getTotalPages() == 0 ? 1 : pageResult.getTotalPages());
        result.put("currentPage", page);
        return result;
    }

    private NoticeDTO toDTO(Notice n) {
        return NoticeDTO.builder()
            .noticeId(n.getNoticeId())
            .adminEmail(n.getAdminEmail())
            .title(n.getTitle())
            .content(n.getContent())
            .viewCount(n.getViewCount())
            .importance(n.getImportance())
            .status(n.getStatus())
            .isDeleted(n.getIsDeleted())
            .createdAt(n.getCreatedAt())
            .noticeType(n.getNoticeType())
            .build();
    }
}
