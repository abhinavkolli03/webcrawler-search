package assignment;

//Node class used to store elements in the parse tree
public class Node {
    //instance variables to store element and left/right children
    String val;
    Node left;
    Node right;
    //various constructors to initialize the Node
    public Node() {}
    public Node(String val) {
        this.val = val;
    }
    public Node(String val, Node left, Node right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }
}