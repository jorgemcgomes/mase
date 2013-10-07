/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package deprecated;

/**
 *
 * @author jorge
 */
public class EnumTest {
    
    public enum Distance {
        BRAY, EUCLIDEAN, COSINE
    }
    
    public static void main(String[] args) {
        String stuff = "EUCLIDEAN";
        Distance choice = null;
        for(Distance d : Distance.values()) {
            if(stuff.equals(d.toString())) {
                choice = d;
            }
        }
        System.out.println(choice);
    }
    
}
