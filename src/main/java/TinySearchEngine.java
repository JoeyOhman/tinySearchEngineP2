import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.*;

public class TinySearchEngine implements TinySearchEngineBase {

    private HashMap<String, LinkedList<DocumentProperties>> words;
    private HashMap<Document, Integer> documents;

    private LinkedList<Entry<QueryFormat, LinkedList<DocumentProperties>>> cache;

    public TinySearchEngine() {
        words = new HashMap<String, LinkedList<DocumentProperties>>(); // Key = Word, Value = list of docs
        documents = new HashMap<Document, Integer>(); // Key = doc, Value = amountOfWordsInDoc

        cache = new LinkedList<Entry<QueryFormat, LinkedList<DocumentProperties>>>();
    }

    public void preInserts() {
    }

    public void insert(Sentence sentence, Attributes attributes) {

        // How many times does a word appear in a document? => docProp
        // How many words are there in a document? => Keep hashMap of <doc, #words>
        // How many documents in total? => Keep list of all docs while indexing. Save its length when done
        // How many documents contain a word? => length of docProp list

        Document doc = attributes.document;

        // Increase word counter for that document by the number of words in sentence
        int wordsInDoc = documents.containsKey(doc) ? documents.get(doc) : 0;
        int wordsInSentence = sentence.getWords().size();
        documents.put(doc, wordsInDoc + wordsInSentence);


        for (Word word : sentence.getWords()) {
            if (words.containsKey(word.word)) {
                // Word already exists
                boolean docExists = false;
                for (DocumentProperties docP : words.get(word.word)) {
                    if (docP.getDocument() == doc) {
                        // Document exists, increment its counter
                        docP.count++;
                        docExists = true;
                        break;
                    }
                }
                // Document does not exist, create new docP and add it
                if (!docExists) {
                    // Adding it first increases its performance for coming words in doc
                    words.get(word.word).addFirst(new DocumentProperties(attributes));
                }
            } else {
                // Word does not exit
                LinkedList<DocumentProperties> linkedList = new LinkedList<DocumentProperties>();
                linkedList.add(new DocumentProperties(attributes));
                words.put(word.word, linkedList);
            }
        }

    }

    public void postInserts() {

        for (Map.Entry<String, LinkedList<DocumentProperties>> entry : words.entrySet()) {
            for (DocumentProperties docP : entry.getValue()) {
                docP.setTfidf(tfidf(entry.getKey(), docP.getDocument()));
            }
        }

        //System.out.println("words: " + words.size());
        //System.out.println("documents: " + documents.size());

    }

    public List<Document> search(String s) {

        LinkedList<DocumentProperties> result = new LinkedList<DocumentProperties>();

        String[] terms = s.split(" ");
        String property = "", direction = "";

        boolean orderBy = false;
        if (terms.length > 3 && terms[terms.length - 3].equalsIgnoreCase("orderby")) {
            orderBy = true;
            property = terms[terms.length - 2];
            direction = terms[terms.length - 1];

            String[] temp = new String[terms.length - 3];
            /*for (int i = 0; i < terms.length - 3; i++) {
                temp[i] = terms[i];
            }*/
            System.arraycopy(terms, 0, temp, 0, terms.length - 3);
            terms = temp;
        }

        if (terms.length == 1) {
            if (words.get(terms[0]) != null)
                result.addAll(words.get(terms[0]));
        } else {

            Stack<String> operators = new Stack<String>();
            Stack<LinkedList<DocumentProperties>> listOperands = new Stack<LinkedList<DocumentProperties>>();
            Stack<Boolean> operand = new Stack<Boolean>(); // To keep track of when to pop from which stack

            for (String token : terms) {

                if (token.equals("|") || token.equals("+") || token.equals("-")) {
                    operand.push(false);
                    operators.push(token);
                } else {
                    // It is a word
                    if (operand.peek()) {
                        operand.pop();
                        LinkedList<DocumentProperties> operand1 = listOperands.pop(), operand2 = words.get(token);
                        operand.pop();
                        String operator = operators.pop();
                        boolean combineAgain = operand.size() > 0 ? operand.peek() : false;
                        operand.push(true);
                        listOperands.push(query(operand1, operand2, operator));

                        while (combineAgain) {
                            operand.pop();
                            operand2 = listOperands.pop();
                            operand.pop();
                            operand1 = listOperands.pop();
                            operand.pop();
                            operator = operators.pop();
                            combineAgain = operand.size() > 0 ? operand.peek() : false;
                            operand.push(true);
                            listOperands.push(query(operand1, operand2, operator));
                        }
                    } else {
                        operand.push(true);
                        listOperands.push(words.get(token));
                    }
                }

            }

            result = listOperands.pop();

            System.out.println("SIZE OF OPERAND STACK WHEN DONE: " + listOperands.size());
            System.out.println("SIZE OF OPERATOR STACK WHEN DONE: " + operators.size());
        }

        if (orderBy) {
            try {
                QueryOrderByHandler.handleQueryKeyWords(property, direction, result);
            } catch (Exception e) {
                QueryOrderByHandler.handleQueryError();
                return null;
            }
        }

        LinkedList<Document> resultDocuments = new LinkedList<Document>();

        for (DocumentProperties docP : result) {
            resultDocuments.add(docP.getDocument());
            //System.out.println("Relevance: " + docP.getTfidf());
        }

        //System.out.println("Cache size: " + cache.size());

        return resultDocuments;
    }

