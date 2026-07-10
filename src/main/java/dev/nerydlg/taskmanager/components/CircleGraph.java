package dev.nerydlg.taskmanager.components;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CircleGraph extends JComponent {

  private static final Integer THICKNESS = 50;
  private static final Integer LINE_DISTANCE = 25;
  private static final Integer LABEL_DISTANCE = 20;
  private static final Stroke graphStroke = new BasicStroke(THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);
  private static final Stroke basicStroke = new BasicStroke(1);

  private final int startX = 100;
  private final int startY = 50;
  private final Integer width;
  private final Integer height;
  private final Map<String, Integer> data;
  private final List<Color> colors;
  private final String title;

  /***
   * Creates a new circle graph with the given width, height, and data.
   * The width and height must be positive.
   * The data is a map of label and counts.
   * @param width
   * @param height
   * @param data
   */
  public CircleGraph(String title, Integer width, Integer height, Map<String, Integer> data, List<Color> colors) {
    if(width < THICKNESS || height < THICKNESS) {
      throw new IllegalArgumentException("Width and height must be positive");
    }
    this.title = title;
    this.width = width;
    this.height = height;
    this.data = data;
    this.colors = colors;
    setPreferredSize(new Dimension(width+200, height+100));
    setAutoscrolls(true);
    setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder(this.title), BorderFactory.createEmptyBorder(0, 0, 10, 10)));
  }

  @Override
  protected void paintComponent(java.awt.Graphics g) {
    Graphics2D g2d = (Graphics2D) g;

    RenderingHints rh = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2d.setRenderingHints(rh);

    if(data.isEmpty()) {
      g2d.setColor(Color.GRAY);
      g2d.setStroke(graphStroke);
      g2d.draw(new Arc2D.Double(startX, startY, width, height, 0, 360, Arc2D.OPEN));
      g2d.drawString("No data", ((width/2)-55)+startX, ((height/2)+10) + startY);
      return;
    }

    Integer total = getTotalCount();
    double lastAngle = 0;
    int i = 0;
    Map<String,Point2D[]> linePoints = new HashMap<>();
    g2d.setStroke(graphStroke);

    for (Map.Entry<String, Integer> entry : data.entrySet()) {
      double angle = getAngle(entry.getValue(), total);
      Arc2D.Double arc = new Arc2D.Double(startX, startY, width, height, lastAngle, angle, Arc2D.OPEN);
      g2d.setColor(colors.get(i));
      g2d.draw(arc);
      // calculate middle point and store it to use it later
      double midAngle = lastAngle + angle / 2;
      Point2D middlePoint = new Arc2D.Double(startX, startY, width, height, midAngle, 0, Arc2D.OPEN).getStartPoint();
      Point2D labelPoint = calculateEndPoint(middlePoint, LINE_DISTANCE);
      linePoints.put(entry.getKey(), new Point2D[]{middlePoint, labelPoint});

      lastAngle += angle;
      // reset colors if needed
      if(i < colors.size()-1) {
        i++;
      } else {
        i = 0;
      }
    }
    // draw lines and labels
    g2d.setColor(Color.BLACK);
    g2d.setStroke(basicStroke);
    for (Map.Entry<String, Point2D[]> point2DEntry : linePoints.entrySet()) {
      double percentage = getPercentage(data.get(point2DEntry.getKey()), total);
      Line2D.Double line = new Line2D.Double(
          point2DEntry.getValue()[0].getX(),
          point2DEntry.getValue()[0].getY(),
          point2DEntry.getValue()[1].getX(),
          point2DEntry.getValue()[1].getY());
      g2d.draw(line);
      Point2D labelPoint = calculateEndPoint(point2DEntry.getValue()[1], LABEL_DISTANCE);
      g2d.drawString(point2DEntry.getKey(),
          (float)labelPoint.getX(),
          (float)labelPoint.getY());
      g2d.drawString(String.format("%.2f %%", percentage), (float)point2DEntry.getValue()[0].getX(), (float)point2DEntry.getValue()[0].getY());

    }
    g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 80));
    g2d.drawString( String.format("%d", total), ((width/2) - 40)+startX, ((height/2) + 20) + startY);
    g2d.setFont(new java.awt.Font("Arial", java.awt.Font.PLAIN, 16));
    g2d.drawString( this.title, ((width/2) - 50)+startX, ((height/2) + 40) + startY);
  }

  private void drawInnerCircle(Graphics2D g2d) {
    double xCenter = (width/4) + startX;
    double yCenter = (height/4) + startY;
    Ellipse2D.Double innerCircle = new Ellipse2D.Double(xCenter, yCenter, width/2, height/2);
    g2d.setColor(getBackground());
    g2d.fill(innerCircle);
  }

  private Integer getTotalCount() {
    return data.values().stream().mapToInt(Integer::intValue).sum();
  }

  private Double getPercentage(Integer count, Integer total) {
    return (double) count / total * 100;
  }

  private Double getAngle(Integer count, Integer total) {
    return (double) count / total * 360;
  }

  private Point2D calculateEndPoint(Point2D point, Integer distance){
    Point2D endPoint = null;
    double middleX = (width/2) + startX;
    double middleY = (height/2) + startY;
    if(point.getX() >= middleX && point.getY() <= middleY) { // top-right
      endPoint = new Point2D.Double(point.getX() + distance, point.getY() - distance);
    } else if(point.getX() <= middleX && point.getY() <= middleY) { // top-left
      endPoint = new Point2D.Double(point.getX() - distance, point.getY() - distance);
    } else if(point.getX() <= middleX && point.getY() >= middleY) { // bottom-left
      endPoint = new Point2D.Double(point.getX() - distance, point.getY() + distance);
    } else { // bottom-right
      endPoint = new Point2D.Double(point.getX() + distance, point.getY() + distance);
    }

    return endPoint;
  }

}
