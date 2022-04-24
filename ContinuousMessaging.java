package com.Readfiles_threads;
/**
 * @author ABDELHADI MOUZAFIR 
 */

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class ContinuousMessaging {
}


// ? class de servuer
class Servers extends Thread {

    // ? apart fileinputstream and bufferedImages j'ai travaillé avec ces alternatiives qui fait le m^me travail auissi
    private DataOutputStream dataOutputStream = null;
    private DataInputStream dataInputStream = null;
    private ServerSocket serverSocket;
    // ? Il me permet d'afficher les frames de la part du client
    JFrame frame;
    JLabel label;
    // ? pour envoyer les clicks capturé de la part du serveur (contrôleur dela machine du client)
    int left = 0;


    public static void main(String[] args) {
        // ? lancement du thread serveur
        Servers serv = new Servers();
        serv.start();

    }

    // ? pour avoir la position exact du cuurseur du contôleur de la machine comme ça je peut les similuer de l'autre côté de la machine du client
    private Point getPointer() {
        return MouseInfo.getPointerInfo().getLocation();
    }

    // ? ici j'ai fiat un listner sur les clicks du contôleur ; les trois buttons de la souris

    private void clicks() {

        frame.addMouseListener(new MouseListener() {
            public void mousePressed(MouseEvent me) {
            }

            public void mouseReleased(MouseEvent me) {
            }

            public void mouseEntered(MouseEvent me) {
            }

            public void mouseExited(MouseEvent me) {
            }

            public void mouseClicked(MouseEvent me) {
                if (me.getButton() == MouseEvent.BUTTON1) {
                    left = 1;
                }
                if (me.getButton() == MouseEvent.BUTTON2) {
                    left = 2;
                }
                if (me.getButton() == MouseEvent.BUTTON3) {
                    left = 3;
                }
            }
        });

    }

