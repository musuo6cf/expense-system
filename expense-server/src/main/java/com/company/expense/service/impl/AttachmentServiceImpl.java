package com.company.expense.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.company.expense.common.ResultCode;
import com.company.expense.entity.Attachment;
import com.company.expense.entity.Expense;
import com.company.expense.exception.BusinessException;
import com.company.expense.mapper.AttachmentMapper;
import com.company.expense.mapper.ExpenseMapper;
import com.company.expense.service.AttachmentService;
import com.company.expense.vo.AttachmentVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "pdf");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final String UPLOAD_ROOT = "uploads";

    private final AttachmentMapper attachmentMapper;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttachmentVO upload(Long expenseId, MultipartFile file, Long userId) {
        Expense expense = expenseMapper.selectById(expenseId);
        if (expense == null) {
            throw new BusinessException(ResultCode.EXPENSE_NOT_FOUND);
        }
        if (!expense.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (file.isEmpty()) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "文件为空");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (StringUtils.isBlank(extension) || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "仅支持 jpg、jpeg、png、pdf 格式");
        }

        String year = String.valueOf(LocalDate.now().getYear());
        String month = String.format("%02d", LocalDate.now().getMonthValue());
        Path uploadDir = Paths.get(UPLOAD_ROOT, year, month).toAbsolutePath();
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "创建上传目录失败");
        }

        String storedFilename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path targetPath = uploadDir.resolve(storedFilename);

        try {
            file.transferTo(targetPath.toFile());
        } catch (IOException e) {
            throw new BusinessException(ResultCode.FILE_UPLOAD_ERROR.getCode(), "文件保存失败: " + e.getMessage());
        }

        Attachment attachment = new Attachment();
        attachment.setExpenseId(expenseId);
        attachment.setFileName(originalFilename);
        attachment.setFilePath(targetPath.toString().replace(File.separator, "/"));
        attachment.setFileSize(file.getSize());
        attachment.setUploadTime(LocalDateTime.now());
        attachmentMapper.insert(attachment);

        return toVO(attachment);
    }

    @Override
    public List<AttachmentVO> listByExpenseId(Long expenseId) {
        LambdaQueryWrapper<Attachment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attachment::getExpenseId, expenseId)
                .orderByDesc(Attachment::getUploadTime);
        return attachmentMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, Long userId) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }

        Expense expense = expenseMapper.selectById(attachment.getExpenseId());
        if (expense != null && !expense.getApplicantId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        try {
            Path filePath = Paths.get(attachment.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
        }

        attachmentMapper.deleteById(id);
    }

    @Override
    public Attachment getAttachment(Long id) {
        Attachment attachment = attachmentMapper.selectById(id);
        if (attachment == null) {
            throw new BusinessException(ResultCode.FILE_NOT_FOUND);
        }
        return attachment;
    }

    private AttachmentVO toVO(Attachment attachment) {
        AttachmentVO vo = new AttachmentVO();
        vo.setId(attachment.getId());
        vo.setExpenseId(attachment.getExpenseId());
        vo.setFileName(attachment.getFileName());
        vo.setFilePath(attachment.getFilePath());
        vo.setFileSize(attachment.getFileSize());
        vo.setUploadTime(attachment.getUploadTime());
        return vo;
    }

    private String getExtension(String filename) {
        if (StringUtils.isBlank(filename) || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
