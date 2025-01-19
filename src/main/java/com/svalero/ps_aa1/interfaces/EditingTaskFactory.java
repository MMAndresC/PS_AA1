package com.svalero.ps_aa1.interfaces;

import com.svalero.ps_aa1.service.EditingVideoService;
import org.bytedeco.javacv.Frame;

import javafx.concurrent.Task;
import java.util.concurrent.PriorityBlockingQueue;

@FunctionalInterface
public interface EditingTaskFactory {
    Task<String> createEditingTask(Frame frame, int frameIndex, PriorityBlockingQueue<EditingVideoService.FrameTask> frameQueue);
}