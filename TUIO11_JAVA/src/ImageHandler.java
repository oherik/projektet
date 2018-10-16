import com.fazecast.jSerialComm.SerialPort;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;





public class ImageHandler {

    private List<BufferedImage> storyImageList = new ArrayList<>();
    private Map<Integer, Integer[]> storySolutionMap = new HashMap<>();
    private Map<Integer, BufferedImage> sensorImageMap = new HashMap<>();
    private BufferedImage correctImage;
    private BufferedImage wrongImage;

    private int currentStoryImageIndex;

    SerialPort comPort;
    List<Integer> activeSensors;

    boolean hasAnswered = false;
    boolean answerIsCorrect = false;


    public class SerialReadThread extends Thread {
        public void run(){
            comPort = SerialPort.getCommPorts()[0];
            comPort.openPort();
            try {
                while (true)
                {
                    while (comPort.bytesAvailable() == 0)
                        Thread.sleep(20);

                    byte[] readBuffer = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                    if(numRead > 0 ){
                        byte status = readBuffer[0];
                       // System.out.println("Status from Arduino: " + status);
                        if(status == 49){
                            makeTry();
                        }

                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            comPort.closePort();
        }
    }

    public ImageHandler(){
        SerialReadThread readThread = new SerialReadThread();
        readThread.start();

        try {
            correctImage = ImageIO.read(new File("./img/ja.jpg"));
            wrongImage = ImageIO.read(new File("./img/nej.jpg"));

            currentStoryImageIndex = 0;
            BufferedImage mission1Image = ImageIO.read(new File("./img/bild1.jpg"));
            storyImageList.add(mission1Image);

            Integer[] solutionForMission1 = {0, 6, 3, 9};
            storySolutionMap.put(0, solutionForMission1);

            sensorImageMap.put(0, ImageIO.read(new File("./img/gyroscope.jpg")));
            sensorImageMap.put(3, ImageIO.read(new File("./img/magnetometer.jpg")));
            sensorImageMap.put(6, ImageIO.read(new File("./img/proximity.jpg")));
            sensorImageMap.put(9, ImageIO.read(new File("./img/light.jpg")));



        } catch	(Exception e){
            System.out.println("oj. " + e);
        }

    }

    public void makeTry(){
        System.out.println("Gör en gissning");
        StringBuilder sb = new StringBuilder();
        for(Integer i : activeSensors){
            sb.append(i + ", ");
        }

        System.out.println("Hittade sensorer: " + sb);


        Integer[] correctSensors = storySolutionMap.get(currentStoryImageIndex);

        System.out.println(correctSensors);
        answerIsCorrect =  new HashSet<Integer>(Arrays.asList(correctSensors))
                .equals(new HashSet<Integer>(activeSensors));

        hasAnswered = true;

    }

    private void handleCorrectAnswer(Graphics g){
        System.out.println("Rätt svar");
        g.drawImage(correctImage, 0,0,null);
    }

    private void handleWrongAnswer(Graphics g){
        System.out.println("Fel svar");
        g.drawImage(wrongImage, 0,0,null);
    }



    public void drawBaseImage(Graphics g){
        BufferedImage currentImage = storyImageList.get(currentStoryImageIndex);
        g.drawImage(currentImage, 0, 0, null);
    }

    public void update(List<Integer> activeSensors, Graphics g){
        this.activeSensors = activeSensors;

        drawBaseImage(g);

        if(hasAnswered){
            if(answerIsCorrect){
                handleCorrectAnswer(g);
            } else {
                handleWrongAnswer(g);
            }
        }

    }
}
