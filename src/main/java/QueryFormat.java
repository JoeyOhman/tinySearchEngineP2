import java.util.LinkedList;

public class QueryFormat {

    private LinkedList<DocumentProperties> operand1, operand2;
    private String operator;

    public QueryFormat(LinkedList<DocumentProperties> operand1, LinkedList<DocumentProperties> operand2, String operator) {
        this.operand1 = operand1;
        this.operand2 = operand2;
        this.operator = operator;
    }

    public static boolean equivalent(QueryFormat q1, QueryFormat q2) {
        if (!q1.operator.equals(q2.operator))
            return false;
        else {
            String commonOperator = q1.operator;
            if(commonOperator.equals("-")) {
                if(q1.operand1 == q2.operand1 && q1.operand2 == q2.operand2)
                    return true;
                else
                    return false;
            } else {
                if ((q1.operand1 == q2.operand1 && q1.operand2 == q2.operand2) ||
                        (q1.operand1 == q2.operand2 && q1.operand2 == q2.operand1)) {

                    // Same operator (| or +) and same operands
                    return true;
                } else
                    return false;
            }
        }
    }

}