    public void run() {
        try {
            // ? une instance du Jframe
            frame = new JFrame();
            label = new JLabel();
            // ? j'ai fixé le size pour 720p puisque je teste sur la mçme machine
            frame.setSize(1280, 720);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            serverSocket = new ServerSocket(5000);


            while (true) {
                try {
                    // ? tester la fonction qui me retourne la position de mon  curseur
                    System.out.println("info mouse " + getPointer().x);
                    System.out.println("listening to port:5000");
                    Socket clientSocket = serverSocket.accept();
                    System.out.println(clientSocket + " connected.");

                    // ? ici j'ouvre les inputstreams et les outputstreams entre les deux parties
                    dataInputStream = new DataInputStream(clientSocket.getInputStream());
                    dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                    // ? j'envoie la position du curseur
                    dataOutputStream.writeInt(getPointer().x);
                    dataOutputStream.flush();
                    dataOutputStream.writeInt(getPointer().y);
                    dataOutputStream.flush();
                    //? je teste sur le mouse click
                    clicks();
                    // ? j'envoie le click
                    dataOutputStream.writeInt(left);
                    dataOutputStream.flush();

                    // ? la focntion qui fait tout
                    // ? il y aura pas un problème , tout letemps on aura qu'une seule image grâce  à l'overwriting .
                    receiveFile("C:\\Users\\Abdelhadi\\Downloads\\openScreen.png"); //************************************ CHANGER LE LIEN

                    dataInputStream.close();
                    dataOutputStream.close();
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void receiveFile(String fileName) throws Exception {

        // ? je mis d'après le file du frame stocké
        int bytes = 0;
        FileOutputStream fileOutputStream = new FileOutputStream(fileName);

        long size = dataInputStream.readLong();
        byte[] buffer = new byte[4 * 1024];
        while (size > 0 && (bytes = dataInputStream.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
            fileOutputStream.write(buffer, 0, bytes);
            size -= bytes;
        }
        fileOutputStream.close();


        // ? j'affiche le frame à chaque fois
        label.setIcon(new ImageIcon(new ImageIcon(fileName).getImage().getScaledInstance(1280, 720, Image.SCALE_DEFAULT)));
        frame.setContentPane(label);
        frame.setVisible(true);


    }
}

// ? clas serveur
class Cliient extends Thread {

    // private Socket socket;
    private long nextTime = 0;
    // ? le client peut choisir combien de minutes il le faut pour le meeting
    private int timeLimit = 0;
    // ? je mémorise le moment o a débuter le partage d'écran
    final long currentTimestamp = System.currentTimeMillis();


    public Cliient(int timeLimit) {
        this.timeLimit = timeLimit;
    }

    // ? old
    private void getNextFreq() {
        nextTime = nextTime + 1;
        //return currentTime+value;
    }

    // ? la foction avec class robot qui peut simulier les clicks sans intervention humaine
    private void mouseClicks(int buttonClicked) throws AWTException {
        Robot rb = new Robot();
        if (buttonClicked == 1) {
            rb.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        }
        if (buttonClicked == 2) {
            rb.mousePress(InputEvent.BUTTON2_DOWN_MASK);
        }
        if (buttonClicked == 3) {
            rb.mousePress(InputEvent.BUTTON3_DOWN_MASK);
        }

    }

    public static void main(String[] args) {
        System.out.println("TIME LIMIT OF SCREEN SHARING IN ***MINUTES***");
        Scanner scan = new Scanner(System.in);
        int timeLimit = scan.nextInt();
        scan.close();
        // ? lancement du thread client
        Cliient cl = new Cliient(timeLimit);
        cl.start();
    }

    public void run() {


        try {

            // Blob.capture("C:\\Users\\Abdelhadi\\Downloads\\pppp.png");
            //Thread.sleep(250);
            //Clientc.test();

            // ? je teste sur le temps écoulé
            while ((System.currentTimeMillis() - currentTimestamp) < (long) timeLimit * 60 * 1000) {

                // ? la focntion qui fait tout
                sendFile("C:\\Users\\Abdelhadi\\Downloads\\pppp" + "a" + ".png"); //************************************ CHANGER LE LIEN
                getNextFreq();
                // ! pour évier toute inconvénientce
                Thread.sleep(100);
                // Thread.sleep(50);
            }

            //sendFile("C:\\Users\\Abdelhadi\\Downloads\\2135162(1).pdf");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    // ? cette méthode me permet de contrôler la position du curseur
    public void controlMycursor(int x, int y) throws IOException, InterruptedException, AWTException {
        Robot rb = new Robot();
        rb.mouseMove(x, y);
    }


    private void sendFile(String path) throws Exception {
        // ? vous pouver changer  le host par un ip d'une autre machine
        Socket socket = new Socket("localhost", 5000);
        // ? ici je reçcoit et j'enoie à l'aide des streams
        DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
        try (DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream())) {
            int x = dataInputStream.readInt();
            int y = dataInputStream.readInt();
            // ? j'associe la position de x et y récupéré d la art du serveur au client
            controlMycursor(x, y);
            System.out.println("x: " + x);
            System.out.println("y: " + y);

            // ? j'accocie le click reçu au client
            int clicked = dataInputStream.readInt();
            System.out.println("button clicked " + clicked);
            mouseClicks(clicked);

            // ? ici  je prend la capture et je le stocke , pourqui ? parceque la capture est une image , il faut l'envoyer en tant que filestream au lieu d'image
            int bytes = 0;
            File file = new File(path);
            FileInputStream fileInputStream;
            BufferedImage Image;
            Robot r = new Robot();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            //screenSize.setSize(500,500);
            Rectangle capture =
                    new Rectangle(screenSize);

            Image = r.createScreenCapture(capture);
            // ? ici je capte la position du curseur et je l'associe une image png pour le voir dans les frames
            Graphics2D graphics2D = Image.createGraphics();
            Image cursor = ImageIO.read(new File("pngwing.png"));
            graphics2D.drawImage(cursor, x, y, 16, 16, null);
            ImageIO.write(Image, "jpg", new File(path));
            System.out.println("frame saved");

            fileInputStream = new FileInputStream(file);

            dataOutputStream.writeLong(file.length());
            // ? break it pour éviter le problème d'image incomplet
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer)) != -1) {
                dataOutputStream.write(buffer, 0, bytes);
                dataOutputStream.flush();
            }
            fileInputStream.close();

        } catch (AWTException | HeadlessException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
