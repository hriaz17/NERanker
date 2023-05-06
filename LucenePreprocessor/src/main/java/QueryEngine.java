import edu.stanford.nlp.util.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import java.io.*;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.tartarus.snowball.ext.PorterStemmer;

import edu.stanford.nlp.simple.Sentence;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;



class Question {
    private String category = "";
    private String question = "";
    private String answer = "";

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    @Override
    public String toString() {
        return "Question{" +
                "category='" + category + '\'' +
                ", question='" + question + '\'' +
                ", answer='" + answer + '\'' +
                '}';
    }
}

public class QueryEngine {
    final static String indexPath = "src/main/resources/index";
    final static String questionsFilePath = "questions.txt";
    static IndexSearcher searcher;
    static Analyzer analyzer;
    static IndexReader indexReader;
    static List<Question> questions = new ArrayList<>();

    static {
        try {
            String indexPath = StringUtils.join(new String[]{System.getProperty("user.dir"), IndexTrainer.INDEX_PATH}, "/");
            Directory indexDirectory = FSDirectory.open(Paths.get(indexPath));
            indexReader = DirectoryReader.open(indexDirectory);
            searcher = new IndexSearcher(indexReader);
            analyzer = new StandardAnalyzer();

            File file = new File(QueryEngine.class.getClassLoader().getResource(questionsFilePath).toURI());
            try (Scanner inputScanner = new Scanner(file)) {
                int cnt = 2;
                Question question = new Question();
                while (inputScanner.hasNextLine()) {
                    String line = inputScanner.nextLine();
                    if (line.equals('\n') || cnt == -1) {
                        cnt = 2;
                        continue;
                    }
                    switch (cnt) {
                        case 2:
                            question.setCategory(line.trim());
                            break;
                        case 1:
                            question.setQuestion(line.trim());
                            break;
                        case 0:
                            question.setAnswer(line.trim());
                            questions.add(question);
                            question = new Question();
                            break;
                    }
                    cnt--;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

//            System.out.println(questions);

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws IOException, ParseException {

        searcher.setSimilarity(new LMDirichletSimilarity());
        QueryEngine.getTop20Hits();
    }

    public static void getTop20Hits() throws IOException, ParseException {
        int cnt = 0;
        int hitPerPage = 20;
        int i = 0;
        for (Question item : questions)
        {
            Query q = new QueryParser("content", analyzer).parse(QueryParser.escape(item.getQuestion()));
            TopDocs docs = searcher.search(q, hitPerPage);
            System.out.println("For question: " + item.getQuestion());
            System.out.println("Gold Answer for this question: " + item.getAnswer().toLowerCase());
            ScoreDoc[] hitsDocs = docs.scoreDocs;
            // create a folder for each question
            String folderName = "Q" + (i+1);
            File folder = new File(folderName);
            if (!folder.exists())
            {
                boolean success = folder.mkdir();
            }
            // create a text file in folder Q+i
            String fileName = "Q" + (i+1) +"_candidate_answers";
            File file = new File(folder, fileName);
            if (file.exists()) {
                file.delete();
                System.out.println("File " + fileName + " deleted since it already existed");
            }
            file.createNewFile();
            System.out.println("New File " + fileName + " created.");
            FileWriter writer = new FileWriter(file, true);
            // have some form of marker for the correct answer
            for (ScoreDoc hitDoc : hitsDocs)
            {
                Document doc = searcher.doc(hitDoc.doc);
                // System.out.println("Document name:" + doc.getField("docid"));
                // TODO: maybe create an isAnswer boolean flag to indicate if this doc's docid is the correct one to
                // satisfy the query. If it is, add some special character's to indicate the gold standard docid
                if (item.getAnswer().toLowerCase().contains(doc.get("docid").trim())) {
                    cnt++;
                    // DEBUG ONLY: content of document whose title is the answer
                    // System.out.println(doc.getField("content").stringValue()+"\n");
                }
                else {
                    //System.out.println("Question: " + item.getQuestion() + "'s top 20 hits do not contain the answer!");
                    //System.out.println("System's wrong answer: " + item.getAnswer());
                }
                writer.append((doc.getField("docid").stringValue())+"\n");

            }
            writer.close();

            i += 1;

        }
        System.out.println("-----------");
        System.out.println("%% P@"+ hitPerPage + ": " + cnt);
    }


    public static List<ResultClass> runQuery(String query) {
        List<ResultClass> ans = new ArrayList<ResultClass>();
        query = stemming(lemmatization(RemoveSpecialCharacters(query)));
        try {
            Query q = new QueryParser("content", analyzer).parse(query);
            int hitsPerPage = 10;
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            for (int i = 0; i < hits.length; i++) {
                ResultClass r = new ResultClass();
                int docId = hits[i].doc;
                r.DocName = searcher.doc(docId);
                r.docScore = hits[i].score;
                ans.add(r);
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        return ans;
    }

    private static String RemoveSpecialCharacters(String str) {
        return str.replaceAll("\n", " ")
                .replaceAll("\\[\\s*tpl\\s*\\]", " ")
                .replaceAll("\\[\\s*/\\s*tpl\\s*\\]", " ")
                .replaceAll("https?://\\S+\\s?", "")
                .replaceAll("[^ a-zA-Z\\d]", " ")
                .toLowerCase().trim();
    }

    public static String lemmatization(String str) {
        StringBuilder result = new StringBuilder();
        if (str.isEmpty()) {
            return str;
        }
        for (String lemma : new Sentence(str.toLowerCase()).lemmas()) {
            result.append(lemma).append(" ");
        }
        return result.toString();
    }

    public static String stemming(String str) {
        StringBuilder result = new StringBuilder();
        if (str.isEmpty()) {
            return str;
        }
        for (String word : new Sentence(str.toLowerCase()).words()) {
            result.append(getStem(word)).append(" ");
        }
        return result.toString();
    }

    public static String getStem(String term) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }

}