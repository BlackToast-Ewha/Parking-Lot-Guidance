

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import java.io.IOException;


public class SettingAreaScore extends JFrame implements ActionListener{

	JPanel panel;
	JLabel gateLabel,elevLabel,disLabel;
	JTextField gateField,elevField,disField;
	JButton saveButton;
	JPanel buttonPanel;
	
	public SettingAreaScore(){
		setSize(800,300);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("������ ������ ������");
		BoxLayout layout = new BoxLayout(this.getContentPane(),BoxLayout.Y_AXIS);
		this.setLayout(layout);
		//setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		
		//panel = new JPanel(new GridLayout(6,1));
		buttonPanel = new JPanel(new FlowLayout());
		gateLabel = new JLabel("������ ��/�ⱸ ��ȣ�� �������� ���� ������ �Է��ϼ���",SwingConstants.CENTER);
		elevLabel = new JLabel("���������� ��ȣ�� �������� ���� ������ �Է��ϼ���",SwingConstants.CENTER);
		disLabel = new JLabel("����� ���� ��ȣ�� �������� ���� ������ �Է��ϼ���",SwingConstants.CENTER);
		gateField = new JTextField(5);
		elevField = new JTextField(5);
		disField = new JTextField(5);
		gateField.setSize(2, 1);
		saveButton = new JButton("���� ���� ����");
		
		this.add(gateLabel);
		this.add(gateField);
		this.add(elevLabel);
		this.add(elevField);
		this.add(disLabel);
		this.add(disField);
		//buttonPanel.add(saveButton);
		saveButton.addActionListener(this);
		saveButton.setHorizontalAlignment(0);
		saveButton.setLocation(400,10);
		//his.add(panel);
		this.add(saveButton,BorderLayout.SOUTH);
		
		setVisible(true);
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == saveButton){
			try{
				PrintWriter pw = new PrintWriter("managerUI_Test.txt");
				String gateArea = gateField.getText();
				pw.println(gateArea);
				String elevArea = elevField.getText();
				pw.println(elevArea);
				String disArea = disField.getText();
				pw.println(disArea);
				
				pw.close();
			}
			catch(IOException e1){
				System.out.println(e1.getMessage());
			}
		}
	}
	

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String temp;
		
		SettingAreaScore admin = new SettingAreaScore();
		

	}


	
}
