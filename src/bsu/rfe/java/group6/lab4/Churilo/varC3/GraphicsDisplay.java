package bsu.rfe.java.group6.lab4.Churilo.varC3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Stack;

public class GraphicsDisplay extends JPanel {
    private Double[][] graphics1Data = null;
    private Double[][] graphics1DataOriginal = null;

    private Double[][] graphics2Data = null;
    private Double[][] graphics2DataOriginal = null;

    private boolean showAxis = true;
    private boolean showMarkers = true;
    private boolean isTurned = false;

    private boolean scaleMode = false;
    private boolean changeMode = false;

    private int selectedMarker  = -1;

    private double minX;
    private double maxX;
    private double minY;
    private double maxY;

    private Double[][] viewport = new  Double[2][2];
    private Stack<Double[][]> undoHistory = new Stack<>();

    private Double[] originalPoint = new Double[2];

    private double scale;
    private double scaleX;
    private double scaleY;

    private BasicStroke graphics1Stroke;
    private BasicStroke graphics2Stroke;
    private BasicStroke axisStroke;
    private BasicStroke markerStroke;
    private BasicStroke gridStroke;
    private BasicStroke selectionStroke;

    private Font axisFont;
    private Font labelsFont;

    private static DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance();

    private Rectangle2D.Double selectionRect = new Rectangle2D.Double();

