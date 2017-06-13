package simulation.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by xiaoke on 17-6-13.
 */
public class DeviceFrame extends JFrame {

    private final String did;

    private JPanel jp;

    private JComboBox<Integer> port;

    private JComboBox<String> mtype;

    private JTextField desc;

    public DeviceFrame(String did) {
        super(did);
        this.did = did;
        show0();
    }


    private void show0() {
        this.setVisible(true);
        this.setContentPane(getMainPanel());
        this.setResizable(false);
        this.validate();
        this.setSize(new Dimension(400, 200));
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    }

    private JPanel getMainPanel() {
        jp = new JPanel();
        jp.setLayout(new GridLayout(5, 2, 0, 10));
        jp.add(new JLabel("端口数量：", JLabel.CENTER));
        port = new JComboBox<Integer>();
        for (int i = 1; i <= 16; i ++) {
            port.addItem(i);
        }
        jp.add(port);
        port.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    int portNum = (Integer)port.getSelectedItem();
                    System.out.println(portNum);
                    for (int i = 0; i < portNum; i++) {
                        jp.add(new JLabel("value of port" + i + "：", JLabel.CENTER));
                        jp.add(new TextField());
                        jp.repaint();
                    }
                }
            }
        });
        jp.add(new JLabel("测量类型：", JLabel.CENTER));
        mtype = new JComboBox<String>();
        mtype.addItem("SWITCH");
        mtype.addItem("DIGITL");
        mtype.addItem("ANALOG");
        jp.add(mtype);
        jp.add(new JLabel("设备描述：", JLabel.CENTER));
        desc = new JTextField();
        jp.add(desc);
        return jp;
    }


}
