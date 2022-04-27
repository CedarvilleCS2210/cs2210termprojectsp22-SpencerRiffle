package termproject;

import java.util.HashSet;
import java.util.Set;

/**
 * Title:        Term Project 2-4 Trees
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.0
 */
public class TwoFourTree
        implements Dictionary {

    private Comparator treeComp;
    private int size = 0;
    private TFNode treeRoot = null;

    public TwoFourTree(Comparator comp) {
        treeComp = comp;
    }

    private TFNode root() {
        return treeRoot;
    }

    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Searches dictionary to determine if key is present
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    public Object findElement(Object key) {
        TFNode currNode = treeRoot;
        // Iterate through TwoFourTree, checking for key
        while (true) {
            int indice = findFirstGreaterThanOrEqual(currNode, key);
            // If not the last child indice (i.e. no key), check if key matches
            if (indice != currNode.getNumItems()) {
                if (treeComp.isEqual(currNode.getItem(indice).key(), key)) {
                    return currNode;
                }
            }
            // If no match, enter the child if it exists
            if (currNode.getChild(indice) != null) {
                currNode = currNode.getChild(indice);
            }
            // Return null if no child exists
            else {
                return null;
            }
        }
    }

    /**
     * Inserts provided element into the Dictionary
     * @param key of object to be inserted
     * @param element to be inserted
     */
    public void insertElement(Object key, Object element) {
        // TODO
    }

    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
        // TODO
        return null;
    }
    
    /**
     * Finds the first key greater that or equal to given key
     * @param node 
     * @param key
     * @return index of first item greater than or equal
     */
    protected int findFirstGreaterThanOrEqual(TFNode node, Object key) {
        // Check for a valid user key
        if (!treeComp.isComparable(key)) {
            throw new TwoFourTreeException("User's key was not comparable");
        }
        // Iterate through node's item indices
        int i = 0;
        for (; i < node.getNumItems(); i++) {
            // Check for a valid item key
            if (!treeComp.isComparable(node.getItem(i).key())) {
                throw new TwoFourTreeException("Node's key was not comparable");
            }
            // Check if item's key if equal
            if (treeComp.isGreaterThanOrEqualTo(node.getItem(i).key(), key)) {
                break;
            }
        }
        return i;
    }
    
    /**
     * Finds the parent's index number of the given child node
     * @param node
     * @return parent's index number of the child
     */
    protected int whatChildIsThis(TFNode node) {
        // Check for null parent
        if (node.getParent() == null) {
            throw new TwoFourTreeException("Node's parent does not exist");
        }
        // Iterate through parent's child indices to check for self
        int i = 0;
        TFNode parent = node.getParent();
        for (; i < parent.getNumItems(); i++) {
            if (parent.getChild(i) == node) {
                break;
            }
        }
        return i;
    }
    
    /**
     * Finds the in-order successor of the item
     * @return the in-order successor of the given node
     */
    private Item inOrderSuccessor(TFNode node, int key) {
        // Find Item's left child
        int childIndice = findFirstGreaterThanOrEqual(node, key);
        // Switch to right child unless indice was already the right child
        if (childIndice < node.getNumItems()) {
            childIndice++;
        }
        // Get child at index
        TFNode child = node.getChild(childIndice);
        // Return leftmost Item in child
        return child.getItem(0);
    }
    
    private void fixOverflow(TFNode node) {
        // TODO
    }
    
    /**
     * Fixes any underflow in the given node within TwoFourTree
     * @param node Node that was underflowed
     */
    private void fixUnderflow(TFNode node) {
        int MIN_ITEMS = 2;
        TFNode parent = node.getParent();
        // Check for parent underflow (only occurs in root)
        if (parent == null) {
            // Set child to root and remove old node
            treeRoot = node.getChild(0);
            node = null;
        }
        // Determine leftTranfser
        else if (whatChildIsThis(node) > 0) {
            if (parent.getChild(whatChildIsThis(node) - 1).getNumItems() >= MIN_ITEMS) {
                leftTransfer(node);
            }
        }
        // Determine rightTransfer
        else if (whatChildIsThis(node) < parent.getNumItems()) {
            if (parent.getChild(whatChildIsThis(node) + 1).getNumItems() >= MIN_ITEMS) {
                rightTransfer(node);
            }
        }
        // Determine leftFusion
        else if (whatChildIsThis(node) > 0) {
            if (parent.getChild(whatChildIsThis(node) - 1).getNumItems() == 1) {
                leftFusion(node);
                
                // Check for underflow at end
                if (parent.getNumItems() == 0) {
                    fixUnderflow(parent);
                }
            }
        }
        // Determine rightFusion
        else if (whatChildIsThis(node) < parent.getNumItems()) {
            if (parent.getChild(whatChildIsThis(node) + 1).getNumItems() == 1) {
                rightFusion(node);
                
                // Check for underflow at end
                if (parent.getNumItems() == 0) {
                    fixUnderflow(parent);
                }
            }
        }
    }
    
    /**
     * Moves left sibling's max Item to parent and parent Item to given node
     * @param node 
     */
    private void leftTransfer(TFNode node) {
        // Get parent and sibling information
        TFNode parent = node.getParent();
        int siblingIndex = whatChildIsThis(node) - 1;
        TFNode sibling = parent.getChild(siblingIndex);
        
        // Store node that will be attached to underflowed node
        TFNode transferNode = sibling.getChild(sibling.getNumItems());
        // Remove transferNode from sibling's children
        sibling.setChild(sibling.getNumItems(), null);
        // Remove leftChild's item
        Item leftItem = sibling.deleteItem(sibling.getNumItems() - 1);
        // Replace parent with leftChild's item
        Item parentItem = parent.replaceItem(siblingIndex, leftItem);
        // Insert parent's item into underflowed node
        node.insertItem(0, parentItem);
        // Fix pointer(s)
        node.setChild(0, transferNode);
    }
    
    /**
     * Moves right sibling's min Item to parent and parent Item to given node
     * @param node 
     */
    private void rightTransfer(TFNode node) {
        // Get parent and sibling information
        TFNode parent = node.getParent();
        int siblingIndex = whatChildIsThis(node) + 1;
        TFNode sibling = parent.getChild(siblingIndex);
        
        // Store node that will be attached to underflowed node
        TFNode transferNode = sibling.getChild(0);
        // Remove leftChild's item (and shift children to correct pointers)
        Item rightItem = sibling.removeItem(0);
        // Replace parent with leftChild's item
        Item parentItem = parent.replaceItem(siblingIndex - 1, rightItem);
        // Insert parent's item into underflowed node
        node.insertItem(0, parentItem);
        // Fix pointer(s)
        node.setChild(node.getNumItems(), transferNode);
    }
    
    /**
     * Moves parent to left sibling's max item, removing current node
     * @param node 
     */
    private void leftFusion(TFNode node) {
        // WARNING:
        // This removes the original node from the tree completely
        
        // Get parent and sibling information
        TFNode parent = node.getParent();
        int siblingIndex = whatChildIsThis(node) - 1;
        TFNode sibling = parent.getChild(siblingIndex);
        
        // Get parentItem
        Item parentItem = parent.getItem(siblingIndex);
        // Remove parent item from its node (drops sibling)
        parent.removeItem(siblingIndex);
        // Move parentItem to sibling
        sibling.insertItem(sibling.getNumItems(), parentItem);
        // Store node that will be reattached
        TFNode transferNode = node.getChild(node.getNumItems());
        
        // Fix pointer(s)
        sibling.setChild(sibling.getNumItems(), transferNode);
        parent.setChild(siblingIndex, sibling);
    }
    
    /**
     * Moves parent to right sibling's min item, removing current node
     * @param node 
     */
    private void rightFusion(TFNode node) {
        // WARNING:
        // This removes the original node from the tree completely
        
        // Get parent and sibling information
        TFNode parent = node.getParent();
        int siblingIndex = whatChildIsThis(node) + 1;
        TFNode sibling = parent.getChild(siblingIndex);
        
        // Get parentItem (and fix pointers)
        Item parentItem = parent.getItem(siblingIndex - 1);
        // Remove parent item from its node (and remove underflowed node)
        parent.removeItem(siblingIndex - 1);
        // Move parentItem to sibling
        sibling.insertItem(0, parentItem);
        // Store node that will be reattached
        TFNode transferNode = node.getChild(node.getNumItems());
        
        // Fix pointer(s)
        sibling.setChild(0, transferNode);
    }

    public static void main(String[] args) {
        Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);

        Integer myInt1 = new Integer(47);
        myTree.insertElement(myInt1, myInt1);
        Integer myInt2 = new Integer(83);
        myTree.insertElement(myInt2, myInt2);
        Integer myInt3 = new Integer(22);
        myTree.insertElement(myInt3, myInt3);

        Integer myInt4 = new Integer(16);
        myTree.insertElement(myInt4, myInt4);

        Integer myInt5 = new Integer(49);
        myTree.insertElement(myInt5, myInt5);

        Integer myInt6 = new Integer(100);
        myTree.insertElement(myInt6, myInt6);

        Integer myInt7 = new Integer(38);
        myTree.insertElement(myInt7, myInt7);

        Integer myInt8 = new Integer(3);
        myTree.insertElement(myInt8, myInt8);

        Integer myInt9 = new Integer(53);
        myTree.insertElement(myInt9, myInt9);

        Integer myInt10 = new Integer(66);
        myTree.insertElement(myInt10, myInt10);

        Integer myInt11 = new Integer(19);
        myTree.insertElement(myInt11, myInt11);

        Integer myInt12 = new Integer(23);
        myTree.insertElement(myInt12, myInt12);

        Integer myInt13 = new Integer(24);
        myTree.insertElement(myInt13, myInt13);

        Integer myInt14 = new Integer(88);
        myTree.insertElement(myInt14, myInt14);

        Integer myInt15 = new Integer(1);
        myTree.insertElement(myInt15, myInt15);

        Integer myInt16 = new Integer(97);
        myTree.insertElement(myInt16, myInt16);

        Integer myInt17 = new Integer(94);
        myTree.insertElement(myInt17, myInt17);

        Integer myInt18 = new Integer(35);
        myTree.insertElement(myInt18, myInt18);

        Integer myInt19 = new Integer(51);
        myTree.insertElement(myInt19, myInt19);

        myTree.printAllElements();
        System.out.println("done");

        myTree = new TwoFourTree(myComp);
        final int TEST_SIZE = 10000;


        for (int i = 0; i < TEST_SIZE; i++) {
            myTree.insertElement(new Integer(i), new Integer(i));
            //          myTree.printAllElements();
            //         myTree.checkTree();
        }
        System.out.println("removing");
        for (int i = 0; i < TEST_SIZE; i++) {
            int out = (Integer) myTree.removeElement(new Integer(i));
            if (out != i) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
            if (i > TEST_SIZE - 15) {
                myTree.printAllElements();
            }
        }
        System.out.println("done");
    }

    public void printAllElements() {
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        }
        else {
            printTree(root(), indent);
        }
    }

    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
            printTree(start.getChild(i), indent);
        }
    }

    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("Child to parent confusion");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("Mixed null and non-null children");
                    printTFNode(start);
                }
                else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("Parent to child confusion");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("Duplicate children of node");
                            printTFNode(start);
                        }
                    }
                }

            }
        }

        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }

    }
}