    public GraphicsDisplay() {
        setBackground(Color.WHITE);

        graphics1Stroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {8, 2, 8, 2, 8, 2, 2, 2, 2, 2, 2, 2}, 0.0f);
        graphics2Stroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10.0f, new float[] {8, 4}, 0.0f);
        axisStroke = new BasicStroke(2.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        markerStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, null, 0.0f);
        gridStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {4, 4}, 0.0f);
        selectionStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[] {10, 10}, 0.0f);

        axisFont = new Font("Serif", Font.BOLD, 36);
        labelsFont = new Font("Serif", 0, 10);

        formatter.setMaximumFractionDigits(5);
    }

    public void showGraphics(Double[][] graphicsData){
        if (this.graphics1Data == null)
            this.graphics1Data = graphicsData;
        else if (this.graphics2Data == null)
            this.graphics2Data = graphicsData;
        else {
            this.graphics1Data = this.graphics2Data;
            this.graphics2Data = graphicsData;
        }
        repaint();
    }

    public void setShowAxis(boolean showAxis){
        this.showAxis = showAxis;
        repaint();
    }

    public void setShowMarkers(boolean showMarkers){
        this.showMarkers = showMarkers;
        repaint();
    }

    public void setIsTurned(boolean isTurned){
        this.isTurned = isTurned;
        repaint();
    }

    public void paintComponent(Graphics g){
        super.paintComponent(g);

        Graphics2D canvas = (Graphics2D)g;
        if (isTurned) rotateGraphics(canvas);

        if (graphics1Data == null || graphics1Data.length == 0) return;

        minX = graphics1Data[0][0];
        maxX = minX;
        minY = graphics1Data[0][1];
        maxY = minY;
        for (int i = 1; i < graphics1Data.length; i++){
            if (graphics1Data[i][0] < minX)
                minX = graphics1Data[i][0];
            if (graphics1Data[i][0] > maxX)
                maxX = graphics1Data[i][0];
            if (graphics1Data[i][1] < minY)
                minY = graphics1Data[i][1];
            if (graphics1Data[i][1] > maxY)
                maxY = graphics1Data[i][1];
        }

        if (graphics2Data != null && graphics2Data.length != 0){
            for (int i = 0; i < graphics2Data.length; i++){
                if (graphics2Data[i][0] < minX)
                    minX = graphics2Data[i][0];
                if (graphics2Data[i][0] > maxX)
                    maxX = graphics2Data[i][0];
                if (graphics2Data[i][1] < minY)
                    minY = graphics2Data[i][1];
                if (graphics2Data[i][1] > maxY)
                    maxY = graphics2Data[i][1];
            }
        }

        if (!isTurned) {
            double scaleX = getSize().getWidth() / (maxX - minX);
            double scaleY = getSize().getHeight() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);

            if (scale == scaleX) {
                double yIncrement = (getSize().getHeight() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getWidth() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }
        else {
            double scaleX = getSize().getHeight() / (maxX - minX);
            double scaleY = getSize().getWidth() / (maxY - minY);
            scale = Math.min(scaleX, scaleY);

            if (scale == scaleX) {
                double yIncrement = (getSize().getWidth() / scale - (maxY - minY)) / 2;
                maxY += yIncrement;
                minY -= yIncrement;
            }
            if (scale == scaleY) {
                double xIncrement = (getSize().getHeight() / scale - (maxX - minX)) / 2;
                maxX += xIncrement;
                minX -= xIncrement;
            }
        }

        Stroke oldStroke = canvas.getStroke();
        Color oldColor = canvas.getColor();
        Paint oldPaint = canvas.getPaint();
        Font oldFont = canvas.getFont();

        if (showAxis) paintAxis(canvas);
        paintGraphics(canvas);
        if (showMarkers) paintMarkers(canvas);

        canvas.setFont(oldFont);
        canvas.setPaint(oldPaint);
        canvas.setColor(oldColor);
        canvas.setStroke(oldStroke);
    }

    public void zoomToRegion(double x1, double y1, double x2, double y2){
        viewport[0][0] = x1;
        viewport[0][1] = y1;
        viewport[1][0] = x2;
        viewport[1][1] = y2;
        repaint();
    }

    private void paintGraphics(Graphics2D canvas){
        canvas.setStroke(graphics1Stroke);
        canvas.setColor(Color.RED);

        GeneralPath graphics1 = new GeneralPath();
        for (int i = 0; i < graphics1Data.length; i++){
            Point2D.Double point = xyToPoint(graphics1Data[i][0], graphics1Data[i][1]);
            if (i > 0)
                graphics1.lineTo(point.getX(), point.getY());
            else
                graphics1.moveTo(point.getX(),point.getY());
        }

        canvas.draw(graphics1);

        if (graphics2Data != null && graphics2Data.length != 0){
            canvas.setStroke(graphics2Stroke);
            canvas.setColor(Color.BLUE);

            GeneralPath graphics2 = new GeneralPath();
            for (int i = 0; i < graphics2Data.length; i++){
                Point2D.Double point = xyToPoint(graphics2Data[i][0], graphics2Data[i][1]);
                if (i > 0)
                    graphics2.lineTo(point.getX(), point.getY());
                else
                    graphics2.moveTo(point.getX(),point.getY());
            }

            canvas.draw(graphics2);
        }
    }

    private void paintMarkers(Graphics2D canvas){
        canvas.setStroke(markerStroke);
        boolean isBlack;
        canvas.setPaint(Color.BLACK);

        for (Double[] point: graphics1Data){
            GeneralPath marker = new GeneralPath();
            Point2D.Double center = xyToPoint(point[0], point[1]);

            int k = 1;
            /*if (!isTurned)
                k = 1;
            else
                k = 0;*/

            String f = Double.valueOf(Math.abs(point[k])).toString();;
            int i = 0;
            int sum = 0;
            while (f.charAt(i) != '.' && f.charAt(i) != ','){
                sum += f.charAt(i) - '0';
                i++;
            }
            if (sum < 10)
                canvas.setColor(Color.BLACK);
            else
                canvas.setColor(Color.RED);

            marker.moveTo(center.getX(), center.getY());
            marker.lineTo(center.getX(), center.getY() - 5);
            marker.lineTo(center.getX() + 5, center.getY() - 5);

            marker.moveTo(center.getX(), center.getY());
            marker.lineTo(center.getX() + 5, center.getY());
            marker.lineTo(center.getX() + 5, center.getY() + 5);

            marker.moveTo(center.getX(), center.getY());
            marker.lineTo(center.getX(), center.getY() + 5);
            marker.lineTo(center.getX() - 5, center.getY() + 5);

            marker.moveTo(center.getX(), center.getY());
            marker.lineTo(center.getX() - 5, center.getY());
            marker.lineTo(center.getX() - 5, center.getY() - 5);

            canvas.draw(marker);
        }

        if (graphics2Data != null && graphics2Data.length != 0){
            for (Double[] point: graphics2Data){
                GeneralPath marker = new GeneralPath();
                Point2D.Double center = xyToPoint(point[0], point[1]);

                int k = 1;
                /*if (!isTurned)
                    k = 1;
                else
                    k = 0;*/

                String f = Double.valueOf(Math.abs(point[k])).toString();
                int i = 0;
                int sum = 0;
                while (f.charAt(i) != '.' && f.charAt(i) != ','){
                    sum += f.charAt(i) - '0';
                    i++;
                }
                if (sum < 10)
                    canvas.setColor(Color.BLACK);
                else
                    canvas.setColor(Color.BLUE);

                marker.moveTo(center.getX(), center.getY());
                marker.lineTo(center.getX(), center.getY() - 5);
                marker.lineTo(center.getX() + 5, center.getY() - 5);

                marker.moveTo(center.getX(), center.getY());
                marker.lineTo(center.getX() + 5, center.getY());
                marker.lineTo(center.getX() + 5, center.getY() + 5);

                marker.moveTo(center.getX(), center.getY());
                marker.lineTo(center.getX(), center.getY() + 5);
                marker.lineTo(center.getX() - 5, center.getY() + 5);

                marker.moveTo(center.getX(), center.getY());
                marker.lineTo(center.getX() - 5, center.getY());
                marker.lineTo(center.getX() - 5, center.getY() - 5);

                canvas.draw(marker);
            }
        }
    }

    private void rotateGraphics(Graphics2D canvas){
        canvas.translate(0, getHeight());
        canvas.rotate(-Math.PI/2);
    }

    private void paintAxis(Graphics2D canvas){
        canvas.setStroke(axisStroke);
        canvas.setColor(Color.BLACK);
        canvas.setPaint(Color.BLACK);
        canvas.setFont(axisFont);
        FontRenderContext context = canvas.getFontRenderContext();


        //Oy
        if (minX <= 0.0 && 0.0 <= maxX) {
            canvas.draw(new Line2D.Double(xyToPoint(0, maxY), xyToPoint(0, minY)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(0, maxY);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() + 5, arrow.getCurrentPoint().getY() + 20);
            arrow.lineTo(arrow.getCurrentPoint().getX() - 10, arrow.getCurrentPoint().getY());
            arrow.closePath();

            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("y", context);
            Point2D.Double labelPos = xyToPoint(0, maxY);
            canvas.drawString("y", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() - bounds.getY()));
        }

        //Ox
        if (minY <= 0.0 && 0.0 <= maxY) {
            canvas.draw(new Line2D.Double(xyToPoint(minX, 0), xyToPoint(maxX, 0)));

            GeneralPath arrow = new GeneralPath();
            Point2D.Double lineEnd = xyToPoint(maxX, 0);
            arrow.moveTo(lineEnd.getX(), lineEnd.getY());
            arrow.lineTo(arrow.getCurrentPoint().getX() - 20, arrow.getCurrentPoint().getY() - 5);
            arrow.lineTo(arrow.getCurrentPoint().getX(), arrow.getCurrentPoint().getY() + 10);
            arrow.closePath();

            canvas.draw(arrow);
            canvas.fill(arrow);

            Rectangle2D bounds = axisFont.getStringBounds("x", context);
            Point2D.Double labelPos = xyToPoint(maxX, 0);
            canvas.drawString("x", (float) (labelPos.getX() - bounds.getWidth() - 10), (float) (labelPos.getY() - bounds.getY()));
        }

    }

    protected Point2D.Double xyToPoint(double x, double y){
        double deltaX = x - viewport[0][0];
        double deltaY = viewport[0][1] - y;
        return new Point2D.Double(deltaX * scaleX, deltaY * scaleY);
    }

    protected Double[] pointToXY(int x, int y){
       return new Double[]{viewport[0][0] + (double)x / scaleX, viewport[0][1] + (double)y / scaleY};
   }

    protected Point2D.Double shiftPoint(Point2D.Double src, double deltaX, double deltaY){
        Point2D.Double dest = new Point2D.Double();
        dest.setLocation(src.getX() + deltaX * scaleX, src.getY() - deltaY * scaleY);
        return dest;
    }

    protected int findPoint(int x, int y){
        if (graphics1Data != null){
            int pos = 0;
            for (Double[] point: graphics1Data){
                Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0)
                    return pos;
                pos++;
            }
        }
        if (graphics2Data != null){
            int pos = 0;
            for (Double[] point: graphics2Data){
                Point2D.Double screenPoint = xyToPoint(point[0], point[1]);
                double distance = (screenPoint.getX() - (double)x) * (screenPoint.getX() - (double)x) + (screenPoint.getY() - (double)y) * (screenPoint.getY() - (double)y);
                if (distance < 100.0)
                    return graphics1Data.length + pos;
                pos++;
            }
        }
        return -1;
    }

    public void reset(){
        showGraphics(graphics1DataOriginal);
        showGraphics(graphics2DataOriginal);
    }

    public class MouseHandler extends MouseAdapter{
        public MouseHandler(){

        }

        public void mouseClicked(MouseEvent event){
            if (event.getButton() == 3){
                if (!undoHistory.isEmpty())
                    viewport = undoHistory.pop();
                else
                    zoomToRegion(minX, maxY, maxX, minY);
                repaint();
            }
        }

        public void mousePressed(MouseEvent event){
            if (event.getButton() == 1){
                selectedMarker = findPoint(event.getX(), event.getY());
                originalPoint = pointToXY(event.getX(), event.getY());

                if (selectedMarker >= 0){
                    changeMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
                }
                else {
                    scaleMode = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                    selectionRect.setFrame(event.getX(), event.getY(), 1, 1);
                }
            }
        }

        public void mouseReleased(MouseEvent event){
            if (event.getButton() == 1){
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

                if (changeMode){
                    changeMode = false;
                }
                else {
                    scaleMode = false;
                    Double[] finalPoint = pointToXY(event.getX(), event.getY());
                    undoHistory.push(viewport);
                    viewport = new Double[2][2];
                    zoomToRegion(originalPoint[0], originalPoint[1], finalPoint[0], finalPoint[1]);
                    repaint();
                }
            }
        }
    }

    public class MouseMotionHandler implements MouseMotionListener {
        public MouseMotionHandler(){

        }

        public void mouseMoved(MouseEvent event){
            selectedMarker = findPoint(event.getX(), event.getY());
            if (selectedMarker >= 0)
                setCursor(Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR));
            else
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

            repaint();
        }

        public void mouseDragged(MouseEvent event){
            if (changeMode){
                Double[] currentPoint = pointToXY(event.getX(), event.getY());
                double newY = currentPoint[1];

                if (newY > viewport[0][1])
                    newY = viewport[0][1];
                if (newY < viewport[1][1])
                    newY = viewport[1][1];

                graphics1Data[selectedMarker][1] = newY;

                repaint();
            } else {
                double width = event.getX() - selectionRect.getX();
                if (width < 5.0)
                    width = 5.0;

                double height = event.getY() - selectionRect.getY();
                if (height < 5.0)
                    height = 5.0;

                selectionRect.setFrame(selectionRect.getX(), selectionRect.getY(), width, height);
                repaint();
            }
        }
    }
}
