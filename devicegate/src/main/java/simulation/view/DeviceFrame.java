package simulation.view;

import devicegate.conf.V;
import org.apache.log4j.PropertyConfigurator;
import simulation.DeviceMain;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by xiaoke on 17-11-2.
 */
public class DeviceFrame {
    static JFrame jf = new JFrame("Add");
    static JTextField appName = new JTextField();
    static JTextField devMark = new JTextField();
    static JTextField portNum = new JTextField();
    static JComboBox<String> devType = new JComboBox<String>(new String[]{"SWITCH", "DIGITL", "ANALOG"});
    static JButton addBtn = new JButton("Add");

    public static void main(String[] args) {
        PropertyConfigurator.configure(V.LOG_PATH);
        initMainFrame();
    }

    public static void initMainFrame() {
        jf.setSize(500, 300);
        jf.setResizable(false);
        jf.setLayout(new BorderLayout());
        jf.add(initCenterPane(), BorderLayout.CENTER);
        jf.add(initBtnPane(), BorderLayout.SOUTH);
        addBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addDevice();
            }
        });

        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.setVisible(true);
        jf.validate();
    }

    public static JPanel initCenterPane() {
        JPanel jp = new JPanel();
        jp.setLayout(new GridLayout(4, 2, 10, 20));
        jp.add(new JLabel("AppName", JLabel.CENTER));
        jp.add(appName);
        jp.add(new JLabel("DevMark", JLabel.CENTER));
        jp.add(devMark);
        jp.add(new JLabel("PortNum", JLabel.CENTER));
        jp.add(portNum);
        jp.add(new JLabel("Type", JLabel.CENTER));
        jp.add(devType);
        //jp.add(new JLabel("AppName", JLabel.RIGHT));
        return jp;
    }

    public static JPanel initBtnPane() {
        JPanel jp = new JPanel();
        jp.setLayout(new FlowLayout(FlowLayout.CENTER, 30, 30));
        jp.add(addBtn);
        return jp;
    }

    public static void addDevice() {
        String app = appName.getText().trim();
        String dmark = devMark.getText().trim();
        int pn = 0;
        try {
            pn = Integer.parseInt(portNum.getText().trim());
        } catch (Exception e) {
            pn = -1;
        }
        String type = devType.getSelectedItem().toString();
        if (app == null || app.isEmpty()) {
            JOptionPane.showMessageDialog(null, "AppName cannot be empty", "AppName Error",JOptionPane.ERROR_MESSAGE);
            appName.setText("");
            return;
        } else if (dmark == null || dmark.isEmpty()) {
            JOptionPane.showMessageDialog(null, "DevMark cannot be empty", "DevMark Error",JOptionPane.ERROR_MESSAGE);
            devMark.setText("");
            return;
        } else if (pn <= 0) {
            JOptionPane.showMessageDialog(null, "PortNum should be a positive", "PortNum Error",JOptionPane.ERROR_MESSAGE);
            portNum.setText("");
            return;
        }
        startANewDev(app, dmark, pn, type);
    }

    private static void startANewDev(String app, String dmark, int pn, String type) {
        try {
            DeviceMain.addNewDevice(app, dmark, type, pn);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), "Add Device Error",JOptionPane.ERROR_MESSAGE);
            appName.setText("");
            devMark.setText("");
            portNum.setText("");
        }
    }
}
