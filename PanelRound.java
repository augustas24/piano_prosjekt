import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class PanelRound extends JPanel {
    private int roundTopLeft = 0;
    private int roundTopRight = 0;
    private int roundBottomLeft = 0;
    private int roundBottomRight = 0;

    public PanelRound() {
        this.setOpaque(false);
    }

    public int getRoundTopLeft() {
        return this.roundTopLeft;
    }
  
    public void setRoundTopLeft(int roundTopLeft) {
        this.roundTopLeft = roundTopLeft;
        this.repaint();
    }
  
    public int getRoundTopRight() {
        return this.roundTopRight;
    }
  
    public void setRoundTopRight(int roundTopRight) {
        this.roundTopRight = roundTopRight;
        this.repaint();
    }

    public int getRoundBottomLeft() {
      return this.roundBottomLeft;
    }

   public void setRoundBottomLeft(int roundBottomLeft) {
      this.roundBottomLeft = roundBottomLeft;
      this.repaint();
    }

   public int getRoundBottomRight() {
      return this.roundBottomRight;
    }

   public void setRoundBottomRight(int roundBottomRight) {
      this.roundBottomRight = roundBottomRight;
      this.repaint();
    }

    protected void paintComponent(Graphics grphcs) {
        Graphics2D g2 = (Graphics2D)grphcs.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(this.getBackground());
        Area area = new Area(this.createRoundTopLeft());
        if (this.roundTopRight > 0) {
           area.intersect(new Area(this.createRoundTopRight()));
        }
  
        if (this.roundBottomLeft > 0) {
           area.intersect(new Area(this.createRoundBottomLeft()));
        }
  
        if (this.roundBottomRight > 0) {
           area.intersect(new Area(this.createRoundBottomRight()));
        }
  
        g2.fill(area);
        g2.dispose();
        super.paintComponent(grphcs);
    }

    private Shape createRoundTopLeft() {
      int width = this.getWidth();
      int height = this.getHeight();
      int roundX = Math.min(width, this.roundTopLeft);
      int roundY = Math.min(height, this.roundTopLeft);
      Area area = new Area(new RoundRectangle2D.Double(0.0, 0.0, (double)width, (double)height, (double)roundX, (double)roundY));
      area.add(new Area(new Rectangle2D.Double((double)(roundX / 2), 0.0, (double)(width - roundX / 2), (double)height)));
      area.add(new Area(new Rectangle2D.Double(0.0, (double)(roundY / 2), (double)width, (double)(height - roundY / 2))));
      return area;
   }

   private Shape createRoundTopRight() {
      int width = this.getWidth();
      int height = this.getHeight();
      int roundX = Math.min(width, this.roundTopRight);
      int roundY = Math.min(height, this.roundTopRight);
      Area area = new Area(new RoundRectangle2D.Double(0.0, 0.0, (double)width, (double)height, (double)roundX, (double)roundY));
      area.add(new Area(new Rectangle2D.Double(0.0, 0.0, (double)(width - roundX / 2), (double)height)));
      area.add(new Area(new Rectangle2D.Double(0.0, (double)(roundY / 2), (double)width, (double)(height - roundY / 2))));
      return area;
   }

   private Shape createRoundBottomLeft() {
      int width = this.getWidth();
      int height = this.getHeight();
      int roundX = Math.min(width, this.roundBottomLeft);
      int roundY = Math.min(height, this.roundBottomLeft);
      Area area = new Area(new RoundRectangle2D.Double(0.0, 0.0, (double)width, (double)height, (double)roundX, (double)roundY));
      area.add(new Area(new Rectangle2D.Double((double)(roundX / 2), 0.0, (double)(width - roundX / 2), (double)height)));
      area.add(new Area(new Rectangle2D.Double(0.0, 0.0, (double)width, (double)(height - roundY / 2))));
      return area;
   }

   private Shape createRoundBottomRight() {
      int width = this.getWidth();
      int height = this.getHeight();
      int roundX = Math.min(width, this.roundBottomRight);
      int roundY = Math.min(height, this.roundBottomRight);
      Area area = new Area(new RoundRectangle2D.Double(0.0, 0.0, (double)width, (double)height, (double)roundX, (double)roundY));
      area.add(new Area(new Rectangle2D.Double(0.0, 0.0, (double)(width - roundX / 2), (double)height)));
      area.add(new Area(new Rectangle2D.Double(0.0, 0.0, (double)width, (double)(height - roundY / 2))));
      return area;
   }

}