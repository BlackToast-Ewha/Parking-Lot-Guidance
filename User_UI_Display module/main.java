

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


public class main {
	
	
	
	/*주차 구역, 인터섹션, 전광판 변수 선언 */
	static ParkingArea[][] area = new ParkingArea[12][];
	public static Intersection[] n = new Intersection[8];
	static Area[] parkArea = new Area[12];
	static Display[][] display = new Display[8][4];

	/*주차 구역을 담는 우선순위 큐와 주차 구역 이름을 담는 String변수*/
	static PriorityQueue<Area> entranceArea; 
	static PriorityQueue<Area> elevatorArea; 
	static PriorityQueue<Area> disabledArea; 
	static String totalAssign; 
	
	/*MySQL 데이터베이스 연결에 필요한 변수 선언*/
	static final String JDBC_driver = "com.mysql.cj.jdbc.Driver";
	static final String DB_url = "jdbc:mysql://localhost:3306/use_your_db_name";
	static final String User = "use_your_user_name";
	static final String password = "use_your_password";
	static Connection conn = null;
	static Statement stmt = null;
	
	/*Display 함수에 넘겨주기 위해 사용하는 List<Intersection> 변수*/
	static List<Intersection> originalPath;
	static List<Intersection> gateUIPath;
	
	/*전광판 객체를 초기화하는 함수*/
	public static void setDisplay() {
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 4; j++) {
				display[i][j] = new Display(i, j);
				// j=0 -> u, j=1 -> l, j=2 -> d, j=3 -> r 전광판을 나타냄,
			}
		}

	}

	/*전광판에 화살표를 출력하는 함수*/
	public static void printDisplay(List<Intersection> path) {
		// 여기서 쪼갠다음에 하드코딩
		int a = 0, b, c;
		for (int i = 0; i < path.size() - 1; i++) { // path 에 intersection이 한 개일
													// 때는 실행되지 않음
			c = a; // 이전의 i 저장
			a = path.get(i).index;
			b = path.get(i + 1).index;
			if ((b - a) == 1) {// (1->2, 2->3, 3->4, 4->5, 5->6, 7->8)
				if (c < a) { // 정상 루트. 이전 index 가 현재 index 보다 작을 때
					if (a == 3) {
						display[2][0].printLeft();// 3 번 인터섹션 }
					} else if (a == 4) {
						display[3][0].printLeft(); // 4->5
					} else if (a == 7) {
						display[6][3].printRight();// 7->8
					} else
						display[a - 1][0].printStraight();
				} else {// 운전자가 지도를 따르지 않아 재탐색한 경우 . 더 높은 index를 가진 intersection 에서 더 낮은 index를 가진 intersection 6->1->2 5->2->3 8->1->2 7->3->4
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
						System.out.println("restart from intersection3"); // 3에서 재시작하여 32
				} else if (a == 8) { // 8->7
					display[7][0].printLeft();
				} else { // 6->5, 5->4, 2->1
					display[a - 1][2].printStraight();
				}

			} else {// index가 이어지지 않은 경우. 1->6, 1->8, 2->5, 3->7, 그리고 그 반대 6->1, 5->2, 7->3 8->1
				if (c < a) {// index 가 작은데서 큰데로 왔을 때
					if (b > a) {// 016(816) 018 125 237
						if (a == 1 && b == 8)
							display[0][3].printStraight(); // 018 이런일 일어나면 안됨.
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

				} else {// index 가 큰데서 작은데로 왔을 때 // 재탐색의 경우, 437 652 873 216 325
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

	/* 주차 구역 한 칸들을 초기화 하는 함수*/
	public static void setParkingArea() {

		area[0] = new ParkingArea[24];// A => 65
		area[1] = new ParkingArea[24];// B
		area[2] = new ParkingArea[12];// C
		area[3] = new ParkingArea[8];// D
		area[4] = new ParkingArea[7];// E
		area[5] = new ParkingArea[12];// F
		area[6] = new ParkingArea[10];// G
		area[7] = new ParkingArea[20];// H,엘리베이터 근처
		area[8] = new ParkingArea[2];// I,장애인
		area[9] = new ParkingArea[4];// J,장애인
		area[10] = new ParkingArea[3];// K,장애인
		area[11] = new ParkingArea[3];// L,엘리베이터 근처

		// 주차 공간 객체 선언, 나중에 데이터 베이스와 연동할 부분
		for (int i = 0; i < area.length; i++) {
			for (int j = 0; j < area[i].length; j++) {
				switch (i) { // ParkingArea(구역이름, 입차여부, 구역 내에서의 index, 장애인/비장애인)
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

	/* 주차장의 Intersection 초기화*/
	public static void setIntersection() { // 앞에서 선언한 8 개의 intersection 에 객체를 생성하여 부여
		// 1~8 사이의 인덱스 부여
		for (int i = 0; i < 8; i++) {
			n[i] = new Intersection((i + 1));

		}
		/* row와 column 을 설정하는 곳 */

		n[3].setRowCol(0, 0);
		n[4].setRowCol(1, 0);
		n[5].setRowCol(2, 0);
		n[2].setRowCol(0, 1);
		n[1].setRowCol(1, 1);
		n[0].setRowCol(2, 1);
		n[6].setRowCol(0, 2);
		n[7].setRowCol(2, 2);

		// n[i].adjacencies = 한 인터섹션에서 연결된 edge들의 집합

		n[0].adjacencies = new Edge[] { new Edge(n[1], 1), new Edge(n[5], 1), new Edge(n[7], 1) };

		n[0].adjacencies[0].Decide_Area(area[8]);// area[8]=I구역
		n[0].adjacencies[1].Decide_Area(area[1]);// area[1]=B구역
		n[0].adjacencies[1].Decide_Area(area[2]);// area[2]=C구역
		n[0].adjacencies[2].Decide_Area(area[7]);// area[7]=H구역

		n[1].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[2], 1), new Edge(n[4], 1) };

		n[1].adjacencies[0].Decide_Area(area[10]);// area[10]=K구역
		n[1].adjacencies[1].Decide_Area(area[5]);// area[5]=F구역
		n[1].adjacencies[2].Decide_Area(area[0]);// area[0]=A구역
		n[1].adjacencies[2].Decide_Area(area[1]);// area[0]=B구역

		n[2].adjacencies = new Edge[] { new Edge(n[1], 1), new Edge(n[3], 1), new Edge(n[6], 1) };

		n[2].adjacencies[0].Decide_Area(area[8]);// area[8]=I구역
		n[2].adjacencies[1].Decide_Area(area[5]);// area[5]=F구역
		n[2].adjacencies[2].Decide_Area(area[6]);// area[6]=G구역

		n[3].adjacencies = new Edge[] { new Edge(n[2], 1), new Edge(n[4], 1), };

		n[3].adjacencies[0].Decide_Area(area[5]);// area[5]=F구역
		n[3].adjacencies[1].Decide_Area(area[0]);// area[0]=A구역

		n[4].adjacencies = new Edge[] { new Edge(n[3], 1), new Edge(n[1], 1), new Edge(n[5], 1) };

		n[4].adjacencies[0].Decide_Area(area[3]);// area[3]=D구역
		n[4].adjacencies[1].Decide_Area(area[5]);// area[5]=F구역

		n[5].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[4], 1), };

		n[5].adjacencies[0].Decide_Area(area[1]);// area[1]=B구역
		n[5].adjacencies[0].Decide_Area(area[2]);// area[2]=C구역
		n[5].adjacencies[0].Decide_Area(area[10]);// area[10]=K구역
		n[5].adjacencies[1].Decide_Area(area[3]);// area[3]=D구역

		n[6].adjacencies = new Edge[] { new Edge(n[2], 1), new Edge(n[7], 2) };

		n[6].adjacencies[0].Decide_Area(area[6]);// area[6]=G구역
		n[6].adjacencies[1].Decide_Area(area[7]);// area[7]=H구역
		n[6].adjacencies[1].Decide_Area(area[9]);// area[9]=J구역
		n[6].adjacencies[1].Decide_Area(area[11]);// area[11]=L구역

		n[7].adjacencies = new Edge[] { new Edge(n[0], 1), new Edge(n[6], 2) };

		n[7].adjacencies[0].Decide_Area(area[10]);// area[10]=K구역
		n[7].adjacencies[1].Decide_Area(area[7]);// area[7]=H구역
		n[7].adjacencies[1].Decide_Area(area[9]);// area[9]=J구역
		n[7].adjacencies[1].Decide_Area(area[11]);// area[11]=L구역
	}

	/*주차 구역 (A,B 등)을 초기화 하는 함수
	 * 구역의 이름(알파벳), 잔여구역 수, 전체구역 수, 구역과 가까운 인터섹션을 지정하게 됨.
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

	/*2018-08-01 AstarSearch가 parent 뿐만 아니라 child 노드를 설정하게 만들었음
	 * 따라서 기존의 printPath()와 달리 자신의 자식노드를 확인하며 path를 출력하게 하는 함수 필요
	 * 그게 printPathTemp()임. 
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

	/*기존에 사용하던 path 출력함수 */
	public static List<Intersection> printPath(Intersection target) {
		List<Intersection> path = new ArrayList<Intersection>();

		for (Intersection node = target; node != null; node = node.parent) {
			path.add(node);
		}
		

		Collections.reverse(path);

		return path;
	}

	/*destination node(=goal)만 필요하던 기존의 AstarSearch를 source,goal,방금 지나온 인터섹션까지 고려하게 하는
	 * 함수로 변경
	 */
	public static void AstarSearch(Intersection source, Intersection goal, Intersection beforeIntsc) {

		/*지나온 인터섹션들을 담는 객체 Set explored*/
		Set<Intersection> explored = new HashSet<Intersection>();

		/*후보가 되는 인터섹션을 담는 PriorityQueue queue
		 * f_score가 낮을 수록 우선 순위가 높다*/
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
				break; // 바로 while문 탈출. 
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
					if (child != beforeIntsc){ // 연결되어있는 인터섹션이 방금 지나온 인터섹션이 아닌 경우에만 queue에 추가. 후진 안내를 막기 위한 코드임
						queue.add(child);
						current.child = child;
						if(child.index == goal.index)
							break;
					}

				}

			} // for

		} 
	}

	/*선호하는 구역 (엘리베이터, 게이트, 장애인 주차구역)에 따라 인접한 인터섹션의 인덱스를 반환*/
	public static int find_dest(String mode) {
		int findFlag = 0; // 주차 구역이 할당되었는지 체크하는 flag 변수
		String selectedAreaName = ""; // 상위 구역의 이름을 담을 String 변수
		int selectedLotNumber = 0; // 상위 구역 내에서 구역 번호를 담는 int 변수
		Intersection answer = null; // 선정된 주차 구역의 상위 구역의 인접 인터섹션을 담는 Intersection 변수
		if (mode.equals("entrance")) { // 입구 근처를 선택한 경우
			while (findFlag != 1) {
				Area tempArea = entranceArea.poll(); // 우선순위큐의 루트 노드를 꺼냄. 현재 주차장의 상황을 반영한 최선의 주차 구역 도출
				if (tempArea.remainNum != 0) { // 도출한 상위 구역의 잔여 구역수가 0이 아니라면
					selectedAreaName = tempArea.AreaName; // 구역 이름 변수 설정
					selectedLotNumber = tempArea.totalNum - tempArea.remainNum + 1; // 구역번호 변수 설정
					tempArea.remainNum--; // in변수를 1로 바꾸는것과 동일한 역할을 함. 잔여 구역 수를 1 줄임
					entranceArea.add(tempArea); // peek()이 아니라 아예 큐 내에서 Area를 꺼냈으므로 다시 넣어줌
					answer = tempArea.nearIntersection; // 인접 인터섹션 받아옴
					findFlag = 1; // 주차 구역 할당 완료를 뜻하는 변수를 1로 바꿈
				} else {
					if (entranceArea.isEmpty()) // 주차장이 만차이면 findFlag 변수를 0으로 두고 (변경 없이) 루프 탈출
						break;
					continue; // 주차장은 만차가 아니지만 poll()한 주차 구역의 잔여 구역수가 0인 경우 다시 while로 돌아가서 계속 poll()
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
			System.out.println("[만차]주차장에 입차하실 수 없습니다!!");
			return -1;
		}
		totalAssign = selectedAreaName + selectedLotNumber;

		System.out.println("배정된 주차구역은 " + selectedAreaName + selectedLotNumber + "입니다");
		return answer.index - 1; //(0~7 사이의 인덱스 반환)
	}

	/* 입구, 엘리베이터, 장애인 주차 구역에 따른 각각의 우선순위큐를 설정함.
	*우선순위는 선호구역으로부터의 거리^2+잔여구역/전체구역수 비율로 결정 */
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

	/*인터섹션의 h_score를 유동적으로 계산*/
	public static void setIntersectionInfo(Intersection goal) {
		for (int i = 0; i < 8; i++) {
			n[i].calculateHScore(goal);
		}
	}

	/*선호도에 맞게 주차 구역의 점수를 계산하는 함수*/
	/*시간 복잡도가 높으나 주차장 초기 세팅에만 쓰이는 함수이므로 하루에 1회 이하로 호출됨*/
	public static void setParkAreaScore() throws IOException {
		int setCount = 0;
		/*주차장 관리자가 입력한 정보에 맞추어 점수 계산*/
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
			System.out.println("================= 주차 안내 시스템 =================");
			System.out.println(carPlate + "님 선호하는 구역을 선택해 주세요");
			System.out.println("1. 주차장 입/출구 근처 ");
			System.out.println("2. 엘리베이터 근처 ");
			System.out.println("3. 장애인 전용 좌석");
			System.out.println("4. 종료");
			System.out.print("번호를 입력해주세요 : ");
			user_input = sc.nextInt();

			switch (user_input) {
			case 1: {
				int dest = 0;// 목적지 인터섹션의 번호를 저장할 변수
				try {
					dest = find_dest("entrance");
				} // 목적지 인터섹션 및 구역을 찾는다.
				catch (NullPointerException e) {
					System.out.println(e);
				}
				System.out.println("Destination: n" + dest);


				if (dest == -1) { // 주차장이 만차인 경우 dest에는 -1이 return되도록 설정해놨음
					System.out.println("주차장에 입차하실 수 없습니다! 시스템을 마치겠습니다");
					System.exit(0); // 시스템 전체 종료
				}

				else {
					AstarSearch(n[0], n[dest], null);// 목적지 인터섹션까지의 경로를 출력
					gateUIPath = printPath(n[dest]); // list type 의 path 받음
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
					System.out.println("주차장에 입차하실 수 없습니다!");
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
					System.out.println("주차장에 잔여 주차 구역이 없습니다!");
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
				System.out.println("시스템을 마치겠습니다.");
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

	/*차량이 전광판의 안내 지시를 따르지 않고 (고의든 아니든) 길을 잘못 들었을 때 해당 인터섹션에서 바로 
	 * 주차 구역을 새로 설정해주자고 이야기 했을 때 만든 함수
	 * 운전자가 에러를 일으켰을 때 호출되는 함수이며 현재 사용하지 않고 있음
	 * 그러나 나중에 필요하게 될까봐(다른 함수의 치명적인 에러 등의 문제로) 일단 삭제하지 않았음
	 */
	@SuppressWarnings("null")
	public static int find_dest_err(Intersection current, String[] area_path, String numPlate, Intersection beforeGoal)
			throws IOException, SQLException {
		boolean firstFind = false;
		Intersection errIntersection = null;

		Statement stmt1 = conn.createStatement();

		for (int i = 0; i < parkArea.length; i++) {
			if (parkArea[i].nearIntersection == current) { // 어떤 주차 구역의 인접 인터섹션이 현재 인터섹션인 경우
				if (parkArea[i].remainNum != 0) { // 잔여구역이 있다면
					String name = parkArea[i].AreaName;
					int lot = parkArea[i].totalNum - parkArea[i].remainNum + 1;
					stmt1.executeUpdate("update cars set parkArea = '" + name + Integer.toString(lot) + "' "+ "where carPlate = '" + numPlate + "'");
					parkArea[i].remainNum--;
					// 할당하고 firstFind를 true로 바꿈 --> 현재 인터섹션의 배열 내 인덱스를 반환하게 됨
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
	
	/*인터섹션은 차량에게 화살표로 방향 안내를 하기 위해 이전에 지나온 인터섹션을 알아야 함
	 * 따라서 각 인터섹션은 다음 인터섹션의 안내를 위해 자신의 인덱스를 데이터베이스의 column 중 beforeIntsc에 저장
	 * 모든 인터섹션은 자신에게 다가오는 차량에 대해 이 작업을 수행하며 
	 * 방향 안내 이후 해당 작업을 수행함
	 */
	public static void saveIntscIndex(int myIndex, Connection conn) throws IOException, SQLException {
		Statement stmt2 = conn.createStatement();

		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		stmt2.executeUpdate("update cars set beforeIndex='" + Integer.toString(myIndex) + "' where carPlate ='" + numPlate + "'");

	}

	/*detect.py가 intersection에서 detect한 차량 번호판의 문자열은 intersection_getIn이라는 폴더에
	 * 저장해야함 */
	/* myIntersection은 각 인터섹션의 번호를 의미하며 이 부분은 하드 코딩되어 업로드 해야 한다 */
	
	public static void intersectionDisplay(Connection conn, Statement stmt, int myIntersection) throws IOException, SQLException {
		BufferedReader br = new BufferedReader(new FileReader("intersection_getIn\\nowEnter.txt"));
		String numPlate = br.readLine();

		int beforeIndex, currentIndex, nextIndex;
		System.out.println("인터섹션의 번호는 0~7 이다. 1~8 이 아닌 배열 내의 인덱스를 출력한다\n\n");

		List<Intersection> currentPath = new ArrayList<Intersection>();

		ResultSet rs_car = stmt.executeQuery("select parkArea,assignPath,beforeIndex from cars where carPlate = '" + numPlate + "'");

		while (rs_car.next()) {
			String[] area_path = { rs_car.getString(1), rs_car.getString(2), rs_car.getString(3) };
			area_path[1] = area_path[1].replaceAll(" ", "");
			int driverError = Arrays.asList(area_path[1].split(",")).indexOf(Integer.toString(myIntersection));
			// System.out.println("운전자가 길을 잘못들었다면 -1 출력 :"+driverError);

			int beforeIndex_sql = Integer.parseInt(area_path[2]);
			if (driverError == -1) {
				String goalAreaName = area_path[0].substring(0, 1);
				// 길을 잘못 든 운전자에게 할당된 주차 구역을 알아냄
				Intersection goalIntersection = parkArea[goalAreaName.charAt(0) - 65].nearIntersection; 																	// 가져옴
				AstarSearch(n[myIntersection - 1], goalIntersection, n[beforeIndex_sql - 1]);//
				List<Intersection> newPathMyIntersection = printPath(goalIntersection);
				List<Intersection> tempPath = printPathTemp(n[myIntersection-1],goalIntersection,n[beforeIndex_sql-1]);

				currentIndex = myIntersection - 1;
				System.out.println("새롭게 지정된 path는 " + newPathMyIntersection);
				System.out.println("임시로 만든 printPathTemp의 출력은 "+tempPath);
				if (newPathMyIntersection.size() != 1) {
					System.out.println("(0~7 사이 출력)운전자가 길을 잘못 들었으며 새롭게 지정된 Path의 길이가 1보다 길 때,그리고 새로운 Path의 길이는 "+ newPathMyIntersection.size());
					Intersection beforeIntsc = n[beforeIndex_sql-1];
					currentPath.add(beforeIntsc);
					currentPath.addAll(tempPath);
					System.out.println(currentPath);
					

					printDisplay(currentPath);
				} else {
					System.out.println("운전자가 길을 잘못 들었으며 새롭게 지정된 Path의 길이가 1일 때 (0~7 사이 출력)");
					System.out.println("현재 인터섹션의 번호");
					System.out.println(currentIndex);
					// 여기는 아무것도 프린트 하지 않
				}

			} else {
				String[] assignPathArr = area_path[1].split(",");
				List<String> tempPath = Arrays.asList(assignPathArr);
				currentIndex = myIntersection - 1;
				if (tempPath.indexOf(Integer.toString(myIntersection)) != 0) {
					beforeIndex = Integer.parseInt(tempPath.get(tempPath.indexOf(Integer.toString(myIntersection)) - 1))- 1;
				} else {
					System.out.println("현재 인터섹션은 운전자에게 기존에 할당된 Path의 첫번째 인터섹션이므로 이전 인터섹션을 나타내는 beforeIndex 변수에 -1을 할당한다");
					beforeIndex = -1;
				}
				int myIndexOfOriginPath = tempPath.indexOf(Integer.toString(myIntersection));

				if (myIndexOfOriginPath + 1 != tempPath.size()) {
					System.out.println("운전자가 기존의 path대로 운전하고 있으며 현재 인터섹션 이후에도 거쳐가야할 인터섹션이 더 남아있을 때");
					nextIndex = Integer.parseInt(tempPath.get(myIndexOfOriginPath + 1)) - 1;
					System.out.println("현재 인터섹션의 번호, 이전 인터섹션의 번호, 다음에 지나가야할 인터섹션의 번호");
					System.out.println(currentIndex + " " + beforeIndex + " " + nextIndex);
					currentPath.add(n[beforeIndex]);
					currentPath.add(n[currentIndex]);
					currentPath.add(n[nextIndex]);
					printDisplay(currentPath);

				} else {
					System.out.println("운전자가 기존의 path대로 운전하고 있으며 현재 인터섹션 이후에 거쳐가야 할 인터섹션이 없을 때");
					System.out.println("현재 인터섹션의 번호, 이전 인터섹션의 번호");
					System.out.println(currentIndex + " " + beforeIndex);
					// 여기도 프린트 할거 없을 듯?
				}

			}
		}
	}

	// main에서는 사용자 입력받아서 적절한 함수만 호출하는 식으로 변경
	public static void main(String[] args) throws IOException, SQLException, InterruptedException {

		Scanner sc = new Scanner(System.in);
		int user_input = -1;

		/*주차 구역, 인터섹션, 상위 주차 구역, 선호도에 따른 우선순위 큐, 전광판 객체, 주차 구역이 갖는 점수 초기화*/
		setParkingArea();
		setIntersection();
		setArea();
		setPriorityQueue();
		setDisplay();
		setParkAreaScore();

		/*주차장의 데이터베이스와 연결*/
		try {
			Class.forName(JDBC_driver);
			conn = DriverManager.getConnection(DB_url, User, password);
			stmt = conn.createStatement();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		// gateUI(user_input,sc,conn, stmt);
		
		saveIntscIndex(1, conn);
		intersectionDisplay(conn, stmt, 2);
		
		

	}

}
