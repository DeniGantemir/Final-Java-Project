package searchengine.services;

import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;

public class LemmaOperation {

public String splitTextIntoWords(String text) {

    StringBuilder myText = new StringBuilder();
    String regex = "[^А-Яа-яA-Za-z ]";
    text = text.replaceAll(regex, " ");

    String[] words = text.split("\\s+");

    if (text.isBlank()) {
        return "";
    }
    for (String word : words) {
        myText.append(word).append("\n");
    }
    return myText.toString().strip();
}

    public HashMap<String, Integer> lemmaCounter(String string) throws IOException {
        LuceneMorphology luceneMorph;
        HashMap<String, Integer> lemmaCounts = new HashMap<>();
        String[] lemmaString = splitTextIntoWords(removeHtmlTags(string)).toLowerCase().split("\n");
        List<String> wordbaseForm;
        for (String word : lemmaString) {
            List<String> wordForm;
            if (word.matches(word.replaceAll("[^А-Яа-я ]", " "))) {
                luceneMorph = new RussianLuceneMorphology();
                wordForm = luceneMorph.getMorphInfo(word);
                boolean isValidRU = false;
                for (String baseForm : wordForm) {
                    String[] parts = baseForm.split("\\|");
                    String pos = parts[1];
                    if (!pos.matches("n СОЮЗ")
                            && !pos.matches("o МЕЖД")
                            && !pos.matches("l ПРЕДЛ")
                            && !pos.matches("p ЧАСТ")) {
                        isValidRU = true;
                        break;
                    }
                }
                if (isValidRU) {
                    wordbaseForm = luceneMorph.getNormalForms(word);
                    String lemma = wordbaseForm.get(0).split("\\|")[0];
//                    lemmaCounts.put(lemma, lemmaCounts.getOrDefault(lemma, 0) + 1);
                    lemmaCounts.put(lemma, lemmaCounts.containsKey(lemma) ? lemmaCounts.get(lemma) + 1 : 1);
                }
            }
        }
        return lemmaCounts;
    }
    public String removeHtmlTags(String htmlContent) {
        return Jsoup.parse(htmlContent).select("body").text();
    }
}
