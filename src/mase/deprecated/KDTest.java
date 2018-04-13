/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.util;

import java.util.ArrayList;
import java.util.Random;
import mase.util.KdTree.SearchFilter;

/**
 *
 * @author jorge
 */
public class KDTest {
    
    public static void main(String[] args) {
        
        
        Random rand = new Random();
        KdTree<Integer> t = new KdTree.Euclidean<>(2);
        int targetCount = 0;
        for(int i = 0 ; i < 1000 ; i++) {
            int elem = rand.nextInt(10);
            if(elem % 10 == 0)
                targetCount++;
            t.addPoint(new double[]{rand.nextDouble(), rand.nextDouble()}, elem);
        }
        
        ArrayList<KdTree.SearchResult<Integer>> nearestNeighbours = t.nearestNeighbours(new double[]{0,0}, 1000);
        int count = 0;
        for(KdTree.SearchResult<Integer> r : nearestNeighbours) {
            if(r.payload != null) {
                count++;
            }
        }
        System.out.println("total: " + count);
        nearestNeighbours = t.nearestNeighbours(new double[]{0,0}, 1000, new SearchFilter<Integer>() {
            @Override
            public boolean accepts(Integer element) {
                return element % 10 == 0;
            }
        });
        count = 0;
        for(KdTree.SearchResult<Integer> r : nearestNeighbours) {
            if(r.payload != null) {
                count++;
            }
        }
        System.out.println("mod 0: " + count + " expected: " + targetCount);       
    }
    
}
