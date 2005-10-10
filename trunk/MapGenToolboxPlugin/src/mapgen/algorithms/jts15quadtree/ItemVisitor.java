package mapgen.algorithms.jts15quadtree;

/**
 * A visitor for items in an index.
 *
 * @version 1.4
 */

public interface ItemVisitor
{
  void visitItem(Object item);
}