import com.fazecast.jSerialComm.SerialPort;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.List;





public class ImageHandler {
    private BufferedImage removeSensorImage;
    private List<BufferedImage> tutorialImageList = new ArrayList<>();
    private List<BufferedImage> storyImageList = new ArrayList<>();
    private List<BufferedImage> correctImageList = new ArrayList<>();
    private List<BufferedImage> wrongImageList = new ArrayList<>();
    private List<Rectangle> bounds = new ArrayList<>();
    private Map<Integer, Integer> storySolutionMap = new HashMap<>();
    private Map<Integer, BufferedImage> sensorImageMap = new HashMap<>();
    private BufferedImage correctImage;
    private BufferedImage wrongImage;
    private BufferedImage winImage;
    private boolean needsToRemoveSensors = true;
    private boolean isInTutorial = true;
    private boolean isInWinState = false;

    private boolean pauseLedSending = false;

    private long lastCorrectTime;
    private int pauseTime = 5000;

    private long winTime;
    private int winPauseTime = 10000;

    private int currentTutorialIndex = 0;
    private int currentStoryIndex = 0;

    private TuioDemoObject currentSensor;

    SerialPort comPort;
    List<TuioDemoObject> activeSensors;

    boolean hasAnswered = false;
    boolean answerIsCorrect = false;
    boolean listenToButton = false;

    private boolean runReadThread;


    public class SerialReadThread extends Thread {
        public void run(){
            comPort = SerialPort.getCommPorts()[0];
            comPort.setComPortParameters(9600, 8, 1, 0);
            comPort.setComPortTimeouts(SerialPort.TIMEOUT_WRITE_BLOCKING, 0, 0);
            comPort.openPort();


            try {
                while (runReadThread)
                {


                    if(listenToButton ||isInTutorial ) {
                        while (comPort.bytesAvailable() == 0) {
                            Thread.sleep(20);
                        }

                        byte[] readBuffer = new byte[comPort.bytesAvailable()];
                        int numRead = comPort.readBytes(readBuffer, readBuffer.length);
                        if (numRead > 0) {
                            byte status = readBuffer[0];
                            if (status == 49) {
                                System.out.println("Knapptryck");
                                handleButtonPress();
                            }

                        }

                    } else { // Set leds and stuff
                        if(!needsToRemoveSensors && !isInWinState && !pauseLedSending){

                        setCurrentLed(currentStoryIndex);
                        Thread.sleep(50);
                        if(hasAnswered){
                            if(answerIsCorrect){
                                setLedCorrect();
                            } else {
                                setLedWrong();
                            }
                        } else {
                            setLedActive();
                        }
                        } else {
                            Thread.sleep(50);
                        }
                    }
                }
            } catch (Exception e) { e.printStackTrace(); }
            comPort.closePort();
        }
    }

