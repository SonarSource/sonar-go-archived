package org.sonar.uast;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class Uast {

  private Uast() {
    // utility class
  }

  public static UastNode from(Reader reader) throws IOException {
    return Unmarshaller.unmarshal(reader);
  }

  public static boolean syntacticallyEquivalent(UastNode node1, UastNode node2) {
    if (node1.token == null && node2.token != null) {
      return false;
    }
    if (node2.token != null && !node1.token.value.equals(node2.token.value)) {
      return false;
    }
    if (node1.kinds.contains(UastNode.Kind.UNSUPPORTED) || node2.kinds.contains(UastNode.Kind.UNSUPPORTED)) {
      return false;
    }
    CommentFilteredList list1 = new CommentFilteredList(node1.children);
    CommentFilteredList list2 = new CommentFilteredList(node2.children);
    if (list1.computeSize() != list2.computeSize()) {
      return false;
    }
    Iterator<UastNode> child1 = list1.iterator();
    Iterator<UastNode> child2 = list2.iterator();
    while (child1.hasNext()) {
      if (!syntacticallyEquivalent(child1.next(), child2.next())) {
        return false;
      }
    }
    return true;
  }

  public static boolean syntacticallyEquivalent(List<UastNode> node1, List<UastNode> node2) {
    if (node1.size() != node2.size()) {
      return false;
    }
    Iterator<UastNode> it1 = node1.iterator();
    Iterator<UastNode> it2 = node2.iterator();
    while (it1.hasNext()) {
      if (!syntacticallyEquivalent(it1.next(), it2.next())) {
        return false;
      }
    }
    return true;
  }

  private static class CommentFilteredList {

    private final List<UastNode> children;

    private CommentFilteredList(List<UastNode> children) {
      this.children = children;
    }

    private int computeSize() {
      int size = 0;
      Iterator<UastNode> iterator = iterator();
      while (iterator.hasNext()) {
        iterator.next();
        size++;
      }
      return size;
    }

    private Iterator<UastNode> iterator() {
      return new FilterIterator(children.iterator());
    }

    private class FilterIterator implements Iterator<UastNode> {

      private final Iterator<UastNode> iterator;

      private UastNode nextNode;

      private FilterIterator(Iterator<UastNode> iterator) {
        this.iterator = iterator;
        this.nextNode = findNext();
      }

      @Override
      public boolean hasNext() {
        return nextNode != null;
      }

      @Override
      public UastNode next() {
        if (nextNode == null) {
          throw new NoSuchElementException();
        }
        UastNode node = nextNode;
        nextNode = findNext();
        return node;
      }

      private UastNode findNext() {
        while (iterator.hasNext()) {
          UastNode node = iterator.next();
          if (!node.kinds.contains(UastNode.Kind.COMMENT)) {
            return node;
          }
        }
        return null;
      }
    }

  }

}
