/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.evorbc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import mase.evaluation.VectorBehaviourResult;
import mase.util.KdTree;
import org.apache.commons.lang3.tuple.Pair;

/**
 *
 * @author jorge
 */
public class KdTreeCollector extends Collector {
    
    public static final int PENDING_REMOVALS_LIMIT = 100;
    private static final long serialVersionUID = 1L;
    private KdTree<CollectionEntry> tree;
    private int pendingRemovals = 0;

    private static final KdTree.SearchFilter<CollectionEntry> NON_DELETED = new KdTree.SearchFilter<CollectionEntry>() {
        @Override
        public boolean accepts(CollectionEntry element) {
            return element.ind != null;
        }
    };    
    
    @Override
    protected void removeFromCollection(CollectionEntry oldEntry) {
        super.removeFromCollection(oldEntry);
        // soft remove
        // the entry stays in the tree, but is flagged as removed
        oldEntry.br = null;
        oldEntry.ind = null;
        oldEntry.novelty = -1;
        oldEntry.generation = -1;
        oldEntry.fitness = -1;
        
        pendingRemovals++;
        // rebuild the tree
        if(pendingRemovals > PENDING_REMOVALS_LIMIT) {
            System.err.println("REBUILDING THE TREE");
            tree = new KdTree.Euclidean<>(vbr(collection.iterator().next()).length);
            for(CollectionEntry e : collection) {
                tree.addPoint(vbr(e), e);
            }
            pendingRemovals = 0;
        }
        
    }

    @Override
    protected void addToCollection(CollectionEntry entry) {
        super.addToCollection(entry);
        if(!(entry.br instanceof VectorBehaviourResult)) {
            throw new RuntimeException("Behaviour should be VectorBehaviourResult but is " + entry.br + ", behaviour-index: " + behaviourIndex);
        }
        if(tree == null) {
            // not created yet, since we need the first entry to see the number of behaviour dimensions
            tree = new KdTree.Euclidean<>(vbr(entry).length);
        }
        tree.addPoint(vbr(entry), entry);
    }

    @Override
    protected List<Pair<CollectionEntry, Double>> getNearestNeighbours(CollectionEntry entry, int k) {
        if(tree == null) {
            return new ArrayList<>();
        }
        // +1 to exclude self if that's the case
        ArrayList<KdTree.SearchResult<CollectionEntry>> nns = tree.nearestNeighbours(vbr(entry), k + 1, NON_DELETED);
        ArrayList<Pair<CollectionEntry, Double>> result = new ArrayList<>(k);
        Iterator<KdTree.SearchResult<CollectionEntry>> iter = nns.iterator();
        while(result.size() < k && iter.hasNext()) {
            KdTree.SearchResult<CollectionEntry> next = iter.next();
            if(next.payload != null && next.payload != entry) {
                result.add(Pair.of(next.payload, next.distance));
            }
        }
        return result;
    }
    
    private double[] vbr(CollectionEntry e) {
        return ((VectorBehaviourResult) e.br).getBehaviour();
    }    
}
