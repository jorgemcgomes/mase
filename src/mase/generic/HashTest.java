/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.generic;

import java.util.Arrays;
import java.util.HashSet;

/**
 *
 * @author jorge
 */
public class HashTest {

    public static void main(String[] args) {
        byte[][] states = generateStates(3, 12);

        for (int s = 0; s < 20; s++) {
            HashSet<Integer> hashes = new HashSet<Integer>();
            int conflictCount = 0;
            long start = System.currentTimeMillis();
            for (int i = 0; i < states.length; i++) {
                int h = hashVector(states[i]);
                //int h = XXHash.digestFast32(states[i], s, false);
                if (hashes.contains(h)) {
                    conflictCount++;
                } else {
                    hashes.add(h);
                }
            }
            long dur = System.currentTimeMillis() - start;
            System.out.println(states.length + " states. " + conflictCount + " hases conflicts. Time: " + dur);
        }

    }

    public static byte[][] generateStates(int D, int L) {
        int N = (int) Math.pow(D, L);
        byte[][] res = new byte[N][L];
        for (int l = 0; l < L; l++) {
            for (int n = 0; n < N; n++) {
                res[n][l] = (byte) ((n / (int) Math.pow(D, l)) % D);
            }
        }
        return res;
    }

    public static int hashVector(byte[] array) {
        int hash = 0;
        for (byte b : array) {
            hash += (b & 0xFF);
            hash += (hash << 10);
            hash ^= (hash >>> 6);
        }
        hash += (hash << 3);
        hash ^= (hash >>> 11);
        hash += (hash << 15);
        return Math.abs(hash);
    }

}
