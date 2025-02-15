package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import searchengine.dto.dtoClasses.IndexDTO;
import searchengine.dto.dtoClasses.LemmaDTO;
import searchengine.dto.dtoClasses.PageDTO;
import searchengine.modelEntity.IndexEntity;
import searchengine.modelEntity.LemmaEntity;
import searchengine.modelEntity.PageEntity;
import searchengine.modelEntity.SiteEntity;
import searchengine.repository.IndexEntityRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

import static org.hibernate.tool.schema.SchemaToolingLogging.LOGGER;

@Service
@RequiredArgsConstructor
public class PageLemmaIndexClass {
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexEntityRepository indexEntityRepository;
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();
    private final DTOTransferService dtoTransferService = new DTOTransferService();

    public void getPageLemmaIndexSiteMethod(SiteEntity siteEntity, String url) throws IOException {
        ForkSiteParser parser = new ForkSiteParser(url);
        TreeSet<String> urlForkJoinParser = new TreeSet<>(forkJoinPool.invoke(parser));

        LemmaFinder lemmaFinder = LemmaFinder.getInstance();

        for (String pageUrl : urlForkJoinParser) {
            Document doc = Jsoup.connect(pageUrl).get();
            String content = doc.html();
            try {
                PageDTO pageDTO = new PageDTO();
                pageDTO.setPath(normalizeUrl(pageUrl, url));
                pageDTO.setCode(doc.connection().response().statusCode());
                pageDTO.setContent(content);
                pageDTO.setSiteEntityId(siteEntity.getId());
                PageEntity pageEntity = dtoTransferService.mapToPageEntity(pageDTO, siteEntity);
                pageRepository.save(pageEntity);

                // убираю HTML-теги
                String cleanContent = Jsoup.parse(content).text();
                Map<String, Integer> lemmaCounts = lemmaFinder.collectLemmas(cleanContent);

                lemmaCounts.forEach((lemma, frequency) -> {
                    LemmaDTO lemmaDTO = new LemmaDTO();
                    lemmaDTO.setLemma(lemma);
                    lemmaDTO.setFrequency(frequency);
                    lemmaDTO.setSiteEntityId(siteEntity.getId());
                    LemmaEntity lemmaEntity = dtoTransferService.mapToLemmaEntity(lemmaDTO, siteEntity);
                    lemmaRepository.save(lemmaEntity);

                    IndexDTO indexDTO = new IndexDTO();
                    indexDTO.setRank((float) frequency);
                    indexDTO.setLemmaEntityId(lemmaEntity.getId());
                    indexDTO.setPageEntityId(pageEntity.getId());
                    IndexEntity indexEntity = dtoTransferService.mapToIndexEntity(indexDTO, lemmaEntity, pageEntity);
                    indexEntityRepository.save(indexEntity);
                });
            } catch (Exception e) {
                LOGGER.info("Ошибка: " + e);
            }
        }
    }
    private String normalizeUrl(String url, String mainUrl) {
        String relativeUrl = url.replaceAll(mainUrl, "");
        return relativeUrl.isEmpty()? "/" : relativeUrl;
    }
}
