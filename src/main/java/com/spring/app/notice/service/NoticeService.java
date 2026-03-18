package com.spring.app.notice.service;

import com.spring.app.notice.domain.NoticeDTO;
import java.util.List;
import java.util.Map;

public interface NoticeService {
    List<NoticeDTO> getNoticeList();
    List<NoticeDTO> getFaqList();
    NoticeDTO getNotice(Long noticeId);
    void saveNotice(NoticeDTO dto);
    void deleteNotice(Long noticeId);
    Map<String, Object> getPagedNoticeList(int page, int size);
    Map<String, Object> getPagedFaqList(int page, int size);
    Map<String, Object> getPagedAllNotices(int page, int size);
    NoticeDTO getNoticeForEdit(Long noticeId);
}
