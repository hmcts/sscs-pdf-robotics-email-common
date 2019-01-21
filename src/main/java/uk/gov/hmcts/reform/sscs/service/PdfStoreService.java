package uk.gov.hmcts.reform.sscs.service;

import static java.util.Collections.singletonList;
import static org.springframework.http.MediaType.APPLICATION_PDF;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import uk.gov.hmcts.reform.document.domain.UploadResponse;
import uk.gov.hmcts.reform.sscs.ccd.domain.DocumentLink;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocument;
import uk.gov.hmcts.reform.sscs.ccd.domain.SscsDocumentDetails;
import uk.gov.hmcts.reform.sscs.domain.pdf.ByteArrayMultipartFile;

@Service
@Slf4j
public class PdfStoreService {
    private final EvidenceManagementService evidenceManagementService;

    @Autowired
    public PdfStoreService(EvidenceManagementService evidenceManagementService) {
        this.evidenceManagementService = evidenceManagementService;
    }

    public List<SscsDocument> store(byte[] content, String fileName) {
        ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().content(content).name(fileName).contentType(APPLICATION_PDF).build();
        try {
            UploadResponse upload = evidenceManagementService.upload(singletonList(file), "sscs");
            String location = upload.getEmbedded().getDocuments().get(0).links.self.href;

            DocumentLink documentLink = DocumentLink.builder().documentUrl(location).build();
            SscsDocumentDetails sscsDocumentDetails = SscsDocumentDetails.builder()
                    .documentFileName(fileName)
                    .documentDateAdded(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                    .documentLink(documentLink)
                    .build();
            SscsDocument pdfDocument = SscsDocument.builder().value(sscsDocumentDetails).build();

            return Collections.singletonList(pdfDocument);
        } catch (RestClientException e) {
            log.error("Failed to store pdf document but carrying on [" + fileName + "]", e);
            return Collections.emptyList();
        }
    }

    public List<SscsDocument> store(byte[] content, String fileName, String comment) {
        ByteArrayMultipartFile file = ByteArrayMultipartFile.builder().content(content).name(fileName).contentType(APPLICATION_PDF).build();
        try {
            UploadResponse upload = evidenceManagementService.upload(singletonList(file), "sscs");
            String location = upload.getEmbedded().getDocuments().get(0).links.self.href;

            DocumentLink documentLink = DocumentLink.builder().documentUrl(location).build();
            SscsDocumentDetails sscsDocumentDetails = SscsDocumentDetails.builder()
                .documentFileName(fileName)
                .documentDateAdded(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                .documentLink(documentLink)
                .documentEmailContent(comment)
                .documentComment(comment)
                .documentType("Other document")
                .build();
            SscsDocument pdfDocument = SscsDocument.builder().value(sscsDocumentDetails).build();

            return Collections.singletonList(pdfDocument);
        } catch (RestClientException e) {
            log.error("Failed to store pdf document but carrying on [" + fileName + "]", e);
            return Collections.emptyList();
        }
    }
}
