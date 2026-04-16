package ai.graph.controller;

import ai.graph.workflow.ImageAuditWorkflow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@RestController
public class ViolationReviewController {

    @Autowired
    private ImageAuditWorkflow auditWorkflow;

    @Value("classpath:椅子.jpg")
    Resource resource;

    @GetMapping("checkImage/{workFlowId}")
    public Mono<Object> checkImage(@PathVariable("workFlowId") String workFlowId) {
        try {
            return auditWorkflow.start(workFlowId, resource);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("checkUploadImage/{workFlowId}")
    public Mono<Object> checkUploadImage(@PathVariable("workFlowId") String workFlowId, @RequestPart("image") MultipartFile multipartFile) {
        try {
            return auditWorkflow.start(workFlowId, multipartFile.getResource());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @GetMapping("imageReCheck/{review}/{workFlowId}")
    public Mono<Object> imageReCheck(@PathVariable("review") String review, @PathVariable("workFlowId") String workFlowId) throws Exception {
        return auditWorkflow.reStart(review, workFlowId);
    }

}
