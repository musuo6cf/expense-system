package com.company.expense.service;

import com.company.expense.entity.Attachment;
import com.company.expense.vo.AttachmentVO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentVO upload(Long expenseId, MultipartFile file, Long userId);

    List<AttachmentVO> listByExpenseId(Long expenseId);

    void delete(Long id, Long userId);

    Attachment getAttachment(Long id);
}
