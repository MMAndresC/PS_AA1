package com.svalero.ps_aa1.service;

import com.svalero.ps_aa1.interfaces.ShutdownExecutorService;
import com.svalero.ps_aa1.task.EditVideoTask;
import com.svalero.ps_aa1.utils.HistoryLogger;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FFmpegFrameGrabber;

public class EditingVideoService extends Service<List<EditingVideoService.FrameTask>> implements ShutdownExecutorService {
    private final String videoFilter;
    private final String videoPath;
    private final int brightness;
    private final PriorityBlockingQueue<FrameTask> frameQueue;
    private final ExecutorService executorService;
    private final List<FrameTask> editedFrames;
    private double frameRate;
    private int width;
    private int height;
    private int videoCodec;

    public EditingVideoService(String videoFilter, String videoPath, int brightness){
        this.videoFilter = videoFilter;
        this.videoPath = videoPath;
        this.brightness = brightness;
        this.executorService = Executors.newFixedThreadPool(10);
        this.frameQueue = new PriorityBlockingQueue<>();
        this.editedFrames = new ArrayList<>();
    }

    protected Task<List<FrameTask>> createTask() {
        return new Task<>() {
            @Override
            protected List<FrameTask> call() throws Exception {
                try (FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(videoPath)) {
                    frameGrabber.start();
                    getAttributesVideo(frameGrabber);
                    int totalFrames = frameGrabber.getLengthInFrames();
                    System.out.println("totalFrames" + totalFrames);
                    int processedFrames = 0;
                    for (int i = 0; i < totalFrames; i++) {
                        //Get frame
                        int frameIndex = i;
                        Frame frame = frameGrabber.grabImage();
                        if (frame == null) break;
                        Task<EditingVideoService.FrameTask> task
                                = new EditVideoTask(frame, frameIndex, frameQueue, videoFilter, brightness);
                        task.setOnFailed(event -> {
                            System.out.println("failed " + frameIndex);
                        });
                        task.setOnSucceeded(event -> {
                            System.out.println("task succeeded");
                        });
                        executorService.submit(task);
                    }
                        while (processedFrames < totalFrames) {
                            //Block empty queue
                            FrameTask frameTask = frameQueue.take();
                            //Save frame in List
                            editedFrames.add(frameTask);
                            System.out.println("Procesando frame en orden: " + frameTask.getIndex());
                            processedFrames++;
                        }
                    frameGrabber.stop();
                }catch (Exception e) {
                    HistoryLogger.logError("Error al inicializar el frame grabber: " + e.getMessage());
                }finally {
                    executorService.shutdown();
                }
                return editedFrames;
            }
        };
    }

    @Override
    public void shutdownNow(){
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
        }
    }
    @Override
    public void shutdown(){
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    public void getAttributesVideo(FFmpegFrameGrabber frameGrabber){
        this.frameRate = frameGrabber.getFrameRate();
        this.width = frameGrabber.getImageWidth();
        this.height = frameGrabber.getImageHeight();
        this.videoCodec = frameGrabber.getVideoCodec();
    }

    //Getters


    public double getFrameRate() {
        return frameRate;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getVideoCodec() {
        return videoCodec;
    }

    public static class FrameTask implements Comparable<FrameTask> {
        private final int index;
        private final Frame frame;

        public FrameTask(int index, Frame frame) {
            this.index = index;
            this.frame = frame;
        }

        public int getIndex() {
            return index;
        }

        public Frame getFrame() {
            return frame;
        }

        //Order by index
        @Override
        public int compareTo(FrameTask other) {
            return Integer.compare(this.index, other.index);
        }
    }
}
