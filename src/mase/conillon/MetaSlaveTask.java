/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mase.conillon;

import java.util.ArrayList;
import result.Result;
import tasks.Task;

/**
 *
 * @author jorge
 */
public class MetaSlaveTask extends Task {

    private static final long serialVersionUID = 1L;
    
    ArrayList<SlaveTask> tasks;
    MetaSlaveResult res;
    
    public MetaSlaveTask(ArrayList<SlaveTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public Result getResult() {
        return res;
    }

    @Override
    public void run() {
        ArrayList<SlaveResult> list = new ArrayList<>();
        for(SlaveTask t : tasks) {
            t.run();
            list.add(t.getResult());
        }
        this.res = new MetaSlaveResult(list);
    }
    
}
