package logic;

import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * taken from https://github.com/husonlab/catlynet/blob/master/src/catlynet/model/DisjunctiveNormalForm.java
 * and added minor modifications
 */
class DisjunctiveNormalForm {

    static String compute(String expression) {
        String result = recurse(expression.replaceAll("\\s+", ","))
                .stream().map(Object::toString)
                .collect(Collectors.joining(","));

        result = result.replaceAll("([&,])(\\d+|\\d+\\.\\d+)(,)([A-Za-z-_/().'%][A-Za-z0-9-_/().'%]*)", "$1%%$2%coeffOf%$4");
        return result;
    }

    private static Collection<String> recurse(String expression) {
        if (expression.startsWith("(")) {
            int other = findBalance(expression, 0);
            if (other == expression.length() - 1)
                return recurse(expression.substring(1, other));
            else {
                int operator = expression.charAt(other + 1);
                if (operator == ',') {
                    return union(recurse(expression.substring(1, other)), recurse(expression.substring(other + 2)));
                } else if (operator == '&') {
                    return product(recurse(expression.substring(1, other)), recurse(expression.substring(other + 2)));
                }
            }
        } else {
            int pos = nextOr(expression, 0);
            if (pos > 0) {
                return union(recurse(expression.substring(0, pos)), recurse(expression.substring(pos + 1)));
            }
            pos = nextAnd(expression, 0);
            if (pos > 0) {
                return product(recurse(expression.substring(0, pos)), recurse(expression.substring(pos + 1)));
            }
        }
        return Collections.singletonList(expression);
    }


    private static int findBalance(String expression, int i) {
        int depth = 0;
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == '(')
                depth++;
            else if (ch == ')')
                depth--;
            if (depth == 0)
                return i;
            i++;
        }
        return -1;
    }

    private static int nextOr(String expression, int i) {
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == ',')
                return i;
            else if (ch == '(')
                return -1;
            i++;

        }
        return -1;
    }

    private static int nextAnd(String expression, int i) {
        while (i < expression.length()) {
            int ch = expression.charAt(i);
            if (ch == '&')
                return i;
            else if (ch == '(')
                return -1;
            i++;

        }
        return -1;
    }

    public static Collection<String> union(Collection<String> a, Collection<String> b) {
        final TreeSet<String> set = new TreeSet<>(a);
        set.addAll(b);
        return set;
    }

    public static Collection<String> product(Collection<String> a, Collection<String> b) {
        final TreeSet<String> set = new TreeSet<>();
        for (String aString : a) {
            for (String bString : b) {
                if (aString.equals(bString))
                    set.add(aString);
                else
                    set.add(aString + "&" + bString);
            }
        }
        return set;
    }
}

