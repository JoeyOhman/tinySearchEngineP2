import se.kth.id1020.TinySearchEngineBase;
import se.kth.id1020.util.Attributes;
import se.kth.id1020.util.Document;
import se.kth.id1020.util.Sentence;
import se.kth.id1020.util.Word;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

public class TinySearchEngine implements TinySearchEngineBase {

    private HashMap<String, LinkedList<DocumentProperties>> words;
    private HashMap<Document, Integer> documents;

    public TinySearchEngine() {
        words = new HashMap<String, LinkedList<DocumentProperties>>(); // Key = Word, Value = list of docs
        documents = new HashMap<Document, Integer>(); // Key = doc, Value = amountOfWordsInDoc

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

    }

    public List<Document> search(String s) {

        LinkedList<DocumentProperties> result = new LinkedList<DocumentProperties>();

        String[] terms = s.split(" ");
        String property = "", direction = "";

        if(terms.length == 1) {
            if(words.get(terms[0]) != null)
                result.addAll(words.get(terms[0]));
        } else {

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

            // TODO probably doesn't work for all nested cases, operands and operators should be in the same stack!
            Stack<String> operators = new Stack<String>();
            Stack<LinkedList<DocumentProperties>> listOperands = new Stack<LinkedList<DocumentProperties>>();
            boolean prevTokenT = false; // Checks whether last token was a word
            boolean beforeLastOperatorT = false; // Checks whether token before last operator is a word

            for (String token : terms) {

                if (token.equals("|") || token.equals("+") || token.equals("-")) {
                    operators.push(token);
                    if(prevTokenT)
                        beforeLastOperatorT = true;
                    else
                        beforeLastOperatorT = false;

                    prevTokenT = false;
                } else {
                    // It is a word
                    if (prevTokenT) {
                        List<DocumentProperties> operand1 = listOperands.pop(), operand2 = words.get(token);
                        String operator = operators.pop();
                        listOperands.push(query(operand1, operand2, operator));

                        if(beforeLastOperatorT) {
                            operand2 = listOperands.pop();
                            operand1 = listOperands.pop();
                            operator = operators.pop();
                            listOperands.push(query(operand1, operand2, operator));
                        }
                    } else {
                        listOperands.push(words.get(token));
                    }
                    prevTokenT = true;
                }

            }

            result = listOperands.pop();

            System.out.println("SIZE OF OPERAND STACK WHEN DONE: " + listOperands.size());
            System.out.println("SIZE OF OPERATOR STACK WHEN DONE: " + operators.size());

            if (orderBy) {
                try {
                    QueryOrderByHandler.handleQueryKeyWords(property, direction, result);
                } catch (Exception e) {
                    QueryOrderByHandler.handleQueryError();
                    return null;
                }
            }
        }

        LinkedList<Document> resultDocuments = new LinkedList<Document>();
        //System.out.println("Occurrences: ");
        for (DocumentProperties docP : result) {
            resultDocuments.add(docP.getDocument());
            //System.out.print("  " + docP.combinedOccurrence);
        }
        //System.out.println();

        return resultDocuments;
    }

    private LinkedList<DocumentProperties> query(List<DocumentProperties> operand1, List<DocumentProperties> operand2, String operator) {

        LinkedList<DocumentProperties> res;

        if (operator.equals("|")) {
            res = union(operand1, operand2);
        } else if(operator.equals("+")) {
            res = intersection(operand1, operand2);
        } else {
            res = difference(operand1, operand2);
        }

        return res;
    }

    private LinkedList<DocumentProperties> union(List<DocumentProperties> list1, List<DocumentProperties> list2) {
        LinkedList<DocumentProperties> union = new LinkedList<DocumentProperties>();
        if (list1 != null)
            union.addAll(list1);
        if (list2 != null) {
            for(DocumentProperties docP : list2) {
                if(!docPropListContainsDoc(union, docP.getDocument()))
                    union.add(docP);
            }
        }
        return union;
    }

    private LinkedList<DocumentProperties> intersection(List<DocumentProperties> list1, List<DocumentProperties> list2) {

        LinkedList<DocumentProperties> intersection = new LinkedList<DocumentProperties>();

        for(DocumentProperties docP : list1) {
            if(docPropListContainsDoc(list2, docP.getDocument()))
                intersection.add(docP);
        }

        return intersection;
    }

    private LinkedList<DocumentProperties> difference(List<DocumentProperties> list1, List<DocumentProperties> list2) {

        LinkedList<DocumentProperties> difference = new LinkedList<DocumentProperties>();

        for(DocumentProperties docP : list1) {
            if(! docPropListContainsDoc(list2, docP.getDocument()))
                difference.add(docP);
        }

        return difference;
    }

    private boolean docPropListContainsDoc(List<DocumentProperties> list, Document doc) {
        if(list == null || doc == null)
            return false;

        for(DocumentProperties docPRes : list) {
            if(doc == docPRes.getDocument())
                return true;
        }
        return false;
    }

    public String infix(String s) {
        return null;
    }

    private int appearancesOfWordInDoc(Word word, Document document) {
        for (DocumentProperties docP : words.get(word.word)) {
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

    private int amountOfDocumentsContains(Word word) {
        return words.get(word.word).size();
    }

    private double tfidf(Word word, Document doc) {
        double tf = appearancesOfWordInDoc(word, doc) / amountOfWordsInDoc(doc);
        double idf = Math.log10(amountOfDocuments() / amountOfDocumentsContains(word));
        return tf * idf;
    }
}
