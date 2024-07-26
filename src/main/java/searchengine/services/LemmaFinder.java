package searchengine.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;

import java.io.IOException;
import java.util.*;

@Slf4j
public class LemmaFinder {
    private final LuceneMorphology russianLuceneMorphology;
    private final LuceneMorphology englishLuceneMorphology;
    private static final String WORD_TYPE_REGEX = "\\W\\w&&[^а-яА-Я\\s]";
    private static final String WORD_TYPE_REGEX_ENGLISH = "\\W\\w&&[^a-zA-Z\\s]";
    private static final String[] particlesNames = new String[]{"МЕЖД", "ПРЕДЛ", "СОЮЗ"};
    private static final String[] englishParticlesNames = new String[]{
            "a", "an", "the", "of", "in", "on", "at", "by", "with", "from",
            "to", "for", "as", "is", "are", "am", "be", "been", "being",
            "and", "but", "or", "so", "yet", "for", "if", "when", "why",
            "how", "not", "no", "up", "down", "in", "out", "on", "off",
            "over", "under", "above", "below", "beside", "within"
    };

    public static LemmaFinder getInstance() throws IOException {
        LuceneMorphology russianMorphology = new RussianLuceneMorphology();
        LuceneMorphology englishMorphology = new EnglishLuceneMorphology();
        return new LemmaFinder(russianMorphology, englishMorphology);
    }

    private LemmaFinder(LuceneMorphology russianLuceneMorphology, LuceneMorphology englishLuceneMorphology) {
        this.russianLuceneMorphology = russianLuceneMorphology;
        this.englishLuceneMorphology = englishLuceneMorphology;
    }

    /**
     * Метод разделяет текст на слова, находит все леммы и считает их количество.
     *
     * @param text текст из которого будут выбираться леммы
     * @return ключ является леммой, а значение количеством найденных лемм
     */
    public Map<String, Integer> collectLemmas(String text) {
        String[] russianWords = arrayContainsRussianWords(text);
        String[] englishWords = arrayContainsEnglishWords(text);
        HashMap<String, Integer> lemmas = new HashMap<>();

        for (String word : russianWords) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = russianLuceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticleRus(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = russianLuceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }

        for (String word : englishWords) {
            if (word.isBlank()) {
                continue;
            }

            List<String> wordBaseForms = englishLuceneMorphology.getMorphInfo(word);
            if (anyWordBaseBelongToParticleRus(wordBaseForms)) {
                continue;
            }

            List<String> normalForms = englishLuceneMorphology.getNormalForms(word);
            if (normalForms.isEmpty()) {
                continue;
            }

            String normalWord = normalForms.get(0);

            if (lemmas.containsKey(normalWord)) {
                lemmas.put(normalWord, lemmas.get(normalWord) + 1);
            } else {
                lemmas.put(normalWord, 1);
            }
        }

        return lemmas;
    }

    /**
     * @param text текст из которого собираем все леммы
     * @return набор уникальных лемм найденных в тексте
     */
    public Set<String> getLemmaSet(String text) {
        String[] russianTextArray = arrayContainsRussianWords(text);
        String[] englishTextArray = arrayContainsEnglishWords(text);
        Set<String> lemmaSet = new HashSet<>();

        for (String word : russianTextArray) {
            if (!word.isEmpty() && isCorrectWordFormRus(word)) {
                List<String> wordBaseForms = russianLuceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticleRus(wordBaseForms)) {
                    continue;
                }
                lemmaSet.addAll(russianLuceneMorphology.getNormalForms(word));
            }
        }

        for (String word : englishTextArray) {
            if (!word.isEmpty() && isCorrectWordFormEng(word)) {
                List<String> wordBaseForms = englishLuceneMorphology.getMorphInfo(word);
                if (anyWordBaseBelongToParticleEng(wordBaseForms)) {
                    continue;
                }
                lemmaSet.addAll(englishLuceneMorphology.getNormalForms(word));
            }
        }

        return lemmaSet;
    }

    private boolean anyWordBaseBelongToParticleRus(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticlePropertyRus);
    }

    private boolean hasParticlePropertyRus(String wordBase) {
        for (String property : particlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
    private boolean anyWordBaseBelongToParticleEng(List<String> wordBaseForms) {
        return wordBaseForms.stream().anyMatch(this::hasParticlePropertyEng);
    }

    private boolean hasParticlePropertyEng(String wordBase) {
        for (String property : englishParticlesNames) {
            if (wordBase.toUpperCase().contains(property)) {
                return true;
            }
        }
        return false;
    }
    private String[] arrayContainsRussianWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^а-я\\s])", " ")
                .trim()
                .split("\\s+");
    }

    private String[] arrayContainsEnglishWords(String text) {
        return text.toLowerCase(Locale.ROOT)
                .replaceAll("([^a-z\\s])", " ")
                .trim()
                .split("\\s+");
    }
    private boolean isCorrectWordFormRus(String word) {
        List<String> wordInfo = russianLuceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX)) {
                return false;
            }
        }
        return true;
    }
    private boolean isCorrectWordFormEng(String word) {
        List<String> wordInfo = englishLuceneMorphology.getMorphInfo(word);
        for (String morphInfo : wordInfo) {
            if (morphInfo.matches(WORD_TYPE_REGEX_ENGLISH)) {
                return false;
            }
        }
        return true;
    }
}
