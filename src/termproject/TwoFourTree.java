package termproject;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

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
        // Node containing the value
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
        // Create item
        Item item = new Item(key, element);
        
        // Check empty tree
        if(treeRoot == null) {
            treeRoot = new TFNode();
        }
        // Node to keep track of place in tree
        TFNode curr = treeRoot;
        
        int index = findFirstGreaterThanOrEqual(curr, key);
        // Find leaf node to insert at
        while (curr.getChild(index) != null) {
            curr = curr.getChild(index);
            index = findFirstGreaterThanOrEqual(curr, key);
        }
        
        // At a leaf; insert item
        curr.insertItem(index, item);
        // Fix pointer(s) (must be null, because inserting at leaf)
        curr.setChild(index + 1, null);
        // Check for overflow
        if (curr.getNumItems() > curr.getMaxItems()) {
            fixOverflow(curr);
        }
    }

    /**
     * Searches dictionary to determine if key is present, then
     * removes and returns corresponding object
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    public Object removeElement(Object key) throws ElementNotFoundException {
        
        // Find node containing key
        TFNode elementNode = (TFNode)findElement(key);
        // If key does not exist
        if (elementNode == null) {
            throw new TwoFourTreeException("Key to remove not found");
        }
        
        // If key exists
        Item inOrS = inOrderSuccessor(elementNode, key);
        
        Item returnItem = new Item();
        
        int index = findFirstGreaterThanOrEqual(elementNode, key);
        // Continue remove based on leaf or not
        if(elementNode.getChild(0) == null) {
            returnItem = elementNode.removeItem(index);
            
            if(elementNode.getNumItems() == 0) {
                fixUnderflow(elementNode);
            }
            
            return returnItem.key();
        }
        else {
            // Get inOrderSuccessor node
            TFNode currChild = (TFNode) findElement(inOrS.key());
            // Replace item to remove with the inOrderSuccessor...
            returnItem = elementNode.replaceItem(index, inOrS);
            // Find index of inOrderSuccessor in the node it was originally in
            index = findFirstGreaterThanOrEqual(currChild, inOrS.key());
            // Do a shifting remove on the inOrderSuccessor
            Item removedItem = currChild.removeItem(index);
            // Check underflow
            if(currChild.getNumItems() == 0) {
                fixUnderflow(currChild);
            }
            //return item
            return returnItem.key();
        }
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
    private Item inOrderSuccessor(TFNode node, Object key) {
        // Find Item's left child
        int childIndice = findFirstGreaterThanOrEqual(node, key);
        // Switch to right child unless indice was already the right child
        // Or unless child is only item in node.
        if (childIndice < node.getNumItems()) {
            childIndice++;
        }
        // Get child at index
        TFNode child = node.getChild(childIndice);
        
        //if item has no inorder successor returns itself
        if(child == null) {
            return node.getItem(childIndice - 1);
        }
        
        while(child.getChild(0) != null) {
            child = child.getChild(0);
        }
        
        // Return leftmost Item in child
        return child.getItem(0);
    }
    
    private void fixOverflow(TFNode node) {
        // Item to be overflowed to new node
        Item overflowItem;
        // Item to be shifted to parent
        Item parentItem;
        // Node to contain the overflowed value
        TFNode newNode = new TFNode();
        // Parent node
        TFNode parent;
        
        // Get & delete overflowed item
        overflowItem = node.deleteItem(node.getNumItems() - 1);
        // Get & delete node to be new parent item
        parentItem = node.deleteItem(node.getNumItems() - 1);
        
        // Move overflowed item to new node
        newNode.insertItem(newNode.getNumItems(), overflowItem);
        // Shift node's overflowed child pointers to new node's child pointers
        newNode.setChild(0, node.getChild(node.getMaxItems()));
        newNode.setChild(newNode.getNumItems(), node.getChild(node.getMaxItems() + 1));
        // Remove node's overflowed child pointers
        node.setChild(node.getMaxItems(), null);
        node.setChild(node.getMaxItems() + 1, null);
        //set Children to point at parent
        if(newNode.getChild(0) != null) {
            newNode.getChild(0).setParent(newNode);
            newNode.getChild(newNode.getNumItems()).setParent(newNode);
        }
        
        // If parent exists, move into parent
        if (node.getParent() != null) {
            // Get parent
            parent = node.getParent();
            // Get parent insertion index
            int index = whatChildIsThis(node);
            // Add parentItem to the parent node
            parent.insertItem(index, parentItem);
            // Fix pointer(s)
            parent.setChild(index + 1, newNode);
            newNode.setParent(parent);
            // Check for parent overflow
            if (parent.getNumItems() > parent.getMaxItems()) {
                fixOverflow(parent);
            }
        }
        // Otherwise, create a new parent
        else {
            //isue connect chuildrerfghbnn
            
            // Create new parent node
            parent = new TFNode();
            // This must be the root, because only the root does not have a parent
            treeRoot = parent;
            // Store parentItem in parent
            parent.insertItem(parent.getNumItems(), parentItem);
            // Fix pointer(s)
            parent.setChild(0, node);
            parent.setChild(parent.getNumItems(), newNode);
            node.setParent(parent);
            newNode.setParent(parent);
        }
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
        else if (whatChildIsThis(node) > 0 && parent.getChild(whatChildIsThis(node) - 1).getNumItems() >= MIN_ITEMS) {
               leftTransfer(node);
        }
        // Determine rightTransfer
        else if (whatChildIsThis(node) < parent.getNumItems() && parent.getChild(whatChildIsThis(node) + 1).getNumItems() >= MIN_ITEMS) {
               rightTransfer(node);
        }
        // Determine leftFusion
        else if (whatChildIsThis(node) > 0 && parent.getChild(whatChildIsThis(node) - 1).getNumItems() == 1) {
            leftFusion(node);
                
            // Check for underflow at end
            if (parent.getNumItems() == 0) {
                fixUnderflow(parent);
            }
        }
        // Determine rightFusion
        else if (whatChildIsThis(node) < parent.getNumItems() && parent.getChild(whatChildIsThis(node) + 1).getNumItems() == 1) {
            rightFusion(node);
                
            // Check for underflow at end
            if (parent.getNumItems() == 0) {
                fixUnderflow(parent);
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
        //myTree.printAllElements();
        myTree.checkTree();
        
        Integer myInt2 = new Integer(83);
        myTree.insertElement(myInt2, myInt2);
        //myTree.printAllElements();
        myTree.checkTree();
        
        Integer myInt3 = new Integer(22);
        myTree.insertElement(myInt3, myInt3);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt4 = new Integer(16);
        myTree.insertElement(myInt4, myInt4);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt5 = new Integer(49);
        myTree.insertElement(myInt5, myInt5);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt6 = new Integer(100);
        myTree.insertElement(myInt6, myInt6);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt7 = new Integer(38);
        myTree.insertElement(myInt7, myInt7);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt8 = new Integer(3);
        myTree.insertElement(myInt8, myInt8);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt9 = new Integer(53);
        myTree.insertElement(myInt9, myInt9);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt10 = new Integer(66);
        myTree.insertElement(myInt10, myInt10);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt11 = new Integer(19);
        myTree.insertElement(myInt11, myInt11);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt12 = new Integer(23);
        myTree.insertElement(myInt12, myInt12);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt13 = new Integer(24);
        myTree.insertElement(myInt13, myInt13);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt14 = new Integer(88);
        myTree.insertElement(myInt14, myInt14);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt15 = new Integer(1);
        myTree.insertElement(myInt15, myInt15);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt16 = new Integer(97);
        myTree.insertElement(myInt16, myInt16);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt17 = new Integer(94);
        myTree.insertElement(myInt17, myInt17);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt18 = new Integer(35);
        myTree.insertElement(myInt18, myInt18);
        //myTree.printAllElements();
        myTree.checkTree();

        Integer myInt19 = new Integer(51);
        myTree.insertElement(myInt19, myInt19);
        myTree.checkTree();

        myTree.printAllElements();
        System.out.println("done");
        
        //Testing Left transfer
        System.out.println("Removing 66 to cause LT");
        int out = (Integer) myTree.removeElement(66);
        if(out != 66) {
            System.out.println("Incorrect element removed.");
        }
        myTree.printAllElements();
        myTree.checkTree();
        
        //Testing Right Transfer
        //CAUSES AN ERROR
        System.out.println("Removing 53 to cause RT");
        out = (Integer) myTree.removeElement(53);
        if(out != 53) {
            System.out.println("Incorrect element removed.");
        }
        myTree.printAllElements();
        myTree.checkTree();

        //myTree = new TwoFourTree(myComp);
        
        /*
        final int TEST_SIZE = 10000;
        
        // Testing for 10,000 integer values 
        int[] arr = new int[TEST_SIZE];
        Random rd = new Random();
        for (int i = 0; i < arr.length; i++) {
            arr[i] = rd.nextInt(TEST_SIZE / 5);
        }
        

        System.out.println("inserting");
        for (int i = 0; i < TEST_SIZE; i++) {
            //Adding key and element values from array to tree
            myTree.insertElement(arr[i], arr[i]);
            //prints and checks first 25 items
            if(i < 25) {
                System.out.println("Inserting: " + arr[i]);
                myTree.printAllElements();
                myTree.checkTree();
            }
        }
        System.out.println("removing");
        for (int i = 0; i < TEST_SIZE; i++) {
            int out = (Integer) myTree.removeElement(arr[i]);
            if (out != arr[i]) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
            
            //prints and checks last 25 items
            if (i > TEST_SIZE - 25) {
                System.out.println("Removing: " + arr[i]);
                myTree.printAllElements();
                myTree.checkTree();
            }
        }
        System.out.println("done");
        */
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
