package assignment;

import java.util.*;

//Class to construct and utilize the parse tree implementation for queries
public class ParseTree {
    //stack data structures to keep track of updating nodes and operators to parse tree
    Stack<Node> nodes;
    Stack<Character> operators;

    //constructor to create the parse tree given a query from the WebQueryEngine
    public ParseTree(String query) {
        //initializes the stacks
        nodes = new Stack<Node>();
        operators = new Stack<Character>();
        StringBuilder word = new StringBuilder("");
        //stores possible operators to be indexed for later
        String possibleOperations = "|&";
        //if query is a phrase, pushes the phrase as one word to root node
        if(query.indexOf("\"") == 0 && query.charAt(query.length()-1) == '\"')
            nodes.push(new Node(query.substring(1, query.length()-1)));
        //otherwise, applies the shunting yard algorithm to parse the query to tree
        else {
            //wraps the query with parentheses in case query is missing them
            query = "(" + query + ")";
            char[] tokenLetters = query.toCharArray();
            //iterates through all letters of the token
            for(int i = 0; i < tokenLetters.length; i++) {
                char token = tokenLetters[i];
                //appends to word if it is alphanumeric, an exclamation mark, or extra special characters
                if(checkAlphanumeric(token) || token == 33
                        || (token == 39) || (token >= 44 && token <= 46)) {
                    word.append(token);
                }
                //if open parentheses, then push to operators stack
                else if (token == '(') {
                    operators.push(token);
                }
                //if closed parentheses, then there must be an open parentheses
                else if (token == ')') {
                    //pushes current word as node if it is not empty
                    if(word.length() > 0) {
                        String finalWord = fixWords(word.toString());
                        nodes.push(new Node(finalWord));
                        word.setLength(0);
                    }
                    //checks to see if there are enough elements in both stacks to perform next operation
                    if(operators.size() >= 1 && nodes.size() >= 2) {
                        //continues to pop out from operators until open parentheses is found
                        while (operators.peek() != '(') {
                            //creates a new node with the last operator as value and the two words as children
                            nodes.push(new Node(Character.toString(operators.pop()), nodes.pop(), nodes.pop()));
                        }
                    }
                    //pops out final open parentheses
                    operators.pop();
                }
                //checks if token is an operation
                else if(possibleOperations.contains(Character.toString(token))) {
                    //adds current word as a node if it is present
                    if(word.length() > 0) {
                        String finalWord = fixWords(word.toString());
                        nodes.push(new Node(finalWord));
                        word.setLength(0);
                    }
                    //push operator immediately if stack is empty
                    if(operators.isEmpty()) {
                        operators.push(token);
                    }
                    //otherwise check if stack follows algorithm's operator precedence
                    else {
                        //sees if current operator has lower priority than the last added operator in stack
                        while(!operators.isEmpty() && possibleOperations.indexOf(operators.peek()) >
                                possibleOperations.indexOf(token)) {
                            //if so, then pull out last operator from stack and create new node
                            if(operators.size() >= 1 && nodes.size() >= 2)
                                nodes.push(new Node(Character.toString(operators.pop()), nodes.pop(), nodes.pop()));
                        }
                        //once operator precedence is set in stack, then push current token
                        operators.push(token);
                    }
                }
                //in case a space is encountered
                else if(Character.toString(token).equals(" ")) {
                    //check if this is an implicit AND space, meaning that another word appears after space
                    if (i + 1 < tokenLetters.length && word.length() > 0
                            && (checkAlphanumeric(tokenLetters[i+1]) || tokenLetters[i+1] == '(')) {
                        //cleans final word and pushes it as new node
                        String finalWord = fixWords(word.toString());
                        nodes.push(new Node(finalWord));
                        //resets word and iteration to include & instead of space in current location
                        word.setLength(0);
                        tokenLetters[i] = '&';
                        i--;
                    }
                }
                //if none of the cases are satisfied, then a parsing error occurred and an empty node returned
                else {
                    nodes = new Stack<Node>();
                    nodes.push(new Node());
                    break;
                }
            }
        }
        //if either stack is above this threshold, then parsing failed and empty node returned
        if(nodes.size() > 1 || operators.size() > 0) {
            nodes = new Stack<>();
            nodes.push(new Node());
        }
    }

    //getter method to return the root node of tree
    public Node getRoot() {
        return nodes.peek();
    }

    //method to clean word of punctuation on ends and preserve punctuation between letters
    public String fixWords(String word) {
        //removes unnecessary punctuation from left side of word
        while(!word.isEmpty() && checkExtraChar(word.charAt(0))) {
            word = word.substring(1);
        }
        //removes unnecessary punctuation from right side of word
        while(!word.isEmpty() && checkExtraChar(word.charAt(word.length() - 1))) {
            word = word.substring(0, word.length()-1);
        }
        return word;
    }

    //checks if element is alphanumeric
    public boolean checkAlphanumeric(char ch) {
        return (ch >= 97 && ch <= 122) || (ch >= 48 && ch <= 57);
    }

    //checks if element is not alphanumeric or exclamation mark
    public boolean checkExtraChar(char ch) {
        if((ch >= 48 && ch <= 57) || (ch >= 97 && ch <= 122) ||
                (ch == 33)) {
            return false;
        }
        return true;
    }
}