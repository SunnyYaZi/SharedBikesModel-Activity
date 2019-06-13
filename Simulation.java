package com.zf.ofo.simulation_dj;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;
import java.lang.Float;

public class Simulation {
	public static void main(String[] args) throws Exception {
	
//		for (double alpha=0;alpha<=2.01;alpha=alpha+0.2) {
//			for (double beta=0;beta<=3.01;beta=beta+0.2) {
//				System.out.println("----------"+alpha+"------------");
				double alpha = 1.0;
				double beta = 1.0;
				
				int POINum = 10000;
				int step = 3600;//second
				int totalTime = 24*3600;
		
				ArrayList<POIProperty> POIList = new ArrayList<POIProperty>();	
				HashMap<Integer, POIProperty> POIMap =  new HashMap<Integer,POIProperty>();		
				
				CsvReader POIInfo = new CsvReader("E:\\ProgramProcess\\SharedBikes\\about6ofo6moike\\poiPositionKMeans\\kEqual10000\\poiCenterPositionWithIndex\\part-00000.csv",',',Charset.forName("utf-8"));
				CsvWriter writeInfo = new CsvWriter("E:\\ProgramProcess\\SharedBikes\\mobike\\2018-05-22\\dataProcess\\simulation\\result1\\result1.csv",',',Charset.forName("utf-8"));
				
				while(POIInfo.readRecord()){
					POIProperty POI = new POIProperty();
					POI.id = Integer.parseInt(POIInfo.get(0));
					//POI.type = Integer.parseInt(POIInfo.get(3));
					POI.POILon = Double.parseDouble(POIInfo.get(1));
					POI.POILat = Double.parseDouble(POIInfo.get(2));
					//POI.cluster = Integer.parseInt(POIInfo.get(4));
					//POI.bikeCount = Integer.parseInt(POIInfo.get(1));
					
					POIList.add(POI);
					POIMap.put(POI.id, POI);			
				}
				POIInfo.close();
				
				CsvReader outNum = new CsvReader("E:\\ProgramProcess\\SharedBikes\\mobike\\2018-05-22\\dataProcess\\bikeOutMin\\fitOutNumEach1H\\fitOutNumEach1H.csv",',',Charset.forName("utf-8"));
				int[] outNumStep = new int[totalTime/step];
				int i = -1;
				while(outNum.readRecord()){
					i=i+1;
					outNumStep[i]=(int)Float.parseFloat(outNum.get(1));		
				}
				outNum.close();
				
//				CsvReader rTypeTrans = new CsvReader("E:\\type_trans.csv",',',Charset.forName("utf-8"));
//				float[][] typeTrans = new float[20][20];
//				int typei = -1;
//				while(rTypeTrans.readRecord()){
//					typei=typei+1;
//					for(int c=0;c<20;c++) {
//						typeTrans[typei][c]=Float.parseFloat(rTypeTrans.get(c));
//					}
//							
//				}
//				outNum.close();
				
				CsvReader rPOIoutact = new CsvReader("E:\\ProgramProcess\\SharedBikes\\mobike\\2018-05-22\\dataProcess\\centerNumAndOutActivityKmeans10000\\part-00000.csv",',',Charset.forName("utf-8"));
				float[] POIoutact = new float[POINum];
				int poiouti = -1;
				float poioutsum=0;
				while(rPOIoutact.readRecord()){
					poiouti=poiouti+1;
					POIoutact[poiouti]=(float) Math.pow(Float.parseFloat(rPOIoutact.get(1)),alpha);
					poioutsum = poioutsum+POIoutact[poiouti];
				}		
				rPOIoutact.close();
				
				for(int m=0;m<POINum;m++) {
					POIoutact[m] = POIoutact[m]/poioutsum;
				}
				
				for(int n=1;n<POINum;n++) {
					POIoutact[n] = POIoutact[n]+POIoutact[n-1];
				}
				//System.out.println(POIact[POINum-1]);
				
				CsvReader rPOIinact = new CsvReader("E:\\ProgramProcess\\SharedBikes\\mobike\\2018-05-22\\dataProcess\\centerNumAndInActivityKmeans10000\\part-00000.csv",',',Charset.forName("utf-8"));
				float[] POIinact = new float[POINum];
				int poiini = -1;
				float poiinsum=0;
				while(rPOIinact.readRecord()){
					poiini=poiini+1;
					POIinact[poiini]=(float) Math.pow(Float.parseFloat(rPOIinact.get(1)),beta);
					poiinsum = poiinsum+POIinact[poiini];
				}		
				rPOIinact.close();
				
				for(int m=0;m<POINum;m++) {
					POIinact[m] = POIinact[m]/poiinsum;
				}
				
//				for(int n=1;n<POINum;n++) {
//					POIinact[n] = POIinact[n]+POIinact[n-1];
//				}
				
				int stepi = -1;
				for(int currentTime = step;currentTime<=totalTime;currentTime=currentTime+step) {
					stepi = stepi + 1;
					if(stepi>=totalTime/step) stepi=0;
					//System.out.println(stepi);
					boolean flagN = true;
					int choseN = 0;
					while(flagN) {
						double choOp = Math.random();
						int chosOPOI = -1;
						for(int k=0;k<POINum;k++) {
							if(choOp<=POIoutact[k]) {
								chosOPOI = k;
								break;
							}
						}
//						if(POIMap.get(chosOPOI).bikeCount>0) {
							choseN = choseN +1;
							if(choseN==outNumStep[stepi]) flagN = false;
							
							double chosOPOILon = POIMap.get(chosOPOI).POILon;
							double chosOPOILat = POIMap.get(chosOPOI).POILat;
							//int chosOPOIType = POIMap.get(chosOPOI).type;
							
							double[] p = new double[POINum];
							for(POIProperty poi : POIList) {
								if(poi.id==chosOPOI) {
									p[poi.id] = 0;
								}else {
									p[poi.id] = computeP(chosOPOILon,chosOPOILat,poi.POILon,poi.POILat)*POIinact[poi.id];//typeTrans[chosOPOIType][poi.type];
								}
							}
							
							double sump = 0;
							for(int k=0;k<POINum;k++) {
								sump = sump + p[k];
							}
							for(int k=0;k<POINum;k++) {
								p[k] = p[k]/sump;
							}
							for(int k=1;k<POINum;k++) {
								p[k] = p[k]+p[k-1];
							}
							
							double choDp = Math.random();
							//System.out.println(choDp);
							int chosDPOI = -1;
							for(int k=0;k<POINum;k++) {
								//System.out.println(p[k]);
								if(choDp<=p[k]) {
									chosDPOI = k;
									//System.out.println(chosDPOI);
									break;
								}
							}
							
							//System.out.println(chosOPOI);
							//System.out.println(chosDPOI);
							//POIMap.get(chosOPOI).updateBikeCount(-1);
							//POIMap.get(chosDPOI).updateBikeCount(1);
							
							writeInfo.write(POIMap.get(chosOPOI).id+"");
							//writeInfo.write(POIMap.get(chosOPOI).type+"");
							//writeInfo.write(POIMap.get(chosOPOI).bikeCount+"");
							writeInfo.write(POIMap.get(chosDPOI).id+"");
							//writeInfo.write(POIMap.get(chosDPOI).type+"");
							//writeInfo.write(POIMap.get(chosDPOI).bikeCount+"");
							writeInfo.write(currentTime+"");
							writeInfo.write(countDistance(POIMap.get(chosOPOI).POILon,POIMap.get(chosOPOI).POILat,POIMap.get(chosDPOI).POILon,POIMap.get(chosDPOI).POILat)+"");
							writeInfo.endRecord();
//						}
					}
				}
				writeInfo.close();
//			}
//		}
	}
	
	public static double computeP(double preLng,double preLat,double lng,double lat){
		double mu = 7.07684643;
		double sigma = 0.4985788;
		double dis = countDistance(preLng,preLat,preLng,lat)+countDistance(preLng,lat,lng,lat);
		double p = (1/(dis*sigma*Math.sqrt(2*Math.PI)))*Math.exp(-(Math.pow(Math.log(dis)-mu, 2))/(2*sigma*sigma));
		return p;
	}
	
	public static double countDistance(double preLng,double preLat,double lng,double lat){
		double radLat1 = preLat*Math.PI/180.0;
		double radLat2 = lat*Math.PI/180.0;
		double a = radLat1 - radLat2;
		double b = preLng*Math.PI/180.0 - lng*Math.PI/180.0;
		double h = Math.pow(Math.sin(a/2), 2) + Math.cos(radLat1)*Math.cos(radLat2)*Math.pow(Math.sin(b/2), 2);
		double dis = 2*6378137*Math.asin(Math.sqrt(h));
		//dis = Math.round(dis*10000)/10000.0;
		return dis;
	}
	
}
