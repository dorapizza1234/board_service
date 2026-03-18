package com.spring.app.notice.controller;

import com.spring.app.auth.domain.CustomUserDetails;
import com.spring.app.notice.domain.NoticeDTO;
import com.spring.app.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/notice/")
public class NoticeController {

    private final NoticeService noticeService;

    // 공지사항 + FAQ 목록 (공개, 페이징)
    @GetMapping("list")
    public String list(Model model,
                       @RequestParam(value = "page", defaultValue = "1") int page,
                       @RequestParam(value = "size", defaultValue = "10") int size) {
        Map<String, Object> noticeData = noticeService.getPagedNoticeList(page, size);
        Map<String, Object> faqData = noticeService.getPagedFaqList(1, 20);
        model.addAttribute("noticeList", noticeData.get("list"));
        model.addAttribute("faqList", faqData.get("list"));
        model.addAttribute("totalPages", noticeData.get("totalPages"));
        model.addAttribute("currentPage", page);
        return "notice/list";
    }

    // 상세 보기 (공개)
    @GetMapping("view")
    public String view(@RequestParam("noticeId") Long noticeId, Model model) {
        model.addAttribute("notice", noticeService.getNotice(noticeId));
        return "notice/view";
    }

    // 작성 폼 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("write")
    public String writeForm(Model model) {
        return "notice/write";
    }

    // 작성 완료 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("write")
    @ResponseBody
    public int writeSubmit(NoticeDTO dto,
                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            dto.setAdminEmail(userDetails.getUsername());
            noticeService.saveNotice(dto);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // 삭제 (관리자, soft delete)
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("delete")
    @ResponseBody
    public int delete(@RequestParam("noticeId") Long noticeId) {
        try {
            noticeService.deleteNotice(noticeId);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }

    // 관리자: 전체 공지+FAQ 관리 목록 (페이징)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("admin/list")
    public String adminList(Model model,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "size", defaultValue = "15") int size) {
        Map<String, Object> data = noticeService.getPagedAllNotices(page, size);
        model.addAttribute("noticeList", data.get("list"));
        model.addAttribute("totalCount", data.get("total"));
        model.addAttribute("totalPages", data.get("totalPages"));
        model.addAttribute("currentPage", page);
        return "notice/admin_list";
    }

    // 수정 폼 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("edit")
    public String editForm(@RequestParam("noticeId") Long noticeId, Model model) {
        model.addAttribute("notice", noticeService.getNoticeForEdit(noticeId));
        return "notice/edit";
    }

    // 수정 저장 (관리자)
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("edit")
    @ResponseBody
    public int editSubmit(NoticeDTO dto,
                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            dto.setAdminEmail(userDetails.getUsername());
            noticeService.saveNotice(dto);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
