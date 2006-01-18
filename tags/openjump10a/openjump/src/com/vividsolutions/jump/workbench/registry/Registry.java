package com.vividsolutions.jump.workbench.registry;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jump.util.CollectionMap;

/**
 * While a Registry is similar to a Blackboard in that they are both flexible
 * repositories of information, there are some subtle differences:
 * <ul>
 * <li>The Registry is a bit more structured (values are Lists as opposed to
 * general Objects).
 * <li>There is only one Registry, whereas there are Blackboards on several
 * different levels (the Workbench Blackboard, the Task Blackboard, the Layer
 * Blackboard, the LayerViewPanel Blackboard), thus representing varying degrees
 * of scope.
 * <li>Registry keys are in general "well known" to a greater degree than
 * Blackboard keys, which plugins tend to create as needed. Thus the Registry
 * can be thought of as being more static, and the Blackboard more fluid.
 * <li>Registry entries are intended to be much more static than Blackboard
 * entries. You might well think about persisting a Registry, but probably never
 * a Blackboard
 * <li>In the bigger world, Registries have all kinds of security,
 * classification and lifecyle features that probably would not appear on a
 * Blackboard.
 * </ul>
 */
public class Registry {
    private CollectionMap classificationToEntriesMap = new CollectionMap();

    public Registry createEntry(Object classification, Object entry) {
        classificationToEntriesMap.addItem(classification, entry);
        return this;
    }

    public List getEntries(Object classification) {
        return (List) new ArrayList(classificationToEntriesMap.getItems(classification));
    }
}