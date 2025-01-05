package com.svalero.ps_aa1.task;

import javafx.concurrent.Task;

public class LoadingTask extends Task<Integer> {
    @Override
    protected Integer call() throws Exception{
        for(int i = 0; i <= 100; i++) {
            Thread.sleep(15);
            updateProgress(i, 100);
        }
        return null;
    }

}
