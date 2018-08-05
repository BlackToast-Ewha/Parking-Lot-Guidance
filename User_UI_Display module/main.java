

/*2018-08-03 UPDATE
 * (1) Path�� ȭ��ǥ�� ��ȯ�ϴ� �۾� �Ϸ�
 * (2) ��ȯ�� ȭ��ǥ�� �ֿܼ� ����ϴ� �۾� �Ϸ�
 * (3) ���� ���� ���� �Ϸ�
 * (4) ���ͼ��� ���� �Ϸ�
 * (5) ������ ���� ���� ���� ���ͼ����� �ε��� ���� �Ϸ�
 * (6) AstarSearch()�� ���� ���� �Ϸ�
 * (7) ������ �ʴ� �ڵ�� �ǹ� ���� �ּ� ���� �Ϸ�
 */

import gnu.io.CommPortIdentifier;
import java.util.PriorityQueue;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.List;
import java.util.Comparator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Scanner;
import java.sql.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;


public class main extends JFrame{
	
	
	
	/*���� ����, ���ͼ���, ������ ���� ���� */
	static ParkingArea[][] area = new ParkingArea[12][];
	public static Intersection[] n = new Intersection[8];
	static Area[] parkArea = new Area[12];
	static Display[][] display = new Display[8][4];

	/*���� ������ ��� �켱���� ť�� ���� ���� �̸��� ��� String����*/
	static PriorityQueue<Area> entranceArea; 
	static PriorityQueue<Area> elevatorArea; 
	static PriorityQueue<Area> disabledArea; 
	static String totalAssign; 
	
	/*MySQL �����ͺ��̽� ���ῡ �ʿ��� ���� ����*/
	static final String JDBC_driver = "com.mysql.cj.jdbc.Driver";
	static final String DB_url = "jdbc:mysql://localhost:3306/blacktoast?useSSL=false&serverTimezone=UTC";
	static final String User = "root";
	static final String password = "team15";
	static Connection conn = null;
	static Statement stmt = null;
	
	/*Display �Լ��� �Ѱ��ֱ� ���� ����ϴ� List<Intersection> ����*/
	static List<Intersection> originalPath;
	static List<Intersection> gateUIPath;
	
