import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
     * Encoding corpus by frequency approach, the more frequent the pair of vocabulary is, the more likely it will be together
     * But it has a weakness, it assumes that the input text has whitespace whereas some language don't even have whitespace.
     * It's also extremely slow, so use it for training corpus only.
     */
class BytePairEncoding {
    

    private final Map<String, Integer> corpus = new HashMap<>();
    private final Set<String> vocabulary = new LinkedHashSet<>();
    private final String WHITESPACE_REGEX = "\\s+";
    private final String NONWHITESPACE_REGEX = "\\S+";

    class VocabularyData {
        int frequency;
        Map<String, List<List<Integer>>> positions;

        public VocabularyData() {
            this.frequency = 0;
            this.positions = new HashMap<>();
        }

    }

    class TokenInformation {
        int startIndex;
        int endIndex;
        String token;

        public TokenInformation(int startIndex, int endIndex, String token) {
            this.startIndex = startIndex;
            this.endIndex = endIndex;
            this.token = token;
        }
    }

    private String parseWord(String word) {
        final String EOW = "_";
        final StringBuilder concatenatedWord = new StringBuilder();

        for (int i = 0; i < word.length() ; i++) {
            concatenatedWord.append(word.charAt(i) + " ");
            vocabulary.add(word.charAt(i) + "");
        }

        concatenatedWord.append(EOW);
        vocabulary.add(EOW);

        return concatenatedWord.toString();
    }

    private void parseText(String text) {

        String[] candidateOfCorpus = text.trim().split(WHITESPACE_REGEX);
        
        for (String word : candidateOfCorpus) {
            String concatenatedWord = parseWord(word);
            corpus.put(concatenatedWord.toString(), corpus.getOrDefault(concatenatedWord.toString(), 0) + 1);
        }

    }

    private Map.Entry<Map<String, VocabularyData>, String> mostFrequentPair() {

        final Iterator<String> iterator = corpus.keySet().iterator();
        final Map<String, VocabularyData> pairFrequency = new HashMap<>();
        final Pattern pattern = Pattern.compile(NONWHITESPACE_REGEX);

        while (iterator.hasNext()) {

            final Stack<TokenInformation> stack = new Stack<>();
            String word = iterator.next();

            Matcher matcher = pattern.matcher(word);
            
            while (matcher.find()) {
                
                final String foundToken = matcher.group(0);

                if (!stack.isEmpty()) {
                    TokenInformation previousToken = stack.pop();
                    String concatenatedString = previousToken.token + foundToken;


                    VocabularyData updateVocabularyData = pairFrequency.getOrDefault(concatenatedString, new VocabularyData());
                    updateVocabularyData.frequency += corpus.get(word);

                    Map<String, List<List<Integer>>> positions = updateVocabularyData.positions;
                    List<List<Integer>> indexes = positions.getOrDefault(word, new ArrayList<>());
                    indexes.add(new ArrayList<>(Arrays.asList(previousToken.startIndex, matcher.end())));
                    positions.put(word, indexes);

                    pairFrequency.put(concatenatedString, updateVocabularyData);
                }

                stack.push(new TokenInformation(matcher.start(), matcher.end(), foundToken));
            }
        }

        String maximumPair = null;
        int maximumFrequency = 0;
    
        for (Map.Entry<String, VocabularyData> data : pairFrequency.entrySet()) {
            
            VocabularyData vocabularyData = data.getValue();
            if (vocabularyData.frequency > maximumFrequency) {
                maximumPair = data.getKey();
                maximumFrequency = vocabularyData.frequency;
            }
        }

        return maximumPair != null ? Map.entry(pairFrequency, maximumPair) : null;
    }

    private void mergeFrequentPair(Map<String, List<List<Integer>>> modifiedCorpus) {
        
        Iterator<String> iterator = modifiedCorpus.keySet().iterator();

        while (iterator.hasNext()) {

            final String word = iterator.next();
            final StringBuilder modifiedWord = new StringBuilder(word);
            final List<List<Integer>> indexes = modifiedCorpus.get(word.toString());
            
            int deletedWhitespace = 0;

            for (List<Integer> index : indexes) {
                
                
                for (int i = index.get(0) - deletedWhitespace; i < index.get(1) - deletedWhitespace; i++) {
                    
                    if (Character.isWhitespace(modifiedWord.charAt(i))) {
                        modifiedWord.deleteCharAt(i);
                        deletedWhitespace++;
                    }

                }
            }            

            final int wordFrequency = corpus.get(word);
            corpus.remove(word);
            corpus.put(modifiedWord.toString(), wordFrequency);

        }


    }

    
    public Set<String> tokenize(String text, int k) {
        parseText(text);

        while (k-- > 0) {
            Map.Entry<Map<String, VocabularyData>, String> frequentPair = mostFrequentPair();

            if (frequentPair == null) {
                break;
            }

            vocabulary.add(frequentPair.getValue());
            mergeFrequentPair(frequentPair.getKey().get(frequentPair.getValue()).positions);
        
        }

        return vocabulary;

    }

    public void test() {

        // for (String word : corpus.keySet()) {
        //     System.out.println(word + " " + corpus.get(word));
        // }

        Iterator<String> it = vocabulary.iterator();

        while (it.hasNext()) {
            System.out.println(it.next());
        }
    }

}


class Main {
    public static void main(String args[]) {

        String text = "";
        BytePairEncoding bpe = new BytePairEncoding();

        bpe.tokenize(text, 20);
        bpe.test();
    }
}