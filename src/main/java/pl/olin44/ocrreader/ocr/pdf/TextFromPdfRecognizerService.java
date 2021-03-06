package pl.olin44.ocrreader.ocr.pdf;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pl.olin44.ocrreader.ocr.common.BufferedImageReader;
import pl.olin44.ocrreader.ocr.common.ExtensionResolver;
import pl.olin44.ocrreader.ocr.common.UnsupportedDataTypeException;
import pl.olin44.ocrreader.ocr.text_recognition.TextRecognizer;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class TextFromPdfRecognizerService {

    private static final String PDF_EXTENSION = "pdf";

    private final TextRecognizer textRecognizer;

    private final BufferedImageReader bufferedImageReader;

    public TextFromPdfRecognizerService(TextRecognizer textRecognizer,
                                        BufferedImageReader bufferedImageReader) {
        this.textRecognizer = textRecognizer;
        this.bufferedImageReader = bufferedImageReader;
    }

    public String recognize(MultipartFile multipartFile) {
        validateExtension(multipartFile);
        return readAllPages(multipartFile);
    }

    private String readAllPages(MultipartFile multipartFile) {
        return bufferedImageReader.read(multipartFile).stream()
                .map(textRecognizer::recognize)
                .collect(Collectors.joining("\n"));
    }

    public String recognize(MultipartFile pdfFile, Integer pageNumber) {
        validateExtension(pdfFile);
        if (pageNumber == null || pageNumber < 0) {
            throw new NoSuchPageError("Page number can't be negative value or null");
        }
        return readFromPage(pdfFile, pageNumber);
    }

    private void validateExtension(MultipartFile multipartFile) {
        if (!Objects.equals(ExtensionResolver.resolve(multipartFile), PDF_EXTENSION)) {
            throw new UnsupportedDataTypeException();
        }
    }

    private String readFromPage(MultipartFile multipartFile, Integer pageNumber) {
        BufferedImage bufferedImage = bufferedImageReader.read(multipartFile, pageNumber);
        return textRecognizer.recognize(bufferedImage);
    }
}
