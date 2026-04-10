package com.gym.management.service;

import com.gym.management.dto.MemberPaymentExcelDTO;
import com.gym.management.entity.Member;
import com.gym.management.entity.Payment;
import com.gym.management.enums.MemberStatus;
import com.gym.management.repository.MemberRepository;
import com.gym.management.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExcelService {

    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;

    private static final String[] HEADERS = {
            "Member ID", "Name", "Email", "Phone", "Status", "Join Date",
            "Latest Payment Amount", "Payment Month", "Payment Year", "Payment Date", "Verified"
    };

    /**
     * Export all members with their latest payment information to Excel
     */
    @Transactional(readOnly = true)
    public ByteArrayInputStream exportMembersWithPayments() {
        log.info("Starting export of members with payment data");
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Members");

            // Create header row with styling
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = createHeaderStyle(workbook);

            for (int i = 0; i < HEADERS.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(HEADERS[i]);
                cell.setCellStyle(headerStyle);
                sheet.autoSizeColumn(i);
            }

            // Fetch all members
            List<Member> members = memberRepository.findAll();
            log.info("Found {} members to export", members.size());

            // Create data rows
            int rowNum = 1;
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);

            for (Member member : members) {
                Row row = sheet.createRow(rowNum++);

                // Member data
                row.createCell(0).setCellValue(member.getId());
                row.createCell(1).setCellValue(member.getName());
                row.createCell(2).setCellValue(member.getEmail());
                row.createCell(3).setCellValue(member.getPhone());
                row.createCell(4).setCellValue(member.getStatus().toString());

                Cell joinDateCell = row.createCell(5);
                joinDateCell
                        .setCellValue(Date.from(member.getJoinDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                joinDateCell.setCellStyle(dateStyle);

                // Find latest payment for this member
                Optional<Payment> latestPayment = paymentRepository
                        .findTopByMemberOrderByPaymentYearDescPaymentMonthDesc(member);

                if (latestPayment.isPresent()) {
                    Payment payment = latestPayment.get();

                    Cell amountCell = row.createCell(6);
                    amountCell.setCellValue(payment.getAmount().doubleValue());
                    amountCell.setCellStyle(currencyStyle);

                    row.createCell(7).setCellValue(payment.getPaymentMonth());
                    row.createCell(8).setCellValue(payment.getPaymentYear());

                    Cell paymentDateCell = row.createCell(9);
                    paymentDateCell.setCellValue(
                            Date.from(payment.getPaymentDate().atStartOfDay(ZoneId.systemDefault()).toInstant()));
                    paymentDateCell.setCellStyle(dateStyle);

                    row.createCell(10).setCellValue(payment.getVerified() ? "Yes" : "No");
                } else {
                    // No payment data
                    row.createCell(6).setCellValue("N/A");
                    row.createCell(7).setCellValue("N/A");
                    row.createCell(8).setCellValue("N/A");
                    row.createCell(9).setCellValue("N/A");
                    row.createCell(10).setCellValue("N/A");
                }
            }

            // Auto-size columns
            for (int i = 0; i < HEADERS.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Successfully exported {} members to Excel", members.size());
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            log.error("Failed to export data to Excel", e);
            throw new RuntimeException("Failed to export data to Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Import members from Excel file
     * Only imports member data, not payment data
     */
    @Transactional
    public Map<String, Object> importMembers(MultipartFile file) {
        log.info("Starting import of members from Excel file: {}", file.getOriginalFilename());
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int skippedCount = 0;

        try (InputStream is = file.getInputStream(); Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            // Skip header row
            if (rows.hasNext()) {
                rows.next();
            }

            int rowNumber = 1;
            while (rows.hasNext()) {
                Row currentRow = rows.next();
                rowNumber++;

                try {
                    // Read member data (columns 1-5: Name, Email, Phone, Status, Join Date)
                    String name = getCellValueAsString(currentRow.getCell(1));
                    String email = getCellValueAsString(currentRow.getCell(2));
                    String phone = getCellValueAsString(currentRow.getCell(3));
                    String statusStr = getCellValueAsString(currentRow.getCell(4));
                    LocalDate joinDate = getCellValueAsDate(currentRow.getCell(5));

                    // Validate required fields
                    if (name == null || name.trim().isEmpty()) {
                        errors.add("Row " + rowNumber + ": Name is required");
                        continue;
                    }
                    if (email == null || email.trim().isEmpty()) {
                        errors.add("Row " + rowNumber + ": Email is required");
                        continue;
                    }
                    if (!isValidEmail(email)) {
                        errors.add("Row " + rowNumber + ": Invalid email format - " + email);
                        continue;
                    }
                    if (phone == null || phone.trim().isEmpty()) {
                        errors.add("Row " + rowNumber + ": Phone is required");
                        continue;
                    }
                    if (joinDate == null) {
                        errors.add("Row " + rowNumber + ": Join date is required");
                        continue;
                    }

                    // Check for duplicate email
                    if (memberRepository.findByEmail(email).isPresent()) {
                        skippedCount++;
                        log.warn("Row {}: Skipping duplicate email: {}", rowNumber, email);
                        errors.add("Row " + rowNumber + ": Member with email " + email + " already exists - skipped");
                        continue;
                    }

                    // Parse status (default to ACTIVE if not provided or invalid)
                    MemberStatus status = MemberStatus.ACTIVE;
                    if (statusStr != null && !statusStr.trim().isEmpty()) {
                        try {
                            status = MemberStatus.valueOf(statusStr.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            errors.add("Row " + rowNumber + ": Invalid status '" + statusStr
                                    + "', using ACTIVE as default");
                        }
                    }

                    // Create new member
                    Member member = new Member();
                    member.setName(name.trim());
                    member.setEmail(email.trim().toLowerCase());
                    member.setPhone(phone.trim());
                    member.setStatus(status);
                    member.setJoinDate(joinDate);

                    memberRepository.save(member);
                    successCount++;
                    log.debug("Successfully imported member: {} ({})", name, email);

                } catch (Exception e) {
                    log.error("Error processing row {}: {}", rowNumber, e.getMessage());
                    errors.add("Row " + rowNumber + ": " + e.getMessage());
                }
            }

            result.put("success", true);
            result.put("successCount", successCount);
            result.put("skippedCount", skippedCount);
            result.put("errorCount", errors.size());
            result.put("errors", errors);
            result.put("message", String.format("Import completed: %d created, %d skipped, %d errors",
                    successCount, skippedCount, errors.size()));

            log.info("Import completed - Success: {}, Skipped: {}, Errors: {}",
                    successCount, skippedCount, errors.size());

        } catch (IOException e) {
            log.error("Failed to read Excel file: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("message", "Failed to read Excel file: " + e.getMessage());
            errors.add("File reading error: " + e.getMessage());
            result.put("errors", errors);
        } catch (Exception e) {
            log.error("Unexpected error during import: {}", file.getOriginalFilename(), e);
            result.put("success", false);
            result.put("message", "Import failed: " + e.getMessage());
            errors.add("Unexpected error: " + e.getMessage());
            result.put("errors", errors);
        }

        return result;
    }

    // Helper methods

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-mm-dd"));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("$#,##0.00"));
        return style;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    private LocalDate getCellValueAsDate(Cell cell) {
        if (cell == null) {
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
            } else if (cell.getCellType() == CellType.STRING) {
                // Try to parse string as date
                return LocalDate.parse(cell.getStringCellValue());
            }
        } catch (Exception e) {
            // Return null if parsing fails
        }
        return null;
    }

    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
