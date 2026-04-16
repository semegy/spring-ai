package ai.utils.mock;

import ai.po.ImageAuditResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Supplier;

public enum MockData {

    IMAGE_AUDIT_PASS(() -> {
        ObjectReader mapper = new ObjectMapper().readerFor(ImageAuditResult.class);
        try {
            InputStream inputStream = new ClassPathResource("data/ImageAuditResultMockData.json").getInputStream();
            return mapper.readValue(inputStream, ImageAuditResult.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    });

    private final Supplier<?> dataSupplier;

    MockData(Supplier<?> dataSupplier) {
        this.dataSupplier = dataSupplier;
    }

    public <T> T getData() {
        return (T) dataSupplier.get();
    }
}