    public ImageHandler(){
        runReadThread = true;
        SerialReadThread serialReadThread = new SerialReadThread();


        serialReadThread.start();

        try {
            BufferedImage tut1 = ImageIO.read(new File("./img/Intro/Intro - 1.png"));
            BufferedImage tut2 = ImageIO.read(new File("./img/Intro/Intro - 2.png"));
            BufferedImage tut3 = ImageIO.read(new File("./img/Intro/Intro - 3.png"));
            tutorialImageList.add(tut1);
            tutorialImageList.add(tut2);
            tutorialImageList.add(tut3);

            removeSensorImage = ImageIO.read(new File("./img/Intro/Intro - 4.png"));
            isInTutorial = true;
            currentTutorialIndex = 0;




            currentStoryIndex = 0;
            BufferedImage base1 = ImageIO.read(new File("./img/Questions/Q1 - 1.png"));
            BufferedImage base2 = ImageIO.read(new File("./img/Questions/Q2 - 1.png"));
            BufferedImage base3 = ImageIO.read(new File("./img/Questions/Q3 - 1.png"));
            BufferedImage base4 = ImageIO.read(new File("./img/Questions/Q4 - 1.png"));
            BufferedImage base5 = ImageIO.read(new File("./img/Questions/Q5 - 1.png"));
            storyImageList.add(base1);
            storyImageList.add(base2);
            storyImageList.add(base3);
            storyImageList.add(base4);
            storyImageList.add(base5);

            BufferedImage base1w = ImageIO.read(new File("./img/Questions/Q1 - 2.png"));
            BufferedImage base2w = ImageIO.read(new File("./img/Questions/Q2 - 2.png"));
            BufferedImage base3w = ImageIO.read(new File("./img/Questions/Q3 - 2.png"));
            BufferedImage base4w = ImageIO.read(new File("./img/Questions/Q4 - 2.png"));
            BufferedImage base5w = ImageIO.read(new File("./img/Questions/Q5 - 2.png"));
            wrongImageList.add(base1w);
            wrongImageList.add(base2w);
            wrongImageList.add(base3w);
            wrongImageList.add(base4w);
            wrongImageList.add(base5w);

            BufferedImage base1c = ImageIO.read(new File("./img/Questions/Q1 - 3.png"));
            BufferedImage base2c = ImageIO.read(new File("./img/Questions/Q2 - 3.png"));
            BufferedImage base3c = ImageIO.read(new File("./img/Questions/Q3 - 3.png"));
            BufferedImage base4c = ImageIO.read(new File("./img/Questions/Q4 - 3.png"));
            BufferedImage base5c = ImageIO.read(new File("./img/Questions/Q5 - 3.png"));
            correctImageList.add(base1c);
            correctImageList.add(base2c);
            correctImageList.add(base3c);
            correctImageList.add(base4c);
            correctImageList.add(base5c);

            bounds.add(new Rectangle(0,0,33,33));
            bounds.add(new Rectangle(67,0,33,33));
            bounds.add(new Rectangle(33,33,33,33));
            bounds.add(new Rectangle(0,67,33,33));
            bounds.add(new Rectangle(67,67,33,33));






            storySolutionMap.put(0, 0);
            storySolutionMap.put(1, 9);
            storySolutionMap.put(2, 4);
            storySolutionMap.put(3, 7);
            storySolutionMap.put(4, 8);



            sensorImageMap.put(8, ImageIO.read(new File("./img/Sensors (Overlays)/Compass.png")));
            sensorImageMap.put(5, ImageIO.read(new File("./img/Sensors (Overlays)/Light.png")));
            sensorImageMap.put(9, ImageIO.read(new File("./img/Sensors (Overlays)/Camera.png")));
            sensorImageMap.put(0, ImageIO.read(new File("./img/Sensors (Overlays)/Antenna.png")));
            sensorImageMap.put(3, ImageIO.read(new File("./img/Sensors (Overlays)/Proximity.png")));
            sensorImageMap.put(4, ImageIO.read(new File("./img/Sensors (Overlays)/Microphone.png")));
            sensorImageMap.put(1, ImageIO.read(new File("./img/Sensors (Overlays)/Temperature.png")));
            sensorImageMap.put(7, ImageIO.read(new File("./img/Sensors (Overlays)/GPS.png")));
            sensorImageMap.put(6, ImageIO.read(new File("./img/Sensors (Overlays)/Gyroscope.png")));



            winImage = ImageIO.read(new File("./img/Finale/Finale.png"));



        } catch	(Exception e){
            System.out.println("oj. " + e);
        }

    }

    private boolean isSensorInsideBounds(TuioDemoObject sensor, int storyIndex){
        Point p = new Point(Math.round(sensor.getX()*100), Math.round(sensor.getY()*100));
        return bounds.get(storyIndex).contains(p);
    }

    private void setLedCorrect(){
        try {
            comPort.getOutputStream().write(Integer.valueOf(7).byteValue());
            comPort.getOutputStream().flush();
        }catch(Exception e) {
            comPort.closePort();
        }


    }

    private void setLedActive(){
        try {
            comPort.getOutputStream().write(Integer.valueOf(6).byteValue());
            comPort.getOutputStream().flush();
        }catch(Exception e) {
            comPort.closePort();
        }


    }

    private void setCurrentLed(int i){
        try {
            comPort.getOutputStream().write(Integer.valueOf(i).byteValue());
            comPort.getOutputStream().flush();
        }catch(Exception e) {
            comPort.closePort();
        }

    }
    private void setLedWrong(){
        try {
            comPort.getOutputStream().write(Integer.valueOf(8).byteValue());
            comPort.getOutputStream().flush();
        }catch(Exception e) {
            comPort.closePort();
        }

    }

