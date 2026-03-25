package com.anju.service;

import com.anju.entity.Property;
import com.anju.entity.Property.PropertyStatus;
import com.anju.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DataExchangeService {

    private final PropertyRepository propertyRepository;

    public record ImportResult(int successCount, int failureCount, List<String> errors) {}

    @Transactional
    public ImportResult importProperties(MultipartFile file) {
        List<String> errors = new ArrayList<>();
        Set<String> seenUniqueCodes = new HashSet<>();
        int successCount = 0;
        int failureCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String headerLine = reader.readLine();
            if (headerLine == null) {
                errors.add("Empty file: no header found");
                return new ImportResult(0, 1, errors);
            }

            String[] headers = headerLine.toLowerCase().split(",");
            int uniqueCodeIdx = -1;
            int rentIdx = -1;
            int depositIdx = -1;

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].trim();
                if ("uniquecode".equals(h) || "unique_code".equals(h) || "\"uniqueCode\"".equals(h)) {
                    uniqueCodeIdx = i;
                } else if ("rent".equals(h)) {
                    rentIdx = i;
                } else if ("deposit".equals(h)) {
                    depositIdx = i;
                }
            }

            if (uniqueCodeIdx == -1 || rentIdx == -1 || depositIdx == -1) {
                errors.add("Missing required headers. Required: uniqueCode, rent, deposit");
                return new ImportResult(0, 1, errors);
            }

            String line;
            int lineNumber = 1;
            List<Property> propertiesToSave = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String[] values = parseCsvLine(line);

                if (values.length < Math.max(Math.max(uniqueCodeIdx, rentIdx), depositIdx) + 1) {
                    errors.add("Line " + lineNumber + ": insufficient columns");
                    failureCount++;
                    continue;
                }

                String uniqueCode = values[uniqueCodeIdx].trim();
                String rentStr = values[rentIdx].trim();
                String depositStr = values[depositIdx].trim();

                if (uniqueCode.isEmpty()) {
                    errors.add("Line " + lineNumber + ": uniqueCode is required");
                    failureCount++;
                    continue;
                }

                if (seenUniqueCodes.contains(uniqueCode)) {
                    errors.add("Line " + lineNumber + ": duplicate uniqueCode '" + uniqueCode + "' in file");
                    failureCount++;
                    continue;
                }

                if (propertyRepository.existsByUniqueCode(uniqueCode)) {
                    errors.add("Line " + lineNumber + ": uniqueCode '" + uniqueCode + "' already exists");
                    failureCount++;
                    continue;
                }

                BigDecimal rent;
                BigDecimal deposit;
                try {
                    rent = new BigDecimal(rentStr);
                    if (rent.compareTo(BigDecimal.ZERO) < 0) {
                        errors.add("Line " + lineNumber + ": rent cannot be negative");
                        failureCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNumber + ": invalid rent format '" + rentStr + "'");
                    failureCount++;
                    continue;
                }

                try {
                    deposit = new BigDecimal(depositStr);
                    if (deposit.compareTo(BigDecimal.ZERO) < 0) {
                        errors.add("Line " + lineNumber + ": deposit cannot be negative");
                        failureCount++;
                        continue;
                    }
                } catch (NumberFormatException e) {
                    errors.add("Line " + lineNumber + ": invalid deposit format '" + depositStr + "'");
                    failureCount++;
                    continue;
                }

                seenUniqueCodes.add(uniqueCode);
                Property property = Property.builder()
                        .uniqueCode(uniqueCode)
                        .rent(rent)
                        .deposit(deposit)
                        .status(PropertyStatus.DRAFT)
                        .build();
                propertiesToSave.add(property);
            }

            propertyRepository.saveAll(propertiesToSave);
            successCount = propertiesToSave.size();

        } catch (Exception e) {
            errors.add("File processing error: " + e.getMessage());
            failureCount++;
        }

        return new ImportResult(successCount, failureCount, errors);
    }

    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString());
                sb = new StringBuilder();
            } else {
                sb.append(c);
            }
        }
        result.add(sb.toString());
        return result.toArray(new String[0]);
    }
}
