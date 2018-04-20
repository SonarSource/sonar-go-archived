/*
 * SonarQube Go Plugin
 * Copyright (C) 2018-2018 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.uast;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import javax.annotation.Nullable;

public final class SyntacticEquivalence {

  private SyntacticEquivalence() {
    // utility class
  }

  public static boolean areEquivalent(@Nullable UastNode node1, @Nullable UastNode node2) {
    if (node1 == null && node2 == null) {
      return true;
    }
    if (node1 == null || node2 == null) {
      return false;
    }
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
      if (!areEquivalent(child1.next(), child2.next())) {
        return false;
      }
    }
    return true;
  }

  public static boolean areEquivalent(List<UastNode> node1, List<UastNode> node2) {
    if (node1.size() != node2.size()) {
      return false;
    }
    Iterator<UastNode> it1 = node1.iterator();
    Iterator<UastNode> it2 = node2.iterator();
    while (it1.hasNext()) {
      if (!areEquivalent(it1.next(), it2.next())) {
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
