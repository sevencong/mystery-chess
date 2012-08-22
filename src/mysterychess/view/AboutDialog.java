package mysterychess.view;

import common.view.JLinkButton;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import mysterychess.util.Util;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2011</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class AboutDialog extends JDialog {

    public AboutDialog(Frame parent) {
        super(parent);
        try {
            initComponent();
            this.setSize(460, 340);
            initLabel();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void initComponent() throws Exception {
        setResizable(false);
        getContentPane().setLayout(borderLayout1);
        jPanel1.setLayout(gridLayout1);
        okButton.setText("OK");
        gridLayout1.setHgap(5);
        mainPanel.setLayout(borderLayout4);
        westPanel.setPreferredSize(new Dimension(210, 10));
        westPanel.setLayout(borderLayout3);
        applicationNameLabel.setFont(new java.awt.Font("Arial", Font.BOLD, 16));
        applicationNameLabel.setForeground(new Color(30, 0, 150));
        applicationNameLabel.setPreferredSize(new Dimension(23, 25));
        applicationNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        jLabel1.setBackground(SystemColor.controlShadow);
        jLabel1.setFont(new java.awt.Font("Arial", Font.BOLD, 11));
        jLabel1.setForeground(new Color(50, 0, 180));
        jLabel1.setBorder(BorderFactory.createEtchedBorder());
        jLabel1.setOpaque(true);
        jLabel1.setHorizontalAlignment(SwingConstants.LEFT);
        jLabel1.setText("Developers:");
        contributorsText.setBackground(SystemColor.controlHighlight);
        contributorsText.setForeground(new Color(20, 0, 150));
        contributorsText.setBorder(BorderFactory.createEtchedBorder());
        contributorsText.setEditable(false);
        jPanel2.setLayout(borderLayout5);
        linkToProject.setText("Get latest version!");
        linkToProject.setLinkURL(new URL("http://code.google.com/p/mystery-chess/downloads/list"));
        jPanel2.setPreferredSize(new Dimension(38, 55));
        nothPanel.setLayout(borderLayout2);
        nothPanel.setPreferredSize(new Dimension(74, 30));
        copyrightLabel.setForeground(new Color(20, 0, 150));
        copyrightLabel.setPreferredSize(new Dimension(25, 25));
        copyrightLabel.setHorizontalAlignment(SwingConstants.CENTER);
        copyrightLabel.setVerticalAlignment(SwingConstants.TOP);
        buttonPanel.setPreferredSize(new Dimension(57, 38));
        buttonPanel.add(jPanel1);
        jPanel1.add(okButton);
        this.getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);
        jPanel3.add(linkToProject);
        jScrollPane1.getViewport().add(contributorsText);
        this.getContentPane().add(westPanel, java.awt.BorderLayout.WEST);
        westPanel.add(iconLabel, java.awt.BorderLayout.CENTER);
        westPanel.add(jPanel2, java.awt.BorderLayout.SOUTH);
        nothPanel.add(jLabel1, java.awt.BorderLayout.SOUTH);
        mainPanel.add(nothPanel, java.awt.BorderLayout.NORTH);
        mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        mainPanel.add(jPanel3, java.awt.BorderLayout.SOUTH);
        jPanel2.add(applicationNameLabel, java.awt.BorderLayout.CENTER);
        jPanel2.add(copyrightLabel, java.awt.BorderLayout.SOUTH);
        this.getContentPane().add(jPanel4, java.awt.BorderLayout.EAST);

        okButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
    }
    JPanel jPanel1 = new JPanel();
    JButton okButton = new JButton();
    GridLayout gridLayout1 = new GridLayout();
    JPanel buttonPanel = new JPanel();
    JPanel mainPanel = new JPanel();
    JLabel applicationNameLabel = new JLabel();
    JPanel westPanel = new JPanel();
    BorderLayout borderLayout1 = new BorderLayout();
    JLabel iconLabel = new JLabel();
    JLabel jLabel1 = new JLabel();
    JScrollPane jScrollPane1 = new JScrollPane();
    JTextArea contributorsText = new JTextArea();
    JPanel jPanel3 = new JPanel();
    JPanel jPanel2 = new JPanel();
    JLabel copyrightLabel = new JLabel();
    JLinkButton linkToProject = new JLinkButton();
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel nothPanel = new JPanel();
    BorderLayout borderLayout2 = new BorderLayout();
    BorderLayout borderLayout4 = new BorderLayout();
    BorderLayout borderLayout5 = new BorderLayout();
    JPanel jPanel4 = new JPanel();
    TitledBorder titledBorder1 = new TitledBorder("");

    private void initLabel() {
        this.setTitle(Util.getApplicationName() + " - " + Util.getVersion());
        iconLabel.setIcon(new ImageIcon(Util.getAboutImage()));
        StringBuilder developers = new StringBuilder("");
        for (String a : Util.getDevelopers()) {
            developers.append(a);
            developers.append("\n");
        }

        contributorsText.setText(developers.toString());
        copyrightLabel.setText("Copyright \u00A9 2011");
        applicationNameLabel.setText(Util.getApplicationName() + " - " + Util.getVersion());
    }
}