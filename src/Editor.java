import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import java.awt.BorderLayout;

import net.miginfocom.swing.MigLayout;

import javax.swing.JScrollPane;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;

import javax.swing.JButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.JLayeredPane;
import javax.swing.JSlider;
import java.awt.Window.Type;
import javax.swing.SwingConstants;
import javax.swing.JToggleButton;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.JComboBox;


public class Editor
{

    private JFrame frmEditor;
    private Color fillColor;
    private Color borderColor;
    private JColorChooser colorChooser;
    
    private JSlider opacitySlider;
    private JSlider strokeSlider;
    
    /**
     * Launch the application.
     */
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    Editor window = new Editor();
                    window.frmEditor.setVisible(true);
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public Editor()
    {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize()
    {
        frmEditor = new JFrame();
        frmEditor.setType(Type.UTILITY);
        frmEditor.setTitle("Editor");
        frmEditor.setBounds(100, 100, 1280, 720);
        frmEditor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JPanel panel = new JPanel();
        frmEditor.getContentPane().add(panel, BorderLayout.CENTER);
        panel.setLayout(new MigLayout("", "[823.00,grow]", "[82.00][425.00,grow]"));
        
        JPanel toolPanel = new JPanel();
        toolPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        toolPanel.setBackground(Color.WHITE);
        toolPanel.setForeground(Color.BLACK);
        panel.add(toolPanel, "cell 0 0,grow");
        toolPanel.setLayout(new MigLayout("", "[59.00][][][120.00][][][][][][][][][][][][]", "[66.00,grow]"));
        
        opacitySlider = new JSlider();
        opacitySlider.setBackground(Color.WHITE);
        opacitySlider.setMinorTickSpacing(5);
        
        toolPanel.add(opacitySlider, "cell 1 0");
        
        strokeSlider = new JSlider();
        strokeSlider.setPaintTicks(true);
        strokeSlider.setPaintLabels(true);
        strokeSlider.setBackground(Color.WHITE);
        toolPanel.add(strokeSlider, "cell 2 0");
        
        final ColorSelectionPanel fillColorPanel = new ColorSelectionPanel();
        final ColorSelectionPanel borderColorPanel = new ColorSelectionPanel();
        
        final JLayeredPane layeredPane = new JLayeredPane();
        layeredPane.setBackground(Color.LIGHT_GRAY);
        toolPanel.add(layeredPane, "cell 0 0,grow");
        layeredPane.setLayer(fillColorPanel, 1);
        fillColorPanel.setToolTipText("Fill color");
        fillColorPanel.setBorder(new LineBorder(UIManager
            .getColor("CheckBox.light"), 3));
        fillColorPanel.setBounds(18, 23, 35, 35);
        fillColorPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                layeredPane.setLayer(fillColorPanel, 1);
                layeredPane.setLayer(borderColorPanel, 0);

                try
                {
                    Color c = JColorChooser.showDialog(null, "Fill Color",
                        new Color(255, 0, 0));
                    fillColor = new Color(c.getRed(), c.getGreen(),
                        c.getBlue(), c.getAlpha());
                    if (c.getTransparency() != 1.0)
                        opacitySlider.setValue((int) (c.getTransparency() * 10));
                    //editingPanel.setColor(fillColor);
                    fillColorPanel.setColor(new Color(c.getRed(), c.getGreen(),
                        c.getBlue(), getOpacitySliderValue()));
                } catch (Exception ex)
                {
                }
            }
        });
        layeredPane.add(fillColorPanel);

        borderColorPanel.setToolTipText("Border color");
        borderColorPanel.setColor(Color.BLACK);
        borderColorPanel.setBorder(new LineBorder(UIManager
            .getColor("CheckBox.light"), 3));
        borderColorPanel.setBounds(3, 8, 35, 35);
        borderColorPanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                layeredPane.setLayer(borderColorPanel, 1);
                layeredPane.setLayer(fillColorPanel, 0);
                try
                {
                    Color c = JColorChooser.showDialog(null, "Border Color",
                        new Color(255, 255, 255));
                    borderColor = new Color(c.getRed(), c.getGreen(), c
                        .getBlue(), c.getAlpha());
                    if (c.getTransparency() != 1.0)
                        opacitySlider.setValue((int) (c.getTransparency() * 10));
                    System.out.println("c.get: " + c.getTransparency());
                    //editingPanel.setBorderColor(borderColor);
                    borderColorPanel.setColor(new Color(c.getRed(), c
                        .getGreen(), c.getBlue(), getOpacitySliderValue()));
                } catch (Exception ex)
                {
                }
            }
        });
        layeredPane.add(borderColorPanel);
        
        
        opacitySlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (layeredPane.getLayer(fillColorPanel) == 1)
                {
                    //editingPanel.setTransparency(getOpacitySliderValue());
                    fillColorPanel.setColor(new Color(fillColorPanel.getColor()
                        .getRed(), fillColorPanel.getColor().getGreen(),
                        fillColorPanel.getColor().getBlue(),
                        getOpacitySliderValue()));
                } else if (layeredPane.getLayer(borderColorPanel) == 1)
                {
                    //editingPanel.setBorderTransparency(getOpacitySliderValue());
                    borderColorPanel.setColor(new Color(borderColorPanel
                        .getColor().getRed(), borderColorPanel.getColor()
                        .getGreen(), borderColorPanel.getColor().getBlue(),
                        getOpacitySliderValue()));
                }
            }
        });
        opacitySlider.setFont(new Font("Tahoma", Font.PLAIN, 11));
        opacitySlider.setPaintTicks(true);
        opacitySlider.setPaintLabels(true);
        opacitySlider.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Opacity Level", TitledBorder.TRAILING, TitledBorder.BOTTOM, null, null));
        opacitySlider.setToolTipText("Opacity level");
        opacitySlider.setMajorTickSpacing(25);
        opacitySlider.setValue(255);
        // Create the label table
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(new Integer(0), new JLabel("0%"));
        labelTable.put(new Integer(25), new JLabel("25%"));
        labelTable.put(new Integer(50), new JLabel("50%"));
        labelTable.put(new Integer(75), new JLabel("75%"));
        labelTable.put(new Integer(100), new JLabel("100%"));
        opacitySlider.setLabelTable(labelTable);
        
        
        // Create the label table
        Hashtable<Integer, JLabel> labelTable2 = new Hashtable<>();
        labelTable2.put(new Integer(0), new JLabel("0"));
        labelTable2.put(new Integer(10), new JLabel("1"));
        labelTable2.put(new Integer(20), new JLabel("2"));
        labelTable2.put(new Integer(30), new JLabel("3"));
        labelTable2.put(new Integer(40), new JLabel("4"));
        labelTable2.put(new Integer(50), new JLabel("5"));
        labelTable2.put(new Integer(60), new JLabel("6"));
        labelTable2.put(new Integer(70), new JLabel("7"));
        labelTable2.put(new Integer(80), new JLabel("8"));
        labelTable2.put(new Integer(90), new JLabel("9"));
        labelTable2.put(new Integer(100), new JLabel("10"));
        
        strokeSlider.setBorder(new TitledBorder(new EmptyBorder(0, 0, 0, 0), "Stroke Size", TitledBorder.TRAILING, TitledBorder.BOTTOM, null, null));
        strokeSlider.setMinorTickSpacing(5);
        strokeSlider.setToolTipText("Stroke Width");
        strokeSlider.setMajorTickSpacing(10);
        strokeSlider.setValue(30);
        strokeSlider.setLabelTable(labelTable2);
        strokeSlider.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                //editingPanel.setStroke(getStrokeSliderValue());
            }
        });
        
        JPanel shapePanel = new JPanel();
        shapePanel.setBackground(Color.WHITE);
        shapePanel.setBorder(new TitledBorder(null, "Shapes", TitledBorder.TRAILING, TitledBorder.BOTTOM, null, null));
        toolPanel.add(shapePanel, "cell 3 0,growy");
        shapePanel.setLayout(new GridLayout(0, 3, 0, 0));
        
        JToggleButton toggleButton = new JToggleButton("0");
        shapePanel.add(toggleButton);
        
        JToggleButton toggleButton_1 = new JToggleButton("1");
        shapePanel.add(toggleButton_1);
        
        JToggleButton toggleButton_2 = new JToggleButton("2");
        shapePanel.add(toggleButton_2);
        
        JToggleButton toggleButton_3 = new JToggleButton("3");
        shapePanel.add(toggleButton_3);
        
        JToggleButton toggleButton_4 = new JToggleButton("4");
        shapePanel.add(toggleButton_4);
        
        JToggleButton toggleButton_5 = new JToggleButton("5");
        shapePanel.add(toggleButton_5);
        
        JPanel editingToolsPanel = new JPanel();
        editingToolsPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Tools", TitledBorder.TRAILING, TitledBorder.BOTTOM, null, null));
        editingToolsPanel.setBackground(Color.WHITE);
        toolPanel.add(editingToolsPanel, "cell 4 0,grow");
        editingToolsPanel.setLayout(new GridLayout(0, 3, 0, 0));
        
        JToggleButton toggleButton_6 = new JToggleButton("0");
        editingToolsPanel.add(toggleButton_6);
        
        JToggleButton toggleButton_7 = new JToggleButton("1");
        editingToolsPanel.add(toggleButton_7);
        
        JToggleButton toggleButton_8 = new JToggleButton("2");
        editingToolsPanel.add(toggleButton_8);
        
        JToggleButton toggleButton_9 = new JToggleButton("3");
        editingToolsPanel.add(toggleButton_9);
        
        JToggleButton toggleButton_10 = new JToggleButton("4");
        editingToolsPanel.add(toggleButton_10);
        
        JToggleButton toggleButton_11 = new JToggleButton("5");
        editingToolsPanel.add(toggleButton_11);
        
        JButton btnSelect = new JButton("Select");
        toolPanel.add(btnSelect, "cell 5 0,grow");
        
        JPanel panel_1 = new JPanel();
        panel_1.setBackground(Color.WHITE);
        toolPanel.add(panel_1, "cell 6 0,grow");
        panel_1.setLayout(new MigLayout("", "[61px]", "[][]"));
        
        JButton btnUndo = new JButton("Undo");
        panel_1.add(btnUndo, "cell 0 0,grow");
        
        JButton btnRedo = new JButton("Redo");
        panel_1.add(btnRedo, "cell 0 1,grow");
        
        JButton btnSubmit = new JButton("Submit");
        toolPanel.add(btnSubmit, "cell 15 0,growy");
        
        JScrollPane scrollPane = new JScrollPane();
        panel.add(scrollPane, "cell 0 1,grow");
        
        JPanel imageLayer = new JPanel();
        imageLayer.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        imageLayer.setBackground(Color.WHITE);
        scrollPane.setViewportView(imageLayer);
        imageLayer.setLayout(new GridLayout(2, 3, 0, 0));
    }
    protected int getOpacitySliderValue()
    {
        double value = ((opacitySlider.getValue() / 100.0) * 255.0);
        return (int) value;
    }

    protected float getStrokeSliderValue()
    {
        float value = ((strokeSlider.getValue() / 10f));
        return value;
    }
    

}
