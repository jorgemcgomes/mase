/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.app.allocation;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author jorge
 */
public class DispTest {
    
    public static void main(String[] args) {
        
        // The distance between two vertices will be the number of different entries between the binary representations of the vertices.
        
        int n = 3;
        int numTypes = 10;
        
        //for
        
    }
    
    public static List<int[]> getVertex(int dimensions, int num) {
        
        
        List<int[]> result = new LinkedList<>();
        int[] initial = new int[dimensions];
        Arrays.fill(initial, 0);
        result.add(initial);
        
        //recursive()
        return null;
    }
    
    private static void recursive(List<int[]> accum, int dimensions, int num, int[] from) {
        
    }
    
    public static int[] flip(int[] v) {
        int[] res = new int[v.length];
        for(int i = 0 ; i < v.length ; i++) {
            res[i] = v[i] == 0 ? 1 : 0;
        }
        return res;
    }
    
}
