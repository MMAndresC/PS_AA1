package com.svalero.ps_aa1.task;

import com.svalero.ps_aa1.service.EditingVideoService;
import com.svalero.ps_aa1.utils.FileManager;
import com.svalero.ps_aa1.utils.ImageFilters;
import javafx.concurrent.Task;
import org.bytedeco.javacv.Frame;

import java.awt.image.BufferedImage;
import java.util.concurrent.PriorityBlockingQueue;

public class EditVideoTask extends Task<EditingVideoService.FrameTask> {
    private final Frame frame;
    private final int frameIndex;
    private final String filter;
    private final FileManager fileManager;
    private final ImageFilters imageFilters;
    private final int brightness;
    private final PriorityBlockingQueue<EditingVideoService.FrameTask> frameQueue;

    public EditVideoTask (
            Frame frame,
            int frameIndex,
            PriorityBlockingQueue<EditingVideoService.FrameTask> frameQueue,
            String filter,
            int brightness
    ){
        this.frame = frame;
        this.frameIndex = frameIndex;
        this.frameQueue = frameQueue;
        this.filter = filter;
        this.imageFilters = new ImageFilters();
        this.fileManager = new FileManager();
        this.brightness = brightness;
    }
    protected EditingVideoService.FrameTask call() throws Exception{
        try {
            BufferedImage buffFrame = fileManager.frameToBufferedImage(frame);
            BufferedImage newBuffFrame = switch (filter) {
                case "bright" -> imageFilters.changeBrightness(brightness, buffFrame, 100, 0, this::updateProgress, true);
                case "gray" -> imageFilters.invertColor(buffFrame, 100, 0, this::updateProgress, true);
                case "color" -> imageFilters.toGrayScale(buffFrame, 100, 0, this::updateProgress, true);
                default -> null;
            };
            if (newBuffFrame != null) {
                Frame editedFrame = fileManager.bufferedImageToFrame(newBuffFrame);
                EditingVideoService.FrameTask frameTask =new EditingVideoService.FrameTask(frameIndex, editedFrame);
                frameQueue.put(frameTask);
                return frameTask;
            } else {
                EditingVideoService.FrameTask frameTask =new EditingVideoService.FrameTask(frameIndex, frame);
                frameQueue.put(frameTask);
                return frameTask;
            }
        }catch (Exception e){
           //Failed get in queue too
            EditingVideoService.FrameTask frameTask =new EditingVideoService.FrameTask(frameIndex, frame);
            frameQueue.put(frameTask);
            return frameTask;
        }
    }
}
