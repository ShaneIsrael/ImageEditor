import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Stack;

import javax.swing.ImageIcon;
import javax.swing.JPanel;


public class EditorPanel extends JPanel implements MouseMotionListener, MouseListener
{
    private static final long serialVersionUID = -8925388346037613270L;
    private Image image;
    private BufferedImage editingLayer;
    private Graphics2D editG2D;
    private Color fillColor = Color.red;
    private Color borderColor = Color.black;
    private String tool = "pencil";

    private Rectangle2D selection;
    private int mx, my, lastX, lastY; // mouse position holders
    private Point clickPoint;
    private float stroke = 3f;

    private Stack<DrawLayer> drawStack;
    private Stack<DrawLayer> redoStack;
    
    private int dlMinX, dlMinY, dlMaxX, dlMaxY;
    private int drawWidth, drawHeight;

    /*
     * Dashed Stroke
     */
    private float dashed_strokeThickness = 2.0f;

    private float miterLimit = 5f;
    private float[] dashPattern = { 5f };
    private float dashPhase = 5f;
    private BasicStroke dashed_stroke = new BasicStroke(dashed_strokeThickness, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER, miterLimit, dashPattern, dashPhase);
    
    Editor editor;


    public EditorPanel(BufferedImage img, Editor e)
    {
        editor = e;
        image = img;

        drawWidth = img.getWidth();
        drawHeight = img.getHeight();
        editingLayer = new BufferedImage(img.getWidth() + 10, img.getHeight() + 10, BufferedImage.TYPE_INT_ARGB);
        editG2D = ((BufferedImage) editingLayer).createGraphics();
        editG2D.setBackground(new Color(0,0,0,0));

        selection = new Rectangle2D.Double();

        drawStack = new Stack<DrawLayer>();
        redoStack = new Stack<DrawLayer>();
        
        this.addMouseMotionListener(this);
        this.addMouseListener(this);
        this.setFocusable(true);
        this.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));

    }

    public void setMouseCursor()
    {
        ImageIcon ii = new ImageIcon(this.getClass().getResource("/images/pen.png"));
        Image cursorImage = ii.getImage();
        Point cursorHotSpot = new Point(16, 16);
        Cursor customCursor = this.getToolkit().createCustomCursor(cursorImage, cursorHotSpot, "Cursor");
        this.setCursor(customCursor);
    }

    @Override
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.drawImage(image, 0, 0, null); // Draw the original image first
        g2d.drawImage(editingLayer, 0, 0, null); // Draw the edit layer next
        
        Iterator<DrawLayer> iter = drawStack.iterator();
        while(iter.hasNext())
        {
            DrawLayer dl = iter.next();
            g2d.drawImage(dl.getImage(), dl.getLocation().x, dl.getLocation().y, null);
        }
        
        super.repaint();
        g2d.dispose();
    }
    public void clearEditingLayer()
    {
        editG2D.dispose();
        editingLayer = new BufferedImage(image.getWidth(null) + 10, image.getHeight(null) + 10, BufferedImage.TYPE_INT_ARGB);
        editG2D = ((BufferedImage) editingLayer).createGraphics();
        editG2D.setBackground(new Color(0,0,0,0));
    }
    public BufferedImage getImage()
    {
        return (BufferedImage) image;
    }

    public void setColor(Color color)
    {
        fillColor = color;
    }

    public void setBorderColor(Color color)
    {
        borderColor = color;
    }

    public void setTransparency(int level)
    {
        fillColor = new Color(fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue(), level);
    }

    public void setBorderTransparency(int level)
    {
        borderColor = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), level);
    }

    public void setTool(String tool)
    {
        this.tool = tool;
    }

    public void draw()
    {
        if(!tool.equals("pencil"))
        {
            editG2D.clearRect(0, 0, editingLayer.getWidth(), editingLayer.getHeight());
            editG2D.setBackground(new Color(0, 0, 0, 0));
        }
        else
            drawPenLine(mx, my, lastX, lastY);
        
        if(tool.equals("line"))
        {
            drawLine(editG2D);
        }
        else if(tool.equals("rectangle"))
        {
            if(editor.fill())
            {
                if(editor.shadow())
                    drawBorderedRectangle(editG2D);
                else
                    drawFilledRectangle(editG2D);
            }
            else
                drawRectangle(editG2D);
        }
        else if(tool.equals("roundRectangle"))
        {
            if(editor.fill())
                drawFilledRoundRectangle(editG2D);
            else
                drawRoundRectangle(editG2D);
        }
        else if(tool.equals("ellipse"))
        {
            if(editor.fill())
                drawFilledEllipse(editG2D);
            else
                drawEllipse(editG2D);
        }
    }
    public void undo()
    {
        if(!drawStack.isEmpty())
        {
            editor.enableRedo();
            redoStack.push(drawStack.pop());
            if(drawStack.isEmpty())
                editor.disableUndo();
        }
    }
    public void redo()
    {
        if(!redoStack.isEmpty())
        {
            editor.enableUndo();
            drawStack.push(redoStack.pop());
            if(redoStack.isEmpty())
                editor.disableRedo();
        }
    }
    private void addNewLayerToStack()
    {
        int width = dlMaxX - dlMinX;
        int height = dlMaxY - dlMinY;
        
        
        redoStack.clear();
        editor.disableRedo();
        

        try
        {
            drawStack.add(new DrawLayer((BufferedImage)editingLayer.getSubimage(dlMinX, dlMinY, width + 10, height + 10), new Point(dlMinX, dlMinY)));
        }
        catch(java.awt.image.RasterFormatException ex)
        {

            System.out.println(dlMinY+height);
        }
        
        if(!drawStack.isEmpty())
            editor.enableUndo();
        clearEditingLayer();
    }
    public Rectangle draggedRect()
    {
        if (mx < 0)
        {
            selection.setFrameFromDiagonal(clickPoint, new Point(0, my));
        } else if (my < 0)
        {
            selection.setFrameFromDiagonal(clickPoint, new Point(mx, 0));
        } else if (my < 0 && mx < 0)
        {
            selection.setFrameFromDiagonal(clickPoint, new Point(0, 0));
        } else
        {
            selection.setFrameFromDiagonal(clickPoint, new Point(mx, my));
        }
        return selection.getBounds();
    }


    private void drawPenLine(int mx2, int my2, int lastX2, int lastY2)
    {
        editG2D.setColor(fillColor);
        editG2D.setStroke(new BasicStroke(stroke));
        editG2D.drawLine(mx2, my2, lastX2, lastY2);
    }

    private void drawLine(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        g.drawLine(clickPoint.x, clickPoint.y, mx, my);
    }
    private void drawEllipse(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        Rectangle rect = draggedRect();
        g.drawOval(rect.x, rect.y, rect.width, rect.height);
    }
    private void drawFilledEllipse(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        Rectangle rect = draggedRect();
        g.fillOval(rect.x, rect.y, rect.width, rect.height);
    }
    private void drawRectangle(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        g.draw(draggedRect());
    }
    private void drawRoundRectangle(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        Rectangle rect = draggedRect();
        g.drawRoundRect(rect.x, rect.y, rect.width, rect.height, rect.height / 2, rect.height / 2);
    }
    private void drawFilledRoundRectangle(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        Rectangle rect = draggedRect();
        g.fillRoundRect(rect.x, rect.y, rect.width, rect.height, rect.height / 2, rect.height / 2);
    }
    private void drawFilledRectangle(Graphics2D g)
    {
        g.setColor(fillColor);
        g.setStroke(new BasicStroke(stroke));
        g.fill(draggedRect());
    }
    private void drawBorderedRectangle(Graphics2D g)
    {
        g.setStroke(new BasicStroke(stroke));

        Color shadow = new Color(borderColor.getRed(), borderColor.getGreen(), borderColor.getBlue(), 100);
        //Color last = g.getColor();
        g.setColor(shadow);

        /*
         * Right bordered shadow
         */

        int stroke_correction = Math.round(stroke);
        if (stroke_correction % 2 != 0)
        {
            if (stroke_correction % 2 >= .5)
            {
                stroke_correction++;
            }
            else
            {
                stroke_correction--;
            }
        }
        g.fillRect((int) (draggedRect().getX() + draggedRect().getWidth()) + (int) (stroke_correction / 2),
                (int) draggedRect().getY() + stroke_correction, stroke_correction, (int) draggedRect().getHeight()
                        - (int) (stroke_correction / 2));
        g.fillRect((int) (draggedRect().getX() + stroke_correction), (int) (draggedRect().getY() + draggedRect()
                .getHeight()) + (int) (stroke_correction / 2), (int) draggedRect().getWidth()
                + (int) (stroke_correction / 2), stroke_correction);
        /*
         * End Shadow
         */

        g.setColor(fillColor);
        g.fill(new Rectangle(draggedRect().x + (int) (stroke_correction / 2), draggedRect().y
                + (int) (stroke_correction / 2), draggedRect().width - (int) (stroke_correction / 2),
                draggedRect().height - (int) (stroke_correction / 2)));

        g.setColor(borderColor);
        g.draw(draggedRect());

        g.setColor(fillColor);
    }
    @Override
    public void mouseDragged(MouseEvent me)
    {
        lastX = mx;
        lastY = my;
        mx = me.getX();
        my = me.getY();
        
        if(mx < 0)
            dlMinX = 0;
        else if(mx < dlMinX)
            dlMinX = mx;
        if(mx > drawWidth)
            dlMaxX = drawWidth;
        else if(mx > dlMaxX)
            dlMaxX = mx;
        if(my < 0)
            dlMinY = 0;
        else if(my < dlMinY)
            dlMinY = my;
        if(my > drawHeight)
            dlMaxY = drawHeight;
        else if(my > dlMaxY)
            dlMaxY = my;
        
        draw(); // get selected tool and draw
    }

    @Override
    public void mouseMoved(MouseEvent me)
    {
        mx = me.getX();
        my = me.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e)
    {
    }

    @Override
    public void mouseEntered(MouseEvent arg0)
    {
    }

    @Override
    public void mouseExited(MouseEvent arg0)
    {
    }

    @Override
    public void mousePressed(MouseEvent me)
    {
        clickPoint = new Point(mx, my);
        dlMinX = mx;
        dlMaxX = mx;
        dlMinY = my;
        dlMaxY = my;
    }

    @Override
    public void mouseReleased(MouseEvent arg0)
    {
        addNewLayerToStack();
    }

    public String getTool()
    {
        return tool;
    }

    public void setStroke(float stroke)
    {
    }

    public void copyImageToClipboard()
    {
    }
    public void disposeAll()
    {
    }

    public void reset()
    {
        drawStack.clear();
        redoStack.clear();
        editor.disableRedo();
        editor.disableUndo();
        clearEditingLayer();
    }
}
