package bsu.rfe.java.group6.lab4.Churilo.varC3;

import javax.swing.*;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;

public class MainFrame extends JFrame{
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private JFileChooser fileChooser = null;

    private JCheckBoxMenuItem showAxisMenuItem;
    private JCheckBoxMenuItem showMarkersMenuItem;
    private JCheckBoxMenuItem turnGraphicsMenuItem;

    private GraphicsDisplay display = new GraphicsDisplay();

    private boolean fileLoaded = false;

    public MainFrame(){
        super("Построение графиков функций на основе заранее подготовленных файлов");

        setSize(WIDTH, HEIGHT);
        Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - WIDTH)/2,(kit.getScreenSize().height - HEIGHT)/2);
        //setExtendedState(MAXIMIZED_BOTH);

        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("Файл");
        menuBar.add(fileMenu);

        Action openGraphicsAction = new AbstractAction("Открыть файл с графиком") {
            public void actionPerformed(ActionEvent e) {
                if (fileChooser == null){
                    fileChooser = new JFileChooser();
                    fileChooser.setCurrentDirectory(new File("."));
                }
                if (fileChooser.showOpenDialog(MainFrame.this) == JFileChooser.APPROVE_OPTION)
                    openGraphics(fileChooser.getSelectedFile());
            }
        };
        fileMenu.add(openGraphicsAction);

        JMenu graphicsMenu = new JMenu("График");
        menuBar.add(graphicsMenu);

        Action showAxisAction = new AbstractAction("Показать оси координат") {
            public void actionPerformed(ActionEvent e) {
                display.setShowAxis(showAxisMenuItem.isSelected());
            }
        };
        showAxisMenuItem = new JCheckBoxMenuItem(showAxisAction);
        graphicsMenu.add(showAxisMenuItem);
        showAxisMenuItem.setSelected(true);

        Action showMarkersAction = new AbstractAction("Показывать маркеры точек") {
            public void actionPerformed(ActionEvent e) {
                display.setShowMarkers(showMarkersMenuItem.isSelected());
            }
        };
        showMarkersMenuItem = new JCheckBoxMenuItem(showMarkersAction);
        graphicsMenu.add(showMarkersMenuItem);
        showMarkersMenuItem.setSelected(true);

        Action turnGraphicsAction = new AbstractAction("Повернуть график на 90° влево") {
            public void actionPerformed(ActionEvent e) {
                display.setIsTurned(turnGraphicsMenuItem.isSelected());
            }
        };
        turnGraphicsMenuItem = new JCheckBoxMenuItem(turnGraphicsAction);
        graphicsMenu.add(turnGraphicsMenuItem);
        turnGraphicsMenuItem.setSelected(false);

        graphicsMenu.addMenuListener(new GraphicsMenuListener());

        getContentPane().add(display, BorderLayout.CENTER);
    }

    protected void openGraphics(File selectedFile){
        try{
            DataInputStream in = new DataInputStream(new FileInputStream(selectedFile));

            Double[][] graphicsData = new Double[in.available() / (Double.SIZE / 8) / 2][];

            int i = 0;
            while (in.available() > 0){
                Double x = in.readDouble();
                Double y = in.readDouble();
                graphicsData[i++] = new Double[] {x, y};
            }

            if (graphicsData != null && graphicsData.length > 0){
                fileLoaded = true;
                display.showGraphics(graphicsData);
            }

            in.close();
        }
        catch (FileNotFoundException ex){
            JOptionPane.showMessageDialog(MainFrame.this, "Указанный файл не найден", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
        catch (IOException ex){
            JOptionPane.showMessageDialog(MainFrame.this, "Ошибка чтения координат точек из файла", "Ошибка загрузки данных", JOptionPane.WARNING_MESSAGE);
            return;
        }
    }

    public static void main(String[] args){
        try {
            DataOutputStream out = new DataOutputStream(new FileOutputStream("Data1.bin"));

            for (double x = -10.0; x < 10.01; x += 0.5){
                out.writeDouble(x);
                double y = x * x;
                out.writeDouble(y);
            }

            out.close();

            out = new DataOutputStream(new FileOutputStream("Data2.bin"));

            for (double x = -10.0; x < 10.01; x += 0.5){
                out.writeDouble(x);
                double y = -1 * x * x + 40;
                out.writeDouble(y);
            }

            out.close();
        } catch (Exception ex){ }
        MainFrame frame = new MainFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private class GraphicsMenuListener implements MenuListener{
        public void menuSelected(MenuEvent e) {
            showAxisMenuItem.setEnabled(fileLoaded);
            showMarkersMenuItem.setEnabled(fileLoaded);
            turnGraphicsMenuItem.setEnabled(fileLoaded);
        }

        public void menuDeselected(MenuEvent e){

        }

        public void menuCanceled(MenuEvent e){

        }
    }
}