    private void handleButtonPress(){
        if(isInTutorial) {
            if (currentTutorialIndex == tutorialImageList.size() - 1){
               // needsToRemoveSensors = activeSensors.size() > 0;
              //  if(!needsToRemoveSensors) {
                    isInTutorial = false;
                    currentStoryIndex = 0;
              //  }
            } else {
                currentTutorialIndex ++;
            }
        } else {
          //  currentStoryIndex++;

            //TODO wat to do
        }



    }

    public void goToNextStory(){
        if(currentStoryIndex == storyImageList.size() - 1){
            winTime = System.currentTimeMillis();
            isInWinState = true;
        } else {
            currentStoryIndex ++;
        }


    }

    public void reset(){
        hasAnswered = false;
        answerIsCorrect = false;
        currentStoryIndex = 0;
        currentTutorialIndex = 0;
        isInWinState = false;
        isInTutorial = true;
        currentSensor = null;


        runReadThread = false;
        try{
            Thread.sleep(100);
            runReadThread = true;
            SerialReadThread readThread = new SerialReadThread();


            readThread.start();

        } catch(Exception e){

        }


    }

    public void computeState(){
        if(isInWinState) {

            if(System.currentTimeMillis() - winTime > winPauseTime){
                reset();
            }



        } else {
            if(!isInTutorial){
                needsToRemoveSensors = needsToRemoveSensors &&  activeSensors.size() > 0;

                if(!needsToRemoveSensors) {

                    if(System.currentTimeMillis() - lastCorrectTime < pauseTime) {
                        // TODO vänta?
                    } else {
                        if(hasAnswered && answerIsCorrect) {
                            try {
                                pauseLedSending = true;
                                goToNextStory();
                                Thread.sleep(50);
                                hasAnswered = false;
                                Thread.sleep(50);
                                answerIsCorrect = false;
                                Thread.sleep(50);
                                pauseLedSending = false;
                            } catch(Exception e){

                            }

                        } else {
                            activeSensors.removeIf(sensor -> !(isSensorInsideBounds(sensor, currentStoryIndex)));

                            hasAnswered = activeSensors.size() > 0;

                            if (hasAnswered) {
                                currentSensor = activeSensors.get(0);
                                Integer correctSensorId = storySolutionMap.get(currentStoryIndex);

                                answerIsCorrect = answerIsCorrect || currentSensor.getSymbolID() == correctSensorId;

                                if (answerIsCorrect) {
                                    lastCorrectTime = System.currentTimeMillis();
                                }

                            } else {
                                currentSensor = null;

                            }


                        }



                    }







                }
            }



        }




        //System.out.println("Hittade sensorer: " + sb);


    }


    private void drawTutorial(Graphics g ) {
        BufferedImage currentImage;

            currentImage = tutorialImageList.get(currentTutorialIndex);

        g.drawImage(currentImage, 0, 0, null);
    }

    private void drawWinState(Graphics g){
        g.drawImage(winImage, 0, 0, null);
    }

    
    private void drawGame(Graphics g) {
        if(needsToRemoveSensors){
            g.drawImage(removeSensorImage,0,0,null);
        } else {
            if (hasAnswered) {
                if (answerIsCorrect) {
                    g.drawImage(correctImageList.get(currentStoryIndex), 0, 0, null);
                } else {
                    g.drawImage(wrongImageList.get(currentStoryIndex), 0, 0, null);
                }
                if (currentSensor != null) {
                    g.drawImage(sensorImageMap.get(currentSensor.getSymbolID()), 0, 0, null);
                }
            } else {
                g.drawImage(storyImageList.get(currentStoryIndex), 0, 0, null);
            }


        }

        
        
    }
    
    
    public void drawBaseImage(Graphics g){
        if (isInTutorial) {
            drawTutorial(g);
        } else {
            if(isInWinState)
                drawWinState(g);
            else
                drawGame(g);
        }
    }

    public void update(List<TuioDemoObject> activeSensors, Graphics g){
        this.activeSensors = activeSensors;

        computeState();
        
        drawBaseImage(g);




    }
}
