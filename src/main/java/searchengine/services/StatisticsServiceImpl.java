package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.modelEntity.LemmaEntity;
import searchengine.modelEntity.PageEntity;
import searchengine.modelEntity.SiteEntity;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;

    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        // Здесь получаю все данные
        List<SiteEntity> siteEntities = siteRepository.findAll();

        // Получаю размер. Ставлю true для setIndexing
        total.setSites(siteEntities.size());
        total.setIndexing(true);

        // начинаем с нуля
        int totalPages = 0;
        int totalLemmas = 0;

        // перебираем сайты
        for (SiteEntity siteEntity : siteEntities) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setUrl(siteEntity.getUrl());
            item.setName(siteEntity.getName());

            // получю pages and lemmas с репозитория
            List<PageEntity> pageEntities = pageRepository.findAll().stream()
                    .filter(page -> page.getSiteEntity().equals(siteEntity))
                    .toList();

            List<LemmaEntity> lemmaEntities = lemmaRepository.findAll().stream()
                    .filter(lemma -> lemma.getSiteEntity().equals(siteEntity))
                    .toList();

            int pages = pageEntities.size();
            int lemmas = lemmaEntities.size();

            // total
            totalPages += pages;
            totalLemmas += lemmas;

            // вносим данные в DetailedStatisticsItem()
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(siteEntity.getStatus().toString());
            LocalDateTime localDateTime = siteEntity.getStatusTime();
            ZonedDateTime zdt = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
            long date = zdt.toInstant().toEpochMilli();
            item.setStatusTime(date);
            if (siteEntity.getLastError()!= null) {
                item.setError(siteEntity.getLastError());
            }

            detailed.add(item);
        }

        total.setPages(totalPages);
        total.setLemmas(totalLemmas);

        // Ответ для StatisticsResponse
        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);

        return response;
    }
}
