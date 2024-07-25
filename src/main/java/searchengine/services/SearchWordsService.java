package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchItem;
import searchengine.dto.search.SearchResponse;
import searchengine.model.IndexEntity;
import searchengine.model.PageEntity;
import searchengine.repository.IndexEntityRepository;

import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class SearchWordsService {
    @Autowired
    private final IndexEntityRepository indexEntityRepository;

    public SearchResponse searchLemmaMethod(String query, String site, int offset, int limit) throws IOException {
        // Делю запрос на слова и делаю их леммами
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        Map<String, Integer> lemmaCounts = lemmaFinder.collectLemmas(query);

        // Исключать из полученного списка леммы, которые встречаются на слишком большом количестве страниц
        lemmaCounts = filterFrequentLemmas(lemmaCounts);

        // Сортируем по количеству frequency Integer
        List<Map.Entry<String, Integer>> sortedLemmas = new ArrayList<>(lemmaCounts.entrySet());
        sortedLemmas.sort(Map.Entry.comparingByValue());

        // Находим pages, которые соответсвуют lemma
        List<SearchItem> searchItems = processOfSavingItems(sortedLemmas, site, query);

        // сортируем по релевантности
        searchItems.sort((a, b) -> Double.compare(b.getRelevance(), a.getRelevance()));

        int totalCount = searchItems.size();

        // устанавливаем limit и offset
        offset = 0;
        limit = 10;
        int effectiveOffset = offset;
        int effectiveLimit = limit;
        searchItems = searchItems.subList(effectiveOffset, Math.min(effectiveOffset + effectiveLimit, searchItems.size()));

        // добавляю в response класс
        SearchResponse response = new SearchResponse();
        response.setResult(!searchItems.isEmpty());
        response.setCount(totalCount);
        response.setData(searchItems);
        return response;
    }
    private List<SearchItem> processOfSavingItems(List<Map.Entry<String, Integer>> sortedLemmas, String site, String query) throws IOException {
        List<SearchItem> searchItems = new ArrayList<>();
        for (Map.Entry<String, Integer> lemma : sortedLemmas) {
            List<IndexEntity> indexes;
            // проверяем поиск по определенному сайту или по всем
            if (site != null && !site.isEmpty()) {
                indexes = indexEntityRepository.findAll().stream()
                        .filter(index -> index.getLemmaEntity().getLemma().equals(lemma.getKey()) && index.getPageEntity().getSiteEntity().getUrl().equals(site))
                        .toList();
            } else {
                indexes = indexEntityRepository.findAll().stream()
                        .filter(index -> index.getLemmaEntity().getLemma().equals(lemma.getKey()))
                        .toList();
            }
            for (IndexEntity index : indexes) {
                PageEntity page = index.getPageEntity();

                SearchItem searchItem = new SearchItem();
                searchItem.setSite(page.getSiteEntity().getUrl());
                searchItem.setSiteName(page.getSiteEntity().getName());
                searchItem.setUri(page.getPath());
                Document doc = Jsoup.parse(page.getContent());
                String title = doc.select("title").text();
                searchItem.setTitle(title);

                String snippet = generateSnippet(removeHtmlTags(page.getContent()), query);
                searchItem.setSnippet(snippet);

                // Находим relevance
                TreeMap<Float, PageEntity> relevanceMap = getRelevance(indexes);

                for (Map.Entry<Float, PageEntity> entry : relevanceMap.entrySet()) {
                    float relevance = entry.getKey();
                    PageEntity pageEntity = entry.getValue();
                    SearchItem searchItemForRelevance = searchItems.stream()
                            .filter(si -> si.getUri().equals(pageEntity.getPath()))
                            .findFirst()
                            .orElse(null);
                    if (searchItemForRelevance != null) {
                        searchItem.setRelevance(relevance);
                    }
                }
                searchItems.add(searchItem);
            }
        }
        return searchItems;
    }

    private Map<String, Integer> filterFrequentLemmas(Map<String, Integer> lemmaCounts) {
        int theMostFrequently = 6; // если 6 - не добавлять в список
        Map<String, Integer> filteredLemmas = new HashMap<>();
        for (Map.Entry<String, Integer> lemma : lemmaCounts.entrySet()) {
            if (lemma.getValue() < theMostFrequently) {
                filteredLemmas.put(lemma.getKey(), lemma.getValue());
            }
        }
        return filteredLemmas;
    }
    private String generateSnippet(String pageContent, String query) throws IOException {
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        // делю запрос, если их больше одного и делаю буквы строчными
        String[] queryWords = query.toLowerCase().split("\\s+");
        Set<String> lemmaForms = new HashSet<>();
        for (String queryWord : queryWords) {
            lemmaForms.addAll(lemmaFinder.getLemmaSet(queryWord));
        }

        StringBuilder snippet = new StringBuilder();
        for (String lemmaForm : lemmaForms) {
            int prefixLength = lemmaForm.length() / 2;
            String prefix = lemmaForm.substring(0, prefixLength); // при совпадений половины длины слова
            int index = pageContent.toLowerCase().indexOf(prefix);
            while (index != -1) {
                int startIndex = Math.max(0, index - 150);
                int endIndex = Math.min(pageContent.length(), index + lemmaForm.length() + 150);
                String snippetText = pageContent.substring(startIndex, endIndex);
                snippetText = highlightedSearchedQueryInPageContent(snippetText, queryWords, lemmaForms);
                if (snippetText.length() > 300) {
                    snippetText = snippetText.substring(0, 300) + "...";
                }
                snippet.append("...").append(snippetText).append("...\n");
                break;
            }
        }
        return snippet.toString().trim();
    }

    private String highlightedSearchedQueryInPageContent(String text, String[] queryWords, Set<String> lemmaForms) {
        String[] words = text.split("\\s+");
        StringBuilder highlightedText = new StringBuilder();
        for (String word : words) {
            boolean match = false;
            for (String queryWord : queryWords) {
                if (word.toLowerCase().contains(queryWord)) {
                    match = true;
                    break;
                }
            }
            for (String lemmaForm : lemmaForms) {
                int prefixLength = lemmaForm.length() / 2;
                String prefix = lemmaForm.substring(0, prefixLength);
                if (word.toLowerCase().startsWith(prefix)) {
                    match = true;
                    break;
                }
            }
            if (match) {
                highlightedText.append("<b>").append(word).append("</b>").append(" ");
            } else {
                highlightedText.append(word).append(" ");
            }
        }
        return highlightedText.toString().trim();
    }
    private String removeHtmlTags(String htmlContent) {
        return Jsoup.parse(htmlContent).select("body").text();
    }

    private Map<PageEntity, Float> getAbsoluteRelevance(List<IndexEntity> pagesList) {
        Map<PageEntity, Float> pagesAbsoluteRelevance = new HashMap<>();
        for (IndexEntity index : pagesList) {
            PageEntity pageEntity = index.getPageEntity();
            if (pagesAbsoluteRelevance.containsKey(pageEntity)) {
                float absoluteRelevance = pagesAbsoluteRelevance.get(pageEntity) + index.getRank();
                pagesAbsoluteRelevance.put(pageEntity, absoluteRelevance);
            } else {
                pagesAbsoluteRelevance.put(pageEntity, index.getRank());
            }
        }
        return pagesAbsoluteRelevance;
    }
    private TreeMap<Float, PageEntity> getRelevance(List<IndexEntity> pagesList) {
        Map<PageEntity, Float> pagesAbsoluteRelevance = getAbsoluteRelevance(pagesList);
        float maxAbsoluteRelevance = pagesAbsoluteRelevance.values().stream().max(Float::compare).orElse(Float.MIN_VALUE);

        TreeMap<Float, PageEntity> pagesRelevance = new TreeMap<>(Comparator.reverseOrder());
        for (Map.Entry<PageEntity, Float> entry : pagesAbsoluteRelevance.entrySet()) {
            float relevance = entry.getValue() / maxAbsoluteRelevance;
            pagesRelevance.put(relevance, entry.getKey());
        }

        return pagesRelevance;
    }
}
