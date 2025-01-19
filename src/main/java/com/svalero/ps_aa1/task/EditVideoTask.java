package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.service.EditingVideoService;
import javafx.concurrent.Task;
import org.bytedeco.javacv.Frame;

import java.util.concurrent.PriorityBlockingQueue;

public class EditVideoTask extends Task<String> {

    public EditVideoTask(Frame frame, int frameIndex, PriorityBlockingQueue<EditingVideoService.FrameTask> frameQueue){

    }
    protected String call() throws Exception{
        return "";
    }
}