	/*������ ��ü�� �ʱ�ȭ�ϴ� �Լ�*/
	public static void setDisplay() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				display[i][j] = new Display(i, j);
				// j=0 -> u, j=1 -> l, j=2 -> d, j=3 -> r �������� ��Ÿ��,
			}
		}

	}

	/*�����ǿ� ȭ��ǥ�� ����ϴ� �Լ�*/
	public static void printDisplay(List<Intersection> path) {
		// ���⼭ �ɰ������� �ϵ��ڵ�
		int a = 0, b, c;
		for (int i = 0; i < path.size() - 1; i++) { // path �� intersection�� �� ����
													// ���� ������� ����
			c = a; // ������ i ����
			a = path.get(i).index;
			b = path.get(i + 1).index;
			if ((b - a) == 1) {// (1->2, 2->3, 3->4, 4->5, 5->6, 7->8)
				if (c < a) { // ���� ��Ʈ. ���� index �� ���� index ���� ���� ��
					if (a == 3) {
						display[2][0].printLeft();// 3 �� ���ͼ��� }
					} else if (a == 4) {
						display[3][0].printLeft(); // 4->5
					} else if (a == 7) {
						display[6][3].printRight();// 7->8
					} else
						display[a - 1][0].printStraight();
				} else {// �����ڰ� ������ ������ �ʾ� ��Ž���� ��� . �� ���� index�� ���� intersection ���� �� ���� index�� ���� intersection 6->1->2 5->2->3 8->1->2 7->3->4
					if (c == 8)
						display[0][0].printStraight();// 812
					else if (c == 7)
						display[2][1].printStraight();// 734
					else
						display[a - 1][3].printLeft();// 612 523
				}
			} else if ((b - a) == -1) {// 6->5, 5->4, 4->3, 3->2, 2->1, 8->7
				if (a == 4) { // 4->3
					display[3][3].printRight();
				} else if (a == 3) { // 3->2
					if (c == 4)
						display[2][3].printRight(); // 432
					if (c == 7)
						display[2][1].printLeft(); // 732
					else
						System.out.println("restart from intersection3"); // 3���� ������Ͽ� 32
				} else if (a == 8) { // 8->7
					display[7][0].printLeft();
				} else { // 6->5, 5->4, 2->1
					display[a - 1][2].printStraight();
				}

			} else {// index�� �̾����� ���� ���. 1->6, 1->8, 2->5, 3->7, �׸��� �� �ݴ� 6->1, 5->2, 7->3 8->1
				if (c < a) {// index �� �������� ū���� ���� ��
					if (b > a) {// 016(816) 018 125 237
						if (a == 1 && b == 8)
							display[0][3].printStraight(); // 018 �̷��� �Ͼ�� �ȵ�.
						else if (a == 3)
							display[2][0].printRight(); // 237
						else
							display[a - 1][0].printLeft(); // 016(816) 125

					} else {// 561 452 781
						if (a == 8)
							display[7][3].printRight(); // 781
						else
							display[a - 1][0].printLeft(); // 561 452

					}

				} else {// index �� ū���� �������� ���� �� // ��Ž���� ���, 437 652 873 216 325
					if (b > a) {// 437 216 325
						if (a == 3)
							display[2][3].printStraight(); // 437
						else
							display[a - 1][2].printRight(); // 216 325

					} else { // 652 873
						if (a == 5)
							display[4][3].printRight(); // 652
						if (a == 7)
							display[6][0].printLeft();// 873

					}
				}

			}

		}
	}

	/* ���� ���� �� ĭ���� �ʱ�ȭ �ϴ� �Լ�*/
	public static void setParkingArea() {

		area[0] = new ParkingArea[24];// A => 65
		area[1] = new ParkingArea[24];// B
		area[2] = new ParkingArea[12];// C
		area[3] = new ParkingArea[8];// D
		area[4] = new ParkingArea[7];// E
		area[5] = new ParkingArea[12];// F
		area[6] = new ParkingArea[10];// G
		area[7] = new ParkingArea[20];// H,���������� ��ó
		area[8] = new ParkingArea[2];// I,�����
		area[9] = new ParkingArea[4];// J,�����
		area[10] = new ParkingArea[3];// K,�����
		area[11] = new ParkingArea[3];// L,���������� ��ó

		// ���� ���� ��ü ����, ���߿� ������ ���̽��� ������ �κ�
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[i].length; j++) {
				switch (i) { // ParkingArea(�����̸�, ��������, ���� �������� index, �����/�������)
				case 0: {
					area[i][j] = new ParkingArea("A", 0, j, 'a');
					break;
				}
				case 1: {
					area[i][j] = new ParkingArea("B", 0, j, 'a');
					break;
				}
				case 2: {
					area[i][j] = new ParkingArea("C", 0, j, 'a');
					break;
				}
				case 3: {
					area[i][j] = new ParkingArea("D", 0, j, 'a');
					break;
				}
				case 4: {
					area[i][j] = new ParkingArea("E", 0, j, 'a');
					break;
				}
				case 5: {
					area[i][j] = new ParkingArea("F", 0, j, 'a');
					break;
				}
				case 6: {
					area[i][j] = new ParkingArea("G", 0, j, 'a');
					break;
				}
				case 7: {
					area[i][j] = new ParkingArea("H", 0, j, 'a');
					break;
				}
				case 8: {
					area[i][j] = new ParkingArea("I", 0, j, 'd');
					// area[i][j] = new ParkingArea("I", 0, j, 'a');
					break;
				}
				case 9: {
					area[i][j] = new ParkingArea("J", 0, j, 'd');
					// area[i][j] = new ParkingArea("J", 0, j, 'a');
					break;
				}
				case 10: {
					area[i][j] = new ParkingArea("K", 0, j, 'd');
					// area[i][j] = new ParkingArea("K", 0, j, 'a');
					break;
				}
				case 11: {
					area[i][j] = new ParkingArea("L", 0, j, 'a');
					break;
				}
				default:
					break;
				}
			}
		}
	}

	/* �������� Intersection �ʱ�ȭ*/
	public static void setIntersection() { // �տ��� ������ 8 ���� intersection �� ��ü�� �����Ͽ� �ο�
		// 1~8 ������ �ε��� �ο�
		for (int i = 0; i < 8; i++) {
			n[i] = new Intersection((i + 1));

		}
		/* row�� column �� �����ϴ� �� */

		n[3].setRowCol(0, 0);
		n[4].setRowCol(1, 0);
		n[5].setRowCol(2, 0);
		n[2].setRowCol(0, 1);
		n[1].setRowCol(1, 1);
		n[0].setRowCol(2, 1);
		n[6].setRowCol(0, 2);
		n[7].setRowCol(2, 2);

		// n[i].adjacencies = �� ���ͼ��ǿ��� ����� edge���� ����

		n[0].adjacencies = new Edge[] { new Edge(n[1], 1), new Edge(n[5], 1), new Edge(n[7], 1) };

		n[0].adjacencies[0].Decide_Area(area[8]);// area[8]=I����
		n[0].adjacencies[1].Decide_Area(area[1]);// area[1]=B����
		n[0].adjacencies[1].Decide_Area(area[2]);// area[2]=C����
		n[0].adjacencies[2].Decide_Area(area[7]);// area[7]=H����

		n[1].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[2], 1), new Edge(n[4], 1) };

		n[1].adjacencies[0].Decide_Area(area[10]);// area[10]=K����
		n[1].adjacencies[1].Decide_Area(area[5]);// area[5]=F����
		n[1].adjacencies[2].Decide_Area(area[0]);// area[0]=A����
		n[1].adjacencies[2].Decide_Area(area[1]);// area[0]=B����

		n[2].adjacencies = new Edge[] { new Edge(n[1], 1), new Edge(n[3], 1), new Edge(n[6], 1) };

		n[2].adjacencies[0].Decide_Area(area[8]);// area[8]=I����
		n[2].adjacencies[1].Decide_Area(area[5]);// area[5]=F����
		n[2].adjacencies[2].Decide_Area(area[6]);// area[6]=G����

		n[3].adjacencies = new Edge[] { new Edge(n[2], 1), new Edge(n[4], 1), };

		n[3].adjacencies[0].Decide_Area(area[5]);// area[5]=F����
		n[3].adjacencies[1].Decide_Area(area[0]);// area[0]=A����

		n[4].adjacencies = new Edge[] { new Edge(n[3], 1), new Edge(n[1], 1), new Edge(n[5], 1) };

		n[4].adjacencies[0].Decide_Area(area[3]);// area[3]=D����
		n[4].adjacencies[1].Decide_Area(area[5]);// area[5]=F����

		n[5].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[4], 1), };

		n[5].adjacencies[0].Decide_Area(area[1]);// area[1]=B����
		n[5].adjacencies[0].Decide_Area(area[2]);// area[2]=C����
		n[5].adjacencies[0].Decide_Area(area[10]);// area[10]=K����
		n[5].adjacencies[1].Decide_Area(area[3]);// area[3]=D����

		n[6].adjacencies = new Edge[] { new Edge(n[2], 1), new Edge(n[7], 2) };

		n[6].adjacencies[0].Decide_Area(area[6]);// area[6]=G����
		n[6].adjacencies[1].Decide_Area(area[7]);// area[7]=H����
		n[6].adjacencies[1].Decide_Area(area[9]);// area[9]=J����
		n[6].adjacencies[1].Decide_Area(area[11]);// area[11]=L����

		n[7].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[6], 2) };

		n[7].adjacencies[0].Decide_Area(area[10]);// area[10]=K����
		n[7].adjacencies[1].Decide_Area(area[7]);// area[7]=H����
		n[7].adjacencies[1].Decide_Area(area[9]);// area[9]=J����
		n[7].adjacencies[1].Decide_Area(area[11]);// area[11]=L����
	}

	/*���� ���� (A,B ��)�� �ʱ�ȭ �ϴ� �Լ�
	 * ������ �̸�(���ĺ�), �ܿ����� ��, ��ü���� ��, ������ ����� ���ͼ����� �����ϰ� ��.
	 */
	public static void setArea() {
		parkArea[0] = new Area("A", 24, 24, n[1], 0);
		parkArea[1] = new Area("B", 24, 24, n[0], 1);
		parkArea[2] = new Area("C", 12, 12, n[0], 2);
		parkArea[3] = new Area("D", 8, 8, n[4], 3);
		parkArea[4] = new Area("E", 7, 7, n[5], 4);
		parkArea[5] = new Area("F", 12, 12, n[2], 5);
		parkArea[6] = new Area("G", 10, 10, n[6], 6);
		parkArea[7] = new Area("H", 20, 20, n[7], 7);
		parkArea[8] = new Area("I", 2, 2, n[0], 8);
		parkArea[9] = new Area("J", 4, 4, n[0], 9);
		parkArea[10] = new Area("K", 3, 3, n[0], 10);
		parkArea[11] = new Area("L", 3, 3, n[7], 11);

	}

	/*2018-08-01 AstarSearch�� parent �Ӹ� �ƴ϶� child ��带 �����ϰ� �������
	 * ���� ������ printPath()�� �޸� �ڽ��� �ڽĳ�带 Ȯ���ϸ� path�� ����ϰ� �ϴ� �Լ� �ʿ�
	 * �װ� printPathTemp()��. 
	 */
	public static List<Intersection> printPathTemp(Intersection source,Intersection dest,Intersection beforeIntsc){
		
		List<List<Intersection>> allPath = new ArrayList<List<Intersection>>();
		
		for(Edge e:source.adjacencies){
			
			if(e.target!=beforeIntsc){
				List<Intersection> path = new ArrayList<Intersection>();
				path.add(source);
				for(Intersection node = e.target; node!= null; node = node.child){
					if(!path.contains(node))
						path.add(node);
				}
				if(!path.contains(dest))
					path.add(dest);
				allPath.add(path);
				
			}
			
		}
		int minIndex = 0;
		for(int i = 0; i < allPath.size(); i++){
			if(allPath.get(i).size() < allPath.get(minIndex).size())
				minIndex = i;
		}
		return allPath.get(minIndex);
	}

	/*������ ����ϴ� path ����Լ� */
	public static List<Intersection> printPath(Intersection target) {
		List<Intersection> path = new ArrayList<Intersection>();

		for (Intersection node = target; node != null; node = node.parent) {
			path.add(node);
		}
		

		Collections.reverse(path);

		return path;
	}

	/*destination node(=goal)�� �ʿ��ϴ� ������ AstarSearch�� source,goal,��� ������ ���ͼ��Ǳ��� ����ϰ� �ϴ�
	 * �Լ��� ����
	 */
	public static void AstarSearch(Intersection source, Intersection goal, Intersection beforeIntsc) {

		/*������ ���ͼ��ǵ��� ��� ��ü Set explored*/
		Set<Intersection> explored = new HashSet<Intersection>();

		/*�ĺ��� �Ǵ� ���ͼ����� ��� PriorityQueue queue
		 * f_score�� ���� ���� �켱 ������ ����*/
		PriorityQueue<Intersection> queue = new PriorityQueue<Intersection>(20, new Comparator<Intersection>() {
			// override compare method
			public int compare(Intersection i, Intersection j) {
				if (i.f_scores > j.f_scores) {
					return 1;
				}

				else if (i.f_scores < j.f_scores) {
					return -1;
				}

				else {
					return 0;
				}
			}

		});
		
		
		// cost from start
		source.g_scores = 0;
		queue.add(source);

		boolean found = false;

		while ((!queue.isEmpty()) && (!found)) {

			// the node in having the lowest f_score value
			Intersection current = queue.poll();
			explored.add(current);

			// goal found
			if (current.index == goal.index) {
				found = true;
				break; // �ٷ� while�� Ż��. 
			}

			// check every child of current node
			for (Edge e : current.adjacencies) {
				Intersection child = e.target;
				double cost = e.cost;
				double temp_g_scores = current.g_scores + cost;
				double temp_f_scores = temp_g_scores + child.h_scores;
				/*
				 * if child node has been evaluated and the newer f_score is
				 * higher, skip
				 */

				if ((explored.contains(child)) && (temp_f_scores >= child.f_scores)) {
					continue;
				}

				/*
				 * else if child node is not in queue or newer f_score is lower
				 */

				else if ((!queue.contains(child)) || (temp_f_scores < child.f_scores)) {

					child.parent = current;
					child.g_scores = temp_g_scores;
					child.f_scores = temp_f_scores;

					if (queue.contains(child)) {
						queue.remove(child);
					}
					if (child != beforeIntsc){ // ����Ǿ��ִ� ���ͼ����� ��� ������ ���ͼ����� �ƴ� ��쿡�� queue�� �߰�. ���� �ȳ��� ���� ���� �ڵ���
						queue.add(child);
						current.child = child;
						if(child.index == goal.index)
							break;
					}

				}

			} // for

		} 
	}

	/*��ȣ�ϴ� ���� (����������, ����Ʈ, ����� ��������)�� ���� ������ ���ͼ����� �ε����� ��ȯ*/
	public static int find_dest(String mode) {
		int findFlag = 0; // ���� ������ �Ҵ�Ǿ����� üũ�ϴ� flag ����
		String selectedAreaName = ""; // ���� ������ �̸��� ���� String ����
		int selectedLotNumber = 0; // ���� ���� ������ ���� ��ȣ�� ��� int ����
		Intersection answer = null; // ������ ���� ������ ���� ������ ���� ���ͼ����� ��� Intersection ����
		if (mode.equals("entrance")) { // �Ա� ��ó�� ������ ���
			while (findFlag != 1) {
				Area tempArea = entranceArea.poll(); // �켱����ť�� ��Ʈ ��带 ����. ���� �������� ��Ȳ�� �ݿ��� �ּ��� ���� ���� ����
				if (tempArea.remainNum != 0) { // ������ ���� ������ �ܿ� �������� 0�� �ƴ϶��
					selectedAreaName = tempArea.AreaName; // ���� �̸� ���� ����
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1; // ������ȣ ���� ����
					tempArea.remainNum--; // in������ 1�� �ٲٴ°Ͱ� ������ ������ ��. �ܿ� ���� ���� 1 ����
					entranceArea.add(tempArea); // peek()�� �ƴ϶� �ƿ� ť ������ Area�� �������Ƿ� �ٽ� �־���
					answer = tempArea.nearIntersection; // ���� ���ͼ��� �޾ƿ�
					findFlag = 1; // ���� ���� �Ҵ� �ϷḦ ���ϴ� ������ 1�� �ٲ�
				} else {
					if (entranceArea.isEmpty()) // �������� �����̸� findFlag ������ 0���� �ΰ� (���� ����) ���� Ż��
						break;
					continue; // �������� ������ �ƴ����� poll()�� ���� ������ �ܿ� �������� 0�� ��� �ٽ� while�� ���ư��� ��� poll()
				}
			}

		}
		if (mode.equals("elevator")) {
			while (findFlag != 1) {
				Area tempArea = elevatorArea.poll();
				if (tempArea.remainNum != 0) {
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;
					tempArea.remainNum--;
					elevatorArea.add(tempArea);
					answer = tempArea.nearIntersection;
					findFlag = 1;
				} else {
					if (elevatorArea.isEmpty()) {
						break;
					}
					continue;
				}
			}
		}
		if (mode.equals("disabled")) {
			while (findFlag != 1) {
				Area tempArea = disabledArea.poll();
				if (tempArea.remainNum != 0) {
					selectedAreaName = tempArea.AreaName;
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1;
					//area[tempArea.indexInPA][selectedLotNumber - 1].in = 1;
					tempArea.remainNum--;
					disabledArea.add(tempArea);
					answer = tempArea.nearIntersection;
					findFlag = 1;
				} else {
					if (disabledArea.isEmpty())
						break;
					continue;
				}
			}
		}
		if (findFlag == 0) {
			System.out.println("[����]�����忡 �����Ͻ� �� �����ϴ�!!");
			return -1;
		}
		totalAssign = selectedAreaName + selectedLotNumber;

		System.out.println("������ ���������� " + selectedAreaName + selectedLotNumber + "�Դϴ�");
		return answer.index - 1; //(0~7 ������ �ε��� ��ȯ)
	}

	/* �Ա�, ����������, ����� ���� ������ ���� ������ �켱����ť�� ������.
	*�켱������ ��ȣ�������κ����� �Ÿ�^2+�ܿ�����/��ü������ ������ ���� */
	public static void setPriorityQueue() {
		entranceArea = new PriorityQueue<Area>(12, new Comparator<Area>() {

			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.entranceScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.entranceScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}

		});
		elevatorArea = new PriorityQueue<Area>(12, new Comparator<Area>() {
			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.elevatorScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.elevatorScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}
		});
		disabledArea = new PriorityQueue<Area>(12, new Comparator<Area>() {
			public int compare(Area a0, Area a1) {
				double score0 = Math.pow(a0.disabledScore, 2) + (a0.remainNum / a0.totalNum);
				double score1 = Math.pow(a1.disabledScore, 2) + (a1.remainNum / a1.totalNum);

				if (score0 < score1)
					return 1;
				else if (score0 > score1)
					return -1;
				else
					return 0;
			}
		});
	}

	/*���ͼ����� h_score�� ���������� ���*/
	public static void setIntersectionInfo(Intersection goal) {
		for (int i = 0; i < 8; i++) {
			n[i].calculateHScore(goal);
		}
	}

	/*��ȣ���� �°� ���� ������ ������ ����ϴ� �Լ�*/
	/*�ð� ���⵵�� ������ ������ �ʱ� ���ÿ��� ���̴� �Լ��̹Ƿ� �Ϸ翡 1ȸ ���Ϸ� ȣ���*/
	public static void setParkAreaScore() throws IOException {
		int setCount = 0;
		/*������ �����ڰ� �Է��� ������ ���߾� ���� ���*/
		BufferedReader br = new BufferedReader(new FileReader("managerUI_Test.txt"));

		while (true) {
			String line;
			String[] tempArr;
			line = br.readLine();
			if (line == null)
				break;
			tempArr = line.split(",");
			if (setCount == 0) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].entranceScore = (tempArr.length - j) * 0.5;
							entranceArea.add(parkArea[i]);
						}
					}
				}
			}
			if (setCount == 1) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].elevatorScore = (tempArr.length - j) * 0.5;
							elevatorArea.add(parkArea[i]);
						}
					}
				}
			}
			if (setCount == 2) {
				for (int i = 0; i < 12; i++) {
					for (int j = 0; j < tempArr.length; j++) {
						if (parkArea[i].AreaName.equals(tempArr[j])) {
							parkArea[i].disabledScore = (tempArr.length - j) * 0.5;
							disabledArea.add(parkArea[i]);
						}
					}
				}
			}
			setCount++;
		}
	}

	public static void gateUI(int user_input, Scanner sc, Connection conn, Statement stmt)
			throws IOException, SQLException {
		while (true) {
			BufferedReader br = new BufferedReader(new FileReader("nowEnter.txt"));
			String line;
			line = br.readLine();
			String carPlate = line;
			String listPath = "";
			System.out.println("================= ���� �ȳ� �ý��� =================");
			System.out.println(carPlate + "�� ��ȣ�ϴ� ������ ������ �ּ���");
			System.out.println("1. ������ ��/�ⱸ ��ó ");
			System.out.println("2. ���������� ��ó ");
			System.out.println("3. ����� ���� �¼�");
			System.out.println("4. ����");
			System.out.print("��ȣ�� �Է����ּ��� : ");
			user_input = sc.nextInt();

			switch (user_input) {
			case 1: {
				int dest = 0;// ������ ���ͼ����� ��ȣ�� ������ ����
				try {
					dest = find_dest("entrance");
				} // ������ ���ͼ��� �� ������ ã�´�.
				catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.println("Destination: n" + dest);


				if (dest == -1) { // �������� ������ ��� dest���� -1�� return�ǵ��� �����س���
					System.out.println("�����忡 �����Ͻ� �� �����ϴ�! �ý����� ��ġ�ڽ��ϴ�");
					System.exit(0); // �ý��� ��ü ����
				}

				else {
					AstarSearch(n[0], n[dest], null);// ������ ���ͼ��Ǳ����� ��θ� ���
					gateUIPath = printPath(n[dest]); // list type �� path ����
					System.out.println("Path: " + gateUIPath);
					printDisplay(gateUIPath);
					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));
					System.out.println(listPath);
					stmt.executeUpdate("insert into cars values('" + carPlate + "','" + totalAssign + "','" + listPath + "');");
					
					break;
				}

			}
			case 2: {
				int dest = 0;
				try {
					dest = find_dest("elevator");
				} catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.println("Destination: n" + (dest + 1));

				if (dest == -1) {
					System.out.println("�����忡 �����Ͻ� �� �����ϴ�!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);
					gateUIPath = printPath(n[dest]);
					System.out.println("Path: " + gateUIPath);
					printDisplay(gateUIPath);
					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));
					stmt.executeUpdate("insert into cars values('" + carPlate + "','" + totalAssign + "','" + listPath + "');");
					break;
				}

			}
			case 3: {
				int dest = 0;
				try {
					dest = find_dest("disabled");

				} catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.println("Destination: n" + (dest + 1));
				if (dest == -1) {
					System.out.println("�����忡 �ܿ� ���� ������ �����ϴ�!");
					System.exit(0);
				} else {
					AstarSearch(n[0], n[dest], null);
					gateUIPath = printPath(n[dest]);
					System.out.println("Path: " + gateUIPath);
					printDisplay(gateUIPath);
					System.out.println();
					listPath = gateUIPath.stream().map(Object::toString).collect(Collectors.joining(", "));
					stmt.executeUpdate("insert into cars values('" + carPlate + "','" + totalAssign + "','" + listPath + "');");

					break;
				}

			}
			case 4: {
				System.out.println("�ý����� ��ġ�ڽ��ϴ�.");
				conn.close();
				stmt.close();
				break;
			}
			}// switch

			if (user_input == 4) {
				break;
			}
		} // while

	}

	/*������ �������� �ȳ� ���ø� ������ �ʰ� (���ǵ� �ƴϵ�) ���� �߸� ����� �� �ش� ���ͼ��ǿ��� �ٷ� 
	 * ���� ������ ���� ���������ڰ� �̾߱� ���� �� ���� �Լ�
	 * �����ڰ� ������ �������� �� ȣ��Ǵ� �Լ��̸� ���� ������� �ʰ� ����
	 * �׷��� ���߿� �ʿ��ϰ� �ɱ��(�ٸ� �Լ��� ġ������ ���� ���� ������) �ϴ� �������� �ʾ���
	 */
	@SuppressWarnings("null")
	public static int find_dest_err(Intersection current, String[] area_path, String numPlate, Intersection beforeGoal)
			throws IOException, SQLException {
		boolean firstFind = false;
		Intersection errIntersection = null;

		Statement stmt1 = conn.createStatement();

		for (int i = 0; i < parkArea.length; i++) {
			if (parkArea[i].nearIntersection == current) { // � ���� ������ ���� ���ͼ����� ���� ���ͼ����� ���
				if (parkArea[i].remainNum != 0) { // �ܿ������� �ִٸ�
					String name = parkArea[i].AreaName;
					int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
					stmt1.executeUpdate("update cars set parkArea = '" + name + Integer.toString(lot) + "' "+ "where carPlate = '" + numPlate + "'");
					parkArea[i].remainNum--;
					// �Ҵ��ϰ� firstFind�� true�� �ٲ� --> ���� ���ͼ����� �迭 �� �ε����� ��ȯ�ϰ� ��
					firstFind = true;
				}
			}
		}
		if (firstFind)
			return current.index - 1;
		else {
			for (int i = 0; i < parkArea.length; i++) {
				if (parkArea[i].remainNum != 0) {
					if (parkArea[i].nearIntersection != beforeGoal) {
						String name = parkArea[i].AreaName;
						int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
						stmt.executeQuery("update cars set parkArea = '" + name + Integer.toString(lot) + "' "+ "where numPlate = '" + numPlate + "'");
						parkArea[i].remainNum--;
						errIntersection = parkArea[i].nearIntersection;
					}
				}
			}
			return errIntersection.index - 1;
		}

	}
	
	/*���ͼ����� �������� ȭ��ǥ�� ���� �ȳ��� �ϱ� ���� ������ ������ ���ͼ����� �˾ƾ� ��
	 * ���� �� ���ͼ����� ���� ���ͼ����� �ȳ��� ���� �ڽ��� �ε����� �����ͺ��̽��� column �� beforeIntsc�� ����
	 * ��� ���ͼ����� �ڽſ��� �ٰ����� ������ ���� �� �۾��� �����ϸ� 
	 * ���� �ȳ� ���� �ش� �۾��� ������
	 */
	public static void saveIntscIndex(int myIndex, Connection conn) throws IOException, SQLException {
		Statement stmt2 = conn.createStatement();

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		stmt2.executeUpdate("update cars set beforeIndex='" + Integer.toString(myIndex) + "' where carPlate ='" + numPlate + "'");

	}

	/*detect.py�� intersection���� detect�� ���� ��ȣ���� ���ڿ��� intersection_getIn�̶�� ������
	 * �����ؾ��� */
	/* myIntersection�� �� ���ͼ����� ��ȣ�� �ǹ��ϸ� �� �κ��� �ϵ� �ڵ��Ǿ� ���ε� �ؾ� �Ѵ� */
	
	public static void intersectionDisplay(Connection conn, Statement stmt, int myIntersection) throws IOException, SQLException {
		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		int beforeIndex, currentIndex, nextIndex;
		System.out.println("���ͼ����� ��ȣ�� 0~7 �̴�. 1~8 �� �ƴ� �迭 ���� �ε����� ����Ѵ�\n\n");

		List<Intersection> currentPath = new ArrayList<Intersection>();

		ResultSet rs_car = stmt.executeQuery("select parkArea,assignPath,beforeIndex from cars where carPlate = '" + numPlate + "'");

		while (rs_car.next()) {
			String[] area_path = { rs_car.getString(1), rs_car.getString(2), rs_car.getString(3) };
			area_path[1] = area_path[1].replaceAll(" ", "");
			int driverError = Arrays.asList(area_path[1].split(",")).indexOf(Integer.toString(myIntersection));
			// System.out.println("�����ڰ� ���� �߸�����ٸ� -1 ��� :"+driverError);

			int beforeIndex_sql = Integer.parseInt(area_path[2]);
			if (driverError == -1) {
				String goalAreaName = area_path[0].substring(0, 1);
				// ���� �߸� �� �����ڿ��� �Ҵ�� ���� ������ �˾Ƴ�
				Intersection goalIntersection = parkArea[goalAreaName.charAt(0) - 65].nearIntersection; 																	// ������
				AstarSearch(n[myIntersection - 1], goalIntersection, n[beforeIndex_sql - 1]);//
				List<Intersection> newPathMyIntersection = printPath(goalIntersection);
				List<Intersection> tempPath = printPathTemp(n[myIntersection-1],goalIntersection,n[beforeIndex_sql-1]);

				currentIndex = myIntersection - 1;
				System.out.println("���Ӱ� ������ path�� " + newPathMyIntersection);
				System.out.println("�ӽ÷� ���� printPathTemp�� ����� "+tempPath);
				if (newPathMyIntersection.size() != 1) {
					System.out.println("(0~7 ���� ���)�����ڰ� ���� �߸� ������� ���Ӱ� ������ Path�� ���̰� 1���� �� ��,�׸��� ���ο� Path�� ���̴� "+ newPathMyIntersection.size());
					Intersection beforeIntsc = n[beforeIndex_sql-1];
					currentPath.add(beforeIntsc);
					currentPath.addAll(tempPath);
					System.out.println(currentPath);
					

					printDisplay(currentPath);
				} else {
					System.out.println("�����ڰ� ���� �߸� ������� ���Ӱ� ������ Path�� ���̰� 1�� �� (0~7 ���� ���)");
					System.out.println("���� ���ͼ����� ��ȣ");
					System.out.println(currentIndex);
					// ����� �ƹ��͵� ����Ʈ ���� ��
				}

			} else {
				String[] assignPathArr = area_path[1].split(",");
				List<String> tempPath = Arrays.asList(assignPathArr);
				currentIndex = myIntersection - 1;
				if (tempPath.indexOf(Integer.toString(myIntersection)) != 0) {
					beforeIndex = Integer.parseInt(tempPath.get(tempPath.indexOf(Integer.toString(myIntersection)) - 1))- 1;
				} else {
					System.out.println("���� ���ͼ����� �����ڿ��� ������ �Ҵ�� Path�� ù��° ���ͼ����̹Ƿ� ���� ���ͼ����� ��Ÿ���� beforeIndex ������ -1�� �Ҵ��Ѵ�");
					beforeIndex = -1;
				}
				int myIndexOfOriginPath = tempPath.indexOf(Integer.toString(myIntersection));

				if (myIndexOfOriginPath + 1 != tempPath.size()) {
					System.out.println("�����ڰ� ������ path��� �����ϰ� ������ ���� ���ͼ��� ���Ŀ��� ���İ����� ���ͼ����� �� �������� ��");
					nextIndex = Integer.parseInt(tempPath.get(myIndexOfOriginPath + 1)) - 1;
					System.out.println("���� ���ͼ����� ��ȣ, ���� ���ͼ����� ��ȣ, ������ ���������� ���ͼ����� ��ȣ");
					System.out.println(currentIndex + " " + beforeIndex + " " + nextIndex);
					currentPath.add(n[beforeIndex]);
					currentPath.add(n[currentIndex]);
					currentPath.add(n[nextIndex]);
					printDisplay(currentPath);

				} else {
					System.out.println("�����ڰ� ������ path��� �����ϰ� ������ ���� ���ͼ��� ���Ŀ� ���İ��� �� ���ͼ����� ���� ��");
					System.out.println("���� ���ͼ����� ��ȣ, ���� ���ͼ����� ��ȣ");
					System.out.println(currentIndex + " " + beforeIndex);
					// ���⵵ ����Ʈ �Ұ� ���� ��?
				}

			}
		}
	}

	// main������ ����� �Է¹޾Ƽ� ������ �Լ��� ȣ���ϴ� ������ ����
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		int user_input = -1;

		/*���� ����, ���ͼ���, ���� ���� ����, ��ȣ���� ���� �켱���� ť, ������ ��ü, ���� ������ ���� ���� �ʱ�ȭ*/
		setParkingArea();
		setIntersection();
		setArea();
		setPriorityQueue();
		setDisplay();
		setParkAreaScore();

		/*�������� �����ͺ��̽��� ����*/
		try {
			Class.forName(JDBC_driver);
			conn = DriverManager.getConnection(DB_url, User, password);
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*gateUI(user_input,sc,conn,stmt) �Լ��� ������ �����ϴ� ������ ȣ��Ǵ� �Լ�
		 * �ٸ� �������� �ʿ� ����
		 */
		// gateUI(user_input,sc,conn, stmt);
		
		/*����� ������ AstarSearch()�� printPathTemp�� ���� ���� Ȯ���� ���� saveIntscIndex()�� ���� ȣ���Ͽ�����
		 * �� ���ͼ��ǿ����� intersectionDisplay()-> saveIntscIndex() ������ ȣ��Ǿ�� ��
		 */
		saveIntscIndex(1, conn);
		intersectionDisplay(conn, stmt, 2);
		// 1,2 / 6,5 ��� �������� ����� �ϰ� ���� //1,8�� ���������� ����� (5�� ���ͼ������� �������Ҷ� 87345�� 87325 ���)
		//�ϴ� �� if~else�� ���������� ���� ���� Ȯ������
		// A~L���� ��� ����� �ȳ��ϴ��� �׽�Ʈ���̽��� �� ���� Ȯ���غ�����
		//printDisplay() �Լ����� ��ģ ��찡 �ִ� ������ ������. (ex: down���� �ȳ��ؾ��� ���� up���� �ȳ��Ѵٵ���..)
		

	}

}