    public String infix(String s) {

        String[] terms = s.split(" ");
        String property = "", direction = "";
        String result;

        boolean orderBy = false;
        if (terms.length > 3 && terms[terms.length - 3].equalsIgnoreCase("orderby")) {
            orderBy = true;
            property = terms[terms.length - 2];
            direction = terms[terms.length - 1];

            String[] temp = new String[terms.length - 3];
            System.arraycopy(terms, 0, temp, 0, terms.length - 3);
            terms = temp;
        }

        Stack<String> operators = new Stack<String>();
        Stack<String> operands = new Stack<String>();
        Stack<Boolean> operand = new Stack<Boolean>(); // To keep track of when to pop from which stack

        if (terms.length == 1) {
            //result = "Query(" + terms[0] + ")";
            operands.push(terms[0]);
        } else {


            for (String token : terms) {

                if (token.equals("|") || token.equals("+") || token.equals("-")) {
                    operand.push(false);
                    operators.push(token);
                } else {
                    // It is a word
                    if (operand.peek()) {
                        operand.pop();
                        String operand1 = operands.pop(), operand2 = token;
                        operand.pop();
                        String operator = operators.pop();
                        boolean combineAgain = operand.size() > 0 ? operand.peek() : false;
                        operand.push(true);
                        operands.push("(" + operand1 + " " + operator + " " + operand2 + ")");

                        while (combineAgain) {
                            operand.pop();
                            operand2 = operands.pop();
                            operand.pop();
                            operand1 = operands.pop();
                            operand.pop();
                            operator = operators.pop();
                            combineAgain = operand.size() > 0 ? operand.peek() : false;
                            operand.push(true);
                            operands.push("(" + operand1 + " " + operator + " " + operand2 + ")");
                        }
                    } else {
                        operand.push(true);
                        operands.push(token);
                    }
                }
            }

        }
        result = "Query(" + operands.pop();
        if (orderBy)
            result += " ORDERBY " + property.toUpperCase() + " " + direction.toUpperCase() + ")";
        else
            result += ")";


        return result;
    }

    private LinkedList<DocumentProperties> query(LinkedList<DocumentProperties> operand1, LinkedList<DocumentProperties> operand2, String operator) {

        QueryFormat queryFormat = new QueryFormat(operand1, operand2, operator);

        for(Entry<QueryFormat, LinkedList<DocumentProperties>> e : cache) {
            if(QueryFormat.equivalent(queryFormat, e.getKey()))
                return e.getValue();
        }

        LinkedList<DocumentProperties> res;

        if (operator.equals("|")) {
            res = union(operand1, operand2);
        } else if (operator.equals("+")) {
            res = intersection(operand1, operand2);
        } else {
            res = difference(operand1, operand2);
        }

        cache.addFirst(new Entry<QueryFormat, LinkedList<DocumentProperties>>(queryFormat, res));

        return res;
    }

    private LinkedList<DocumentProperties> union(List<DocumentProperties> list1, List<DocumentProperties> list2) {
        LinkedList<DocumentProperties> union = new LinkedList<DocumentProperties>();
        if (list1 != null)
            union.addAll(list1);
        if (list2 != null) {
            for (DocumentProperties docP : list2) {
                if (!docPropListContainsDoc(union, docP.getDocument()))
                    union.add(docP);
                
            }
        }
        return union;
    }

    private LinkedList<DocumentProperties> intersection(List<DocumentProperties> list1, List<DocumentProperties> list2) {

        LinkedList<DocumentProperties> intersection = new LinkedList<DocumentProperties>();

        for (DocumentProperties docP : list1) {
            if (docPropListContainsDoc(list2, docP.getDocument()))
                intersection.add(docP);
        }

        return intersection;
    }

    private LinkedList<DocumentProperties> difference(List<DocumentProperties> list1, List<DocumentProperties> list2) {

        LinkedList<DocumentProperties> difference = new LinkedList<DocumentProperties>();

        for (DocumentProperties docP : list1) {
            if (!docPropListContainsDoc(list2, docP.getDocument()))
                difference.add(docP);
        }

        return difference;
    }

    private boolean docPropListContainsDoc(List<DocumentProperties> list, Document doc) {
        if (list == null || doc == null)
            return false;

        for (DocumentProperties docPRes : list) {
            if (doc == docPRes.getDocument())
                return true;
        }
        return false;
    }

    private int appearancesOfWordInDoc(String word, Document document) {
        for (DocumentProperties docP : words.get(word)) {
            if (docP.getDocument() == document)
                return docP.count;
        }
        return 0;
    }

    private int amountOfWordsInDoc(Document doc) {
        return documents.get(doc);
    }

    private int amountOfDocuments() {
        return documents.size();
    }

    private int amountOfDocumentsContains(String word) {
        return words.get(word).size();
    }

    private double tfidf(String word, Document doc) {
        double tf = (double) appearancesOfWordInDoc(word, doc) / amountOfWordsInDoc(doc);
        double idf = Math.log10((double) amountOfDocuments() / amountOfDocumentsContains(word));
        return tf * idf;
    }
}
