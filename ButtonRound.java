import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.FontUIResource;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

public class ButtonRound extends JToggleButton {
        private boolean over;
        private Color color;
        private Color colorOver;
        private Color colorClick;
        private Color borderColor;
        private int radius = 0;
     
        public boolean isOver() {
           return this.over;
        }
     
        public void setOver(boolean over) {
           this.over = over;
        }
     
        public Color getColor() {
           return this.color;
        }
     
        public void setColor(Color color) {
           this.color = color;
           this.setBackground(color);
        }
     
        public Color getColorOver() {
           return this.colorOver;
        }
     
        public void setColorOver(Color colorOver) {
           this.colorOver = Color.CYAN;
        }
     
        public Color getColorClick() {
           return this.colorClick;
        }
     
        public void setColorClick(Color colorClick) {
           this.colorClick = Color.CYAN;
        }
     
        public Color getBorderColor() {
           return this.borderColor;
        }
     
        public void setBorderColor(Color borderColor) {
           this.borderColor = borderColor;
        }
     
        public int getRadius() {
           return this.radius;
        }
     
        public void setRadius(int radius) {
           this.radius = radius;
        }
     
        public ButtonRound() {
           this.setColor(Color.CYAN);
           this.colorOver = new Color(179, 250, 160);
           this.colorClick = new Color(51,153,255);
           this.borderColor = new Color(30, 136, 56);
           this.setContentAreaFilled(false);
           //this.addMouseListener(new 1(this));
        }
     
        protected void paintComponent(Graphics grphcs) {
           Graphics2D g2 = (Graphics2D)grphcs;
           g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
           g2.setColor(this.borderColor);
           g2.fillRoundRect(0, 0, this.getWidth(), this.getHeight(), this.radius, this.radius);
           g2.setColor(this.getBackground());
           g2.fillRoundRect(2, 2, this.getWidth() - 4, this.getHeight() - 4, this.radius, this.radius);
           super.paintComponent(grphcs);
    }
}